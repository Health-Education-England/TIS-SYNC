package uk.nhs.tis.sync.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.tcs.api.dto.PersonViewDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PersonOwnerRule;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipStatus;
import com.transformuk.hee.tis.tcs.service.job.person.ProgrammeMembershipDto;
import com.transformuk.hee.tis.tcs.service.repository.PersonElasticSearchRepository;
import org.elasticsearch.index.query.*;
import uk.nhs.tis.sync.service.api.decorator.PersonViewDecorator;
import com.transformuk.hee.tis.tcs.service.job.person.PersonTrustDto;
import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import uk.nhs.tis.sync.service.impl.PersonTrustRowMapper;
import uk.nhs.tis.sync.service.impl.PersonViewRowMapper;
import uk.nhs.tis.sync.service.impl.ProgrammeMembershipRowMapper;
import uk.nhs.tis.sync.service.strategy.RoleBasedFilterStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PersonElasticSearchService {

  private static final Logger LOG = LoggerFactory.getLogger(PersonElasticSearchService.class);
  private static final String PERSON_TRUST_QUERY = "SELECT personId, trustId FROM PersonTrust WHERE personId IN (:personIds)";
  public static final String CURRENT_STATUS = "CURRENT";

  @Autowired
  private PersonElasticSearchRepository personElasticSearchRepository;
  @Autowired
  private SqlQuerySupplier sqlQuerySupplier;
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  @Autowired
  private Set<RoleBasedFilterStrategy> roleBasedFilterStrategies;
  @Autowired
  private PersonViewDecorator personViewDecorator;

  /**
   * Find a paginated set of people on a specified programme, if a search query is provided, do fuzzy search
   * on the firstname, surname, gmcid and gdcid
   *
   * @param programmeId the programme id to search for
   * @param searchQuery paramter that can map to a forename, surname, gmcid, gdcid
   * @param pageable    the page in which we want to search for including any sorting
   * @return a page of results
   */
  public Page<PersonViewDTO> findPeopleOnProgramme(Long programmeId, String searchQuery, Pageable pageable) {
    BoolQueryBuilder query = new BoolQueryBuilder();

    query = query.must(new MatchQueryBuilder("programmeId", programmeId));
    query = query.must(new MatchQueryBuilder("status", CURRENT_STATUS));

    if (StringUtils.isNotEmpty(searchQuery)) {
      BoolQueryBuilder boolSearchQuery = new BoolQueryBuilder();
      boolSearchQuery.should(new MatchQueryBuilder("fullName", searchQuery));
      boolSearchQuery.should(new WildcardQueryBuilder("surname", "*" + searchQuery + "*"));
      boolSearchQuery.should(new WildcardQueryBuilder("forenames", "*" + searchQuery + "*"));
      boolSearchQuery.should(new MatchQueryBuilder("gmcNumber", searchQuery));
      boolSearchQuery.should(new MatchQueryBuilder("gdcNumber", searchQuery));
      query = query.must(boolSearchQuery);
    }

    Page<PersonView> result = personElasticSearchRepository.search(query, pageable);
    List<PersonViewDTO> personViewDTOS = convertPersonViewToDTO(result.getContent(), null);
    List<PersonViewDTO> decoratedPersonViews = personViewDecorator.decorate(personViewDTOS);
    return new PageImpl<>(decoratedPersonViews, pageable, result.getTotalElements());
  }


  private Pageable replaceSortByIdHack(Pageable pageable) {
    //hack as we dont sort by id but rather personId - this can be removed once we remove the duplicate trainees from the
    //list view
    Sort sort = pageable.getSort();

    Iterator<Sort.Order> sortIterator = sort.iterator();
    List<Sort.Order> sortOrders = Lists.newArrayList();
    while (sortIterator.hasNext()) {
      Sort.Order order = sortIterator.next();
      if (!order.getProperty().equals("id")) {
        sortOrders.add(order);
      } else {
        if (order.isAscending()) {
          sortOrders.add(Sort.Order.asc("personId"));
        } else if (order.isDescending()) {
          sortOrders.add(Sort.Order.desc("personId"));
        } else {
          //yes i know, we actually send sort by id with no direction - doh
        }
      }
    }

    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortOrders));
  }

  private BoolQueryBuilder applyTextBasedSearchQuery(String searchQuery) {
    // this part is the free text part of the query, place a should between all of the searchable fields
    BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
    if (StringUtils.isNotEmpty(searchQuery)) {
      searchQuery = StringUtils.remove(searchQuery, '"'); //remove any quotations that were added from the FE
      shouldQuery
          .should(new MatchQueryBuilder("publicHealthNumber", searchQuery))
          .should(new MatchQueryBuilder("fullName", searchQuery))
          .should(new WildcardQueryBuilder("surname", "*" + searchQuery + "*"))
          .should(new WildcardQueryBuilder("forenames", "*" + searchQuery + "*"))
          .should(new MatchQueryBuilder("gmcNumber", searchQuery))
          .should(new MatchQueryBuilder("gdcNumber", searchQuery))
          .should(new MatchQueryBuilder("role", searchQuery));


      if (StringUtils.isNumeric(searchQuery)) {
        shouldQuery = shouldQuery.should(new TermQueryBuilder("personId", searchQuery));
      }
    }

    LOG.debug("Query is : {}", shouldQuery);
    return shouldQuery;
  }

  /**
   * If the current user is a programme user of a trust user, apply the filters and return a list of filters applied
   * so that any other selected programme or trust(owner) filters aren't applied too
   *
   * @param mustBetweenDifferentColumnFilters
   * @return
   */
  private Set<String> applyRoleBasedFilters(BoolQueryBuilder mustBetweenDifferentColumnFilters) {
    //find if there are any strategies based off roles need executing
    Set<String> appliedFilters = Sets.newHashSet();
    for (RoleBasedFilterStrategy roleBasedFilterStrategy : roleBasedFilterStrategies) {
      Optional<Tuple<String, BoolQueryBuilder>> nameToFilterOptionalTuple = roleBasedFilterStrategy.getFilter();
      if (nameToFilterOptionalTuple.isPresent()) {
        Tuple<String, BoolQueryBuilder> nameToFilterTuple = nameToFilterOptionalTuple.get();
        appliedFilters.add(nameToFilterTuple.v1());
        mustBetweenDifferentColumnFilters.must(nameToFilterTuple.v2());
      }
    }
    return appliedFilters;
  }

  /**
   * Update the ES document for the particular person with id. If no Person is found, delete the document from ES as
   * the change may have removed the trainee from the list
   * <p>
   * Before an update can happen, we first find the existing trainees from ES and remove them, then do a new save.
   * This is because the ES ids are autogenerated, and we dont store any of the Ids in the DB so we can match the records
   * so its far easier to delete the records and reimport
   * <p>
   * This method has now been marked as synchronized as we are getting race conditions when throwing multiple events for
   * the same person record. Without this, two or more threads could be running this method at the same time, both of them
   * will delete the record, then both will run the create method, leading to multiple records in ES
   * <p>
   * A sleep has also be introduced as for some reason, the read and ES update was happening before the commit to the DB occured
   * this meant that the data retrieved from the DB to be pushed to ES was the non updated data.
   * <p>
   * This should eventually be removed when we move the a proper queue system with delays built in as a feature
   *
   * @param personId
   */
  public synchronized void updatePersonDocument(Long personId) {
    Preconditions.checkNotNull(personId, "Person Id cannot be null");

    //horrible hack! we seem to have a race condition
    try {
      Thread.sleep(500L);
    } catch (InterruptedException e) {

    }
    String query = getQuery()
        .replace("WHERECLAUSE", "WHERE p.id=:id");

    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    paramSource.addValue("id", personId);

    List<PersonView> queryResult = namedParameterJdbcTemplate.query(query, paramSource, new PersonViewRowMapper());
    if (CollectionUtils.isNotEmpty(queryResult)) {
      updateDocumentWithTrustData(queryResult);
      updateDocumentWithProgrammeMembershipData(queryResult);
      deletePersonDocument(personId);
      saveDocuments(queryResult);
    } else {
      deletePersonDocument(personId);
    }
    personElasticSearchRepository.refresh();
  }

  public void deletePersonDocument(Long personId) {
    Preconditions.checkNotNull(personId, "Person id cannot be null");
    Iterable<PersonView> foundPersons = personElasticSearchRepository.search(new MatchQueryBuilder("personId", personId));
    personElasticSearchRepository.deleteAll(foundPersons);
  }

  public void updatePersonDocumentForProgramme(Long programmeId) {
    String programmeMembershipQuery = sqlQuerySupplier.getQuery(SqlQuerySupplier.PROGRAMME_MEMBERSHIP_VIEW)
      .replace("WHERECLAUSE", "where programmeId=:programmeId");

    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    paramSource.addValue("programmeId", programmeId);
    List<ProgrammeMembershipDto> programmeMembershipDtos =
      namedParameterJdbcTemplate.query(programmeMembershipQuery, paramSource, new ProgrammeMembershipRowMapper());
    if (programmeMembershipDtos.size() == 0)
      return;

    Set<Long> personIds = programmeMembershipDtos.stream().map(ProgrammeMembershipDto::getPersonId).collect(Collectors.toSet());
    for(Long personId: personIds) {
      updatePersonDocument(personId);
    }
  }


  public void updatePersonDocumentForSpecialty(Long specialtyId) {
    String query = getQuery()
        .replace("WHERECLAUSE", "WHERE s.id=:id");

    List<PersonView> personViews = runQuery(query, specialtyId);
    updateDocumentWithTrustData(personViews);
    updateDocumentWithProgrammeMembershipData(personViews);
    saveDocuments(personViews);
  }

  /**
   * Get the query with most of the template areas removed, leaving the where clause to be filled by the calling method
   *
   * @return
   */
  private String getQuery() {
    String query = sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_VIEW);
    return query.replace("TRUST_JOIN", "")
        .replace("PROGRAMME_MEMBERSHIP_JOIN", "")
        .replace("ORDERBYCLAUSE", "ORDER BY id DESC")
        .replace("LIMITCLAUSE", "");
  }

  private List<PersonView> runQuery(String query, Long id) {
    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    paramSource.addValue("id", id);
    return namedParameterJdbcTemplate.query(query, paramSource, new PersonViewRowMapper());
  }

  public void saveDocuments(List<PersonView> queryResult) {
    if (CollectionUtils.isNotEmpty(queryResult)) {
      queryResult.stream().forEach(pv -> pv.setFullName(pv.getForenames() + " " + pv.getSurname()));
      personElasticSearchRepository.saveAll(queryResult);
    }
  }

  public void updateDocumentWithProgrammeMembershipData(List<PersonView> queryResult) {
    if (CollectionUtils.isNotEmpty(queryResult)) {
      Set<Long> personIds = queryResult.stream().map(PersonView::getPersonId).collect(Collectors.toSet());

      String programmeMembershipQuery = sqlQuerySupplier.getQuery(SqlQuerySupplier.PROGRAMME_MEMBERSHIP_VIEW)
        .replace("WHERECLAUSE", "where personId IN (:personIds)");

      List<ProgrammeMembershipDto> programmeMembershipDtos = namedParameterJdbcTemplate
        .query(programmeMembershipQuery,
          new MapSqlParameterSource("personIds", personIds),
          new ProgrammeMembershipRowMapper());

      Map<Long, Set<ProgrammeMembershipDto>> personIdToProgrammeMembershipDtos = new HashMap<>();

      for (ProgrammeMembershipDto programmeMembershipDto : programmeMembershipDtos) {
        Long personId = programmeMembershipDto.getPersonId();
        if (!personIdToProgrammeMembershipDtos.containsKey(personId)) {
          personIdToProgrammeMembershipDtos.put(personId, Sets.newHashSet());
        }
        personIdToProgrammeMembershipDtos.get(personId).add(programmeMembershipDto);
      }

      queryResult.stream().forEach(pv -> {
        if (personIdToProgrammeMembershipDtos.containsKey(pv.getPersonId())) {
          pv.setProgrammeMemberships(personIdToProgrammeMembershipDtos.get(pv.getPersonId()));
        } else {
          pv.setProgrammeMemberships(Sets.newHashSet());
        }
      });
    }
  }

  public void updateDocumentWithTrustData(List<PersonView> queryResult) {
    if (CollectionUtils.isNotEmpty(queryResult)) {

      Set<Long> personIds = queryResult.stream().map(PersonView::getPersonId).collect(Collectors.toSet());
      List<PersonTrustDto> personTrustDtos = namedParameterJdbcTemplate
          .query(PERSON_TRUST_QUERY, new MapSqlParameterSource("personIds", personIds), new PersonTrustRowMapper());

      Map<Long, Set<PersonTrustDto>> personIdToTrustIds = new HashMap<>();

      for (PersonTrustDto personTrustDto : personTrustDtos) {
        Long personId = personTrustDto.getPersonId();
        if (!personIdToTrustIds.containsKey(personId)) {
          personIdToTrustIds.put(personId, Sets.newHashSet());
        }

        personIdToTrustIds.get(personId).add(personTrustDto);
      }

      queryResult.stream().forEach(pv -> {
        if (personIdToTrustIds.containsKey(pv.getPersonId())) {
          pv.setTrusts(personIdToTrustIds.get(pv.getPersonId()));
        } else {
          pv.setTrusts(Sets.newHashSet());
        }
      });
    }
  }

  private List<PersonViewDTO> convertPersonViewToDTO(List<PersonView> content, ProgrammeMembershipStatus programmeMembershipStatus) {
    if (programmeMembershipStatus == null) {
      programmeMembershipStatus = ProgrammeMembershipStatus.CURRENT;
    }
    final ProgrammeMembershipStatus programmeMembershipStatusFilter = programmeMembershipStatus;
    return content.stream().map(pv -> {
      PersonViewDTO personViewDTO = new PersonViewDTO();
      personViewDTO.setId(pv.getPersonId());
      personViewDTO.setIntrepidId(pv.getIntrepidId());
      personViewDTO.setSurname(pv.getSurname());
      personViewDTO.setForenames(pv.getForenames());
      personViewDTO.setGmcNumber(pv.getGmcNumber());
      personViewDTO.setGdcNumber(pv.getGdcNumber());
      personViewDTO.setPublicHealthNumber(pv.getPublicHealthNumber());
      personViewDTO.setGradeId(pv.getGradeId());
      personViewDTO.setGradeAbbreviation(pv.getGradeAbbreviation());
      personViewDTO.setGradeName(pv.getGradeName());
      personViewDTO.setSiteId(pv.getSiteId());
      personViewDTO.setSiteCode(pv.getSiteCode());
      personViewDTO.setSiteName(pv.getSiteName());
      personViewDTO.setPlacementType(pv.getPlacementType());
      personViewDTO.setSpecialty(pv.getSpecialty());
      personViewDTO.setRole(pv.getRole());
      personViewDTO.setStatus(pv.getStatus());
      personViewDTO.setCurrentOwner(pv.getCurrentOwner());
      if (StringUtils.isNotEmpty(pv.getCurrentOwnerRule())) {
        personViewDTO.setCurrentOwnerRule(PersonOwnerRule.valueOf(pv.getCurrentOwnerRule()));
      }

      // filter programmeMembership status

      Set<ProgrammeMembershipDto> programmeMembershipDtos = pv.getProgrammeMemberships();
      if (!programmeMembershipDtos.isEmpty()) {
        for (ProgrammeMembershipDto membershipDto: programmeMembershipDtos) {
          if (Objects.equals(membershipDto.getProgrammeMembershipStatus(), programmeMembershipStatusFilter)) {
            personViewDTO.setProgrammeId(membershipDto.getProgrammeId());
            personViewDTO.setProgrammeName(membershipDto.getProgrammeName());
            personViewDTO.setProgrammeNumber(membershipDto.getProgrammeNumber());
            personViewDTO.setTrainingNumber(membershipDto.getTrainingNumber());
            personViewDTO.setProgrammeMembershipStatus(programmeMembershipStatusFilter);
          }
        }
      }
      return personViewDTO;
    }).collect(Collectors.toList());
  }
}

package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.mapper.CurriculumMapper;
import uk.nhs.tis.sync.mapper.PlacementDetailsMapper;
import uk.nhs.tis.sync.mapper.PlacementSpecialtyMapper;
import uk.nhs.tis.sync.mapper.PostMapper;
import uk.nhs.tis.sync.mapper.ProgrammeMapper;
import uk.nhs.tis.sync.mapper.SiteMapper;
import uk.nhs.tis.sync.mapper.SpecialtyMapper;
import uk.nhs.tis.sync.mapper.TrustMapper;

@Component
public class DmsRecordAssembler {

  private static final String LOAD = "load";

  private static final String DATA = "data";

  private static final String PARTITION_KEY_TYPE = "schema-table";

  private static final String SCHEMA_REFERENCE = "reference";
  private static final String SCHEMA_TCS = "tcs";
  private static final String TABLE_CONTACT_DETAILS = "ContactDetails";
  private static final String TABLE_CURRICULUM = "Curriculum";
  private static final String TABLE_GDC_DETAILS = "GdcDetails";
  private static final String TABLE_GMC_DETAILS = "GmcDetails";
  private static final String TABLE_PERSON = "Person";
  private static final String TABLE_PERSONAL_DETAILS = "PersonalDetails";
  private static final String TABLE_PLACEMENT = "Placement";
  private static final String TABLE_PLACEMENT_SPECIALTY = "PlacementSpecialty";
  private static final String TABLE_POST = "Post";
  private static final String TABLE_PROGRAMME = "Programme";
  private static final String TABLE_PROGRAMME_MEMBERSHIP = "ProgrammeMembership";
  private static final String TABLE_QUALIFICATION = "Qualification";
  private static final String TABLE_SITE = "Site";
  private static final String TABLE_SPECIALTY = "Specialty";
  private static final String TABLE_TRUST = "Trust";

  private final PostMapper postMapper;

  private final TrustMapper trustMapper;

  private final SiteMapper siteMapper;

  private final ProgrammeMapper programmeMapper;

  private final CurriculumMapper curriculumMapper;

  private final SpecialtyMapper specialtyMapper;

  private final PlacementSpecialtyMapper placementSpecialtyMapper;

  private final PlacementDetailsMapper placementDetailsMapper;

  /**
   * Constructor for a DmsRecordAssembler, which instantiates the relevant mappers.
   */
  DmsRecordAssembler(PostMapper postMapper,
      TrustMapper trustMapper, SiteMapper siteMapper,
      ProgrammeMapper programmeMapper, CurriculumMapper curriculumMapper,
      SpecialtyMapper specialtyMapper,
      PlacementSpecialtyMapper placementSpecialtyMapper,
      PlacementDetailsMapper placementDetailsMapper) {
    this.postMapper = postMapper;
    this.trustMapper = trustMapper;
    this.siteMapper = siteMapper;
    this.programmeMapper = programmeMapper;
    this.curriculumMapper = curriculumMapper;
    this.specialtyMapper = specialtyMapper;
    this.placementSpecialtyMapper = placementSpecialtyMapper;
    this.placementDetailsMapper = placementDetailsMapper;
  }

  /**
   * Assemble a list of DmsDtos from the given dto (e.g. a PostDto and a TrustDto).
   *
   * @param dtos The dto objects which will be mapped to DmsDtos.
   * @return The assembled DmsDtos
   */
  public List<DmsDto> assembleDmsDtos(List<Object> dtos) {
    return dtos.stream()
        .map(this::assembleDmsDto)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * The method that assembles a complete DmsDto starting from a dto (e.g. a PostDto or a TrustDto)
   *
   * @param dto The dto which will be mapped to another dto representative of the "data" portion of
   *            a DmsDto (e.g. PostDmsDto).
   * @return The DmsDto, complete with data and metadata.
   */
  private DmsDto assembleDmsDto(Object dto) {
    Object dmsData = null;
    String schema = null;
    String table = null;

    if (dto instanceof ContactDetailsDTO) {
      dmsData = dto;
      schema = SCHEMA_TCS;
      table = TABLE_CONTACT_DETAILS;
    }

    if (dto instanceof CurriculumDTO) {
      dmsData = curriculumMapper.toDmsDto((CurriculumDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_CURRICULUM;
    }

    if (dto instanceof GdcDetailsDTO) {
      dmsData = dto;
      schema = SCHEMA_TCS;
      table = TABLE_GDC_DETAILS;
    }

    if (dto instanceof GmcDetailsDTO) {
      dmsData = dto;
      schema = SCHEMA_TCS;
      table = TABLE_GMC_DETAILS;
    }

    if (dto instanceof PersonDTO) {
      // dmsData = personMapper.toDmsDto((PersonDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PERSON;
    }

    if (dto instanceof PersonalDetailsDTO) {
      dmsData = dto;
      schema = SCHEMA_TCS;
      table = TABLE_PERSONAL_DETAILS;
    }

    if (dto instanceof PlacementDetailsDTO) {
      dmsData = placementDetailsMapper.toDmsDto((PlacementDetailsDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PLACEMENT;
    }

    if (dto instanceof PlacementSpecialtyDTO) {
      dmsData = placementSpecialtyMapper.toDmsDto((PlacementSpecialtyDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PLACEMENT_SPECIALTY;
    }

    if (dto instanceof PostDTO) {
      dmsData = postMapper.toDmsDto((PostDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_POST;
    }

    if (dto instanceof ProgrammeDTO) {
      dmsData = programmeMapper.toDmsDto((ProgrammeDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PROGRAMME;
    }

    if (dto instanceof ProgrammeMembershipDTO) {
      // dmsData = programmeMembershipMapper.toDmsDto((ProgrammeMembershipDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PROGRAMME_MEMBERSHIP;
    }

    if (dto instanceof QualificationDTO) {
      // dmsData = qualificationMapper.toDmsDto((QualificationDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_QUALIFICATION;
    }

    if (dto instanceof SiteDTO) {
      dmsData = siteMapper.toDmsDto((SiteDTO) dto);
      schema = SCHEMA_REFERENCE;
      table = TABLE_SITE;
    }

    if (dto instanceof SpecialtyDTO) {
      dmsData = specialtyMapper.toDmsDto((SpecialtyDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_SPECIALTY;
    }

    if (dto instanceof TrustDTO) {
      dmsData = trustMapper.toDmsDto((TrustDTO) dto);
      schema = SCHEMA_REFERENCE;
      table = TABLE_TRUST;
    }

    if (dmsData != null) {
      MetadataDto metadata = new MetadataDto(Instant.now().toString(), DATA, LOAD,
          PARTITION_KEY_TYPE, schema, table, UUID.randomUUID().toString());

      return new DmsDto(dmsData, metadata);
    }

    return null;
  }
}

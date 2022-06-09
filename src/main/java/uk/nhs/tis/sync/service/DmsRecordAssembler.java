package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.DmsDtoType;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.mapper.CurriculumMembershipMapper;
import uk.nhs.tis.sync.mapper.CurriculumMembershipMapperImpl;
import uk.nhs.tis.sync.mapper.DmsMapper;

@Component
public class DmsRecordAssembler {

  private static final String LOAD = "load";

  private static final String DATA = "data";

  private static final String PARTITION_KEY_TYPE = "schema-table";

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
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * The method that assembles a list of complete DmsDtos starting from a dto (e.g. a PostDto or a TrustDto)
   *
   * @param dto The dto which will be mapped to another dto representative of the "data" portion of
   *            a DmsDto (e.g. PostDmsDto).
   * @return The list of DmsDtos, complete with data and metadata.
   */
  private List<DmsDto> assembleDmsDto(Object dto) {
    List<DmsDto> dmsDtos = null;
    boolean hasMultipleDmsDtos = false;
    Object dmsData = null;
    String schema = null;
    String table = null;

    DmsDtoType dmsDtoType = DmsDtoType.fromDto(dto);

    if (dmsDtoType != null) {
      schema = dmsDtoType.getSchema();
      table = dmsDtoType.getTable();
      Class<? extends DmsMapper<?, ?>> mapperClass = dmsDtoType.getMapperClass();

      if (mapperClass != null) {
        DmsMapper<?, ?> dmsMapper = Mappers.getMapper(mapperClass);
        dmsData = dmsMapper.objectToDmsDto(dto);
        if (dto instanceof ProgrammeMembershipDTO) {
          hasMultipleDmsDtos = true;
          dmsDtos = buildCurriculumMembershipDmsDtos(schema, table, (ProgrammeMembershipDTO) dto,
              (CurriculumMembershipDmsDto) dmsData);
        }
      } else {
        dmsData = dto;
      }
    }

    if (hasMultipleDmsDtos) {
      return dmsDtos;
    } else {
      if (dmsData != null) {
        MetadataDto metadata = new MetadataDto(Instant.now().toString(), DATA, LOAD,
            PARTITION_KEY_TYPE, schema, table, UUID.randomUUID().toString());

        return Collections.singletonList(new DmsDto(dmsData, metadata));
      }
    }

    return Collections.emptyList();
  }

  /**
   * Builds a list of DmsDtos for the curriculum membership records within a ProgrammeMembershipDTO.
   *
   * @param schema the overall schema for the DTO mapping
   * @param table the database table source for the programme membership
   * @param dto the ProgrammeMembershipDTO containing the curriculum memberships
   * @param programmeMembershipDtoDetails a parent CurriculumMembershipDmsDto with
   *                                      programme membership information
   * @return the list of DmsDtos containing CurriculumMembershipDmsDtos and metadata
   */
  private List<DmsDto> buildCurriculumMembershipDmsDtos(String schema, String table,
                                                        ProgrammeMembershipDTO dto,
                                                        CurriculumMembershipDmsDto programmeMembershipDtoDetails) {
    List<DmsDto> dmsDtos = new ArrayList<>();
    CurriculumMembershipMapper curriculumMembershipMapper = new CurriculumMembershipMapperImpl();
    for (CurriculumMembershipDTO cm : dto.getCurriculumMemberships()) {
      CurriculumMembershipDmsDto cmDmsData = curriculumMembershipMapper.toDmsDto(cm);
      if (cmDmsData != null) {
        curriculumMembershipMapper
            .addProgrammeMembershipDetails(programmeMembershipDtoDetails, cmDmsData);
        MetadataDto metadata = new MetadataDto(Instant.now().toString(), DATA, LOAD,
            PARTITION_KEY_TYPE, schema, table, UUID.randomUUID().toString());
        dmsDtos.add(new DmsDto(cmDmsData, metadata));
      }
    }
    return dmsDtos;
  }
}

package uk.nhs.tis.sync.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.DmsDtoType;
import uk.nhs.tis.sync.dto.MetadataDto;
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
  public List<DmsDto> assembleDmsDtos(List<Object> dtos, String tisTrigger,
      String tisTriggerDetail) {
    return dtos.stream()
        .map(dto -> assembleDmsDto(dto, tisTrigger, tisTriggerDetail))
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
  private List<DmsDto> assembleDmsDto(Object dto, String tisTrigger, String tisTriggerDetail) {
    List<DmsDto> dmsDtoList = new ArrayList<>();
    List<Object> dmsDataList = new ArrayList<>();
    String schema = null;
    String table = null;

    DmsDtoType dmsDtoType = DmsDtoType.fromDto(dto);

    if (dmsDtoType != null) {
      schema = dmsDtoType.getSchema();
      table = dmsDtoType.getTable();
      Class<? extends DmsMapper<?, ?>> mapperClass = dmsDtoType.getMapperClass();

      if (mapperClass != null) {
        DmsMapper<?, ?> dmsMapper = Mappers.getMapper(mapperClass);
        dmsDataList.addAll(dmsMapper.objectToDmsDto(dto));
      } else {
        dmsDataList.add(dto);
      }
    }

    if (!dmsDataList.isEmpty()) {
      for (Object dmsData : dmsDataList) {
        MetadataDto metadata = new MetadataDto(Instant.now().toString(), DATA, LOAD,
            PARTITION_KEY_TYPE, schema, table, UUID.randomUUID().toString(),
            tisTrigger, tisTriggerDetail);
        dmsDtoList.add(new DmsDto(dmsData, metadata));
      }
    }

    return dmsDtoList;
  }
}

package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.mapper.PostDtoToPostDataDmsDtoMapper;
import uk.nhs.tis.sync.mapper.ProgrammeMapper;
import uk.nhs.tis.sync.mapper.SiteMapper;
import uk.nhs.tis.sync.mapper.TrustDtoToTrustDataDmsDtoMapper;

@Component
public class DmsRecordAssembler {

  private static final String LOAD = "load";

  private static final String DATA = "data";

  private static final String PARTITION_KEY_TYPE = "schema-table";

  private static final String SCHEMA_REFERENCE = "reference";
  private static final String SCHEMA_TCS = "tcs";
  private static final String TABLE_POST = "Post";
  private static final String TABLE_PROGRAMME = "Programme";
  private static final String TABLE_SITE = "Site";
  private static final String TABLE_TRUST = "Trust";

  private final PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper;

  private final TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper;

  private final SiteMapper siteMapper;

  private final ProgrammeMapper programmeMapper;

  /**
   * Constructor for a DmsRecordAssembler, which instantiates the relevant mappers.
   */
  DmsRecordAssembler(PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper,
      TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper, SiteMapper siteMapper,
      ProgrammeMapper programmeMapper) {
    this.postDtoToPostDataDmsDtoMapper = postDtoToPostDataDmsDtoMapper;
    this.trustDtoToTrustDataDmsDtoMapper = trustDtoToTrustDataDmsDtoMapper;
    this.siteMapper = siteMapper;
    this.programmeMapper = programmeMapper;
  }

  /**
   * The method that assembles a complete DmsDto starting from a dto (e.g. a PostDto or a TrustDto)
   *
   * @param dto The dto which will be mapped to a -DataDmsDto (e.g. a PostDataDmsDto, or a
   *            TrustDataDmsDto)
   * @return The DmsDto, complete with data and metadata.
   */
  public DmsDto assembleDmsDto(Object dto) {
    Object dmsData = null;
    String schema = null;
    String table = null;

    if (dto instanceof PostDTO) {
      dmsData = postDtoToPostDataDmsDtoMapper.postDtoToPostDataDmsDto((PostDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_POST;
    }

    if (dto instanceof ProgrammeDTO) {
      dmsData = programmeMapper.toDmsDto((ProgrammeDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PROGRAMME;
    }

    if (dto instanceof SiteDTO) {
      dmsData = siteMapper.toDmsDto((SiteDTO) dto);
      schema = SCHEMA_REFERENCE;
      table = TABLE_SITE;
    }

    if (dto instanceof TrustDTO) {
      dmsData = trustDtoToTrustDataDmsDtoMapper.trustDtoToTrustDataDmsDto((TrustDTO) dto);
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

package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import java.time.Instant;
import java.util.UUID;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.mapper.*;

@Component
public class DmsRecordAssembler {

  private static final String LOAD = "load";

  private static final String DATA = "data";

  private static final String PARTITION_KEY_TYPE = "schema-table";

  private static final String SCHEMA_REFERENCE = "reference";
  private static final String SCHEMA_TCS = "tcs";
  private static final String TABLE_POST = "Post";
  private static final String TABLE_SITE = "Site";
  private static final String TABLE_TRUST = "Trust";
  private static final String TABLE_PROGRAMME = "Programme";
  private static final String TABLE_PROGRAMME_MEMBERSHIP = "ProgrammeMembership";

  private PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper;

  private TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper;

  private SiteMapper siteMapper;

  private ProgrammeMapper programmeMapper;

  private ProgrammeMembershipMapper programmeMembershipMapper;

  /**
   * Constructor for a DmsRecordAssembler, which instantiates the relevant mappers.
   */
  DmsRecordAssembler(PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper,
      TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper, SiteMapper siteMapper,
                     ProgrammeMapper programmeMapper,
                     ProgrammeMembershipMapper programmeMembershipMapper) {
    this.postDtoToPostDataDmsDtoMapper = postDtoToPostDataDmsDtoMapper;
    this.trustDtoToTrustDataDmsDtoMapper = trustDtoToTrustDataDmsDtoMapper;
    this.siteMapper = siteMapper;
    this.programmeMapper = programmeMapper;
    this.programmeMembershipMapper = programmeMembershipMapper;
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

    if (dto instanceof ProgrammeDTO) {
      dmsData = programmeMapper.toDmsDto((ProgrammeDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PROGRAMME;
    }

    if (dto instanceof ProgrammeMembershipDTO) {
      dmsData = programmeMembershipMapper.toDmsDto((ProgrammeMembershipDTO) dto);
      schema = SCHEMA_TCS;
      table = TABLE_PROGRAMME_MEMBERSHIP;
    }

    if (dmsData != null) {
      MetadataDto metadata = new MetadataDto(Instant.now().toString(), DATA, LOAD,
          PARTITION_KEY_TYPE, schema, table, UUID.randomUUID().toString());

      return new DmsDto(dmsData, metadata);
    }

    return null;
  }
}

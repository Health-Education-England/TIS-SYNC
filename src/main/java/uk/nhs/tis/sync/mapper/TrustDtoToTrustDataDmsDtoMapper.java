package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;
import uk.nhs.tis.sync.mapper.util.TrustDataDmsDtoUtil;
import uk.nhs.tis.sync.mapper.util.TrustDataDmsDtoUtil.Status;

@Mapper(componentModel = "spring", uses = TrustDataDmsDtoUtil.class)
public interface TrustDtoToTrustDataDmsDtoMapper {

  @Mapping(target = "code", source = "trustDto.code")
  @Mapping(target = "localOffice", source = "trustDto.localOffice")
  @Mapping(target = "status", source = "trustDto.status", qualifiedBy = Status.class)
  @Mapping(target = "trustKnownAs", source = "trustDto.trustKnownAs")
  @Mapping(target = "trustName", source = "trustDto.trustName")
  @Mapping(target = "trustNumber", source = "trustDto.trustNumber")
  @Mapping(target = "intrepidId", source = "trustDto.intrepidId")
  @Mapping(target = "id", source = "trustDto.id")
  TrustDataDmsDto trustDtoToTrustDataDmsDto(TrustDTO trustDto);
}

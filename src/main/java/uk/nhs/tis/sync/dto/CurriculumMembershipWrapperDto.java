package uk.nhs.tis.sync.dto;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import java.util.UUID;
import lombok.Value;

/**
 * A wrapper for {@link CurriculumMembershipDTO} to allow the inclusion of the PM UUID.
 */
@Value
public class CurriculumMembershipWrapperDto {

  UUID programmeMembershipUuid;
  CurriculumMembershipDTO curriculumMembership;
}

package uk.nhs.tis.sync.dto;

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
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.nhs.tis.sync.mapper.CurriculumMapper;
import uk.nhs.tis.sync.mapper.DmsMapper;
import uk.nhs.tis.sync.mapper.PersonMapper;
import uk.nhs.tis.sync.mapper.PlacementDetailsMapper;
import uk.nhs.tis.sync.mapper.PostMapper;
import uk.nhs.tis.sync.mapper.ProgrammeMapper;
import uk.nhs.tis.sync.mapper.SpecialtyMapper;

/**
 * An enumeration representing data request compatible DTOs. mapperClass is null when no mapper is
 * required.
 */
@AllArgsConstructor
@Getter
public enum DmsDtoType {

  CONTACT_DETAILS(ContactDetailsDTO.class, "tcs", "ContactDetails", null),
  CURRICULUM(CurriculumDTO.class, "tcs", "Curriculum", CurriculumMapper.class),
  GDC_DETAILS(GdcDetailsDTO.class, "tcs", "GdcDetails", null),
  GMC_DETAILS(GmcDetailsDTO.class, "tcs", "GmcDetails", null),
  PERSON(PersonDTO.class, "tcs", "Person", PersonMapper.class),
  PERSONAL_DETAILS(PersonalDetailsDTO.class, "tcs", "PersonalDetails", null),
  PLACEMENT_DETAILS(PlacementDetailsDTO.class, "tcs", "Placement", PlacementDetailsMapper.class),
  PLACEMENT_SPECIALTY(PlacementSpecialtyDTO.class, "tcs", "PlacementSpecialty", null),
  POST(PostDTO.class, "tcs", "Post", PostMapper.class),
  PROGRAMME(ProgrammeDTO.class, "tcs", "Programme", ProgrammeMapper.class),
  //  PROGRAMME_MEMBERSHIP(ProgrammeMembershipDTO.class, "tcs", "ProgrammeMembership",
  //  ProgrammeMembershipMapper.class),
  //  QUALIFICATION(QualificationDTO.class, "tcs", "Qualification", QualificationMapper.class),
  SITE(SiteDTO.class, "reference", "Site", null),
  SPECIALTY(SpecialtyDTO.class, "tcs", "Specialty", SpecialtyMapper.class),
  TRUST(TrustDTO.class, "reference", "Trust", null);

  private final Class<?> dtoType;
  private final String schema;
  private final String table;
  private final Class<? extends DmsMapper<?, ?>> mapperClass;

  /**
   * Get the enum matching the DTO type.
   *
   * @param dto The DTO to get the matching enum for.
   * @return The matched enum, or null if no match found.
   */
  public static DmsDtoType fromDto(Object dto) {
    for (DmsDtoType value : DmsDtoType.values()) {
      if (value.dtoType.isInstance(dto)) {
        return value;
      }
    }
    return null;
  }
}

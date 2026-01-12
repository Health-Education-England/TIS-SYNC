package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.QualificationType;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.QualificationDmsDto;

public class QualificationMapperTest {
  private static final Long QUALIFICATION_ID = 50L;
  private static final Long PERSON_ID = 60L;
  private static final QualificationType QUALIFICATION_TYPE = QualificationType.PRIMARY_QUALIFICATION;
  private final QualificationMapper testObj = new QualificationMapperImpl();

  @Test
  public void toDmsDtoShouldMapQualification() {
    PersonDTO personDto = new PersonDTO();
    personDto.setId(PERSON_ID);
    QualificationDTO input = new QualificationDTO();
    input.setId(QUALIFICATION_ID);
    input.setQualificationType(QUALIFICATION_TYPE);
    input.setPerson(personDto);

    QualificationDmsDto expected = testObj.toDmsDto(input);

    assertEquals(QUALIFICATION_ID.toString(), expected.getId(),
        "Mapped qualification id doesn't match expectation");
    assertEquals(QUALIFICATION_TYPE.name(),
        expected.getQualificationType(), "Mapped qualification type doesn't match expectation");
    assertEquals(PERSON_ID.toString(), expected.getPersonId(),
        "Mapped person id doesn't match expectation");
  }

  @Test
  public void toDmsDtoShouldMapNullQualificationType() {
    QualificationDTO input = new QualificationDTO();

    QualificationDmsDto expected = testObj.toDmsDto(input);

    assertEquals(null,
        expected.getQualificationType(), "Mapped qualification type doesn't match expectation");
  }
}

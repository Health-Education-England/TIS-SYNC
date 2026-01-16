package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;

public class CurriculumMapperTest {

  private static final Long ID = 33L;
  private static final String LEGACY_ID = "i33";
  private static final String NAME = "name";
  private static final CurriculumSubType CURRICULUM_SUB_TYPE = CurriculumSubType.DENTAL_CURRICULUM;
  private static final AssessmentType ASSESSMENT_TYPE = AssessmentType.ACADEMIC;
  private static final boolean DOES_THIS_CURRICULUM_LOAD_TO_CCT = true;
  private static final int PERIOD_OF_GRACE = 10;
  private static final Status STATUS = Status.CURRENT;
  private static final int LENGTH = 12;
  private final CurriculumMapper testObj = new CurriculumMapperImpl();

  @Test
  public void toDmsDtoShouldMapCurriculum() {
    SpecialtyDTO specialty = new SpecialtyDTO();
    specialty.setId(2L);

    CurriculumDTO input = new CurriculumDTO();
    input.setId(ID);
    input.setName(NAME);
    input.setIntrepidId(LEGACY_ID);
    input.setCurriculumSubType(CURRICULUM_SUB_TYPE);
    input.setAssessmentType(ASSESSMENT_TYPE);
    input.setDoesThisCurriculumLeadToCct(DOES_THIS_CURRICULUM_LOAD_TO_CCT);
    input.setPeriodOfGrace(PERIOD_OF_GRACE);
    input.setStatus(STATUS);
    input.setLength(LENGTH);
    input.setSpecialty(specialty);

    CurriculumDmsDto expected = testObj.toDmsDto(input);

    assertEquals(ID.toString(), expected.getId(), "Mapped id doesn't match expectation");
    assertEquals(LEGACY_ID, input.getIntrepidId(),
        "Mapped intrepid id doesn't match expectation");
    assertEquals(NAME, expected.getName(), "Mapped name doesn't match expectation");
    assertEquals(CURRICULUM_SUB_TYPE.name(),
        expected.getCurriculumSubType(), "Mapped curriculum subtype name doesn't match expectation");
    assertEquals(ASSESSMENT_TYPE.name(),
        expected.getAssessmentType(), "Mapped assessment type number doesn't match expectation");
    assertEquals(String.valueOf(DOES_THIS_CURRICULUM_LOAD_TO_CCT),
        expected.getDoesThisCurriculumLeadToCct(),
        "Mapped doesThisCurriculumLoadToCct doesn't match expectation");
    assertEquals(String.valueOf(PERIOD_OF_GRACE),
        expected.getPeriodOfGrace(), "Mapped period of grace doesn't match expectation");
    assertEquals(STATUS.name(), expected.getStatus(), "Mapped status doesn't match expectation");
    assertEquals(String.valueOf(LENGTH), expected.getLength(),
        "Mapped length doesn't match expectation");
    assertEquals(specialty.getId().toString(), expected.getSpecialtyId(),
        "Mapped specialty id doesn't match expectation");
  }
}

package uk.nhs.tis.sync.mapper;

import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Test;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;

public class SpecialtyMapperTest {
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

    assertEquals("Mapped id doesn't match expectation", ID.toString(), expected.getId());
    assertEquals("Mapped intrepid id doesn't match expectation", LEGACY_ID,
        input.getIntrepidId());
    assertEquals("Mapped name doesn't match expectation", NAME, expected.getName());
    assertEquals("Mapped curriculum subtype name doesn't match expectation",
        CURRICULUM_SUB_TYPE.name(), expected.getCurriculumSubType());
    assertEquals("Mapped assessment type number doesn't match expectation",
        ASSESSMENT_TYPE.name(), expected.getAssessmentType());
    assertEquals("Mapped doesThisCurriculumLoadToCct doesn't match expectation",
        String.valueOf(DOES_THIS_CURRICULUM_LOAD_TO_CCT),
        expected.getDoesThisCurriculumLeadToCct());
    assertEquals("Mapped period of grace doesn't match expectation",
        String.valueOf(PERIOD_OF_GRACE), expected.getPeriodOfGrace());
    assertEquals("Mapped status doesn't match expectation", STATUS.name(), expected.getStatus());
    assertEquals("Mapped length doesn't match expectation", String.valueOf(LENGTH),
        expected.getLength());
    assertEquals("Mapped specialty id doesn't match expectation", specialty.getId().toString(),
        expected.getSpecialtyId());
  }
}

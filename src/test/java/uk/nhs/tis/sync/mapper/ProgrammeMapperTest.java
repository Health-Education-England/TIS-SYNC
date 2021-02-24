package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeCurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Test;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ProgrammeMapperTest {

  private static final Long ID = 33L;
  private static final String LEGACY_ID = "old";
  private static final String OWNER = "TIS-SS";
  private static final String PROG_NAME = "Devin";
  private static final String PROG_NUM = "-64";
  private static final Status STATUS = Status.DELETE;
  private ProgrammeMapper testObj = new ProgrammeMapperImpl();

  @Test
  public void toDmsDtoShouldMapProgramme() {
    ProgrammeDTO input = new ProgrammeDTO();
    input.setId(ID);
    input.setIntrepidId(LEGACY_ID);
    input.setOwner(OWNER);
    input.setProgrammeName(PROG_NAME);
    input.setProgrammeNumber(PROG_NUM);
    input.setStatus(STATUS);
    ProgrammeCurriculumDTO pc1 = new ProgrammeCurriculumDTO();
    CurriculumDTO curriculum1 = new CurriculumDTO();
    curriculum1.setId(40L);
    pc1.setCurriculum(curriculum1);
    ProgrammeCurriculumDTO pc2 = new ProgrammeCurriculumDTO();
    CurriculumDTO curriculum2 = new CurriculumDTO();
    curriculum2.setId(12L);
    pc2.setCurriculum(curriculum2);
    input.setCurricula(Stream.of(pc1, pc2).collect(Collectors.toSet()));

    ProgrammeDmsDto expected = testObj.toDmsDto(input);

    assertEquals("Mapped id doesn't match expectation", ID.toString(), expected.getId());
    assertEquals("Mapped intrepidId doesn't match expectation", LEGACY_ID,
        input.getIntrepidId());
    assertEquals("Mapped owner doesn't match expectation", OWNER, expected.getOwner());
    assertEquals("Mapped programme name doesn't match expectation",
        PROG_NAME, expected.getProgrammeName());
    assertEquals("Mapped programme number doesn't match expectation",
        PROG_NUM, expected.getProgrammeNumber());
    assertEquals("Mapped status doesn't match expectation", STATUS.name(), expected.getStatus());
    assertEquals("Mapped curricula ids don't match expectation", Stream.of(pc1, pc2)
        .map(ProgrammeCurriculumDTO::getCurriculum)
        .map(CurriculumDTO::getId)
        .map(l -> Long.toString(l))
        .collect(Collectors.toSet()), expected.getCurriculaIds());
  }
}

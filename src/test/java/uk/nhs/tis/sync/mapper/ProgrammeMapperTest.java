package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeCurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;

class ProgrammeMapperTest {

  private static final Long ID = 33L;
  private static final String LEGACY_ID = "old";
  private static final String OWNER = "TIS-SS";
  private static final String PROG_NAME = "Devin";
  private static final String PROG_NUM = "-64";
  private static final Status STATUS = Status.DELETE;
  private ProgrammeMapper testObj = new ProgrammeMapperImpl();

  @Test
  void toDmsDtoShoudMapProgramme() {
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

    ProgrammeDmsDto expected = new ProgrammeDmsDto();
    expected.setId(ID.toString());
    expected.setIntrepidId(LEGACY_ID);
    expected.setOwner(OWNER);
    expected.setProgrammeName(PROG_NAME);
    expected.setProgrammeNumber(PROG_NUM);
    expected.setStatus(STATUS.name());
    expected.setCurriculaIds(Stream.of(pc1, pc2)
        .map(ProgrammeCurriculumDTO::getCurriculum)
        .map(CurriculumDTO::getId)
        .map(l -> Long.toString(l))
        .collect(Collectors.toSet()));

    ProgrammeDmsDto actual = testObj.toDmsDto(input);
    assertEquals(expected, actual, "Mapped DTO does not match expected");
  }
}
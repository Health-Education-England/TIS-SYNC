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

    ProgrammeDmsDto expected = testObj.toDmsDto(input);

    assertEquals("Mapped id doesn't match expectation", ID.toString(), expected.getId());
    assertEquals("Mapped intrepid id doesn't match expectation", LEGACY_ID,
        input.getIntrepidId());
    assertEquals("Mapped owner doesn't match expectation", OWNER, expected.getOwner());
    assertEquals("Mapped programme name doesn't match expectation",
        PROG_NAME, expected.getProgrammeName());
    assertEquals("Mapped programme number doesn't match expectation",
        PROG_NUM, expected.getProgrammeNumber());
    assertEquals("Mapped status doesn't match expectation", STATUS.name(), expected.getStatus());
  }
}

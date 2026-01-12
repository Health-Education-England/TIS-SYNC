package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;

public class ProgrammeMapperTest {

  private static final Long ID = 33L;
  private static final String LEGACY_ID = "old";
  private static final String OWNER = "TIS-SS";
  private static final String PROG_NAME = "Devin";
  private static final String PROG_NUM = "-64";
  private static final Status STATUS = Status.DELETE;
  private final ProgrammeMapper testObj = new ProgrammeMapperImpl();

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

    assertEquals(ID.toString(), expected.getId(), "Mapped id doesn't match expectation");
    assertEquals(LEGACY_ID, input.getIntrepidId(),
        "Mapped intrepid id doesn't match expectation");
    assertEquals(OWNER, expected.getOwner(), "Mapped owner doesn't match expectation");
    assertEquals(PROG_NAME,
        expected.getProgrammeName(), "Mapped programme name doesn't match expectation");
    assertEquals(PROG_NUM,
        expected.getProgrammeNumber(), "Mapped programme number doesn't match expectation");
    assertEquals(STATUS.name(), expected.getStatus(), "Mapped status doesn't match expectation");
  }
}

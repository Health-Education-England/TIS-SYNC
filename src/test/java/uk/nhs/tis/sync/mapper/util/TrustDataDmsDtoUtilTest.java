package uk.nhs.tis.sync.mapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.reference.api.enums.Status;
import org.junit.Before;
import org.junit.Test;

public class TrustDataDmsDtoUtilTest {

  private TrustDataDmsDtoUtil util;

  @Before
  public void setup() {
    util = new TrustDataDmsDtoUtil();
  }

  @Test
  public void shouldReturnStringValueOfStatus() {
    assertEquals("CURRENT", util.status(Status.CURRENT));
  }
}

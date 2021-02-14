package uk.nhs.tis.sync.mapper.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SharedUtilTest {

  private SharedUtil util;

  @Before
  public void setup() {
    util = new SharedUtil();
  }

  @Test
  public void shouldReturnIdInStringForm() {
    assertEquals("4", util.id(4L));
  }
}

package uk.nhs.tis.sync.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProgrammeMembershipMapperTest {

  private ProgrammeMembershipMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ProgrammeMembershipMapperImpl();
  }

  @Test
  void shouldMapNullProgrammeMembershipTypeToNull() {
    String mapped = mapper.map((ProgrammeMembershipType) null);
    assertThat("Unexpected programme membership type.", mapped, nullValue());
  }

  @ParameterizedTest
  @EnumSource(ProgrammeMembershipType.class)
  void shouldMapProgrammeMembershipTypeEnumToString(
      ProgrammeMembershipType programmeMembershipType) {
    String mapped = mapper.map(programmeMembershipType);
    assertThat("Unexpected programme membership type.", mapped,
        is(programmeMembershipType.toString()));
  }

  @Test
  void shouldMapNullLocalDateTimeToNull() {
    Instant mapped = mapper.map((LocalDateTime) null);
    assertThat("Unexpected instant.", mapped, nullValue());
  }

  @Test
  void shouldMapLocalDateTimeToInstant() {
    LocalDateTime now = LocalDateTime.now();
    Instant mapped = mapper.map(now);
    assertThat("Unexpected instant.", mapped, is(now.toInstant(ZoneOffset.UTC)));
  }
}

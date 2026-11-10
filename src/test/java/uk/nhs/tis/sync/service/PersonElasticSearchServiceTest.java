/*
 * The MIT License (MIT)
 *
 * Copyright 2026 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.nhs.tis.sync.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.service.job.person.PersonTrustDto;
import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import com.transformuk.hee.tis.tcs.service.mapper.PersonTrustRowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class PersonElasticSearchServiceTest {

  private static final String STATUS_ACTIVE = "ACTIVE";
  private static final String STATUS_INACTIVE = "INACTIVE";

  @Mock
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @InjectMocks
  private PersonElasticSearchService personElasticSearchService;

  @Test
  void shouldSelectStatusWhenQueryingPersonTrust() throws SQLException {
    PersonView personView = createPersonView(1L);
    mockPersonTrustRows(createPersonTrustRow(1L, 10L, STATUS_ACTIVE));

    personElasticSearchService.updateDocumentWithTrustData(List.of(personView));

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(namedParameterJdbcTemplate).query(sqlCaptor.capture(),
        any(MapSqlParameterSource.class), any(PersonTrustRowMapper.class));
    assertTrue(sqlCaptor.getValue().contains("status"),
        "PersonTrust query should select the status column");
  }

  @Test
  void shouldReturnTrustStatusInPersonView() throws SQLException {
    PersonView personView = createPersonView(1L);
    mockPersonTrustRows(createPersonTrustRow(1L, 10L, STATUS_ACTIVE));

    personElasticSearchService.updateDocumentWithTrustData(List.of(personView));

    Set<PersonTrustDto> trusts = personView.getTrusts();
    assertEquals(1, trusts.size());
    assertEquals(STATUS_ACTIVE, String.valueOf(trusts.iterator().next().getStatus()));
  }

  @Test
  void shouldReturnEachTrustStatusForMultiplePersonViews() throws SQLException {
    PersonView personView1 = createPersonView(1L);
    PersonView personView2 = createPersonView(2L);
    mockPersonTrustRows(
        createPersonTrustRow(1L, 10L, STATUS_ACTIVE),
        createPersonTrustRow(1L, 11L, STATUS_INACTIVE),
        createPersonTrustRow(2L, 20L, STATUS_INACTIVE));

    personElasticSearchService.updateDocumentWithTrustData(
        List.of(personView1, personView2));

    assertEquals(Set.of(STATUS_ACTIVE, STATUS_INACTIVE), personView1.getTrusts().stream()
        .map(trust -> String.valueOf(trust.getStatus()))
        .collect(Collectors.toSet()));
    assertEquals(STATUS_INACTIVE,
        String.valueOf(personView2.getTrusts().iterator().next().getStatus()));
  }

  /**
   * Make the mocked jdbc template run the real PersonTrustRowMapper against the given rows, so that
   * the status mapping is exercised end to end.
   */
  private void mockPersonTrustRows(ResultSet... rows) {
    when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
        any(PersonTrustRowMapper.class))).thenAnswer(invocation -> {
      RowMapper<PersonTrustDto> rowMapper = invocation.getArgument(2);
      List<PersonTrustDto> result = new ArrayList<>();
      for (int i = 0; i < rows.length; i++) {
        result.add(rowMapper.mapRow(rows[i], i));
      }
      return result;
    });
  }

  private ResultSet createPersonTrustRow(Long personId, Long trustId, String status)
      throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getLong("personId")).thenReturn(personId);
    when(resultSet.getLong("trustId")).thenReturn(trustId);
    when(resultSet.getString("status")).thenReturn(status);
    return resultSet;
  }

  private PersonView createPersonView(Long personId) {
    PersonView personView = new PersonView();
    personView.setPersonId(personId);
    return personView;
  }
}

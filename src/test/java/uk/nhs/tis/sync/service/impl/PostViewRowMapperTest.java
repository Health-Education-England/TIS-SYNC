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

package uk.nhs.tis.sync.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.job.post.PostView;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostViewRowMapperTest {

  private PostViewRowMapper rowMapper;

  @Mock
  private ResultSet resultSet;

  @BeforeEach
  void setUp() {
    rowMapper = new PostViewRowMapper();
  }

  @Test
  void shouldMapPostViewFromResultSet() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(223603L);
    when(resultSet.getLong("primarySiteId")).thenReturn(2571L);
    when(resultSet.getLong("approvedGradeId")).thenReturn(555L);
    when(resultSet.getLong("primarySpecialtyId")).thenReturn(174L);
    when(resultSet.wasNull()).thenReturn(false);

    when(resultSet.getString("surnames")).thenReturn("RRRRR");
    when(resultSet.getString("forenames")).thenReturn("LLLLL");

    when(resultSet.getString("nationalPostNumber")).thenReturn("EMD/555/999/F2/002");

    when(resultSet.getString("primarySpecialtyCode")).thenReturn("888");
    when(resultSet.getString("primarySpecialtyName")).thenReturn("Public Health Medicine");

    when(resultSet.getString("programmes")).thenReturn("Foundation Training,General Surgery");
    when(resultSet.getString("fundingStatus")).thenReturn("CURRENT");
    when(resultSet.getString("fundingType")).thenReturn("Funded - Non-tariff,Funded - Tariff");
    when(resultSet.getString("owner")).thenReturn("East Midlands");

    when(resultSet.getString("trustIds")).thenReturn("10,20");
    when(resultSet.getString("programmeIds")).thenReturn("100,200");

    PostView result = rowMapper.mapRow(resultSet, 0);

    assertThat(result.getId()).isEqualTo(223603L);

    assertThat(result.getCurrentTraineeSurnames()).isEqualTo("RRRRR");
    assertThat(result.getCurrentTraineeForenames()).isEqualTo("LLLLL");

    assertThat(result.getNationalPostNumber()).isEqualTo("EMD/555/999/F2/002");

    assertThat(result.getPrimarySiteId()).isEqualTo(2571L);
    assertThat(result.getApprovedGradeId()).isEqualTo(555L);

    assertThat(result.getPrimarySpecialtyId()).isEqualTo(174L);
    assertThat(result.getPrimarySpecialtyCode()).isEqualTo("888");
    assertThat(result.getPrimarySpecialtyName()).isEqualTo("Public Health Medicine");

    assertThat(result.getProgrammeNames())
        .containsExactly("Foundation Training", "General Surgery");

    assertThat(result.getStatus()).isEqualTo(Status.CURRENT);

    assertThat(result.getFundingTypes())
        .containsExactly("Funded - Non-tariff", "Funded - Tariff");

    assertThat(result.getOwner()).isEqualTo("East Midlands");

    assertThat(result.getTrustIds()).containsExactly(10L, 20L);
    assertThat(result.getProgrammeIds()).containsExactly(100L, 200L);
  }

  @Test
  void shouldMapNullableLongFieldsToNullWhenResultSetValueWasNull() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(0L);
    when(resultSet.getLong("primarySiteId")).thenReturn(0L);
    when(resultSet.getLong("approvedGradeId")).thenReturn(0L);
    when(resultSet.getLong("primarySpecialtyId")).thenReturn(0L);

    when(resultSet.wasNull())
        .thenReturn(true)   // id
        .thenReturn(true)   // primarySiteId
        .thenReturn(true)   // approvedGradeId
        .thenReturn(true);  // primarySpecialtyId

    PostView result = rowMapper.mapRow(resultSet, 0);

    assertThat(result.getId()).isNull();
    assertThat(result.getPrimarySiteId()).isNull();
    assertThat(result.getApprovedGradeId()).isNull();
    assertThat(result.getPrimarySpecialtyId()).isNull();
  }

  @Test
  void shouldNotSetStatusWhenFundingStatusIsNull() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(223603L);
    when(resultSet.wasNull()).thenReturn(false);
    when(resultSet.getString(anyString())).thenReturn(null);
    when(resultSet.getString("fundingStatus")).thenReturn(null);

    PostView result = rowMapper.mapRow(resultSet, 0);

    assertThat(result.getStatus()).isNull();
  }

  @Test
  void shouldNotSetStatusWhenFundingStatusIsEmpty() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(223603L);
    when(resultSet.wasNull()).thenReturn(false);
    when(resultSet.getString(anyString())).thenReturn(null);
    when(resultSet.getString("fundingStatus")).thenReturn("");

    PostView result = rowMapper.mapRow(resultSet, 0);

    assertThat(result.getStatus()).isNull();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenFundingStatusIsInvalid() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(223603L);
    when(resultSet.wasNull()).thenReturn(false);

    when(resultSet.getString(anyString())).thenReturn(null);
    when(resultSet.getString("fundingStatus")).thenReturn("INVALID_STATUS");

    assertThatThrownBy(() -> rowMapper.mapRow(resultSet, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("INVALID_STATUS");
  }

  @Test
  void shouldMapEmptyListsWhenConcatenatedFieldsAreNull() throws SQLException {
    when(resultSet.getLong("id")).thenReturn(223603L);
    when(resultSet.wasNull()).thenReturn(false);
    when(resultSet.getString(anyString())).thenReturn(null);

    PostView result = rowMapper.mapRow(resultSet, 0);

    assertThat(result.getProgrammeNames()).isEmpty();
    assertThat(result.getFundingTypes()).isEmpty();
    assertThat(result.getTrustIds()).isEmpty();
    assertThat(result.getProgrammeIds()).isEmpty();
  }
}

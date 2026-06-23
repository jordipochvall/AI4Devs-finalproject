package com.novacasino.application.operator;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.RoundSummaryDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit test for HU-3 audit search delegation in {@link AuditUseCase} (port mocked). */
class AuditUseCaseTest {

    @Test
    void searchRounds_delegatesToPort() {
        final AuditRoundsPort port = mock(AuditRoundsPort.class);
        final OffsetDateTime now = OffsetDateTime.now();
        final RoundSummaryDto row = new RoundSummaryDto(1L, 7L, 10L, 100L, 100, 0, 900, false, null, now);
        final PageResponse<RoundSummaryDto> page = new PageResponse<>(List.of(row), 0, 20, 1, 1);
        when(port.search(eq(1L), isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

        final PageResponse<RoundSummaryDto> result =
                new AuditUseCase(port).searchRounds(1L, null, null, null, null, new PageRequestDto(0, 20));

        assertThat(result.content()).containsExactly(row);
        verify(port).search(eq(1L), isNull(), isNull(), isNull(), isNull(), any());
    }
}

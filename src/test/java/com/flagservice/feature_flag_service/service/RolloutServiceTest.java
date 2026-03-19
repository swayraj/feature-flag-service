package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.dto.FlagEvaluationResponse;
import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolloutServiceTest {

    @Mock
    private FlagRepository flagRepository;

    private RolloutService rolloutService;

    @BeforeEach
    void setUp() {
        rolloutService = new RolloutService(flagRepository);
    }

    // ── FLAG NOT FOUND ───────────────────────────────────────────────────────

    @Test
    void evaluateFlag_flagNotFound_throwsFlagNotFoundException() {
        when(flagRepository.findByNameIgnoreCase("missing_flag")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolloutService.evaluateFlag("missing_flag", "user-1"))
                .isInstanceOf(FlagNotFoundException.class);
    }

    // ── DISABLED FLAG ────────────────────────────────────────────────────────

    @Test
    void evaluateFlag_disabledFlag_returnsFalse() {
        Flag flag = new Flag(1L, "my_flag", "desc", false, 100);
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-1");

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getReason()).isEqualTo("Flag is disabled globally");
    }

    // ── TARGETED USER ────────────────────────────────────────────────────────

    @Test
    void evaluateFlag_targetedUser_returnsTrueRegardlessOfPercentage() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 0);
        flag.setTargetUserIds("user-special, user-vip");
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-special");

        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getReason()).isEqualTo("User is specifically targeted");
    }

    @Test
    void evaluateFlag_nonTargetedUser_notForcedOn() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 0);
        flag.setTargetUserIds("user-special");
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-ordinary");

        // 0% rollout + not targeted → disabled
        assertThat(result.isEnabled()).isFalse();
    }

    // ── ROLLOUT PERCENTAGE ───────────────────────────────────────────────────

    @Test
    void evaluateFlag_zeroPercentRollout_returnsFalse() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 0);
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-1");

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getReason()).isEqualTo("User not in rollout percentage");
    }

    @Test
    void evaluateFlag_hundredPercentRollout_returnsTrue() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 100);
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-1");

        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void evaluateFlag_consistentForSameUser_alwaysSameResult() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 50);
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        // Same user evaluated 3 times — result must be identical every time
        FlagEvaluationResponse first = rolloutService.evaluateFlag("my_flag", "user-42");
        FlagEvaluationResponse second = rolloutService.evaluateFlag("my_flag", "user-42");
        FlagEvaluationResponse third = rolloutService.evaluateFlag("my_flag", "user-42");

        assertThat(first.isEnabled()).isEqualTo(second.isEnabled());
        assertThat(second.isEnabled()).isEqualTo(third.isEnabled());
    }

    @Test
    void evaluateFlag_responseContainsCorrectFlagNameAndUserId() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 100);
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        FlagEvaluationResponse result = rolloutService.evaluateFlag("my_flag", "user-99");

        assertThat(result.getFlagName()).isEqualTo("my_flag");
        assertThat(result.getUserId()).isEqualTo("user-99");
    }

    // ── SEGMENTATION ─────────────────────────────────────────────────────────

    @Test
    void evaluateFlagWithAttributes_matchingSegment_allowsAccess() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 100);
        flag.setUserSegment("{\"country\":\"US\"}");
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        java.util.Map<String, String> attrs = java.util.Map.of("country", "US");
        FlagEvaluationResponse result = rolloutService.evaluateFlagWithAttributes("my_flag", "user-1", attrs);

        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void evaluateFlagWithAttributes_nonMatchingSegment_deniesAccess() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 100);
        flag.setUserSegment("{\"country\":\"US\"}");
        when(flagRepository.findByNameIgnoreCase("my_flag")).thenReturn(Optional.of(flag));

        java.util.Map<String, String> attrs = java.util.Map.of("country", "UK");
        FlagEvaluationResponse result = rolloutService.evaluateFlagWithAttributes("my_flag", "user-1", attrs);

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getReason()).isEqualTo("User does not match segment criteria");
    }
}

package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.exception.FlagValidationException;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlagServiceTest {

    @Mock
    private FlagRepository flagRepository;

    @Mock
    private FlagEventService flagEventService;

    private FlagService flagService;

    @BeforeEach
    void setUp() {
        // Return 1 so the constructor skips seeding sample data
        when(flagRepository.count()).thenReturn(1L);
        flagService = new FlagService(flagRepository, flagEventService);
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void createFlag_validFlag_savesAndBroadcasts() {
        Flag input = new Flag(null, "my_flag", "desc", true, 50);
        Flag saved = new Flag(1L, "my_flag", "desc", true, 50);

        when(flagRepository.existsByNameIgnoreCase("my_flag")).thenReturn(false);
        when(flagRepository.save(input)).thenReturn(saved);

        Flag result = flagService.createFlag(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(flagEventService).broadcastFlagCreated(saved);
    }

    @Test
    void createFlag_duplicateName_throwsValidationException() {
        Flag input = new Flag(null, "my_flag", "desc", true, 50);
        when(flagRepository.existsByNameIgnoreCase("my_flag")).thenReturn(true);

        assertThatThrownBy(() -> flagService.createFlag(input))
                .isInstanceOf(FlagValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createFlag_nameTooShort_throwsValidationException() {
        Flag input = new Flag(null, "ab", "desc", true, 50);

        assertThatThrownBy(() -> flagService.createFlag(input))
                .isInstanceOf(FlagValidationException.class)
                .hasMessageContaining("at least 3 characters");
    }

    @Test
    void createFlag_invalidCharactersInName_throwsValidationException() {
        Flag input = new Flag(null, "my flag!", "desc", true, 50);

        assertThatThrownBy(() -> flagService.createFlag(input))
                .isInstanceOf(FlagValidationException.class)
                .hasMessageContaining("letters, numbers, underscores");
    }

    @Test
    void createFlag_rolloutOver100_throwsValidationException() {
        Flag input = new Flag(null, "my_flag", "desc", true, 150);

        assertThatThrownBy(() -> flagService.createFlag(input))
                .isInstanceOf(FlagValidationException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    void createFlag_negativeRollout_throwsValidationException() {
        Flag input = new Flag(null, "my_flag", "desc", true, -1);

        assertThatThrownBy(() -> flagService.createFlag(input))
                .isInstanceOf(FlagValidationException.class)
                .hasMessageContaining("between 0 and 100");
    }

    // ── GET ─────────────────────────────────────────────────────────────────

    @Test
    void getAllFlags_returnsAllFlags() {
        List<Flag> flags = List.of(
                new Flag(1L, "flag_one", "d", true, 10),
                new Flag(2L, "flag_two", "d", false, 0)
        );
        when(flagRepository.findAll()).thenReturn(flags);

        List<Flag> result = flagService.getAllFlags();

        assertThat(result).hasSize(2);
    }

    @Test
    void getFlagById_existingId_returnsFlag() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 50);
        when(flagRepository.findById(1L)).thenReturn(Optional.of(flag));

        Optional<Flag> result = flagService.getFlagById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("my_flag");
    }

    @Test
    void getFlagById_missingId_returnsEmpty() {
        when(flagRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Flag> result = flagService.getFlagById(99L);

        assertThat(result).isEmpty();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void updateFlag_validUpdate_savesAndBroadcasts() {
        Flag existing = new Flag(1L, "old_name", "old desc", false, 0);
        Flag update = new Flag(null, "new_name", "new desc", true, 75);
        Flag saved = new Flag(1L, "new_name", "new desc", true, 75);

        when(flagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(flagRepository.existsByNameIgnoreCase("new_name")).thenReturn(false);
        when(flagRepository.save(existing)).thenReturn(saved);

        Flag result = flagService.updateFlag(1L, update);

        assertThat(result.getName()).isEqualTo("new_name");
        assertThat(result.getRolloutPercentage()).isEqualTo(75);
        verify(flagEventService).broadcastFlagUpdated(saved);
    }

    @Test
    void updateFlag_sameName_doesNotCheckDuplicates() {
        Flag existing = new Flag(1L, "my_flag", "old desc", false, 0);
        Flag update = new Flag(null, "my_flag", "new desc", true, 50);
        Flag saved = new Flag(1L, "my_flag", "new desc", true, 50);

        when(flagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(flagRepository.save(existing)).thenReturn(saved);

        Flag result = flagService.updateFlag(1L, update);

        assertThat(result.getDescription()).isEqualTo("new desc");
        verify(flagRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void updateFlag_notFound_throwsFlagNotFoundException() {
        when(flagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flagService.updateFlag(99L, new Flag(null, "x_flag", "d", true, 0)))
                .isInstanceOf(FlagNotFoundException.class);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deleteFlag_existingFlag_deletesAndBroadcasts() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 50);
        when(flagRepository.findById(1L)).thenReturn(Optional.of(flag));

        flagService.deleteFlag(1L);

        verify(flagRepository).deleteById(1L);
        verify(flagEventService).broadcastFlagDeleted(1L, "my_flag");
    }

    @Test
    void deleteFlag_notFound_throwsFlagNotFoundException() {
        when(flagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flagService.deleteFlag(99L))
                .isInstanceOf(FlagNotFoundException.class);
    }

    // ── TOGGLE ───────────────────────────────────────────────────────────────

    @Test
    void toggleFlag_enabledFlag_disablesIt() {
        Flag flag = new Flag(1L, "my_flag", "desc", true, 50);
        when(flagRepository.findById(1L)).thenReturn(Optional.of(flag));
        when(flagRepository.save(flag)).thenReturn(flag);

        Flag result = flagService.toggleFlag(1L);

        assertThat(result.isEnabled()).isFalse();
        verify(flagEventService).broadcastFlagToggled(flag);
    }

    @Test
    void toggleFlag_disabledFlag_enablesIt() {
        Flag flag = new Flag(1L, "my_flag", "desc", false, 50);
        when(flagRepository.findById(1L)).thenReturn(Optional.of(flag));
        when(flagRepository.save(flag)).thenReturn(flag);

        Flag result = flagService.toggleFlag(1L);

        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void toggleFlag_notFound_throwsFlagNotFoundException() {
        when(flagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flagService.toggleFlag(99L))
                .isInstanceOf(FlagNotFoundException.class);
    }
}

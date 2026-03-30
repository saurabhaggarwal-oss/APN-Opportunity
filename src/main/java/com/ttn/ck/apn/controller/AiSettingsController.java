package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.AiSettings;
import com.ttn.ck.apn.repository.AiSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai-settings")
@RequiredArgsConstructor
public class AiSettingsController {

    private final AiSettingsRepository aiSettingsRepository;

    /** FR-4.5: Get current AI settings (creates default singleton on first call) */
    @GetMapping
    public SuccessResponseDto<AiSettings> getSettings() {
        AiSettings settings = aiSettingsRepository.getSingleton();
        if (settings == null) {
            settings = new AiSettings();
            settings.setId(1L); // enforce singleton id
            settings.setTone("Professional");
            settings.setAvoidedWords("");
            settings.setCustomInstructions("");
            settings = aiSettingsRepository.save(settings);
        }
        return new SuccessResponseDto<>(settings);
    }

    /** FR-4.5: Update AI settings */
    @PutMapping
    public SuccessResponseDto<AiSettings> updateSettings(@RequestBody AiSettings update) {
        AiSettings settings = aiSettingsRepository.getSingleton();
        if (settings == null) {
            settings = new AiSettings();
            settings.setId(1L); // singleton — always use ID 1
        }
        settings.setTone(update.getTone());
        settings.setAvoidedWords(update.getAvoidedWords());
        settings.setCustomInstructions(update.getCustomInstructions());
        settings.setExampleTitle(update.getExampleTitle());
        settings.setExampleDescription(update.getExampleDescription());
        return new SuccessResponseDto<>(aiSettingsRepository.save(settings));
    }
}

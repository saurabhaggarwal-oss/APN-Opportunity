package com.ttn.ck.apn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_settings")
public class AiSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tone")
    private String tone; // professional, formal, etc.

    @Column(name = "avoided_words", columnDefinition = "TEXT")
    private String avoidedWords; // Comma-separated list

    @Column(name = "custom_instructions", columnDefinition = "TEXT")
    private String customInstructions;

    @Column(name = "example_title", columnDefinition = "TEXT")
    private String exampleTitle;

    @Column(name = "example_description", columnDefinition = "TEXT")
    private String exampleDescription;
}

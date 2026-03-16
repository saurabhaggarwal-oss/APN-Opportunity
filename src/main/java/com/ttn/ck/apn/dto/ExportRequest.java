package com.ttn.ck.apn.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for exporting master opportunity data to Excel.
 * Frontend sends a list of selected UUIDs.
 *
 * Example request body:
 * {
 *   "uuids": [
 *     "550e8400-e29b-41d4-a716-446655440000",
 *     "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotEmpty(message = "At least one UUID must be provided for export")
    private List<String> uuids;
}

package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.CustomerMapping;
import com.ttn.ck.apn.repository.CustomerMappingRepository;
import com.ttn.ck.errorhandler.exceptions.GenericStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerMappingService {

    private final CustomerMappingRepository customerMappingRepository;

    @Transactional
    public void processCsvUpload(MultipartFile file) {
        log.info("Starting Customer Mapping CSV upload processing: {}", file.getOriginalFilename());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstRow) {
                    isFirstRow = false; // Skip header
                    continue;
                }

                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Split keeping quoted commas

                if (columns.length < 1) continue;

                String billdeskName = cleanColumn(columns[0]);
                if (billdeskName == null || billdeskName.isEmpty()) continue;

                CustomerMapping mapping = new CustomerMapping();
                mapping.setBilldeskCustomerName(billdeskName);
                
                // FR 8.2: Column positions
                if (columns.length > 2) mapping.setApnLegalName(cleanColumn(columns[2]));
                if (columns.length > 4) mapping.setIndustry(cleanColumn(columns[4]));
                if (columns.length > 6) mapping.setCountry(cleanColumn(columns[6]));
                if (columns.length > 7) mapping.setStateProvince(cleanColumn(columns[7]));
                if (columns.length > 8) mapping.setPostalCode(cleanColumn(columns[8]));
                if (columns.length > 9) mapping.setWebsite(cleanColumn(columns[9]));

                customerMappingRepository.save(mapping); // Upsert strategy
            }
        } catch (Exception e) {
            log.error("Failed to parse Customer CSV: ", e);
            throw new GenericStatusException("Failed to process CSV file", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String cleanColumn(String col) {
        if (col == null) return null;
        return col.trim().replaceAll("^\"|\"$", ""); // Remove leading/trailing quotes
    }
}

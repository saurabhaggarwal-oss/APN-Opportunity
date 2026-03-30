package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.Resource;
import com.ttn.ck.apn.model.ResourceSnapshot;
import com.ttn.ck.apn.model.UploadBatch;
import com.ttn.ck.apn.repository.ResourceRepository;
import com.ttn.ck.apn.repository.ResourceSnapshotRepository;
import com.ttn.ck.apn.repository.UploadBatchRepository;
import com.ttn.ck.errorhandler.exceptions.GenericStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurIngestionService {

    private final ResourceRepository resourceRepository;
    private final ResourceSnapshotRepository snapshotRepository;
    private final UploadBatchRepository batchRepository;
    private final ResourceGroupingService resourceGroupingService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public void processCurUpload(MultipartFile file) {
        log.info("Starting CUR upload processing for file: {}", file.getOriginalFilename());

        UploadBatch batch = UploadBatch.builder()
                .fileName(file.getOriginalFilename())
                .uploadDate(LocalDateTime.now())
                .status("processing")
                .build();
        batch = batchRepository.save(batch);

        List<Resource> parsedResources = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String resourceId = getStringValue(row.getCell(3));
                    if (resourceId == null || resourceId.trim().isEmpty()) continue;

                    Resource r = new Resource();
                    r.setCustomerName(getStringValue(row.getCell(0)));
                    r.setAccountId(getStringValue(row.getCell(1)));
                    r.setProductName(getStringValue(row.getCell(2)));
                    r.setLineitemResourceId(resourceId);
                    r.setRegion(getStringValue(row.getCell(4)));
                    r.setOperatingSystem(getStringValue(row.getCell(5)));
                    r.setInstanceType(getStringValue(row.getCell(6)));
                    r.setFinalNameTag(getStringValue(row.getCell(7)));
                    r.setFinalAutoscalingName(getStringValue(row.getCell(8)));
                    
                    // Skips 9 (Title), 10 (Description)
                    r.setResourceBirthDate(getDateValue(row.getCell(11)));
                    r.setTotalPeriodCost(getNumericValue(row.getCell(12), java.math.BigDecimal.class));
                    r.setActiveDaysCount(getNumericValue(row.getCell(13), Double.class));
                    
                    Double expectedDays = getNumericValue(row.getCell(14), Double.class);
                    Double persistencePct = getNumericValue(row.getCell(15), Double.class);
                    r.setPersistencePct(persistencePct);

                    r.setBatch(batch);
                    parsedResources.add(r);

                } catch (Exception e) {
                    log.warn("Failed to parse row {}: {}", row.getRowNum(), e.getMessage());
                }
            }

            final UploadBatch finalBatch = batch;
            for (Resource parsed : parsedResources) {
                resourceRepository.findById(parsed.getLineitemResourceId()).ifPresent(existing -> {
                    try {
                        ResourceSnapshot snapshot = ResourceSnapshot.builder()
                                .lineitemResourceId(existing.getLineitemResourceId())
                                .snapshotData(objectMapper.writeValueAsString(existing))
                                .batch(finalBatch)
                                .build();
                        snapshotRepository.save(snapshot);
                    } catch (Exception ex) {
                        log.error("Failed to snapshot resource: {}", existing.getLineitemResourceId());
                    }
                });

                // Dedup strategy (FR 1.2): Upsert
                resourceRepository.save(parsed);
            }

            // FR 3: Group resources
            resourceGroupingService.groupResources(parsedResources);

            batch.setStatus("completed");
            batchRepository.save(batch);

        } catch (Exception e) {
            batch.setStatus("rolled_back");
            batchRepository.save(batch);
            log.error("Upload failed: ", e);
            throw new GenericStatusException("Failed to process Excel file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Transactional
    public void rollbackBatch(Long batchId) {
        log.info("Rolling back batch {}", batchId);
        UploadBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new GenericStatusException("Batch not found", 404));

        List<ResourceSnapshot> snapshots = snapshotRepository.findByBatchId(batchId);
        for (ResourceSnapshot snapshot : snapshots) {
            try {
                Resource oldState = objectMapper.readValue(snapshot.getSnapshotData(), Resource.class);
                resourceRepository.save(oldState);
            } catch (Exception e) {
                log.error("Failed to restore snapshot for: {}", snapshot.getLineitemResourceId());
            }
        }
        batch.setStatus("rolled_back");
        batchRepository.save(batch);
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return null;
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private <T extends Number> T getNumericValue(Cell cell, Class<T> clazz) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return null;
        double value = cell.getNumericCellValue();
        if (clazz == java.math.BigDecimal.class) {
            return clazz.cast(java.math.BigDecimal.valueOf(value));
        } else if (clazz == Double.class) {
            return clazz.cast(value);
        }
        return null;
    }
}

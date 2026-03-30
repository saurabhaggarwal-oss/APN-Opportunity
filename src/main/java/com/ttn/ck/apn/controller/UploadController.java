package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.UploadBatch;
import com.ttn.ck.apn.repository.UploadBatchRepository;
import com.ttn.ck.apn.service.CurIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CurIngestionService curIngestionService;
    private final UploadBatchRepository uploadBatchRepository;

    /** FR-1.1 / FR-1.3: Upload a CUR Excel file */
    @PostMapping
    public SuccessResponseDto<String> uploadCurFile(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload CUR file: {}", file.getOriginalFilename());
        curIngestionService.processCurUpload(file);
        return new SuccessResponseDto<>("File processing queued successfully.");
    }

    /** FR-1.5: List all upload batches ordered by date descending */
    @GetMapping
    public SuccessResponseDto<List<UploadBatch>> getBatchHistory() {
        return new SuccessResponseDto<>(uploadBatchRepository.findAllByOrderByUploadDateDesc());
    }

    /** FR-1.5: Get a specific batch by ID */
    @GetMapping("/{batchId}")
    public SuccessResponseDto<UploadBatch> getBatch(@PathVariable Long batchId) {
        return new SuccessResponseDto<>(uploadBatchRepository.findById(batchId).orElse(null));
    }

    /** FR-9.2: Manually roll back a completed batch */
    @PostMapping("/rollback/{batchId}")
    public SuccessResponseDto<String> rollbackBatch(@PathVariable Long batchId) {
        log.info("Received request to rollback batch ID: {}", batchId);
        curIngestionService.rollbackBatch(batchId);
        return new SuccessResponseDto<>("Batch rolled back successfully.");
    }
}

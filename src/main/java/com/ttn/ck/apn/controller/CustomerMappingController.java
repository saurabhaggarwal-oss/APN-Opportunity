package com.ttn.ck.apn.controller;

import com.ttn.ck.apn.dto.SuccessResponseDto;
import com.ttn.ck.apn.model.CustomerMapping;
import com.ttn.ck.apn.repository.CustomerMappingRepository;
import com.ttn.ck.apn.service.CustomerMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerMappingController {

    private final CustomerMappingService customerMappingService;
    private final CustomerMappingRepository customerMappingRepository;

    /** FR-2.1: Upload Customer Mapping CSV */
    @PostMapping("/upload")
    public SuccessResponseDto<String> uploadCustomerCsv(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload Customer Mapping CSV: {}", file.getOriginalFilename());
        customerMappingService.processCsvUpload(file);
        return new SuccessResponseDto<>("Customer mapping uploaded and processed.");
    }

    /** FR-2: List all customer mappings with optional search */
    @GetMapping
    public SuccessResponseDto<List<CustomerMapping>> getCustomers(
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            // Simple in-memory filter on the name (table is typically small)
            List<CustomerMapping> filtered = customerMappingRepository.findAll().stream()
                    .filter(c -> c.getBilldeskCustomerName() != null &&
                            c.getBilldeskCustomerName().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            return new SuccessResponseDto<>(filtered);
        }
        return new SuccessResponseDto<>(customerMappingRepository.findAll());
    }

    /** FR-2: Get a single customer by name */
    @GetMapping("/{name}")
    public SuccessResponseDto<CustomerMapping> getCustomer(@PathVariable String name) {
        return new SuccessResponseDto<>(customerMappingRepository.findById(name).orElse(null));
    }

    /** FR-2.2: Upsert a single customer mapping via API */
    @PutMapping("/{name}")
    public SuccessResponseDto<CustomerMapping> upsertCustomer(
            @PathVariable String name,
            @RequestBody CustomerMapping mapping) {
        mapping.setBilldeskCustomerName(name);
        return new SuccessResponseDto<>(customerMappingRepository.save(mapping));
    }
}

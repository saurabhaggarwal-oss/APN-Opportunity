package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.CustomerMapping;
import com.ttn.ck.apn.model.Opportunity;
import com.ttn.ck.apn.model.Resource;
import com.ttn.ck.apn.repository.CustomerMappingRepository;
import com.ttn.ck.apn.repository.OpportunityRepository;
import com.ttn.ck.apn.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceGroupingService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerMappingRepository customerMappingRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Default grouping: Customer + Product + Region + NameTag (FR-3.1 / FR-3.2 / FR-3.3)
     */
    @Transactional
    public void groupResources(List<Resource> resources) {
        groupResources(resources, null);
    }

    /**
     * Custom-field grouping (FR-3.4).
     * groupingFields values: CUSTOMER, PRODUCT, REGION, NAME_TAG,
     *   AUTOSCALING_NAME, INSTANCE_TYPE, OPERATING_SYSTEM, ACCOUNT_ID, RESOURCE_ARN
     */
    @Transactional
    public void groupResources(List<Resource> resources, List<String> groupingFields) {
        log.info("Grouping {} resources, custom fields: {}", resources.size(), groupingFields);

        boolean useCustomFields = groupingFields != null && !groupingFields.isEmpty();
        Map<String, List<Resource>> groups = new HashMap<>();

        for (Resource r : resources) {
            String customer    = safeString(r.getCustomerName());
            String product     = safeString(r.getProductName());
            String region      = safeString(r.getRegion());
            String nameTag     = safeString(r.getFinalNameTag());
            String asgName     = safeString(r.getFinalAutoscalingName());
            String instanceType = safeString(r.getInstanceType());
            String os          = safeString(r.getOperatingSystem());
            String accountId   = safeString(r.getAccountId());
            String arn         = safeString(r.getLineitemResourceId());

            String groupKey;
            String groupingReason;
            String groupingFieldsDesc;

            if (useCustomFields) {
                // Build key from selected fields
                StringBuilder keyBuilder = new StringBuilder();
                List<String> usedValues = new ArrayList<>();
                List<String> usedFields = new ArrayList<>();

                for (String field : groupingFields) {
                    switch (field.toUpperCase()) {
                        case "CUSTOMER"        -> { keyBuilder.append(customer).append("|");        usedValues.add(customer);     usedFields.add("Customer"); }
                        case "PRODUCT"         -> { keyBuilder.append(product).append("|");         usedValues.add(product);      usedFields.add("Product"); }
                        case "REGION"          -> { keyBuilder.append(region).append("|");          usedValues.add(region);       usedFields.add("Region"); }
                        case "NAME_TAG"        -> { keyBuilder.append(nameTag).append("|");         usedValues.add(nameTag);      usedFields.add("Name Tag"); }
                        case "AUTOSCALING_NAME"-> { keyBuilder.append(asgName).append("|");        usedValues.add(asgName);      usedFields.add("Autoscaling Name"); }
                        case "INSTANCE_TYPE"   -> { keyBuilder.append(instanceType).append("|");   usedValues.add(instanceType); usedFields.add("Instance Type"); }
                        case "OPERATING_SYSTEM"-> { keyBuilder.append(os).append("|");             usedValues.add(os);           usedFields.add("OS"); }
                        case "ACCOUNT_ID"      -> { keyBuilder.append(accountId).append("|");      usedValues.add(accountId);    usedFields.add("Account ID"); }
                        case "RESOURCE_ARN"    -> { keyBuilder.append(arn).append("|");            usedValues.add(arn);          usedFields.add("Resource ARN"); }
                        default -> log.warn("Unknown grouping field: {}", field);
                    }
                }
                groupKey = generateHash(keyBuilder.toString());
                groupingReason = "Custom grouping by: " + String.join(", ", usedFields);
                groupingFieldsDesc = String.join(", ", usedFields) + " (" + String.join(" | ", usedValues) + ")";
            } else {
                // Default logic: FR-3.2 Name Tag, FR-3.3 Fallback
                if (!nameTag.isEmpty()) {
                    groupKey = generateHash(customer + "|" + product + "|" + region + "|" + nameTag);
                    groupingReason = "Grouped by matching customer, product, region, and Name Tag";
                    groupingFieldsDesc = "Customer, Product, Region, Name Tag (" + nameTag + ")";
                } else if (!asgName.isEmpty()) {
                    groupKey = generateHash(customer + "|" + product + "|" + region + "|" + asgName);
                    groupingReason = "Grouped by matching customer, product, region, and Autoscaling Group";
                    groupingFieldsDesc = "Customer, Product, Region, Autoscaling Name (" + asgName + ")";
                } else {
                    groupKey = generateHash(customer + "|" + product + "|" + region + "|" + instanceType + "|" + arn);
                    groupingReason = "Isolated resource (no Name Tag or Autoscaling Group available)";
                    groupingFieldsDesc = "Customer, Product, Region, Instance Type, ARN";
                }
            }

            r.setGroupKey(groupKey);
            resourceRepository.save(r);
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(r);

            // Create opportunity if not exists
            Opportunity opp = opportunityRepository.findById(groupKey).orElse(new Opportunity());
            if (opp.getGroupKey() == null) {
                opp.setGroupKey(groupKey);
                opp.setCustomerName(r.getCustomerName());
                opp.setProductName(r.getProductName());
                opp.setRegion(r.getRegion());
                opp.setNameTag(nameTag);
                opp.setGroupingReason(groupingReason);
                opp.setGroupingFields(groupingFieldsDesc);
                opp.setStatus("draft");
                opp.setOpportunityType("Expansion");

                // FR-3.7 Use Case
                String uc = deriveUseCase(product);
                opp.setUseCase(uc);

                // FR-2.3 Enrich with customer mapping
                customerMappingRepository.findById(r.getCustomerName()).ifPresent(cm ->
                        opp.setApnLegalName(cm.getApnLegalName())
                );

                opportunityRepository.save(opp);
            }
        }

        // Compute aggregates per group (FR-3.6)
        for (Map.Entry<String, List<Resource>> entry : groups.entrySet()) {
            String key = entry.getKey();
            List<Resource> groupRes = entry.getValue();

            opportunityRepository.findById(key).ifPresent(opp -> {
                BigDecimal sumCost = BigDecimal.ZERO;
                double maxActiveDays = 1.0;
                Set<String> instanceTypes = new LinkedHashSet<>();

                for (Resource gr : groupRes) {
                    if (gr.getTotalPeriodCost() != null) sumCost = sumCost.add(gr.getTotalPeriodCost());
                    if (gr.getActiveDaysCount() != null && gr.getActiveDaysCount() > maxActiveDays) {
                        maxActiveDays = gr.getActiveDaysCount();
                    }
                    if (gr.getInstanceType() != null && !gr.getInstanceType().isEmpty()) {
                        instanceTypes.add(gr.getInstanceType());
                    }
                }

                // FR-3.6: (sum_cost / max_active_days) * 30
                BigDecimal dailyCost = sumCost.divide(BigDecimal.valueOf(maxActiveDays), 4, RoundingMode.HALF_UP);
                BigDecimal mrr = dailyCost.multiply(BigDecimal.valueOf(30));

                opp.setEstimatedMrr(mrr);
                opp.setResourceCount(groupRes.size());
                opp.setInstanceTypes(String.join(", ", instanceTypes));
                opportunityRepository.save(opp);
            });
        }

        log.info("Grouping complete. Created/updated {} opportunity groups.", groups.size());
    }

    /**
     * Re-group ALL resources currently in the DB (used by /resources/regroup endpoint).
     */
    @Transactional
    public int regroupAll(List<String> groupingFields, String customerNameFilter) {
        List<Resource> resources;
        if (customerNameFilter != null && !customerNameFilter.isBlank()) {
            resources = resourceRepository.findByCustomerNameContainingIgnoreCase(customerNameFilter);
        } else {
            resources = resourceRepository.findAll();
        }
        if (resources.isEmpty()) return 0;

        // Clear existing opportunities so they're rebuilt fresh
        opportunityRepository.deleteAll(opportunityRepository.findAll().stream()
                .filter(o -> customerNameFilter == null || customerNameFilter.isBlank() ||
                        (o.getCustomerName() != null &&
                                o.getCustomerName().toLowerCase().contains(customerNameFilter.toLowerCase())))
                .collect(Collectors.toList()));

        groupResources(resources, groupingFields);
        return resources.size();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String deriveUseCase(String product) {
        if (product == null) return "Other";
        String p = product.toLowerCase();
        if (p.contains("ec2")) return "Compute";
        if (p.contains("rds")) return "Database";
        if (p.contains("data transfer")) return "Networking";
        if (p.contains("s3")) return "Storage";
        if (p.contains("lambda")) return "Compute";
        if (p.contains("eks") || p.contains("ecs")) return "Compute";
        return "Other";
    }

    private String safeString(String in) {
        return in == null ? "" : in.trim();
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

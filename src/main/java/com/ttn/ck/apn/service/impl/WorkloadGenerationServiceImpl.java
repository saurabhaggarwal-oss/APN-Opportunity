package com.ttn.ck.apn.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.messaging.RefreshMessage;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.model.OpenAiResponseDTO;
import com.ttn.ck.apn.model.WorkloadEvent;
import com.ttn.ck.apn.model.WorkloadResponseDTO;
import com.ttn.ck.apn.service.WorkloadEventProducer;
import com.ttn.ck.apn.service.WorkloadGenerationService;
import com.ttn.ck.apn.utils.ResourceLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ttn.ck.apn.utils.ApnUtils.BATCH_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadGenerationServiceImpl implements WorkloadGenerationService {

    private final ChatClient chatClient;
    private final ResourceLoaderService resourceLoaderService;
    private final ApnOpportunityDataDao apnOpportunityDataDao;
    private final WorkloadEventProducer workloadEventProducer;
    private final ObjectMapper objectMapper;

    @Override
    public void processUnprocessedWorkloads(RefreshMessage message) {
        log.info("Starting processing of unprocessed workloads. {}", message);
        List<ApnOpportunityRawData> unprocessedData = apnOpportunityDataDao.fetchUnprocessedRawData();
        if (unprocessedData == null || unprocessedData.isEmpty()) {
            log.info("No unprocessed raw data found.");
            return;
        }
        Map<String, List<ApnOpportunityRawData>> groupedByCustomer = unprocessedData.stream()
                .filter(data -> data.getCustomerName() != null)
                .collect(Collectors.groupingBy(ApnOpportunityRawData::getCustomerName));

        groupedByCustomer.forEach((key, value) -> processCustomerWorkloads(key, value, message.getPartnerName()));
        log.info("Finished processing unprocessed workloads.");
    }

    private void processCustomerWorkloads(String customerName, List<ApnOpportunityRawData> customerRecords, String partnerName) {
        log.info("Processing {} records for customer: {}", customerRecords.size(), customerName);
        for (int i = 0; i < customerRecords.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, customerRecords.size());
            List<ApnOpportunityRawData> batch = customerRecords.subList(i, end);
            processBatch(batch, partnerName);
        }
    }

    private void processBatch(List<ApnOpportunityRawData> batch, String partnerName) {
        if (batch.isEmpty()) return;
        try {
            String inputTable = formatRawDataAsTable(batch);
            String fullPrompt = resourceLoaderService.loadFile("prompts/workload-generation-prompt.st") + "\n\n" + inputTable;
            Object responseObj = chatClient.prompt()
                    .user(fullPrompt)
                    .call()
                    .entity(Object.class);
            OpenAiResponseDTO responseDTO = objectMapper.convertValue(responseObj, new TypeReference<>() {});
            List<WorkloadResponseDTO> response = objectMapper.convertValue(responseDTO.getData(), new TypeReference<>() {});
            if (response != null) {
                log.info("Received ChatGPT response for batch insertBatch {} and output {}", batch.size(), response.size());
                apnOpportunityDataDao.updateWorkloadDetailsByLineItemUuid(response);
                publishMasterDataUpdateEvent(response, partnerName);
            }
        } catch (Exception e) {
            log.error("Failed to process batch through ChatGPT: {}", e.getMessage(), e);
        }
    }

    private void publishMasterDataUpdateEvent(List<WorkloadResponseDTO> response, String partnerName) {
        log.info("Publish message to update master data for partner {} and size {}", response.size(), partnerName);
        Map<String, List<WorkloadResponseDTO>> groupedByCustomer = response.stream()
                .filter(data -> data.getWorkloadDescription() != null && data.getAccountId() != null)
                .collect(Collectors.groupingBy(
                        data -> data.getWorkloadDescription() + "_" + data.getAccountId()
                ));
        for (Map.Entry<String, List<WorkloadResponseDTO>> entry : groupedByCustomer.entrySet()) {
            WorkloadResponseDTO item = entry.getValue().stream().findFirst().orElse(null);
            if (Objects.nonNull(item)) {
                WorkloadEvent event = WorkloadEvent.builder()
                        .customerName(item.getCustomerName())
                        .accountId(item.getAccountId())
                        .partnerName(partnerName)
                        .workloadDescription(item.getWorkloadDescription())
                        .build();
                log.info("Event to publish {}", event);
                workloadEventProducer.publishWorkloadEvent(event);
            }
        }

    }

    private String formatRawDataAsTable(List<ApnOpportunityRawData> records) {
        StringBuilder sb = new StringBuilder();

        sb.append("| LINEITEM_UUID | CUSTOMER_NAME | LINEITEM_USAGEACCOUNTID | SERVICE_NAME ")
                .append("| MYCLOUD_REGIONNAME | MYCLOUD_OPERATINGSYSTEM | LINEITEM_RESOURCEID ")
                .append("| MYCLOUD_INSTANCETYPE | PRODUCTCODE | FINAL_NAME_TAG ")
                .append("| TOTAL_PERIOD_COST | RESOURCE_BIRTH_DATE | ACTIVE_DAYS_COUNT |\n");

        sb.append("|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");

        for (ApnOpportunityRawData r : records) {
            sb.append("| ").append(safe(r.getLineitemUuid()))
                    .append(" | ").append(safe(r.getCustomerName()))
                    .append(" | ").append(safe(r.getLineitemUsageaccountid()))
                    .append(" | ").append(safe(r.getServiceName()))
                    .append(" | ").append(safe(r.getMycloudRegionname()))
                    .append(" | ").append(safe(r.getMycloudOperatingsystem()))
                    .append(" | ").append(safe(r.getLineitemResourceid()))
                    .append(" | ").append(safe(r.getMycloudInstancetype()))
                    .append(" | ").append(safe(r.getProductcode()))
                    .append(" | ").append(safe(r.getFinalNameTag()))
                    .append(" | ").append(r.getTotalPeriodCost() != null ? r.getTotalPeriodCost() : "")
                    .append(" | ").append(r.getResourceBirthDate() != null ? r.getResourceBirthDate() : "")
                    .append(" | ").append(r.getActiveDaysCount() != null ? r.getActiveDaysCount() : "")
                    .append(" |\n");
        }

        return sb.toString();
    }

    private String safe(String value) {
        return value != null ? value.replace("|", "/") : "";
    }
}

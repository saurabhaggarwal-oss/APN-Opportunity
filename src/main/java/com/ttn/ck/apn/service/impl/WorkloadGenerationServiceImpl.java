package com.ttn.ck.apn.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import com.ttn.ck.apn.service.WorkloadGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link WorkloadGenerationService} using Spring AI
 * and OpenAI's ChatGPT.
 *
 * <p>
 * Takes raw opportunity data (AWS resource/cost records), formats them
 * as a table, sends them to ChatGPT along with the
 * {@code workload-generation-prompt.st} template, and parses the returned
 * workload titles and descriptions back into master data records.
 * </p>
 *
 * <p>
 * The ChatGPT response enriches raw rows with:
 * <ul>
 * <li>Workload Title — a net-new project-oriented title</li>
 * <li>Workload Description — a 50-word technical description</li>
 * </ul>
 *
 * <p>
 * These enriched fields are then mapped onto
 * {@link ApnOpportunityMasterData} for upsert into the master table.
 * </p>
 */
@Service
public class WorkloadGenerationServiceImpl implements WorkloadGenerationService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadGenerationServiceImpl.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    public WorkloadGenerationServiceImpl(
            ChatModel chatModel,
            ObjectMapper objectMapper,
            @Value("classpath:prompts/workload-generation-prompt.st") Resource promptResource)
            throws IOException {

        this.chatClient = ChatClient.builder(chatModel).build();
        this.objectMapper = objectMapper;
        this.promptTemplate = promptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Processes a batch of raw data records through ChatGPT.
     *
     * <p>
     * Workflow:
     * <ol>
     * <li>Format raw data as a markdown table</li>
     * <li>Append the table to the prompt template</li>
     * <li>Send to ChatGPT via Spring AI ChatClient</li>
     * <li>Parse the response to extract workload titles/descriptions</li>
     * <li>Map results back onto master data POJOs</li>
     * </ol>
     *
     * @param rawDataList list of raw records to process
     * @return list of master data records with generated workload info
     */
    @Override
    public List<ApnOpportunityMasterData> processRawData(List<ApnOpportunityRawData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            log.warn("No raw data to process");
            return Collections.emptyList();
        }

        log.info("Processing {} raw data records through ChatGPT", rawDataList.size());

        try {
            // Step 1: Format raw data as a markdown table for the prompt
            String inputTable = formatRawDataAsTable(rawDataList);

            // Step 2: Build the full prompt by appending the data table
            String fullPrompt = promptTemplate + "\n\n" + inputTable;

            // Step 3: Call ChatGPT via Spring AI
            log.debug("Sending prompt to ChatGPT ({} chars)", fullPrompt.length());
            String response = chatClient.prompt()
                    .user(fullPrompt)
                    .call()
                    .content();

            log.debug("Received ChatGPT response ({} chars)", response.length());

            // Step 4: Parse the response and map to master data
            return mapResponseToMasterData(response, rawDataList);

        } catch (Exception e) {
            log.error("Failed to process raw data through ChatGPT: {}", e.getMessage(), e);
            throw new RuntimeException("Workload generation failed", e);
        }
    }

    /**
     * Formats raw data records as a pipe-delimited markdown table
     * suitable for the ChatGPT prompt.
     */
    private String formatRawDataAsTable(List<ApnOpportunityRawData> records) {
        StringBuilder sb = new StringBuilder();

        // Header row
        sb.append("| LINEITEM_UUID | CUSTOMER_NAME | LINEITEM_USAGEACCOUNTID | SERVICE_NAME ")
                .append("| MYCLOUD_REGIONNAME | MYCLOUD_OPERATINGSYSTEM | LINEITEM_RESOURCEID ")
                .append("| MYCLOUD_INSTANCETYPE | PRODUCTCODE | FINAL_NAME_TAG ")
                .append("| TOTAL_PERIOD_COST | RESOURCE_BIRTH_DATE | ACTIVE_DAYS_COUNT |\n");

        // Separator
        sb.append("|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");

        // Data rows
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

    /**
     * Parses the ChatGPT response and builds master data records.
     *
     * <p>
     * The response is expected to contain a markdown table with
     * "Workload Title" and "Workload Description" columns appended
     * to the original data. We match rows back to raw records by
     * their LINEITEM_UUID.
     * </p>
     */
    private List<ApnOpportunityMasterData> mapResponseToMasterData(
            String response, List<ApnOpportunityRawData> rawDataList) {

        // Group raw data by UUID for easy lookup
        Map<String, List<ApnOpportunityRawData>> rawByUuid = rawDataList.stream()
                .collect(Collectors.groupingBy(ApnOpportunityRawData::getLineitemUuid));

        List<ApnOpportunityMasterData> masterDataList = new ArrayList<>();

        // Parse the response table rows
        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Skip non-data lines (headers, separators, empty)
            if (line.isEmpty() || line.startsWith("|---") || line.startsWith("| LINEITEM_UUID")
                    || line.startsWith("| **") || !line.startsWith("|")) {
                continue;
            }

            String[] cells = line.split("\\|");
            // The table should have at minimum the original columns + Workload Title +
            // Description
            if (cells.length < 4) {
                continue;
            }

            // Extract workload fields from the last two columns
            String workloadTitle = cells.length >= cells.length - 0
                    ? cells[cells.length - 1].trim()
                    : "";
            String workloadDescription = cells.length >= cells.length - 1
                    ? cells[cells.length - 2].trim()
                    : "";

            // Extract UUID from the first data column
            String uuid = cells.length > 1 ? cells[1].trim() : "";

            if (uuid.isEmpty()) {
                continue;
            }

            // Avoid duplicating master records for the same UUID
            boolean alreadyAdded = masterDataList.stream()
                    .anyMatch(m -> uuid.equals(m.getLineitemUuid()));
            if (alreadyAdded) {
                continue;
            }

            // Build master data from raw data context + ChatGPT output
            List<ApnOpportunityRawData> rawRecords = rawByUuid.get(uuid);
            if (rawRecords != null && !rawRecords.isEmpty()) {
                ApnOpportunityRawData firstRaw = rawRecords.get(0);

                ApnOpportunityMasterData masterData = ApnOpportunityMasterData.builder()
                        .lineitemUuid(uuid)
                        .customerName(firstRaw.getCustomerName())
                        .lineitemUsageaccountid(firstRaw.getLineitemUsageaccountid())
                        .partnerProjectTitle(workloadTitle)
                        .customerBusinessProblem(workloadDescription)
                        .cloudPlatform(firstRaw.getCloudPlatform())
                        .loggedDate(firstRaw.getLoggedDate())
                        .build();

                masterDataList.add(masterData);
            }
        }

        log.info("Mapped {} master data records from ChatGPT response", masterDataList.size());
        return masterDataList;
    }

    /**
     * Null-safe string helper for table formatting.
     */
    private String safe(String value) {
        return value != null ? value.replace("|", "/") : "";
    }
}

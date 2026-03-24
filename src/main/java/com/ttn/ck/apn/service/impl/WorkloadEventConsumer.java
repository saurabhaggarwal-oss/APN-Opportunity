package com.ttn.ck.apn.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.ck.apn.dao.ApnOpportunityDataDao;
import com.ttn.ck.apn.model.WorkloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadEventConsumer {

    private final ApnOpportunityDataDao apnOpportunityDataDao;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue.master-data-refresh}", concurrency = "2")
    @Transactional
    public void handleWorkloadEvent(String message) {
        log.info("Message Received in master data refresh queue {}", message);
        WorkloadEvent event = objectMapper.convertValue(message, new TypeReference<>() {});
        log.info("Received WorkloadEvent for customer: {} partner: {}", event.getCustomerName(), event.getPartnerName());

        try {
            apnOpportunityDataDao.insertOpportunityMasterData(
                    event.getCustomerName(),
                    event.getAccountId(),
                    event.getWorkloadDescription()
            );
            log.debug("Master data insertion completed for customer: {}", event.getCustomerName());

            apnOpportunityDataDao.insertOpportunityMappingData(
                    event.getCustomerName(), 
                    event.getAccountId(),
                    event.getWorkloadDescription()
            );
            log.debug("Mapping data insertion completed for customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("Error processing workload event for customer: {}", event.getCustomerName(), e);
            throw e; // Required for transactional rollback and RabbitMQ retry
        }
    }
}

package com.ttn.ck.apn.service.impl;

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
    
    // We fetch workload description from somewhere or utilize an existing standard one if the event only carries customName & partnerName.
    // Based on the user requirements, 'WorkloadEvent' only has customerName and partnerName.
    // Since we are creating a master entry, we will need the workloadDescription which presumably is fetched from RawData or stored elsewhere.
    // For now we will assume the workload description passed here can safely be fetched or use a placeholder per the requirement to 'insert into master table'.
    private static final String DEFAULT_WORKLOAD_DESCRIPTION = "System Generated Workload";

    @RabbitListener(queues = "${app.rabbitmq.queue.opportunity-refresh}")
    @Transactional
    public void handleWorkloadEvent(WorkloadEvent event) {
        log.info("Received WorkloadEvent for customer: {} partner: {}", event.getCustomerName(), event.getPartnerName());

        try {
            apnOpportunityDataDao.insertOpportunityMasterData(
                    event.getCustomerName(), 
                    event.getPartnerName(), 
                    DEFAULT_WORKLOAD_DESCRIPTION
            );
            log.debug("Master data insertion completed for customer: {}", event.getCustomerName());

            apnOpportunityDataDao.insertOpportunityMappingData(
                    event.getCustomerName(), 
                    event.getPartnerName(), 
                    DEFAULT_WORKLOAD_DESCRIPTION
            );
            log.debug("Mapping data insertion completed for customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("Error processing workload event for customer: {}", event.getCustomerName(), e);
            throw e; // Required for transactional rollback and RabbitMQ retry
        }
    }
}

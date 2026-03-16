package com.ttn.ck.apn.queries;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static lombok.AccessLevel.PRIVATE;

@Data
@Configuration
@PropertySource("classpath:queries/apn-queries.properties")
@FieldDefaults(level = PRIVATE)
public class ApnOpportunityQueries {

    @Value("${opportunity.master.data.by.date}")
    String opportunityMasterData;

    @Value("${opportunity.master.data.by.uuid}")
    String opportunityMasterDataByUuid;

    @Value("${opportunity.raw.data.by.master.uuid}")
    String opportunityRawDataByMasterUuid;

    @Value("${update.opportunity.raise.status}")
    String updateOpportunityRaiseStatus;

}
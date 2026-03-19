package com.ttn.ck.apn.utils;

import java.util.HashMap;
import java.util.Map;

public class ApnUtils {

    private ApnUtils () {}


    public static Map<String, Object> defaultApnParams(String startDate, String endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put(START_DATE, startDate);
        map.put(END_DATE, endDate);
        return map;
    }

    public static final int BATCH_SIZE = 10;
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String WORKLOAD_DESCRIPTION = "workloadDescription";
    public static final String OPPORTUNITY_RAISED = "opportunityRaised";
    public static final String OPPORTUNITY_MASTER_UUIDS = "opportunityMasterUuids";
    public static final String USER = "user";
    public static final String MASTER_UUID = "masterUuid";
    public static final String LINEITEM_UUID = "lineitemUuid";
    public static final String WORKLOAD_TITLE = "workloadTitle";
    public static final String CUSTOMER_NAME = "customerName";
    public static final String PARTNER_NAME = "partnerName";
    public static final String ACCOUNT_ID = "accountId";
    public static final String CUSTOMER_COMPANY_NAME = "customerCompanyName";
    public static final String INDUSTRY = "industry";
    public static final String INDUSTRY_OTHER = "industryOther";
    public static final String COUNTRY = "country";
    public static final String STATE = "state";
    public static final String POSTAL_CODE = "postalCode";
    public static final String WEBSITE = "website";
    public static final String UUID = "uuid";

}

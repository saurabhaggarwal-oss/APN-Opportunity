package com.ttn.ck.apn.utils;

import java.util.HashMap;
import java.util.Map;

public class ApnUtils {
    private ApnUtils () {}


    public static Map<String, Object> defaultApnParams(String startDate, String endDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return map;
    }


}

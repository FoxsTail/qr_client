package com.lis.qr_client.extra.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectUtility {

    //--------Map-----------

    /**
     * Input - string with json after qr code scanning,
     * output - parsed to HashMap<String, Object> json
     */

    public static HashMap<String, Object> scannedJsonToMap(String scan_result) {
        ObjectMapper mapper = new ObjectMapper();

        HashMap<String, Object> jsonMap = new HashMap<>();
        try {
            jsonMap = mapper.readValue(scan_result, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonMap;
    }



    /**
     * Transform string attributes to HashMap
     */
    public static HashMap<String, Object> attributesStringToHashMap(String string_json) {

        HashMap<String, Object> attribute_map = null;
        try {
            attribute_map = new ObjectMapper().readValue(string_json, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return attribute_map;
    }




    //-----------------------------------


    /**
     * Find map in the mapList by the given inventory_num
     */

    public static Map<String, Object> findMapByInventoryNum(List<Map<String, Object>> mapListToSearch, String inventory_num) {
        Map<String, Object> searched_map = null;

        for (Map<String, Object> map : mapListToSearch) {
            if ((map.get("inventory_num")).equals(inventory_num)) {
                searched_map = map;
                break;
            }
        }

        return searched_map;
    }


}

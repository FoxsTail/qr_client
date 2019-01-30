package com.lis.qr_client.extra.utility;

import android.os.Environment;
import android.util.Pair;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Log
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

    /**
     * Parsing scannedMap, hidden value saves in the global var,
     * other data build into a plain string
     */
    public static String scannedMapToMsg(HashMap<String, Object> scannedMap) {
        StringBuilder message = new StringBuilder();

        if (scannedMap != null) {
            for (Map.Entry<String, Object> map : scannedMap.entrySet()) {

                message.append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
            }
            return message.toString();

        }
        return null;
    }


    //---------Convert and out-----------

    public static void convertAndSaveListsToCsvFile(String additionalPartForFileName, String[] list_titles, List<Map<String, Object>> list1,
                                                    List<Map<String, Object>> list2,
                                                    List<Map<String, Object>> list3) {

        String csv_string = convertToStringForCsv(list_titles, list1, list2, list3);

        if (csv_string == null) {
            log.info("Nothing to save to the file. Empty data set");

        } else {
            log.info("Data is ok. Saving to the file...");
            log.info("Data string: " + csv_string);

        /*prepare place in the storage*/
            File root = Environment.getExternalStorageDirectory();
            //File dir = new File(root.getAbsolutePath() + "/qr_inventory_result");
            File dir = new File(root.getAbsolutePath() + "/qr_inventory_result");
            dir.mkdirs();

            log.info("dir created");
        /*create csv file name*/
            String filename = getDataTimeForFilename(additionalPartForFileName);

        /*create out file*/
            File destination_file;
            if (filename != null) {
                destination_file = new File(dir, filename);
            } else {
                destination_file = new File(dir, "empty_name.csv");
            }


        /*write to file*/
            try {
                FileWriter fileWriter = new FileWriter(destination_file);

                fileWriter.write(csv_string);

                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.info("Error writing to file-- convertListsToCsv -- cause:" + e.getMessage() + " \n");
            }
        }
    }

    public static String convertToStringForCsv(String[] list_titles, List<Map<String, Object>> list1,
                                               List<Map<String, Object>> list2,
                                               List<Map<String, Object>> list3) {
        StringBuilder sb = new StringBuilder();


            /*columns title for csv*/
        if (list_titles != null) {
            log.info("Convert to csv...Set titles");
            for (int i = 0; i < list_titles.length; i++) {
                sb.append(list_titles[i])
                        .append(";");
            }
            sb.append("\n");
        }


            /*columns data*/
            /*a bit hardcode, best decision for now*/

            /*find list which is not null*/
        List<List<String>> lists = new ArrayList<>();
        List<Integer> lists_size = new ArrayList<>();


        if (list1 != null && list1.size() > 0) {
            lists.add(mapListToStringList(list1));
            lists_size.add(list1.size());
            log.info("List1 size: " + list1.size());
        }
        if (list2 != null && list2.size() > 0) {
            lists.add(mapListToStringList(list2));
            lists_size.add(list2.size());
            log.info("List2 size: " + list2.size());
        }
        if (list3 != null && list3.size()>0) {
            lists.add(mapListToStringList(list3));
            lists_size.add(list3.size());
            log.info("List3 size: " + list3.size());
        }

        if (lists_size != null && lists_size.size() > 0) {
            log.info("Convert to csv...Set data");

            Collections.reverse(lists_size);

            /*define the longest list*/

            for (int i = 0; i < lists_size.get(0); i++) {
                for (List<String> list : lists) {
                    if (i < list.size()) {
                        sb.append(list.get(i));
                    }
                    sb.append(";");
                }
                sb.append("\n");
            }

            return sb.toString();
        } else {
            log.info("Convert to csv has failed...empty data");
            return null;
        }
    }

    public static String getDataTimeForFilename(String additional) {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + "_" + additional + ".csv";
    }

    public static List<String> mapListToStringList(List<Map<String, Object>> mapList) {
        List<String> stringList = new ArrayList<>();

        for (Map<String, Object> map : mapList) {
            stringList.add(map.values().toString());
        }

        return stringList;
    }

}

package com.lis.qr_client.extra.utility;

import android.os.Environment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import lombok.extern.java.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Log
public class ObjectUtility {

    //--------Map-----------

    /**
     * Equipment attributes to the textView string
     * */
public static String jsonAttributesToString(String json_string){
    try {
        HashMap<String, String> attributes = new ObjectMapper().readValue(json_string, HashMap.class);
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue()+"\n");
        }
        String desired_string = sb.toString();
        /*remove extra new line, return*/
        return desired_string.trim();

    } catch (IOException e) {
        e.printStackTrace();
    }
    return "";
}

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

    public static void convertAndSaveListsToCsvFile(String additionalPartForFileName, String[] list_titles,
                                                    List<Map<String, Object>> listScanned,
                                                    List<Map<String, Object>> listOtherRoom,
                                                    List<Map<String, Object>> listNotFound) {

        String csv_string = convertToStringForCsv(list_titles, listScanned, listOtherRoom, listNotFound);

        if (csv_string == null) {
            log.info("Nothing to save to the file. Empty data set");

        } else {
            log.info("Data is pic_ok. Saving to the file...");
            log.info("Data string: " + csv_string);

/*
        prepare place in the phone storage
*/
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/Qr_inventory_result");
            dir.mkdirs();
            log.info("dir created");

        /*create csv file name*/
            String filename = getDataTimeForFilename(additionalPartForFileName);

        /*create out file*/
            File destination_file;
            if (filename != null) {
                destination_file = new File(dir, filename);
            } else {
                destination_file = new File(dir, "empty_name.txt");
            }


        /*write to file*/
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter
                        (new FileOutputStream(destination_file), "cp1251"));

                bufferedWriter.write(csv_string);

                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.info("Error writing to file-- convertListsToCsv -- cause:" + e.getMessage() + " \n");
            }
        }
    }

    /**
     * Convert transferred titles and lists to the one string for csv file
     */

    public static String convertToStringForCsv(String[] list_titles, List<Map<String, Object>> listScanned,
                                               List<Map<String, Object>> listOtherRoom,
                                               List<Map<String, Object>> listNotFound) {
        final String SCANNED_STATE = QrApplication.getInstance().getString(R.string.scanned_title);
        final String OTHER_ROOM_STATE = QrApplication.getInstance().getString(R.string.other_room_title);
        final String NOT_FOUND_STATE = QrApplication.getInstance().getString(R.string.not_found_title);

        StringBuilder sb = new StringBuilder();


            /*columns title for csv*/
        if (list_titles != null) {
            log.info("Convert to csv...Set titles");
            for (int i = 0; i < list_titles.length; i++) {
                sb.append(list_titles[i])
                        .append(";");
            }
            sb.append("\r\n");
        }


            /*columns data*/
        String temp_string;

        /*convert mapList to fileString*/
        temp_string = fileStringFromList(listScanned, SCANNED_STATE);
        if (temp_string != null) {
            sb.append(temp_string);
        }

        temp_string = fileStringFromList(listOtherRoom, OTHER_ROOM_STATE);
        if (temp_string != null) {
            sb.append(temp_string);
        }

        temp_string = fileStringFromList(listNotFound, NOT_FOUND_STATE);
        if (temp_string != null) {
            sb.append(temp_string);
        }


        if (sb != null) {
            log.info("Convert to file string is successful...return data");
            return sb.toString();
        } else {
            log.info("Convert to csv has failed...empty data");
            return null;
        }

    }

    /**
     * Check if null,
     * convert internal map to string format "xxx;xxx;",
     * put to the result string in format "xxx;xxx;value;\r\n"
     */
    public static String fileStringFromList(List<Map<String, Object>> list, String state_value) {
        List<String> temp_list;
        StringBuilder sb = new StringBuilder();

        if (list != null && list.size() > 0) {

            /*tale only map values from mapList*/
            temp_list = mapListInventoryToStringList(list);

            if (temp_list != null) {

                /*to format "xxx;xxx;value;\r\n"*/
                for (String list_string : temp_list) {
                    sb.append(list_string)
                            .append(state_value)
                            .append(";")
                            .append("\r\n");
                }


                return sb.toString();

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getDataTimeForFilename(String additional) {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + "_" + additional + ".csv";
    }

    /**
     * returns string from mapList in format "xxx;xxx;"
     */
    public static List<String> mapListInventoryToStringList(List<Map<String, Object>> mapList) {
        List<String> stringList = new ArrayList<>();

        String map_name;
        String map_inventory_num;
        StringBuilder sb = new StringBuilder();

        if (mapList != null) {
            for (Map<String, Object> map : mapList) {
                /*extract name and inv_number*/
/* //any data
                templist = map.values().toString();
                sb.append(templist)
                        .append(";");
                stringList.add(sb.toString());

*/
                map_name = (String) map.get(DbTables.TABLE_INVENTORY_COLUMN_NAME);
                map_inventory_num = (String) map.get(DbTables.TABLE_INVENTORY__COLUMN_INV_NUMBER);

                /*form string "xxx;xxx;" */
                 sb.append(map_name)
                        .append(";")
                        .append(map_inventory_num)
                        .append(";");

                stringList.add(sb.toString());
            /*remove old data*/
            sb = new StringBuilder();
            }
            return stringList;
        } else {
            return null;
        }

    }
}

package com.lis.qr_client.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.pojo.EquipmentExpanded;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Utility class for different small, repetitive tasks
 */

@Log
public class Utility {


    /**
     * Input - string with json after qr code scanning,
     * output - parsed to HashMap<String, Object> json
     */

    public HashMap<String, Object> scannedJsonToMap(String scan_result) {
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
     * parse cursor to List
     */
    public List cursorToList(Cursor cursor) {
        List convertedList = new ArrayList();

        /*first empty value for the spinner*/
        convertedList.add(" ");

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    convertedList.add(cursor.getInt(cursor.getColumnIndex("room")));

                } while (cursor.moveToNext());
            }
        }

        return convertedList;
    }

    /**
     * parse cursor to List<Map<String, Object>>
     */

    public List<Map<String, Object>> cursorToMapList(Cursor cursor) {
        int id_index_column = cursor.getColumnIndex("id");

        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> temp_map;

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    temp_map = new HashMap<>();
                    /*take id and address separately*/
                    for (byte i = 0; i < cursor.getColumnCount(); i++) {
                        if (i == id_index_column) {
                            temp_map.put(cursor.getColumnName(i), cursor.getInt(i));
                        } else {
                            log.info(cursor.getColumnName(i) + " " + cursor.getString(i));
                            temp_map.put(cursor.getColumnName(i), cursor.getString(i));
                        }
                    }
                    mapList.add(temp_map);

                } while (cursor.moveToNext());
            }
            return mapList;
        } else {
            log.warning("Cursor is null");
            return mapList;
        }
    }

    /**
     * creates Equipment and EquipmentExpanded from map, returns ParentObject
     */
    public ArrayList<Equipment> mapListToEquipmentList(List<Map<String, Object>> equipmentList) {

        ArrayList<Equipment> parentObjects = new ArrayList<>();

        Equipment equipment;
        EquipmentExpanded expanded;

        for (Map<String, Object> equipmentMap : equipmentList) {

            /*convert map to object*/
            equipment = mapToEquipment(equipmentMap);
            expanded = mapToEquipmentExpanded(equipmentMap);

            /*add to child list*/
            equipment.getChildObjectList().add(expanded);

            /*add to parent list*/
            parentObjects.add(equipment);
        }

        return parentObjects;
    }

    /**
     * hand parse map to EquipmentExpanded
     */
    public EquipmentExpanded mapToEquipmentExpanded(Map<String, Object> equipmentMap) {
        String attributes;
        String serial_num;
        Integer id_asDetailIn;
        Integer id_tp;
        Integer id_user;
        Integer room;


        /*attributes*/
        String equipment_string = (String) equipmentMap.get("attributes");
        if (equipment_string != null) {

            /*transform string attributes to HashMap*/
            HashMap<String, Object> attribute_map = attributesStringToHashMap(equipment_string);

            /*parse HashMap to the beautiful string*/
            if (attribute_map != null) {
                attributes = hashMapToString(attribute_map);
            } else {
                attributes = "";
            }
        } else {
            attributes = "";
        }


        /*serial_num*/

        if (equipmentMap.get("serial_num") != null) {
            serial_num = (String) equipmentMap.get("serial_num");
        } else {
            serial_num = "";
        }


        /*id_asDetailIn*/

        if (equipmentMap.get("id_asDetailIn") != null) {
            id_asDetailIn = Integer.parseInt(equipmentMap.get("id_asDetailIn").toString());
        } else {
            id_asDetailIn = null;
        }

        /*id_tp*/

        if (equipmentMap.get("id_tp") != null) {
            id_tp = Integer.parseInt(equipmentMap.get("id_tp").toString());
        } else {
            id_tp = null;
        }

        /*id_user*/

        if (equipmentMap.get("id_user") != null) {
            id_user = Integer.parseInt(equipmentMap.get("id_user").toString());
        } else {
            id_user = null;
        }

        /*room*/

        if (equipmentMap.get("room") != null) {
            room = Integer.parseInt(equipmentMap.get("room").toString());
        } else {
            room = null;
        }

        return new EquipmentExpanded(attributes, serial_num, id_user, id_asDetailIn, id_tp, room);
    }


    /**
     * Parse HashMap to the beautiful string in format "Key: Value \n"
     */
    private String hashMapToString(HashMap<String, Object> attribute_map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, Object> map : attribute_map.entrySet()) {
            stringBuilder
                    .append(" ")
                    .append(map.getKey())
                    .append(": ")
                    .append(map.getValue())
                    .append(";");
        }
        return stringBuilder.toString();
    }


    /**
     * Transform string attributes to HashMap
     */
    public HashMap<String, Object> attributesStringToHashMap(String string_json) {

        HashMap<String, Object> attribute_map = null;
        try {
            attribute_map = new ObjectMapper().readValue(string_json, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return attribute_map;
    }


    /**
     * hand parse map to Equipment
     */
    public Equipment mapToEquipment(Map<String, Object> equipmentMap) {
        int id;
        String type;
        String vendor;
        String model;
        String series;
        String inventory_num;


        /*id*/

        if (equipmentMap.get("id") != null) {
            id = Integer.parseInt(equipmentMap.get("id").toString());
        } else {
            id = 0;
        }

         /*type*/

        if (equipmentMap.get("type") != null) {
            type = (String) equipmentMap.get("type");
        } else {
            type = "";
        }

        /*name*/

        if (equipmentMap.get("vendor") == null || equipmentMap.get("vendor").equals("null")) {
            vendor = "";
        } else {
            vendor = equipmentMap.get("vendor").toString();
        }

        if (equipmentMap.get("model") == null || equipmentMap.get("model").equals("null")) {
            model = "";
        } else {
            model = equipmentMap.get("model").toString();
        }

        if (equipmentMap.get("series") == null || equipmentMap.get("series").equals("null")) {
            series = "";
        } else {
            series = equipmentMap.get("series").toString();
        }


        /*inventory_num*/

        if (equipmentMap.get("inventory_num") != null) {
            inventory_num = (String) equipmentMap.get("inventory_num");
        } else {
            inventory_num = "";
        }


        return new Equipment(id, type, vendor, model, series, inventory_num);
    }


    /**
     * parse cursor to BidiMap<Integer, String>
     * <p>
     * !!! Works only with tables where id column exists
     * <p>
     * Value in bidimap will be combined values from all the table for one record
     * <p>
     * id=1 value= "Name Surname Date etc"
     */


    public BidiMap<Integer, String> cursorToBidiMap(Cursor cursor) {
        log.info("---Cursor to bidiMap---");
        BidiMap<Integer, String> convertedMap = new DualHashBidiMap<>();

        StringBuilder sb = new StringBuilder();
        int temp_id = 0;

        int id_index_column = cursor.getColumnIndex("id");


        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {

                    /*take id and address separately*/
                    for (byte i = 0; i < cursor.getColumnCount(); i++) {

                        if (i == id_index_column) {
                            temp_id = cursor.getInt(i);
                        } else {
                            sb.append(cursor.getString(i) + " ");
                        }
                    }
                    if (temp_id != 0) {
                        convertedMap.put(temp_id, sb.toString());
                    }
                    sb.setLength(0);
                } while (cursor.moveToNext());
            }
        } else {
            log.warning("Cursor is null");
        }

        return convertedMap;
    }


    /**
     * Cursor logging
     */
    public void logCursor(Cursor cursor, String title) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                log.info(title + ". " + cursor.getCount() + " rows");
                do {
                    for (String column_names : cursor.getColumnNames()) {
                        log.info(cursor.getString(cursor.getColumnIndex(column_names)) + ";");
                    }
                    log.info("--------");
                } while (cursor.moveToNext());
            }
        } else {
            log.warning("Cursor is null");
        }
    }


    /**
     * Puts list into the table (internal db)
     */
    public void putListToTableColumn(List<Object> list, String table_name, String column_name, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        log.info("----Putting to sql----");

        /*transaction for safe operation*/
        db.beginTransaction();
        try {
            for (Object object : list) {

                /*put data from list  to the context value*/
                cv.put(column_name, object.toString());
                log.info(column_name + " " + object.toString());

                /*insert to the internal db*/
                long insert_res = db.insert(table_name, null, cv);
//log track
                log.info("----loaded----: " + insert_res);

            }

            db.setTransactionSuccessful();
            log.info("----all is ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }

    }


    /**
     * Puts list of maps into the table (internal db)
     */

    public void putMapListIntoTheTable(List<Map<String, Object>> mapList, String table_name, SQLiteDatabase db) {
        ContentValues cv;

        log.info("----Putting to sql----");

        /*transaction for safe operation*/
        db.beginTransaction();
        try {
            for (Map<String, Object> map : mapList) {

                /*converts map to the context value*/
                cv = mapToContextValueParser(map);

                /*insert to the internal db*/
                long insert_res = db.insert(table_name, null, cv);
//log track
                log.info("----loaded----: " + insert_res);

            }

            db.setTransactionSuccessful();
            log.info("----all is ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }

    }

    /**
     * Converts map to a ContextValue obj
     */

    private ContentValues mapToContextValueParser(Map<String, Object> mapToParse) {
        ContentValues cv = new ContentValues();
        log.info("-----Making cv---");

        for (Map.Entry<String, Object> map : mapToParse.entrySet()) {
            if (map.getValue() != null) {

                log.info("----" + map.getKey() + " " + map.getValue().toString() + "------");


                /*parse attribute hashMap to {"a":"b"} text for sqlite */
                if (map.getKey().equals("attributes")){
                    String string_attributes = null;

                    try {
                        string_attributes = new ObjectMapper().writeValueAsString(map.getValue());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    cv.put(map.getKey(), string_attributes);
                    break;
                }


                    cv.put(map.getKey(), map.getValue().toString());
            } else {
                cv.put(map.getKey(), (String) null);
            }
        }

        return cv;
    }
}

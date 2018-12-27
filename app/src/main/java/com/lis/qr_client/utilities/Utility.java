package com.lis.qr_client.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.widget.FrameLayout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lis.qr_client.activity.MainMenuActivity;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.pojo.EquipmentParent;
import com.lis.qr_client.pojo.EquipmentExpanded;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.utilities.adapter.InventoryAdapter;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.util.*;

import static android.content.Context.MODE_PRIVATE;


/**
 * Utility class for different small, repetitive tasks
 */

@Log
public class Utility {

    //----------Preferences-----------//

    /**
     * remove data from preferences
     */

    public void removeLoginPrefernces(Context context, String key_user_data, String key_is_logged_in){
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key_user_data);
        editor.putBoolean(key_is_logged_in, false);
        editor.apply();
    }


    /**
     * remove data from preferences
     */

    public void removePrefernces(Context context, String preference_key){

        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(preference_key);
        editor.apply();
    }

    /**
     * save user's email and password to Set<String> and then to the preferences
     */

    public void saveUsersDataToPreference(User user, Context context, String key_user_data, String key_is_logged_in){
        Set<String> users_email_passwd = new HashSet<>();
        users_email_passwd.add(user.getEmail());
        users_email_passwd.add(user.getPassword());


        saveStringToPreferences(context, key_user_data, users_email_passwd);
        saveBooleanToPreferences(context, key_is_logged_in);
    }

    /**
     * save boolean to preferences
     */

    public void saveBooleanToPreferences(Context context, String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(preference_key, true);
        editor.apply();

    }

    /**
     * save data Set<String> to preferences
     */

    public void saveStringToPreferences(Context context, String preference_key, Set<String> strings_to_save) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(preference_key, strings_to_save);
        editor.apply();
        log.info("Preferences with key "+preference_key+ " were saved");
    }


    /**
     * get Set<String> data from preferences
     */

    public Boolean getBooleanDataFromPreferences(Context context, String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        if (preference_key != null) {
            return sharedPreferences.getBoolean(preference_key, false);
        } else {
            return null;
        }
    }

    /**
     * get Set<String> data from preferences
     */

    public Set<String> getDataFromPreferences(Context context, String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MainMenuActivity.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        if (preference_key != null) {
            return sharedPreferences.getStringSet(preference_key, null);
        } else {
            return null;
        }
    }


    /**
     * Save muutiple objects (int, string, boolean, object) to preference
     */

   /* public void saveInventoryToPreferences(Context context, String preferenceFileName, List<Pair<String, Object>> params) {

        for(Pair<String, Object> pair: params){
            if(pair.second != null){
                savePreference(context, preferenceFileName, pair.first, pair.second);
            }
        } */


    /**
     * Save object (int, string, boolean, object) to preference
     */
    public void savePreference(Context context, String preferenceFileName, String key, Object value) {
        log.info("---Save object to preferences---");
        log.info("---value---" + value);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (value != null) {
            if (value instanceof Integer) {

                editor.putInt(key, (int) value);

            } else if (value instanceof String) {

                editor.putString(key, value.toString());

            } else if (value instanceof Boolean) {

                editor.putBoolean(key, (boolean) value);

            } else {

                Gson gson = new Gson();
                String jsonString = gson.toJson(value);
                editor.putString(key, jsonString);
            }
        }
        editor.apply();
    }

    public List<Map<String, Object>> preferencesJsonToMapList(Context context, String preferenceFileName, String preferenceKey) {
        String jsonToScan = loadStringOrJsonPreference
                (context, preferenceFileName, preferenceKey);

        Gson gson = new Gson();
        return gson.fromJson(jsonToScan, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
    }

    /**
     * Load Boolean from preference
     */
    public Boolean loadBooleanPreference(Context context, String preferenceFileName, String key) {
        log.info("---Load Boolean from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        boolean value = preferences.getBoolean(key, false);
        log.info("---value---" + value);
        return value;
    }

    /**
     * Load String or Json from preference
     */
    public String loadStringOrJsonPreference(Context context, String preferenceFileName, String key) {
        log.info("---Load String from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);

        String value = preferences.getString(key, "");

        log.info("---value---" + value);
        return value;
    }

    /**
     * Load int from preference
     */
    public int loadIntPreference(Context context, String preferenceFileName, String key) {
        log.info("---Load int from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        int value = preferences.getInt(key, 0);
        log.info("---value---" + value);
        return value;
    }

    /**
     * Clear old preferences (apply - async)
     */
    public void clearOldReferences(Context context, String pref_name) {
        SharedPreferences preferences = context.getSharedPreferences(pref_name, MODE_PRIVATE);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    /**
     * Remove old session data from preferences (apply - async)
     */

    public void removeOldPreferences(Context context, String preferenceFileName, String... preferenceNames) {

        for (String name : preferenceNames) {
            removeOldReference(context, preferenceFileName, name);
        }

    }

    /**
     * Remove old preference (apply - async)
     */
    public void removeOldReference(Context context, String preferenceFileName, String remove_key) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(remove_key);
            editor.apply();
        }
    }

    //---------- -----------//

    /**
     * Takes mapList of inventories from adapter. Search the map with the given inventory.
     * On match deletes map from the list.
     */
    public void deleteInventoryFromList(InventoryAdapter adapter, String inventory_num) {
         /*get list from adapter*/
        List<Map<String, Object>> inventoryList = adapter.getInventories();

                /*delete from list*/
        Map<String, Object> searched_map;
        int position = -1;


        searched_map = findMapByInventoryNum(inventoryList, inventory_num);
        position = inventoryList.indexOf(searched_map);

        if (position > -1) {
            inventoryList.remove(position);
        }
    }

    /**
     * Find map in the mapList by the given inventory_num
     */

    public Map<String, Object> findMapByInventoryNum(List<Map<String, Object>> mapListToSearch, String inventory_num) {
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
     * Toolbar setter dimOnMenu, back button, name
     */
    public void toolbarSetter(ActionBar actionBar, final FrameLayout frameLayout, boolean isChildActivity) {
        if (actionBar != null) {
            if (isChildActivity) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
            if (frameLayout != null) {
                actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
                    @Override
                    public void onMenuVisibilityChanged(boolean b) {
                        if (b) {
                            log.info("visible");
                            frameLayout.getForeground().setAlpha(140);
                        } else {
                            log.info(" ne visible");
                            frameLayout.getForeground().setAlpha(0);

                        }
                    }
                });
            }
        }
    }


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
                    temp_map = cursorToMap(cursor, id_index_column);

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
     * parse cursor to Map<String, Object>
     */

    public Map<String, Object> cursorToMap(Cursor cursor, int id_index_column) {

        Map<String, Object> temp_map = new HashMap<>();

                    /*take id and address separately*/
        for (byte i = 0; i < cursor.getColumnCount(); i++) {
            if (i == id_index_column) {
                temp_map.put(cursor.getColumnName(i), cursor.getInt(i));
            } else {
                log.info(cursor.getColumnName(i) + " " + cursor.getString(i));
                temp_map.put(cursor.getColumnName(i), cursor.getString(i));
            }
        }
        return temp_map;
    }


    /**
     * parse cursor to Equipment
     */

    public static Equipment cursorToEquipment(Cursor cursor) {

        if (cursor != null && (cursor.moveToFirst())) {
            MicroOrm microOrm = new MicroOrm();
            return microOrm.fromCursor(cursor, Equipment.class);
        }
        return null;
    }

    /**
     * parse cursor to User
     */

    public static User cursorToUser(Cursor cursor) {

        if (cursor != null && (cursor.moveToFirst())) {
            MicroOrm microOrm = new MicroOrm();
            return microOrm.fromCursor(cursor, User.class);
        }
        return null;
    }

    /**
     * creates EquipmentParent and EquipmentExpanded from map, returns ParentObject
     */
    public ArrayList<EquipmentParent> mapListToEquipmentParentList(List<Map<String, Object>> equipmentList) {

        ArrayList<EquipmentParent> parentObjects = new ArrayList<>();

        EquipmentParent equipmentParent;
        EquipmentExpanded expanded;

        for (Map<String, Object> equipmentMap : equipmentList) {

            /*convert map to object*/
            equipmentParent = mapToEquipmentParent(equipmentMap);
            expanded = mapToEquipmentExpanded(equipmentMap);

            /*add to child list*/
            equipmentParent.getChildObjectList().add(expanded);

            /*add to parent list*/
            parentObjects.add(equipmentParent);
        }

        return parentObjects;
    }

    /**
     * hand parse map to EquipmentExpanded
     */
    public EquipmentExpanded mapToEquipmentExpanded(Map<String, Object> equipmentMap) {
        String attributes;
        String serial_num;
        String user_info;


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


        /*user_info*/

        if (equipmentMap.get("user_info") != null) {
            user_info = equipmentMap.get("user_info").toString();
        } else {
            user_info = "";
        }


        return new EquipmentExpanded(attributes, serial_num, user_info);
    }


    /**
     * Parse HashMap to the beautiful string in format "Key: Value \n"
     */
    private String hashMapToString(HashMap<String, Object> attribute_map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, Object> map : attribute_map.entrySet()) {
            stringBuilder
                    .append("\n")
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
     * hand parse map to EquipmentParent
     */
    public EquipmentParent mapToEquipmentParent(Map<String, Object> equipmentMap) {
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


        return new EquipmentParent(id, type, vendor, model, series, inventory_num);
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
     * Puts User into the table (internal db)
     */

    public void putUserToTable(User user, String table_name, SQLiteDatabase db) {
        /*Create content values from user*/

        MicroOrm microOrm = new MicroOrm();
        ContentValues cv = microOrm.toContentValues(user);

        log.info("----Putting user to sql----");


        db.beginTransaction();
        try {
         /*insert to the internal db*/
            long insert_res = db.insert(table_name, null, cv);

            //log track
            log.info("----loaded----: " + insert_res);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            log.info("----End transaction----");
        }
    }

    /**
     * Puts Map into the table (internal db)
     */

    public void putMapToTable(Map<String, Object> map, String table_name, SQLiteDatabase db) {
        ContentValues cv;

        log.info("----Putting to sql----");

        /*transaction for safe operation*/
        db.beginTransaction();
        try {
            cv = mapToContextValueParser(map);
            log.info("----cv-------" + cv.toString());

                /*insert to the internal db*/
            long insert_res = db.insert(table_name, null, cv);
//log track
            log.info("----loaded----: " + insert_res);


            db.setTransactionSuccessful();
            log.info("----all is ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
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
                if (map.getKey().equals("attributes")) {
                    String string_attributes = null;

                    try {
                        string_attributes = new ObjectMapper().writeValueAsString(map.getValue());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    cv.put(map.getKey(), string_attributes);
                } else {


                    cv.put(map.getKey(), map.getValue().toString());
                }
            } else {
                cv.put(map.getKey(), (String) null);
            }
        }

        return cv;
    }
}

package com.lis.qr_client.extra.utility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.pojo.*;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.chalup.microorm.MicroOrm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class DbUtility {

    //-------------Cursor---------------

    /**
     * parse cursor to List
     */

    public static List cursorToList(Cursor cursor, String column_name) {
        List convertedList = new ArrayList();

        /*first empty value for the spinner*/
        convertedList.add(" ");

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    convertedList.add(cursor.getInt(cursor.getColumnIndex(column_name)));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return convertedList;
    }

    /**
     * parse cursor to List<Map<String, Object>>
     */

    public static List<Map<String, Object>> cursorToMapList(Cursor cursor) {
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
            cursor.close();
            return mapList;
        } else {
            log.warning("Cursor is null");
            return mapList;
        }
    }

    /**
     * parse cursor to Map<String, Object>
     */

    public static Map<String, Object> cursorToMap(Cursor cursor, int id_index_column) {

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
            Equipment equipment =  microOrm.fromCursor(cursor, Equipment.class);
            cursor.close();
            return equipment;
        }
        return null;
    }


    /**
     * parse cursor to passed classname (example: User.class.getName())
     */

    public static Object cursorToClass(Cursor cursor, String classname) {
        if (cursor != null && (cursor.moveToFirst())) {
            try {
            /*identify the class*/
                Class clazz = Class.forName(classname);

            /*transform cursor to object*/

                MicroOrm microOrm = new MicroOrm();
                return microOrm.fromCursor(cursor, clazz);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        log.info("Cursor is null");
        return null;
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


    public static BidiMap<Integer, String> cursorToBidiMap(Cursor cursor) {
        log.info("---Cursor to bidiMap---");
        BidiMap<Integer, String> convertedMap = new DualHashBidiMap<>();

        StringBuilder sb = new StringBuilder();
        int temp_id = 0;

        int id_index_column = cursor.getColumnIndex("id");

        String cursor_string;

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {

                    /*take id and address separately*/
                    for (byte i = 0; i < cursor.getColumnCount(); i++) {

                        if (i == id_index_column) {
                            temp_id = cursor.getInt(i);
                        } else {
                            cursor_string = cursor.getString(i);
                            if (cursor_string != null) {
                                sb.append(cursor_string + " ");
                            }
                        }
                    }
                    if (temp_id != 0) {
                        convertedMap.put(temp_id, sb.toString());
                    }
                    sb.setLength(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            log.warning("Cursor is null");
        }

        return convertedMap;
    }


    /**
     * Cursor logging
     */
    public static void logCursor(Cursor cursor, String title) {
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


//-----------Context Values-------


    /**
     * Converts map to a ContextValue obj (attributes hardcoded)
     */

    private static ContentValues mapToContextValueParser(Map<String, Object> mapToParse) {
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


    //-----------Transaction-----------

    /**
     * Safety delete from the SQLite(transactions)
     */


    public static void deleteTransaction(String table_name, SQLiteDatabase db) {
        /*delete old info from db table*/
        db.beginTransaction();
        try {
            db.delete(table_name, null, null);
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

    }

    /**
     * Safety insert to the SQLite(transactions)
     */


    public static void insertTransaction(String table_name, ContentValues cv, SQLiteDatabase db) {
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

//-----------Put to db--------------

    /**
     * Puts User into the table (internal db)
     */

    public static void putObjectToTable(Object object, String table_name, SQLiteDatabase db) {
        /*Create content values from user*/

        MicroOrm microOrm = new MicroOrm();
        ContentValues cv = microOrm.toContentValues(object);

        log.info("----Putting " + table_name + " to sql----");

        insertTransaction(table_name, cv, db);
    }


    /**
     * Puts Map into the table (internal db)
     */

    public static void putMapToTable(Map<String, Object> map, String table_name, SQLiteDatabase db) {
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
            log.info("----all is pic_ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }
    }


    /**
     * Puts list of maps into the table (internal db)
     */

    public static void putMapListIntoTheTable(List<Map<String, Object>> mapList, String table_name, SQLiteDatabase db) {
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
            log.info("----all is pic_ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }

    }


    /**
     * Puts list into the table (internal db)
     */
    public static void putListToTableColumn(List<Object> list, String table_name, String column_name, SQLiteDatabase db) {
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
            log.info("----all is pic_ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }

    }


    //-------Object to db-----------

    /**
     * Parse and put user into the internal db
     */

    public static void saveUserToDb(User user, SQLiteDatabase db) {
        String table_user = "user";
        String table_personal_data = "personal_data";
        String table_address = "address";
        String table_workplace = "workplace";
        String table_phone_number = "phone_number";

        putObjectToTable(user, table_user, db);

        PersonalData personalData = user.getPersonalData();

        if (personalData != null) {
            deleteTransaction(table_personal_data, db);
            putObjectToTable(personalData, table_personal_data, db);

            Address address = personalData.getAddress();
            if (address != null) {
                deleteTransaction(table_address, db);
                putObjectToTable(address, table_address, db);

            }

            Workplace workplace = personalData.getWorkplace();
            if (workplace != null) {
                deleteTransaction(table_workplace, db);
                putObjectToTable(workplace, table_workplace, db);
            }

            List<PhoneNumber> phoneNumbers = personalData.getPhoneNumbers();
            if (phoneNumbers != null) {
                deleteTransaction(table_phone_number, db);
                for (PhoneNumber phoneNumber : phoneNumbers) {
                    putObjectToTable(phoneNumber, table_phone_number, db);
                }
            }
        }


    }


}



package com.lis.qr_client.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility class for different small, repetitive tasks
 * */

@Log
public class Utility {

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
     * parse cursor to BidiMap<Integer, String>
     *
     *  !!! Works only with tables where id column exists
     *
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

    /**Cursor logging*/
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
    public void putListToTable(List<Object> list, String table_name, String column_name, SQLiteDatabase db) {
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
            cv.put(map.getKey(), map.getValue().toString());
        }

        return cv;
    }
}
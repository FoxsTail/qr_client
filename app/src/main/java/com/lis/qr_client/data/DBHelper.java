package com.lis.qr_client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lis.qr_client.interfaces.IUserDatabaseHandler;
import com.lis.qr_client.pojo.User;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class DBHelper extends SQLiteOpenHelper implements IUserDatabaseHandler {

    public static final int DB_VERSION=2;

    String[] names = {"Uno", "Due", "Tre"};
    String[] passwords = {"UnoP", "DueP", "TreP"};
    String[] emails = {"UnoP@e", "DueP@e", "TreP@e"};

    int[] id_pData = {1, 3, 5};
    String[] dataName = {"DataUno", "DataUnoDue", "DataUnoTre"};

    String[] phone_numbers = {"345-45-45", "389-99-99", "090-37-90"};


    public DBHelper(Context context) {
        super(context, "qrdb", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        ContentValues cv = new ContentValues();
        log.info("--Create Database----");


        //--------User--------

        sqLiteDatabase.execSQL("create table user (" +
                "username text primary key, " +
                "password text," +
                "email text," +
                "id_pData);");

        for (int i = 0; i < names.length; i++) {
            cv.clear();
            cv.put("username", names[i]);
            cv.put("password", passwords[i]);
            cv.put("email", emails[i]);
            cv.put("id_pData", id_pData[i]);
            sqLiteDatabase.insert("user", null, cv);
        }


        //--------Personal Data--------

        sqLiteDatabase.execSQL("create table personal_data (" +
                "id integer primary key, " +
                "data_name text," +
                "id_phone_number);");

        for (int i = 0; i < id_pData.length; i++) {
            cv.clear();
            cv.put("id", id_pData[i]);
            cv.put("data_name", dataName[i]);
            sqLiteDatabase.insert("personal_data", null, cv);
        }

        for (int j = 1; j <= phone_numbers.length+1; j++) {
            cv.clear();
            cv.put("id_phone_number", j);
            sqLiteDatabase.insert("personal_data", null, cv);
        }


        //--------Phone number--------

        sqLiteDatabase.execSQL("create table phone_number (" +
                "id integer primary key autoincrement," +
                "phone_number text);");

        for (String pn : phone_numbers) {
            cv.clear();
            cv.put("phone_number", pn);
            sqLiteDatabase.insert("phone_number", null, cv);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i == 1 && i1 == 2) {
            sqLiteDatabase.beginTransaction();
            ContentValues cv = new ContentValues();
            try {
                sqLiteDatabase.execSQL("create table phone_number (" +
                        "id integer primary key autoincrement," +
                        "phone_number text);");

                for (String pn : phone_numbers) {
                    cv.clear();
                    cv.put("phone_number", pn);
                    sqLiteDatabase.insert("phone_number", null, cv);
                }

                sqLiteDatabase.execSQL("alter table personal_data add column id_phone_number");

                for (int j = 1; j <= phone_numbers.length+1; j++) {
                    cv.clear();
                    cv.put("id_phone_number", j);
                    sqLiteDatabase.insert("personal_data", null, cv);
                }

                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }


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


    @Override
    public User getByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query("user", null, "username=?", new String[]{username}, null, null, null);) {

            if (cursor.moveToFirst()) {
                return new User(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }

    @Override
    public void deleteAllUsers() {

    }

    @Override
    public void deleteByUsername() {

    }
}

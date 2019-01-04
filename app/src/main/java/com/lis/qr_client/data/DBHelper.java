package com.lis.qr_client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

@Log
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    private Utility utility = new Utility();

    public Utility getUtility() {
        return utility;
    }

    public DBHelper(Context context) {
        super(context, "qr_db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        ContentValues cv = new ContentValues();
        log.info("--Create Database----");


        //--------User--------

        sqLiteDatabase.execSQL("create table user(" +
                "  id integer primary key," +
                "  email text," +
                "  password text," +
                "  id_pd integer);");

        //--------Personal Data--------

        sqLiteDatabase.execSQL("create table personal_data(" +
                "id integer primary key," +
                "name text, " +
                "surname text, " +
                "patronymic text, " +
                "passport text, " +
                "inn text, " +
                "id_tp integer, " +
                "id_wp integer);");


        //--------Address--------

        sqLiteDatabase.execSQL("create table address(" +
                "id integer primary key," +
                "city text, " +
                "street text, " +
                "number text," +
                "floor text," +
                "room integer);");

        //--------Workplace--------

        sqLiteDatabase.execSQL("create table workplace(" +
                "id integer primary key," +
                "position text, " +
                "department text, " +
                "direction text," +
                "remote_workstation integer);");


        //--------Room--------

        sqLiteDatabase.execSQL("create table room (" +
                "room integer primary key);");

        //--------Phone Number--------

        sqLiteDatabase.execSQL("create table phone_number (" +
                "id integer primary key," +
                "phone_number string," +
                "id_pd integer);");


        //--------Equipment--------

        sqLiteDatabase.execSQL("create table equipment(" +
                "id integer primary key," +
                "type text, " +
                "vendor text, " +
                "series text, " +
                "model text, " +
                "serial_num text, " +
                "inventory_num text," +
                "attributes text," +
                "room integer, " +
                "id_asDetailIn integer," +
                "id_tp integer," +
                "id_user integer," +
                "user_info text," +
                "address text);");


        //--------Inventory--------

        sqLiteDatabase.execSQL("create table inventory(" +
                "name text," +
                "inventory_num text primary key," +
                "room int," +
                "address_id);");


    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i == 1 && i1 == 2) {
            sqLiteDatabase.beginTransaction();
            ContentValues cv = new ContentValues();
            try {
             /*   sqLiteDatabase.execSQL("create table phone_number (" +
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
*/
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }


}

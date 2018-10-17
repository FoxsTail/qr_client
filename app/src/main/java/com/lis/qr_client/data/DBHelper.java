package com.lis.qr_client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lis.qr_client.interfaces.IUserDatabaseHandler;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION=1;
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

        //--------Room--------

        sqLiteDatabase.execSQL("create table room (" +
                "room integer primary key);");

        //--------Equipment--------

        sqLiteDatabase.execSQL("create table equipment(" +
                "id integer primary key," +
                "type text, "+
                "vendor text, "+
                "series text, "+
                "model text, "+
                "serial_num text, "+
                "inventory_num text," +
                "attributes text,"+
                "room integer, "+
                "id_asDetailIn integer,"+
                "id_tp integer,"+
                "id_user integer);");

        //--------Address--------

        sqLiteDatabase.execSQL("create table address(" +
                "id integer primary key," +
                "city text, "+
                "street text, "+
                "number text);");

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

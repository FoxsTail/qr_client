package com.lis.qr_client.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class JoinActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPData;
    Button btnGet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);


        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvPData = findViewById(R.id.tvPData);

        btnGet = findViewById(R.id.btnGet);

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        log.info("-----Query answer----");
        Cursor cursor;

        cursor = db.query("user", null, null, null, null, null, null);
        int tableName = cursor.getColumnIndex("username");
        int tableEmail = cursor.getColumnIndex("email");

        if (cursor.moveToFirst()) {
            do {
                log.info("--------");
                log.info("name - " + cursor.getString(tableName));
                log.info("email - " + cursor.getString(tableEmail));
                log.info("--------");
            } while (cursor.moveToNext());
        }

       /* cursor.close();
        cursor = db.rawQuery("select U.username as N, U.email as E, data_name as D " +
                "from user as U inner join personal_data as PD " +
                "on U.id_pData=PD.id where id<?", new String[]{"4"});
        if (cursor.moveToFirst()) {
            do {
                for (String columnNames : cursor.getColumnNames()) {
                    log.info(cursor.getString(cursor.getColumnIndex(columnNames)));
                }
            } while (cursor.moveToNext());
            log.info("--------");
        }*/
    }

    class DBHelper extends SQLiteOpenHelper{

        String[] names = {"Uno", "Due", "Tre", "Quattro", "Cinque"};
        String[] passwords = {"UnoP", "DueP", "TreP", "QuattroP", "CinqueP"};
        String[] emails = {"UnoP@e", "DueP@e", "TreP@e", "QuattroP@e", "CinqueP@e"};

        int[] id_pData = {1, 2, 3, 5, 4};
        String[] dataName = {"DataUno", "DataUnoDue", "DataUnoTre", "DataUnoQuattro", "DataUnoCinque"};


        public DBHelper(Context context) {
            super(context, "qrdb", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            ContentValues cv = new ContentValues();
            log.info("--Create Database----");
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

            sqLiteDatabase.execSQL("create table personal_data (" +
                    "id integer primary key, " +
                    "data_name text);");

            for (int i = 0; i < id_pData.length; i++) {
                cv.clear();

                cv.put("id_pData", id_pData[i]);
                cv.put("data_name", dataName[i]);
                sqLiteDatabase.insert("personal_data", null, cv);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

    }
}

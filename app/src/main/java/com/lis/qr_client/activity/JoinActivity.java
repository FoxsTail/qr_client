package com.lis.qr_client.activity;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
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

        dbHelper.getUtility().logCursor(cursor, "---User---");
        cursor.close();


        cursor = db.rawQuery("select U.username as N, U.email as E, data_name as D " +
                "from user as U inner join personal_data as PD " +
                "on U.id_pData=PD.id where id<?", new String[]{"4"});

        dbHelper.getUtility().logCursor(cursor, "---User with data---");
        cursor.close();

        /*cursor = db.rawQuery("select PD.data_name as Data, PN.phone_number as Phone " +
                "from personal_data as PD inner join phone_number as PN " +
                "on PD.id_phone_number=PN.id", null);*/

        cursor = db.rawQuery("select * from personal_data", null);
        dbHelper.getUtility().logCursor(cursor, "---Data with phone number---");
        cursor.close();
    }


}

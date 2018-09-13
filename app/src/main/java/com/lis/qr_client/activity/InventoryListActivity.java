package com.lis.qr_client.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.List;

@Log
public class InventoryListActivity extends AppCompatActivity {

    private ListView lvEquipments;
    private TextView tvlistLabel;
    private Button btnScanInventory;

    private Cursor cursor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private BidiMap<Integer, String> equipments = new DualHashBidiMap<>();


    Handler handler;

    private Utility utility = new Utility();
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);
        log.info("Here we are, i'm the list");

        lvEquipments = findViewById(R.id.lvEquipments);

        /*get data from sqlite*/
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        handler = new Handler();

        Thread thread = new Thread(runLoadEquipments);
        thread.start();
    }


    Runnable runLoadEquipments = new Runnable() {
        @Override
        public void run() {
            String table_to_select = "equipment";
            cursor = db.query(table_to_select, null, null, null, null, null,
                    null);

            equipments = utility.cursorToBidiMap(cursor);

            handler.post(postEquipmentInList);
        }
    };

    Runnable postEquipmentInList = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
            String[] equipments_strings = equipments.values().toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, equipments_strings);
            lvEquipments.setAdapter(adapter);
        }
    };


    //-------Getters & Setters

    public Utility getUtility() {
        return utility;
    }
}

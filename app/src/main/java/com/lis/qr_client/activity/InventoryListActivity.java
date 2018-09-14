package com.lis.qr_client.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.List;

@Log
public class InventoryListActivity extends MainMenuActivity implements View.OnClickListener {

    public static final int REQUEST_SCAN_QR = 1;
    private static final int DIALOG_SCANNED_CODE = 1;

    private ListView lvEquipments;
    private TextView tvlistLabel;
    private Button btnScanInventory;

    private Cursor cursor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private BidiMap<Integer, String> equipments = new DualHashBidiMap<>();

    //  private HashMap<String, Object> scannedMap;


    // Handler handler;

    private Utility utility = new Utility();
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);
        log.info("Here we are, i'm the list");

        lvEquipments = findViewById(R.id.lvEquipments);

        //--room label setup
        tvlistLabel = findViewById(R.id.tvListLabel);

        Intent intent = getIntent();
        String room_number = intent.getStringExtra("room");

        tvlistLabel.setText("Room " + room_number);


        btnScanInventory = findViewById(R.id.btnScanInventory);
        btnScanInventory.setOnClickListener(this);

        /*get data from sqlite*/
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

      /*  handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                showDialog(DIALOG_SCANNED_CODE);
            }
        };*/


        Thread thread = new Thread(runLoadEquipments);
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScanInventory: {

            /*start Camera activity with result*/
                Intent intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QR);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //-------Dialog--------


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //--------Runnable------


    /**
     * Load equipments from the SQLite
     */
    Runnable runLoadEquipments = new Runnable() {
        @Override
        public void run() {
            String table_to_select = "equipment";
            cursor = db.query(table_to_select, null, null, null, null, null,
                    null);
            /*transform cursor to bidiMap */
            equipments = utility.cursorToBidiMap(cursor);

            /*put equipments in the list*/
            handler.post(postEquipmentInList);
        }
    };

    /**
     * Print equipment into the list
     */
    Runnable postEquipmentInList = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
            String[] equipments_strings = equipments.values().toArray(new String[0]);

            /*create and set adapter to ListView*/
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, equipments_strings);
            lvEquipments.setAdapter(adapter);
        }
    };


    //-------Getters & Setters

    public Utility getUtility() {
        return utility;
    }
}

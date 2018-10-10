package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.adapter.EEA;
import com.lis.qr_client.utilities.dialog_fragment.ExitDialogFragment;
import com.lis.qr_client.utilities.dialog_fragment.ScanDialogFragment;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class InventoryListActivity extends MainMenuActivity implements View.OnClickListener {

    private static final int REQUEST_SCAN_QR = 1;


    private RecyclerView rvEquipments;
    private TextView tvlistLabel;
    private Button btnScanInventory;

    private Cursor cursor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private List<Map<String, Object>> equipments;
    private ArrayList<Equipment> parent_equipments;
    private EEA adapter;
    private String inventoryNum = "inventory_num";

    private Utility utility = new Utility();
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);
        log.info("Here we are, i'm the list");

        //--room label setup
        tvlistLabel = findViewById(R.id.tvListLabel);

        final Intent intent = getIntent();
        String room_number = intent.getStringExtra("room");

        tvlistLabel.setText("Room " + room_number);


        //--btn setup
        btnScanInventory = findViewById(R.id.btnScanInventory);
        btnScanInventory.setOnClickListener(this);

        //--get data from sqlite
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        //--rv setup
        rvEquipments = findViewById(R.id.rvEquipments);

        rvEquipments.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvEquipments.setLayoutManager(layoutManager);


        dialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                /*create dialog*/
                ScanDialogFragment dialogFragment = new ScanDialogFragment();
                Bundle bundle = new Bundle();
                String dialog_tag = "qr_scan";

                inventoryNum = (String) scannedMap.get("inventory_num");
                String scanned_msg = scannedMapToMsg(scannedMap);


                /*get view holder*/
                View view = rvEquipments.findViewWithTag(inventoryNum);
                if (view != null) {
                    RecyclerView.ViewHolder viewHolder = rvEquipments.findContainingViewHolder(view);
                    int position = viewHolder.getAdapterPosition();

                    /*replace found equipment to the end of the list*/
                    List<Equipment> temp_parent_equipments = adapter.getmParentItemList();
                    Equipment equipment_to_move = temp_parent_equipments.get(position);


                    if (equipment_to_move.isSelected()) {

                        scanned_msg = "The equipment has already been scanned!";

                        dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);

                    } else {

                        equipment_to_move.setSelected(true);
                        temp_parent_equipments.remove(position);
                        temp_parent_equipments.add(equipment_to_move);

                        dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);

                        log.info("---Notify data changed---");
                        rvEquipments.getAdapter().notifyDataSetChanged();
                    }

                } else {
                    scanned_msg = "Equipment with inventory number " + inventoryNum + " is not found!";
                    dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);
                }
            }
        };

        Thread thread = new Thread(runLoadEquipments);
        thread.start();
    }

    //--------Runnable------


    /**
     * Load equipments from the SQLite
     */
    private Runnable runLoadEquipments = new Runnable() {
        @Override
        public void run() {
            String table_to_select = "equipment";

            cursor = db.query(table_to_select, null, null, null, null, null,
                    null);

            equipments = utility.cursorToMapList(cursor);

//--logs---
            System.out.println("--------What have we taken from Sqlite--------");
            for (Map<String, Object> map : equipments) {
                log.info(map.keySet().toString() + " " + map.values().toString());
            }
//---------
            /*put equipments in the list*/
            dialogHandler.post(postEquipmentInList);
        }
    };


    /**
     * Print equipment into the list
     */
    private Runnable postEquipmentInList = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(context, "Done loading", Toast.LENGTH_SHORT).show();

            parent_equipments = utility.mapListToEquipmentList(equipments);

            adapter = new EEA(parent_equipments);
            rvEquipments.setAdapter(adapter);
        }
    };

    //-------------------Clicks--------------------//

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
    public void onBackPressed() {
        log.info("InventoryListActivity on backPressed");
        ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
        exitDialogFragment.callDialog(context, exitDialogFragment, new Bundle(), "Quit the room?", "exit");
        //  super.onBackPressed(); вылетает
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}


package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.Inventory;
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.adapter.InventoryAdapter;
import com.lis.qr_client.utilities.adapter.SliderAdapter;
import com.lis.qr_client.utilities.dialog_fragment.ExitDialogFragment;
import com.lis.qr_client.utilities.dialog_fragment.ScanDialogFragment;
import com.lis.qr_client.utilities.fragment.InventoryFragment;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads all view necessary items, runs thread loading data from sqlite db,
 * after puts data in the adapter and set viewPager
 */

@Log
public class InventoryListActivity extends MainMenuActivity implements View.OnClickListener {

    private static final int REQUEST_SCAN_QR = 1;

    private Cursor cursor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;


    private RecyclerView rvEquipments;
    private TextView tvlistLabel;
    private Button btnScanInventory;
    String table_to_select;
    private Toolbar toolbar;
    private ViewPager viewPager;

    private SliderAdapter sliderAdapter;

    private List<Map<String, Object>> equipments;
    private List<Map<String, Object>> scannedEquipments;
    private List<Map<String, Object>> toScanEquipments = new ArrayList<>();

    private InventoryFragment toScanFragment;
    private InventoryFragment resultFragment;

    String titleToScan = "To scan";
    String titleResult = "Result";
    private String inventoryNum = "inventory_num";

    private Utility utility = new Utility();
    private Context context = this;


    /*
    ------equipment------

    private RecyclerView rvEquipments;

    private List<Map<String, Object>> equipments;
    private ArrayList<Equipment> parent_equipments;

    private ExpandableEquipmentAdapter adapter;
*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inventory_list);


        final Intent intent = getIntent();
        String room_number = intent.getStringExtra("room");


        //---get framing layout for dimming
        final FrameLayout frameLayout = findViewById(R.id.frame_inventory_layout);
        frameLayout.getForeground().setAlpha(0);

        //----toolbar
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle("Room №" + room_number);
            setSupportActionBar(toolbar);

            utility.toolbarSetter(getSupportActionBar(), frameLayout, true);
        }


        //--btn setup
        btnScanInventory = findViewById(R.id.btnScanInventory);
        btnScanInventory.setOnClickListener(this);


        //--get data from sqlite
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();


        //----viewpager
        viewPager = findViewById(R.id.inventory_viewpager);

        //-----tab from viewPager
        TabLayout tabLayout = findViewById(R.id.inventory_tabs);
        tabLayout.setupWithViewPager(viewPager);

        /*//--RecyclerView setup
        rvEquipments = findViewById(R.id.rvScanlist);

        rvEquipments.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvEquipments.setLayoutManager(layoutManager);*/


        //TODO:handle dialog for tabvers and replace scanned to the other list

        dialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                /*create dialog*/
                ScanDialogFragment dialogFragment = new ScanDialogFragment();
                Bundle bundle = new Bundle();
                String dialog_tag = "qr_scan";

                /*get scanned mesage and inventory number*/
                inventoryNum = (String) scannedMap.get("inventory_num");
                String scanned_msg = scannedMapToMsg(scannedMap);


                //-------------------------//

                /*get list from adapter*/
                Map<String, Object> searched_map = null;
                int position = -1;

                /*find map with inventory_num in toScanList*/
                searched_map = utility.findMapByInventoryNum(toScanEquipments, inventoryNum);


                /*searched map in toScanList: show result in dialog*/
                if (searched_map != null) {

                    log.info("--------------------" + searched_map.keySet() + " " + searched_map.values());

                    position = toScanEquipments.indexOf(searched_map);

                    dialogFragment.callDialog(context, bundle, scanned_msg, dialog_tag);


                    /*if result in toScan list, remove, notify adapter*/
                    if (position > -1) {

                        toScanEquipments.remove(position);
                        scannedEquipments.add(searched_map);

                        if (scannedEquipments != null) {
                            log.info("---Scanned---" + scannedEquipments.toString());
                        }
                        log.info("---Notify data changed---");

                        toScanFragment.getAdapter().notifyDataSetChanged();
                        resultFragment.getAdapter().notifyDataSetChanged();

                 /*try to find in scannedList*/
                } else {

                        //TODO:Secondary scan - error fragment already added. FIX!
                    searched_map = utility.findMapByInventoryNum(scannedEquipments, inventoryNum);

                    /*searched map in scannedList: show "already scanned" in dialog*/
                    if (searched_map != null) {

                        scanned_msg = "The equipment with inventory number " + inventoryNum + " has already being scanned";

                        dialogFragment.callDialog(context, bundle, scanned_msg, dialog_tag);
                        log.info("The equipment with inventory number " + inventoryNum + " has already being scanned");

                    /*still couldn't find: show "No such item" in dialog*/
                    }else{
                        scanned_msg = "No such item in current inventory list";
                        dialogFragment.callDialog(context, bundle, scanned_msg, dialog_tag);
                    }
                    }
                }


                /*replace scanned equipment*/
/*
                *//*remove map from old list and add to scanned_list*//*
                if (position > -1 && searched_map != null) {

                    toScanEquipments.remove(position);
                    scannedEquipments.add(searched_map);

                    if (scannedEquipments != null) {
                        log.info("---Scanned---" + scannedEquipments.toString());
                    }
                    log.info("---Notify data changed---");

                    toScanFragment.getAdapter().notifyDataSetChanged();
                    resultFragment.getAdapter().notifyDataSetChanged();
                } else {
                    log.info("---Nothing---");

                }*/

            }


               /* if (searched_map != null) {
                    //check isScanned
                    Object isSelected = searched_map.get("isSelected");

                    if (isSelected != null) {
                        log.info("--------------------"+ isSelected);
                        if ((boolean) isSelected) {
                            //selected already dialog

                        } else {
                            //never before
                            //find viewHolder
                            Object item_id = searched_map.get("item_id");
                            if (item_id != null) {
                                log.info("--------------------" + item_id);

                                RecyclerView.ViewHolder viewHolder = rvEquipments.findViewHolderForAdapterPosition((Integer) item_id);
                                log.info("--------------------" + viewHolder.getAdapterPosition());

                                //set selected, replace, notify}

                                searched_map.put("isSelected", true);

                                temp_adapter_list.remove(viewHolder.getAdapterPosition());
                                temp_adapter_list.add(searched_map);


                                log.info("---Notify data changed---");
                                rvEquipments.getAdapter().notifyDataSetChanged();
                            }}
                        }

                    }*/

            //-------------------------//
/*
                    if (view != null) {
                        log.info("-----TAG-----" + view.getTag());
                        RecyclerView.ViewHolder viewHolder = rvEquipments.findContainingViewHolder(view);
                        int position = viewHolder.getAdapterPosition();

*//*                  //----------equipment---------

                    *//**//*replace found equipment to the end of the list*//**//*

                    List<Equipment> temp_parent_equipments = adapter.getmParentItemList();
                    Equipment equipment_to_move = temp_parent_equipments.get(position);

*//*
                        //----------inventory---------
                        List<Map<String, Object>> temp_parent_equipments = adapter.getInventories();
                        Map<String, Object> equipment_to_move = temp_parent_equipments.get(position);


                        //----------equipment---------
                        // if (equipment_to_move.isSelected()) {

                        //----------inventory---------
                        Object isSelected = equipment_to_move.get("isSelected");
                        if (isSelected != null && (boolean) isSelected) {

                            scanned_msg = "The equipment has already been scanned!";

                            dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);

                        } else {
                            //----------equipment---------
                            // equipment_to_move.setSelected(true);

                            //----------inventory---------
                            equipment_to_move.put("isSelected", true);

                            temp_parent_equipments.remove(position);
                            temp_parent_equipments.add(equipment_to_move);

                            dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);

                            log.info("---Notify data changed---");
                            rvEquipments.getAdapter().notifyDataSetChanged();
                        }

                    } else {
                        log.info("----Equipment with inventory number----");

                        scanned_msg = "Equipment with inventory number " + inventoryNum + " is not found!";
                        dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, dialog_tag);
                    }*/
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
/*
            //-----equipment------
            table_to_select = "equipment";
*/

            //-----inventory------
            table_to_select = "inventory";


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

/*
            //--------equipment---------
            parent_equipments = utility.mapListToEquipmentList(equipments);

            adapter = new ExpandableEquipmentAdapter(parent_equipments);
*/
/*


            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);


            adapter = new InventoryAdapter(equipments);

            rvEquipments.setAdapter(adapter);

*/

            /*create adapter and add fragments with data*/
            System.out.println("--------Create slider adapter--------");
            sliderAdapter = new SliderAdapter(getSupportFragmentManager());


            sliderAdapter.addFragment(InventoryFragment.newInstance
                    (new UniversalSerializablePojo(equipments)), titleToScan);
            sliderAdapter.addFragment(InventoryFragment.newInstance
                    (new UniversalSerializablePojo
                            (new ArrayList<Map<String, Object>>())), titleResult);


            /*set adapter to viewPager*/
            viewPager.setAdapter(sliderAdapter);
//TODO; handle if null
            /*get list to scan and already scanned lists*/
            toScanFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleToScan);
            toScanEquipments = toScanFragment.getAdapter().getInventories();

            resultFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleResult);
            scannedEquipments = resultFragment.getAdapter().getInventories();


            Toast.makeText(context, "Done 2", Toast.LENGTH_SHORT).show();

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
        exitDialogFragment.callDialog(context, new Bundle(), "Quit the room?", "exit");
        //  super.onBackPressed(); вылетает
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}

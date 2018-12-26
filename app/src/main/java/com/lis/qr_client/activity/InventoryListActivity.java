package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import com.lis.qr_client.utilities.Utility;
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
    static final String INVENTORY_STATE_BOOLEAN = "inventory_state";
    static final String TO_SCAN_LIST = "to_scan_list";
    static final String SCANNED_LIST = "scanned_list";

    private Cursor cursor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private Button btnScanInventory;
    String table_to_select;
    private Toolbar toolbar;
    private ViewPager viewPager;

    private SliderAdapter sliderAdapter;

    private List<Map<String, Object>> allEquipments = new ArrayList<>();
    private List<Map<String, Object>> scannedEquipments = new ArrayList<>();
    private List<Map<String, Object>> toScanEquipments = new ArrayList<>();

    private InventoryFragment toScanFragment;
    private InventoryFragment resultFragment;

    private String titleToScan;
    private String titleResult;
    private String inventoryNum = "inventory_num";

    private Utility utility = new Utility();
    private Context context = this;
    private String room_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        log.info("---OnCreate InventoryListActivity---");

        setContentView(R.layout.activity_inventory_list);

        room_number = utility.loadStringOrJsonPreference(context, MainMenuActivity.PREFERENCE_FILE_NAME,
                InventoryParamSelectActivity.ROOM_ID_PREFERENCES);


        //---get framing layout for dimming
        final FrameLayout frameLayout = findViewById(R.id.frame_inventory_layout);
        frameLayout.getForeground().setAlpha(0);

        //----toolbar
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.room_number) + room_number);
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

        dialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                /*create dialog*/
                ScanDialogFragment dialogFragment = new ScanDialogFragment();
                Bundle bundle = new Bundle();

                /*get scanned mesage and inventory number*/
                inventoryNum = (String) scannedMap.get("inventory_num");
                String scanned_msg = scannedMapToMsg(scannedMap);
                String dialog_tag;

                //-------------------------//

                /*get list from adapter*/
                int position = -1;

                /*find map with inventory_num in toScanList*/
                Map<String, Object> searched_map = utility.findMapByInventoryNum(toScanEquipments, inventoryNum);


                /*searched map in toScanList: show result in dialog*/
                if (searched_map != null) {

                    log.info("--------------------" + searched_map.keySet() + " " + searched_map.values());
                    dialog_tag = "qr_scan";

                    position = toScanEquipments.indexOf(searched_map);

                    dialogFragment.callDialog(context, bundle, scanned_msg, dialog_tag);


                    /*if result in toScan list, remove, notify adapter*/
                    if (position > -1) {

                        /*for ok picture*/
                        searched_map.put("scanned", true);

                        toScanEquipments.remove(position);
                        scannedEquipments.add(searched_map);

                        if (scannedEquipments != null) {
                            log.info("---Scanned---" + scannedEquipments.toString());
                        }
                        log.info("---Notify data changed---");

                        toScanFragment.getAdapter().notifyDataSetChanged();
                        resultFragment.getAdapter().notifyDataSetChanged();

                    }

                /*try to find in scannedList*/
                } else {
                    /*reset searche_map*/
                    searched_map = utility.findMapByInventoryNum(scannedEquipments, inventoryNum);

                    /*searched map in scannedList: show "already scanned" in dialog*/
                    if (searched_map != null) {
                        dialog_tag = "qr_scanned";

                        log.info("--Map was found in the scanned list" + searched_map.toString());

                        scanned_msg = getString(R.string.equipment_already_scanned, inventoryNum);

                        new ScanDialogFragment().callDialog(context, bundle, scanned_msg, dialog_tag);
                        log.info(scanned_msg);

                    /*still couldn't find: show "No such item" in dialog*/
                    } else if ((searched_map = utility.findMapByInventoryNum(allEquipments, inventoryNum)) != null) {

                        log.info("--Equipment was found in the other room list" + searched_map.toString());
                        scanned_msg = getString(R.string.equipment_in_the_other_room)+" ";

                        Object room = searched_map.get("room");

                        if (room != null) {
                            scanned_msg += room.toString();
                        }

                        dialog_tag = "qr_found_in_address";
                        new ScanDialogFragment().callDialog(context, bundle, scanned_msg, dialog_tag);
                    } else {

                        log.info("--Equipment was not found in this ");

                        dialog_tag = "qr_not_found";
                        scanned_msg = getString(R.string.equipment_not_found);
                        new ScanDialogFragment().callDialog(context, bundle, scanned_msg, dialog_tag);
                    }
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

            /*check if there any saved inventory data*/

            boolean is_saved_inventory = utility.loadBooleanPreference(context, MainMenuActivity.PREFERENCE_FILE_NAME,
                    INVENTORY_STATE_BOOLEAN);

            table_to_select = "inventory";

            cursor = db.query(table_to_select, null, null, null, null, null,
                    null);

            allEquipments = utility.cursorToMapList(cursor);

            /*if so load equpments*/

            if (is_saved_inventory) {

                log.info("---Have the saved data---");
                /*load equipments*/
                log.info("--load saved equipments---");


                toScanEquipments = utility.preferencesJsonToMapList
                        (context, MainMenuActivity.PREFERENCE_FILE_NAME, TO_SCAN_LIST);

                scannedEquipments = utility.preferencesJsonToMapList
                        (context, MainMenuActivity.PREFERENCE_FILE_NAME, SCANNED_LIST);

            }

            /*load equipments to scan*/
            else {
                //-----inventory------

                cursor = null;

                cursor = db.query(table_to_select, null, "room=?", new String[]{room_number}, null, null,
                        null);

                toScanEquipments = utility.cursorToMapList(cursor);

//--logs---
                System.out.println("--------What have we taken from Sqlite--------");
                for (Map<String, Object> map : toScanEquipments) {
                    log.info(map.keySet().toString() + " " + map.values().toString());
                }
//---------
            }
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
            Toast.makeText(context, getString(R.string.done_loading), Toast.LENGTH_SHORT).show();

            /*create adapter and add fragments with data*/
            System.out.println("--------Create slider adapter--------");
            sliderAdapter = new SliderAdapter(getSupportFragmentManager());

            titleToScan = context.getResources().getString(R.string.to_scan);
            titleResult = context.getResources().getString(R.string.result);


            sliderAdapter.addFragment(InventoryFragment.newInstance
                    (new UniversalSerializablePojo(toScanEquipments)), titleToScan);

            if (scannedEquipments == null) {
                sliderAdapter.addFragment(InventoryFragment.newInstance
                        (new UniversalSerializablePojo(new ArrayList<Map<String, Object>>())), titleResult);
            } else {
                sliderAdapter.addFragment(InventoryFragment.newInstance
                        (new UniversalSerializablePojo(scannedEquipments)), titleResult);
            }

            /*set adapter to viewPager*/
            viewPager.setAdapter(sliderAdapter);


            /*get list to scan and already scanned lists*/
            toScanFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleToScan);
            toScanEquipments = toScanFragment.getAdapter().getInventories();

            resultFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleResult);
            scannedEquipments = resultFragment.getAdapter().getInventories();


            Toast.makeText(context, getString(R.string.done_all), Toast.LENGTH_SHORT).show();

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
    protected void onStop() {
        super.onStop();
        log.info("---OnStop InventoryListActivity---");

   /*save data outside the activity*/

        //change to save to object
        //saveInventoryToPreferences(context, MainMenuActivity.PREFERENCE_FILE_NAME);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log.info("---OnRestart InventoryListActivity---");
    }

    @Override
    public void onBackPressed() {
        log.info("InventoryListActivity on backPressed");
        ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
        exitDialogFragment.callDialog(context, new Bundle(), getString(R.string.quit_room_msg), "exit");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveInventoryToPreferences(final Context context, final String preferenceFileName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("--saveInventoryToPreferences--- current thread is " + Thread.currentThread());

                utility.savePreference(context, preferenceFileName, INVENTORY_STATE_BOOLEAN, true);

                if (toScanEquipments != null) {
                    utility.savePreference(context, preferenceFileName, TO_SCAN_LIST, toScanEquipments);
                }
                if (scannedEquipments != null) {
                    utility.savePreference(context, preferenceFileName, SCANNED_LIST, scannedEquipments);
                }
            }
        }).start();
    }
    /*or knock it from context in full info()*/


}

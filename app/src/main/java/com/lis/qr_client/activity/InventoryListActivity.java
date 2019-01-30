package com.lis.qr_client.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.badoo.mobile.util.WeakHandler;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyBundle;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.dialog_fragment.FinishInventoryDialogFragment;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.pojo.UniversalSerializablePojo;
import com.lis.qr_client.extra.utility.Utility;
import com.lis.qr_client.extra.adapter.SliderAdapter;
import com.lis.qr_client.extra.dialog_fragment.ExitDialogFragment;
import com.lis.qr_client.extra.dialog_fragment.ScanDialogFragment;
import com.lis.qr_client.extra.fragment.InventoryFragment;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

/**
 * Loads all view necessary items, runs thread loading data from sqlite db,
 * after puts data in the adapter and set viewPager
 */

@Log
public class InventoryListActivity extends BaseActivity {
    private static final int REQUEST_SCAN_QR = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;

    Activity activity = this;

    private SQLiteDatabase db;

    private ViewPager viewPager;
    private Button btnScanInventory;


    private List<Map<String, Object>> allEquipments = new ArrayList<>();
    private List<Map<String, Object>> scannedEquipments = new ArrayList<>();
    private List<Map<String, Object>> toScanEquipments = new ArrayList<>();

    private InventoryFragment toScanFragment;
    private InventoryFragment resultFragment;

    protected HashMap<String, Object> scannedMap;
    private String room_number;

    WeakHandler dialogHandler;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);
        log.info("---OnCreate InventoryListActivity---");

        setContentView(R.layout.activity_inventory_list);


        //---get room number for toolbar title
        room_number = PreferenceUtility.loadStringOrJsonPreference
                (QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                        MyPreferences.ROOM_ID_PREFERENCES);


        //---get framing layout for dimming
        final FrameLayout frameLayout = findViewById(R.id.frame_inventory_layout);
        frameLayout.getForeground().setAlpha(0);

        //----set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar,
                    getString(R.string.room_number) + room_number, frameLayout, true);
        }


        //--btn setup
        // btnScanInventory = findViewById(R.id.btnScanInventory);
            /*btnScanInventory.setOnClickListener(this);
            btnScanInventory.setEnabled(false);*/


        //--get data from sqlite
        DBHelper dbHelper = QrApplication.getDbHelper();
        db = dbHelper.getWritableDatabase();


        //----viewpager
        viewPager = findViewById(R.id.inventory_viewpager);

        //-----tab from viewPager
        TabLayout tabLayout = findViewById(R.id.inventory_tabs);
        tabLayout.setupWithViewPager(viewPager);

        dialogHandler = new WeakHandler(callback);


        //--bottom navigation bar

        LinearLayout layout_home = findViewById(R.id.layout_home);
        LinearLayout layout_scan = findViewById(R.id.layout_scan);
        LinearLayout layout_finish = findViewById(R.id.layout_finish);

        layout_home.setOnClickListener(onClickListener);
        layout_scan.setOnClickListener(onClickListener);
        layout_finish.setOnClickListener(onClickListener);


        if (savedInstanceState == null) {
            thread = new Thread(runLoadEquipments);
            thread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.info("---OnStart InventoryListActivity---");
    }

    //-----------Menu------------//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.item_log_out: {

                /*clean user's shared preferences (or all preferences?)*/
                PreferenceUtility.removeLoginPreferences(this, MyPreferences.PREFERENCE_SAVE_USER,
                        MyPreferences.PREFERENCE_IS_USER_SAVED);

                /*launch welcome page*/
                Intent intent = new Intent(this, WelcomeActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);

    }

    //-------Callback------//

    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //create dialog
            ScanDialogFragment dialogFragment = new ScanDialogFragment();
            Bundle bundle = new Bundle();

            //get scanned mesage and inventory number
            String inventoryNum = (String) scannedMap.get("inventory_num");
            String scanned_msg = ObjectUtility.scannedMapToMsg(scannedMap);
            String dialog_tag;

            //-------------------------//

            //get list from adapter
            int position = -1;

            //find map with inventory_num in toScanList
            Map<String, Object> searched_map = ObjectUtility.findMapByInventoryNum(toScanEquipments, inventoryNum);


            //searched map in toScanList: show result in dialog
            if (searched_map != null) {

                log.info("--------------------" + searched_map.keySet() + " " + searched_map.values());
                dialog_tag = "qr_scan";

                position = toScanEquipments.indexOf(searched_map);

                dialogFragment.callDialog(getFragmentManager(),
                        bundle, scanned_msg, getString(R.string.found_title), dialog_tag);


                //if result in toScan list, remove, notify adapter
                if (position > -1) {

                    //for ok picture
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

                //try to find in scannedList
            } else {
                //reset searche_map
                searched_map = ObjectUtility.findMapByInventoryNum(scannedEquipments, inventoryNum);

                //searched map in scannedList: show "already scanned" in dialog
                if (searched_map != null) {
                    dialog_tag = "qr_scanned";

                    log.info("--Map was found in the scanned list" + searched_map.toString());

                    scanned_msg = getString(R.string.equipment_already_scanned, inventoryNum);

                    new ScanDialogFragment().callDialog
                            (getFragmentManager(), bundle, scanned_msg, getString(R.string.already_scanned_title), dialog_tag);
                    log.info(scanned_msg);

                    //still couldn't find: show "No such item" in dialog
                } else if ((searched_map = ObjectUtility.findMapByInventoryNum(allEquipments, inventoryNum)) != null) {

                    log.info("--Equipment was found in the other room list" + searched_map.toString());
                    scanned_msg = getString(R.string.equipment_in_the_other_room) + " ";

                    Object room = searched_map.get("room");

                    if (room != null) {
                        scanned_msg += room.toString();
                    }

                    dialog_tag = "qr_found_in_address";
                    new ScanDialogFragment().callDialog(getFragmentManager(), bundle, scanned_msg,
                            getString(R.string.other_room_title), dialog_tag);
                } else {

                    log.info("--Equipment was not found in this ");

                    dialog_tag = "qr_not_found";
                    scanned_msg = getString(R.string.equipment_not_found);
                    new ScanDialogFragment().callDialog(getFragmentManager(),
                            bundle, scanned_msg, getString(R.string.not_found_title), dialog_tag);
                }
            }
            return false;
        }
    };

    //--------Runnable------


    /**
     * Load equipments from the SQLite
     */
    private Runnable runLoadEquipments = new Runnable() {
        @Override
        public void run() {

            log.info("-- runLoadEquipments --");

            /*check if there any saved inventory data*/

            boolean is_saved_inventory = PreferenceUtility.loadBooleanPreference(QrApplication.getInstance(),
                    MyPreferences.PREFERENCE_FILE_NAME,
                    MyPreferences.PREFERENCE_INVENTORY_STATE_BOOLEAN);

            String table_to_select = DbTables.TABLE_INVENTORY;

             /*select all equipments by the following address from the table*/
            Cursor cursor = db.query(table_to_select, null, null, null, null, null,
                    null);

            allEquipments = DbUtility.cursorToMapList(cursor);


            /*if so load equpments*/

            if (is_saved_inventory) {

                log.info("---Have the saved data---");
                /*load equipments*/
                log.info("--load saved equipments---");


                toScanEquipments = PreferenceUtility.preferencesJsonToMapList
                        (QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME, MyPreferences.PREFERENCE_TO_SCAN_LIST);


                scannedEquipments = PreferenceUtility.preferencesJsonToMapList
                        (QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME, MyPreferences.PREFERENCE_SCANNED_LIST);

            }

            /*load equipments to scan*/
            else {
                //-----inventory------
                log.info("---No saved data. Taking from db---");


                /*select only this room equipments*/
                cursor = db.query(table_to_select, null, "room=?", new String[]{room_number}, null, null,
                        null);

                toScanEquipments = DbUtility.cursorToMapList(cursor);

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

            log.info("-- postEquipmentInList --");

            if (toScanFragment == null) {
                log.info(" long before toScanFragment == null");
            } else {
                if (toScanFragment.getAdapter() == null) {
                    log.info("long before toScanFragment.getAdapter() == null");
                } else {
                    log.info("long before all is ok");
                }
            }

            Toast.makeText(QrApplication.getInstance(), getString(R.string.done_loading), Toast.LENGTH_SHORT).show();

            /*create adapter and add fragments with data*/
            System.out.println("--------Create slider adapter--------");
            SliderAdapter sliderAdapter = new SliderAdapter(getSupportFragmentManager());

            String titleToScan = getString(R.string.to_scan);
            String titleResult = getString(R.string.result);


            /*add list to scan to the visual component*/
            sliderAdapter.addFragment(InventoryFragment.newInstance
                    (new UniversalSerializablePojo(toScanEquipments), getFragmentManager()), titleToScan);

            /*add scanned list (or null, if the session is new) to the visual component*/
            if (scannedEquipments == null) {
                sliderAdapter.addFragment(InventoryFragment.newInstance
                                (new UniversalSerializablePojo(new ArrayList<Map<String, Object>>()), getFragmentManager()),
                        titleResult);
            } else {
                sliderAdapter.addFragment(InventoryFragment.newInstance
                        (new UniversalSerializablePojo(scannedEquipments), getFragmentManager()), titleResult);
            }

            /*set adapter to viewPager*/
            viewPager.setAdapter(sliderAdapter);


            /*get list to scan and already scanned lists*/
            toScanFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleToScan);
            toScanEquipments = toScanFragment.getAdapter().getInventories();
            resultFragment = (InventoryFragment) sliderAdapter.getFragmentByTitle(titleResult);
            scannedEquipments = resultFragment.getAdapter().getInventories();


            Toast.makeText(QrApplication.getInstance(), getString(R.string.done_all), Toast.LENGTH_SHORT).show();

/*
            btnScanInventory.setEnabled(true);
*/

        }
    };

    //-------------------Clicks--------------------//

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.layout_home: {
                    log.info("Inventory list --- home button pressed");
                    Intent intent = new Intent(QrApplication.getInstance(), MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                }
                case R.id.layout_scan: {
                    log.info("Inventory list --- scan button pressed");
                    Intent intent = new Intent(QrApplication.getInstance(), CameraActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QR);
                    break;
                }
                case R.id.layout_finish: {
                    log.info("Inventory list --- finish inventory button pressed");
                    /* request the permission */
                    checkStoragePermission(activity);

                    /* show dialog "finish inventory and make a report?" */
                    FinishInventoryDialogFragment finishDialog = new FinishInventoryDialogFragment();

                    /*transfer scanned and to_scan lists to the dialog*/
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(MyBundle.TO_SCAN_LIST, new UniversalSerializablePojo(toScanEquipments));
                    bundle.putSerializable(MyBundle.SCANNED_LIST, new UniversalSerializablePojo(scannedEquipments));
                    bundle.putString(MyBundle.ROOM, room_number);

                    log.info("Bundle size to pass "+bundle.size());
                    finishDialog.callDialog(getFragmentManager(), bundle, getString(R.string.finish_and_save),
                            getString(R.string.finish), "finish");

                    /*create csv and save to the phone memory*/

                    /*notify user and finish the activity*/

                    break;
                }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SCAN_QR) {
                final String scan_result = data.getStringExtra("scan_result");

                /*show alertDialog with scanned data*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       /* converts gotten data from json to map*/
                        scannedMap = ObjectUtility.scannedJsonToMap(scan_result);
                        dialogHandler.sendEmptyMessage(1);
                    }

                }).start();

            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.mission_was_canceled), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        log.info("---OnRestart InventoryListActivity---");
    }


    @Override
    protected void onStop() {
        super.onStop();
        log.info("---OnStop InventoryListActivity---");
        saveInventoryToPreferences(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME);
    }


    @Override
    public void onBackPressed() {
        log.info("InventoryListActivity on backPressed");
        ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
        exitDialogFragment.callDialog(getFragmentManager(), new Bundle(), getString(R.string.quit_room_msg), getString(R.string.exit),"exit");
    }

    /*or knock it from context in full info()*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Inventory List -- onDestroy()---");
/*
   remove links
*/
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

        callback = null;
        dialogHandler = null;

        db = null;

        viewPager = null;

        runLoadEquipments = null;
        postEquipmentInList = null;

        resultFragment = null;
        toScanFragment = null;

    }

    //------Methods-----

    public void saveInventoryToPreferences(final Context context, final String preferenceFileName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("--saveInventoryToPreferences--- current thread is " + Thread.currentThread());

                PreferenceUtility.savePreference(context, preferenceFileName,
                        MyPreferences.PREFERENCE_INVENTORY_STATE_BOOLEAN, true);

                if (toScanEquipments != null) {
                    PreferenceUtility.savePreference(context, preferenceFileName,
                            MyPreferences.PREFERENCE_TO_SCAN_LIST, toScanEquipments);
                }
                if (scannedEquipments != null) {
                    PreferenceUtility.savePreference(context, preferenceFileName,
                            MyPreferences.PREFERENCE_SCANNED_LIST, scannedEquipments);
                }
            }
        }).start();
    }


    //-------------Permission-------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /*ok, run save file*/
                } else {
                    Toast.makeText(QrApplication.getInstance(), getString(R.string.no_storage_write_permission),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkStoragePermission(Activity activity) {

        boolean hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission) {
            log.info("Has storage permission");
            /*run save file*/

        } else {
            log.info("No storage permission. Ask for it");
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }
}
//----------QUESTIONS-----------
/*
ON SVAED INSTANCE STATE
    onSvaedInstanceState - не использую. потому что сохранение идет всегда, а восстановление от случая к случаю. При переходе
    к отдельному оборудованию состояние и списки не сохраняются, приходится вытягивать из сохраненных преференсес. При повороте экрана,
    восстановление (onRestoreInstanceState) проходит норм.
    Проще закидывать все в преференсес и вытягивать в случае перегрузки активити.
*/
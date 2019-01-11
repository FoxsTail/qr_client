package com.lis.qr_client.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.*;
import android.widget.*;
import com.badoo.mobile.util.WeakHandler;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.async_helpers.AsyncMultiDbManager;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Log
public class InventoryParamSelectActivity extends BaseActivity {
    private static final int LOAD_ADDRESS = 1;
    private static final int LOAD_ROOMS = 2;

    private Spinner spinAddress, spinRoom;
    private ProgressBar pbLoadEquipment;
    private Button btnStart;

    private SQLiteDatabase db;

    private Cursor cursor;

    private BidiMap<Integer, String> addresses = new DualHashBidiMap<>();


    private String chosen_room = null;
    private String chosen_address = null;

    private Thread thread;
    private Handler handler;

    private int saved_address_preference;


    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        log.info("InventoryParamSelectActivity --- onCreate()");


        setContentView(R.layout.activity_inventory_param_select);

        /*run load from preferences*/
        saved_address_preference = PreferenceUtility.loadIntPreference(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                MyPreferences.ADDRESS_ID_PREFERENCES);


        //---get framing layout for dimming
        final FrameLayout frameLayout = findViewById(R.id.frame_paramSelect_layout);
        frameLayout.getForeground().setAlpha(0);

        //---set toolbar
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar, getString(R.string.select_room),
                    frameLayout, true);
        }

        //---------

        url = "http://" + getString(R.string.emu_ip) + ":" + getString(R.string.port);


        spinAddress = findViewById(R.id.spinAddress);
        spinRoom = findViewById(R.id.spinRoom);
//        spinRoom.setEnabled(false);

        pbLoadEquipment = findViewById(R.id.pbLoadEquipment);
        pbLoadEquipment.setVisibility(View.INVISIBLE);

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(onClickListener);


        /*get data from sqlite*/
        DBHelper dbHelper = QrApplication.getDbHelper();
        db = dbHelper.getWritableDatabase();

        handler = new Handler(handlerCallback);


        thread = new Thread(runLoadAddress);
        thread.start();


    }

    //---------------------------//

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        /*if the value is empty*/
            if (chosen_address == null || chosen_room == null) {
                log.info("---Address or Room is null. Err.--");
                Toast.makeText(QrApplication.getInstance(), getString(R.string.choose_address_n_room), Toast.LENGTH_SHORT).show();
                return;
            }

            if (chosen_address.equals(" ") || chosen_room.equals(" ")) {
                log.info("---Nothing is selected--");
                Toast.makeText(QrApplication.getInstance(), getString(R.string.choose_address_n_room), Toast.LENGTH_SHORT).show();
            } else {

                if (url != null) {

                /*remove previous session data*/
                    PreferenceUtility.removeOldPreferences(QrApplication.getInstance(),
                            MyPreferences.PREFERENCE_FILE_NAME,
                            MyPreferences.PREFERENCE_INVENTORY_STATE_BOOLEAN,
                            MyPreferences.PREFERENCE_TO_SCAN_LIST,
                            MyPreferences.PREFERENCE_SCANNED_LIST);

                    AsyncMultiDbManager asyncMultiDbManager = new AsyncMultiDbManager
                            (QrApplication.getInstance(), null, null, null, true,
                                    InventoryListActivity.class, chosen_room, btnStart, pbLoadEquipment, false);

                    asyncMultiDbManager.runAsyncLoader();

                } else {
                    log.warning("---URL IS NULL---");

                }
            }

        }
    };
    //--------Spinners stuff----------//


    /*Handler callback*/

    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            log.info("---HANDLER---");
            switch (msg.what) {
                case LOAD_ADDRESS: {
                    log.info("---Prepare address spinner---");
                    spinnerPrepare(spinAddress, (List<Object>) msg.obj, getString(R.string.address), saved_address_preference);
                    break;
                }
                case LOAD_ROOMS: {
                    log.info("---Prepare room spinner---");
                    spinnerPrepare(spinRoom, (List<Object>) msg.obj, getString(R.string.room), 0);
                    break;
                }
            }

            return false;
        }
    };


    /**
     * Prepares spinner: sets adapter with passed data, prompt, itemSelected listener
     */

    private void spinnerPrepare(Spinner spinner, List spinner_array, String spinner_prompt, int selection) {

        ArrayAdapter<Object> arrayAdapter = new ArrayAdapter<>(QrApplication.getInstance(),
                android.R.layout.simple_spinner_item, spinner_array);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        spinner.setPrompt(spinner_prompt);

        if (selection > 0) {

        /*get item_id from spinner by it's value*/
            int spinner_position = spinner_array.indexOf(addresses.get(selection));

        /*set selection*/
            spinner.setSelection(spinner_position);
        }

        log.info("---Set spinner listener---");
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }

    /**
     * OnItemSelected;
     * loads rooms when address id selected
     */

    //TODO: ! replace address-room-equipment to address-tech_platform(room from here we use)-equipment
    AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            log.info("---OnItemSelected---");

            switch (parent.getId()) {
                case R.id.spinAddress: {

                    log.info("---spin Address selected---");

                    /*get value from selected Item*/
                    chosen_address = parent.getItemAtPosition(position).toString();

                    /*if the value is empty*/
                    if (chosen_address.equals(" ")) {
                        log.info("---Nothing is selected (address)---");
                        break;
                    }

                    /*get id_address from address value*/
                    int id_address = addresses.getKey(chosen_address);
                    log.info("---address id is " + id_address + "---");


                    int saved_address = PreferenceUtility.loadIntPreference
                            (QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                                    MyPreferences.ADDRESS_ID_PREFERENCES);
                    log.info("---pref is " + saved_address + "---");


                    /*if loaded address equals to already loaded one - take data from sqlite*/

                    if (saved_address > 0 && saved_address == id_address) {
                       /*load rooms from sqlite from sqlite*/

                        Thread thread = new Thread(runLoadRooms);
                        thread.start();


                    } else {
                        /*load from db data and rooms for the address*/

                         /*params for request*/
                        if (url != null) {

                        /*get data from server*/
                            String url_inventory = url + getString(R.string.api_inventory_by_address_load) + id_address;
                            String table_name = DbTables.TABLE_INVENTORY;

                            AsyncMultiDbManager asyncLoader = new AsyncMultiDbManager
                                    (QrApplication.getInstance(), table_name, null, url_inventory, false,
                                            null, chosen_room, btnStart, pbLoadEquipment,
                                            true);
                            asyncLoader.runAsyncLoader();

                        /*async get rooms from server*/
                            String url_room = url + getString(R.string.api_room_by_address_load) + id_address;
                            table_name = DbTables.TABLE_ROOM;
                            String column_name = "room";


                            AsyncMultiDbManager dbManager = new AsyncMultiDbManager(QrApplication.getInstance(), table_name, column_name,
                                    url_room, false, null, runLoadRooms, false);


                            log.info("--- call AsyncMapListLoader---");
                            dbManager.runAsyncLoader();
                        } else {
                            log.warning("---URL IS NULL!---");

                        }

                        /*put to already loaded list*/
                        PreferenceUtility.savePreference(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                                MyPreferences.ADDRESS_ID_PREFERENCES, id_address);
                    }


                    break;
                }
                case R.id.spinRoom: {
                    log.info("---spin Room selected---");

                    /*get value from selected Item*/
                    chosen_room = parent.getItemAtPosition(position).toString();


                    if (chosen_room != null) {
                        PreferenceUtility.savePreference(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                                MyPreferences.ROOM_ID_PREFERENCES, chosen_room);
                    }

                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    //------------Runnable--------------//

    /**
     * Load rooms
     */

    private Runnable runLoadRooms = new Runnable() {
        @Override
        public void run() {
            log.info("---run load Rooms---");
            cursor = db.query(DbTables.TABLE_ROOM, null, null, null, null,
                    null, null);

            List rooms = DbUtility.cursorToList(cursor, "room");

            System.out.println("-----Romz-----");

            for (Object room : rooms) {
                System.out.println("----------");
                System.out.println(room);
            }

            log.info("---Call dialogHandler---");

            Message msg = handler.obtainMessage(LOAD_ROOMS, rooms);

            handler.sendMessage(msg);

        }
    };


    /**
     * Load Address from SQLite;
     * Put in the BidiMap;
     * Pass the address array to the dialogHandler with LOAD_ADDRESS arg;
     */
    //TODO: put in the onResume() ?
    private Runnable runLoadAddress = new Runnable() {
        @Override
        public void run() {
            log.info("---Run Load addresses from sqLite---");

                    /*calling new thread to create an array from sqlite table */
            String table_to_select = DbTables.TABLE_ADDRESS;

            cursor = db.query(table_to_select, new String[]{"id", "city", "street", "number"},
                    null, null, null,
                    null, null);

        /*parse cursor to BidiMap*/
            addresses = DbUtility.cursorToBidiMap(cursor);

        /*creates adapter, transfer data from array to the listView*/
//--log
            for (Map.Entry<Integer, String> map : addresses.entrySet()) {
                System.out.println("-----------");
                System.out.println(map.getKey());
                System.out.println(map.getValue());
            }
//------
               /*set first elem null*/
            List<String> address_strings = new ArrayList<>();
            address_strings.add(" ");
            address_strings.addAll(addresses.values());

            log.info("---Call dialogHandler---");

            Message msg = handler.obtainMessage(LOAD_ADDRESS, address_strings);
            handler.sendMessage(msg);

        }
    };

    @Override
    protected void onStop() {
        log.info("---InventoryParamSelect -- onStop()---");
        super.onStop();

            /*set null buttons and pb*/
        spinAddress = null;
        spinRoom = null;
        pbLoadEquipment = null;
        btnStart = null;

            /*other ref's*/
        toolbar = null;

        /*handler ref destroy*/
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        /*runnables*/
        runLoadAddress = null;
        runLoadRooms = null;

        /*threads*/
        if (thread != null) {
            thread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---ParamSelect -- onDestroy()---");
    }
}

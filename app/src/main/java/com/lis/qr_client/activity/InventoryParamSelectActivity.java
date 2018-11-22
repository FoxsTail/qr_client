package com.lis.qr_client.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.async_helpers.AsyncDbManager;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.dialog_fragment.ExitDialogFragment;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Log
public class InventoryParamSelectActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int LOAD_ADDRESS = 1;
    public static final int LOAD_ROOMS = 2;


    private Spinner spinAddress, spinRoom;
    private ProgressBar pbLoadEquipment;
    private Button btnStart;
    private Toolbar toolbar;

    private Context context = this;


    DBHelper dbHelper;
    SQLiteDatabase db;

    Cursor cursor;

    List rooms = new ArrayList<>();
    BidiMap<Integer, String> addresses = new DualHashBidiMap<>();


    String table_to_select = "address";
    String chosen_room = null;
    String chosen_address = null;

    Thread thread;
    static Handler handler;

    Utility utility = new Utility();


    String url;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inventory_param_select);

        //---get framing layout for dimming
        final FrameLayout frameLayout = findViewById(R.id.frame_paramSelect_layout);
        frameLayout.getForeground().setAlpha(0);

        //---set toolbar
         toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle("Select Room");
            setSupportActionBar(toolbar);

            utility.toolbarSetter(getSupportActionBar(), frameLayout,true);

        }

        //---------

        url = "http://"+getString(R.string.emu_ip)+":"+getString(R.string.port);

        spinAddress = findViewById(R.id.spinAddress);
        spinRoom = findViewById(R.id.spinRoom);
//        spinRoom.setEnabled(false);

        pbLoadEquipment = findViewById(R.id.pbLoadEquipment);
        pbLoadEquipment.setVisibility(View.INVISIBLE);

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);


        /*get data from sqlite*/
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        handler = new Handler(handlerCallback);


        thread = new Thread(runLoadAddress);
        thread.start();


    }


    //-----------Menu------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qr_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            // arrow <- is pressed
            case android.R.id.home:{
                NavUtils.navigateUpFromSameTask(this);
                // super.onBackPressed();
                return true;
            }
            default:
                return true;
        }

    }




    //---------------------------//


    @Override
    public void onClick(View v) {
        /*if the value is empty*/
        if ( chosen_address == null || chosen_room == null) {
            log.info("---Address or Room is null. Err.--");
            return;
        }

        if ( chosen_address.equals(" ") || chosen_room.equals(" ")) {
            log.info("---Nothing is selected--");
            Toast.makeText(this, "Choose the address and the room!", Toast.LENGTH_SHORT).show();
        } else {

            if(url != null) {
                /*String url_room = url + "/equipments/room/" + chosen_room;
                String table_name = "equipment";*/
                String url_room = url + "/inventory/room/" + chosen_room;
                String table_name = "inventory";
                AsyncDbManager asyncDbManager = new AsyncDbManager(table_name, url_room, context, dbHelper, db, btnStart,
                        pbLoadEquipment, InventoryListActivity.class,
                        true, true, chosen_room);
                asyncDbManager.runAsyncMapListLoader();
            }else {
                log.warning("---URL IS NULL---");

            }
            }

    }

    //--------Spinners stuff----------//


    /*Handler callback*/

    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            log.info("---HANDLER---");
            switch (msg.what) {
                case LOAD_ADDRESS: {
                    log.info("---Prepare address spinner---");
                    spinnerPrepare(spinAddress, (List<Object>) msg.obj, "Address");
                    break;
                }
                case LOAD_ROOMS: {
                    log.info("---Prepare room spinner---");
                    spinnerPrepare(spinRoom, (List<Object>) msg.obj, "Room");
                    break;
                }
            }

            return false;
        }
    };


    /**
     * Prepares spinner: sets adapter with passed data, prompt, itemSelected listener
     */

    private void spinnerPrepare(Spinner spinner, List spinner_array, String spinner_prompt) {

        ArrayAdapter<Object> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinner_array);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        spinner.setPrompt(spinner_prompt);

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


                    /*params for request*/
                    if(url != null) {
                        String url_address = url + "/addresses/rooms/" + id_address;
                        String table_name = "room";
                        String column_name = "room";

                    /*async get rooms and put into sqLite*/
                        AsyncDbManager dbManager = new AsyncDbManager(table_name, column_name, url_address, context, dbHelper, db, false, runLoadRooms);
                        log.info("--- call AsyncMapListLoader---");
                        dbManager.runAsyncMapListLoader();
                    }else {
                        log.warning("---URL IS NULL!---");

                    }
                    break;
                }
                case R.id.spinRoom: {
                    log.info("---spin Room selected---");

                    /*get value from selected Item*/
                    chosen_room = parent.getItemAtPosition(position).toString();
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

    Runnable runLoadRooms = new Runnable() {
        @Override
        public void run() {
            log.info("---run load Rooms---");
            cursor = db.query("room", null, null, null, null,
                    null, null);

            rooms = utility.cursorToList(cursor);

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
    public Runnable runLoadAddress = new Runnable() {
        @Override
        public void run() {
            log.info("---Run Load addresses from sqLite---");

                    /*calling new thread to create an array from sqlite table */
            cursor = db.query(table_to_select, null, null, null, null,
                    null, null);

        /*parse cursor to BidiMap*/
            addresses = utility.cursorToBidiMap(cursor);

        /*creates adapter, transfer data from array to the listView*/
//--log
            for (Map.Entry<Integer, String> map : addresses.entrySet()) {
                System.out.println("-----------");
                System.out.println(map.getKey());
                System.out.println(map.getValue());
            }
//------
               /*set first elem null*/
            //String[] address_strings = addresses.values().toArray(new String[0]);

            List<String> address_strings = new ArrayList<>();
            address_strings.add(" ");
            address_strings.addAll(addresses.values());

            log.info("---Call dialogHandler---");

            Message msg = handler.obtainMessage(LOAD_ADDRESS, address_strings);
            handler.sendMessage(msg);

        }
    };


}

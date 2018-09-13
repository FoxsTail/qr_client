package com.lis.qr_client.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.async_helpers.AsyncDbManager;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
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
    private Context context = this;

    DBHelper dbHelper;
    SQLiteDatabase db;

    Cursor cursor;

    List rooms = new ArrayList<>();
    BidiMap<Integer, String> addresses = new DualHashBidiMap<>();


    String table_to_select = "address";
    String chosen_room = null;

    Thread thread;
    static Handler handler;

    Utility utility = new Utility();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.info("-----I'm here!!!!--------");
        setContentView(R.layout.activity_inventory_param_select);

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

    /**/

    @Override
    public void onClick(View v) {
        /*if the value is empty*/
        if (chosen_room.equals(" ")) {
            log.info("---Nothing is selected (room)---");
            Toast.makeText(this, "Choose the room!", Toast.LENGTH_SHORT).show();
        } else {

            String url = "http://10.0.3.2:8090/equipments/room/" + chosen_room;
            String table_name = "equipment";
            AsyncDbManager asyncDbManager = new AsyncDbManager(table_name, url, context, dbHelper, db, btnStart,
                    pbLoadEquipment, InventoryListActivity.class,
                    true, true);
            asyncDbManager.runAsyncMapListLoader();
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
                    String selected_value = parent.getItemAtPosition(position).toString();

                    /*if the value is empty*/
                    if (selected_value.equals(" ")) {
                        log.info("---Nothing is selected (address)---");
                        break;
                    }

                    /*get id_address from address value*/
                    int id_address = addresses.getKey(selected_value);
                    log.info("---address id is " + id_address + "---");


                    /*params for request*/
                    //TODO:replace 10.0.3.2:8090 with some constant path in production
                    String url = "http://10.0.3.2:8090/addresses/rooms/" + id_address;
                    String table_name = "room";
                    String column_name = "room";

                    /*async get rooms and put into sqLite*/
                    AsyncDbManager dbManager = new AsyncDbManager(table_name, column_name, url, context, dbHelper, db, false, runLoadRooms);
                    log.info("--- call AsyncMapListLoader---");
                    dbManager.runAsyncMapListLoader();

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

            log.info("---Call handler---");

            Message msg = handler.obtainMessage(LOAD_ROOMS, rooms);
            handler.sendMessage(msg);

        }
    };



    /**
     * Load Address from SQLite;
     * Put in the BidiMap;
     * Pass the address array to the handler with LOAD_ADDRESS arg;
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

            log.info("---Call handler---");

            Message msg = handler.obtainMessage(LOAD_ADDRESS, address_strings);
            handler.sendMessage(msg);

        }
    };



}

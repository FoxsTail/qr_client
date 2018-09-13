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
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
public class InventoryParamSelectActivity extends AppCompatActivity {
    public static final int LOAD_ADDRESS = 1;
    public static final int LOAD_ROOMS = 2;


    private Spinner spinAddress, spinRoom;
    private Context context = this;

    DBHelper dbHelper;
    SQLiteDatabase db;

    Cursor cursor;

    List<Integer> rooms = new ArrayList<>();
    BidiMap<Integer, String> addresses = new DualHashBidiMap<>();


    String table_to_select = "address";

    Thread thread;
    static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.info("-----I'm here!!!!--------");
        setContentView(R.layout.activity_inventory_param_select);

        spinAddress = findViewById(R.id.spinAddress);
        spinRoom = findViewById(R.id.spinRoom);
//        spinRoom.setEnabled(false);

        /*get data from sqlite*/
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        handler = new Handler(handlerCallback);

//------------------------------

        thread = new Thread(runLoadAddress);
        thread.start();

//-------------------------------

    }

    /*Handler callback*/

    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            log.info("---HANDLER---");
            switch (msg.what) {
                case LOAD_ADDRESS: {
                    log.info("---Prepare address spinner---");
                    spinnerPrepare(spinAddress, (String[]) msg.obj, "Address");
                    break;
                }
                case LOAD_ROOMS: {
                    log.info("---Prepare room spinner---");
                    spinnerPrepare(spinRoom, (Object[]) msg.obj, "Room");
                    break;
                }
            }

            return false;
        }
    };

    /**
     * Prepares spinner: sets adapter with passed data, prompt, itemSelected listener
     */

    private void spinnerPrepare(Spinner spinner, Object[] spinner_array, String spinner_prompt) {

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

    AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            log.info("---OnItemSelected---");

            switch (parent.getId()) {
                case R.id.spinAddress: {

                    log.info("---spin Address selected---");

                    /*get value from selected Item*/
                    String selected_value = parent.getItemAtPosition(position).toString();

                    /*get id_address from address value*/
                    int id_address = addresses.getKey(selected_value);
                    log.info("---address id is "+id_address+"---");


                    /*request to the db to get rooms*/
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

                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    /**
     * Load rooms
     */

    Runnable runLoadRooms = new Runnable() {
        @Override
        public void run() {
            log.info("---run load Rooms---");
            cursor = db.query("room", null, null, null, null,
                    null, null);

            rooms = cursorToList(cursor);

            System.out.println("-----Romz-----");

            for(Integer room: rooms){
                System.out.println("----------");
                System.out.println(room);
            }

            Integer[] room_strings = rooms.toArray(new Integer[0]);

            log.info("---Call handler---");

            Message msg = handler.obtainMessage(LOAD_ROOMS, room_strings);
            handler.sendMessage(msg);

        }
    };


    /**parse cursor to List*/
    private List cursorToList(Cursor cursor) {
        List convertedList = new ArrayList();

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    convertedList.add(cursor.getInt(cursor.getColumnIndex("room")));

                } while (cursor.moveToNext());
            }
        }

        return convertedList;
    }


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
            addresses = cursorToBidiMap(cursor);

        /*creates adapter, transfer data from array to the listView*/
//--log
            for (Map.Entry<Integer, String> map : addresses.entrySet()) {
                System.out.println("-----------");
                System.out.println(map.getKey());
                System.out.println(map.getValue());
            }
//------
               /*set first elem null*/
            String[] address_strings = addresses.values().toArray(new String[0]);

            log.info("---Call handler---");

            Message msg = handler.obtainMessage(LOAD_ADDRESS, address_strings);
            handler.sendMessage(msg);

        }
    };


    /**
     * parse cursor to BidiMap<Integer, String>
     */
//TODO:sort to get an empty val??
    private BidiMap<Integer, String> cursorToBidiMap(Cursor cursor) {
        log.info("---Cursor to bidiMap---");
        BidiMap<Integer, String> convertedMap = new DualHashBidiMap<>();

        StringBuilder sb = new StringBuilder();
        int temp_id = 0;

        /*----for the first select item*/
        convertedMap.put(0, "Select");


        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {

                    /*take id and address separately*/
                    for (byte i = 0; i < cursor.getColumnCount(); i++) {

                        if (i == 0) {
                            temp_id = cursor.getInt(i);
                        } else {
                            sb.append(cursor.getString(i) + " ");
                        }
                    }
                    if (temp_id != 0) {
                        convertedMap.put(temp_id, sb.toString());
                    }
                    sb.setLength(0);
                } while (cursor.moveToNext());
            }
        } else {
            log.warning("Cursor is null");
        }

        return convertedMap;
    }
}

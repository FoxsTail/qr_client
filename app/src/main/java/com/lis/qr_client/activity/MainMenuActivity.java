package com.lis.qr_client.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

@Log
public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private TextView tvDialogChange;
    private ProgressBar pbInventory;

    private final int REQUEST_SCAN_QR = 1;
    private static final int DIALOG_EXIT = -1;
    private static final int DIALOG_SCANNED_CODE = 1;

    private HashMap<String, Object> scannedMap;

    DBHelper dbHelper;
    SQLiteDatabase db;

    private String qr_hidden_key = "hidden";
    private Object qr_hidden_value;

    static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        tvDialogChange = findViewById(R.id.tvDialogChange);

        btnFormulyar = findViewById(R.id.btnFormulyar);
        btnFormulyar.setOnClickListener(this);
        btnInventory = findViewById(R.id.btnInventory);
        btnInventory.setOnClickListener(this);
        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(this);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnScanQR.setOnClickListener(this);

        pbInventory = findViewById(R.id.pbInventory);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        //TODO:remove on production
        /*delete all data in address*/
        db.beginTransaction();
        try {
            db.delete("address", null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                showDialog(DIALOG_SCANNED_CODE);
            }
        };


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFormulyar: {

            }
            break;
            case R.id.btnInventory: {
                new AsyncInventory().execute();
                Intent intent = new Intent(this, InventoryParamSelectActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btnProfile: {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        handler.sendEmptyMessage(1);

                    }
                }).start();
            }
            break;

            /*call CameraActivity for scanning*/
            case R.id.btnScanQR: {
                Intent intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QR);
            }
            break;

        }
    }


    /*
    If scan was successful, returned alertDialog with parsed data
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SCAN_QR) {

                final String scan_result = data.getStringExtra("scan_result");
                //TODO: after data returns the app sometimes just done or crush for unknown reason; could be thread issue
                /*show alertDialog with scanned data*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       /* converts gotten data from json to map*/
                        scannedMap = scannedJsonToMap(scan_result);
                        handler.sendEmptyMessage(1);
                    }

                }).start();

                /*All in main thread version*/
             /*   scannedMap = scannedJsonToMap(scan_result);
                showDialog(DIALOG_SCANNED_CODE);*/
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Great mission was canceled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        showDialog(DIALOG_EXIT);
    }


    //--------------------Dialog-----------------------//
    @Override
    protected Dialog onCreateDialog(int id) {
        log.info("-----OnCreateDialog-----");

        switch (id) {

            /*shows scanned data in dialog*/
            case (DIALOG_SCANNED_CODE): {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);

                adb.setTitle("Scan result");

                /*Convert map to a plain msg*/
                String scanned_msg = scannedMapToMsg(scannedMap);
                adb.setMessage(scanned_msg);

                adb.setPositiveButton("Ok", dialogListener);

                return adb.create();
            }
            case (DIALOG_EXIT): {
                Toast.makeText(getApplicationContext(), "Farewell, my beloved friend!", Toast.LENGTH_LONG).show();
                finish();
            }
            break;

        }
        return super.onCreateDialog(id);
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        log.info("-----OnPrepareDialog-----");
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case (DIALOG_SCANNED_CODE): {

                /*Renew dialog window with new data*/

                String scanned_msg = scannedMapToMsg(scannedMap);
                ((AlertDialog) dialog).setMessage(scanned_msg);
            }
        }
    }


    /*
        defines alertDialog answer buttons
    */
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE: {

                }
                break;
                case Dialog.BUTTON_NEGATIVE: {
                    Toast.makeText(getApplicationContext(), "NO", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    };

    //----------------------------------------//




    /*
    Input - string with json after qr code scanning,
     output - parsed to HashMap<String, Object> json
    */

    public HashMap<String, Object> scannedJsonToMap(String scan_result) {
        ObjectMapper mapper = new ObjectMapper();

        HashMap<String, Object> jsonMap = new HashMap<>();
        try {
            jsonMap = mapper.readValue(scan_result, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonMap;
    }


    /*
     Parsing scannedMap, hidden value saves in the global var,
     other data build into a plain string
     */

    public String scannedMapToMsg(HashMap<String, Object> scannedMap) {
        StringBuilder message = new StringBuilder();

        if (scannedMap != null) {
            for (Map.Entry<String, Object> map : scannedMap.entrySet()) {

                /*saves hidden key value, for extended db search*/

                if (map.getKey().equals(qr_hidden_key)) {
                    qr_hidden_value = map.getValue();
                    message.append("------hidden---" + qr_hidden_value + "-------");

                    break;
                }

                message.append(map.getKey() + " : " + map.getValue() + "\n");
            }
            return message.toString();

        }
        return null;
    }


    /*Gets city, street, number from api;
     parses;
     puts to the sqlite*/
    class AsyncInventory extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnInventory.setVisibility(View.INVISIBLE);
            pbInventory.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Loading adresses...", LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            log.info("----Do in background----");

            /*request to the main db to get multiple maps with "city, street, numbers", which are available*/
            String url = "http://10.0.3.2:8090/addresses/only_address";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String table_name = "address";

            /*connect to the url and put the result in sqlite table*/
            try {
                saveDataFromGetUrl(restTemplate, url, table_name);

            } catch (ResourceAccessException e) {
                log.warning("Failed to connect to " + url);
                log.warning("Failure cause: " + e.getMessage() + "\n" + e.getStackTrace().toString());
            }

//log track
            /*show me what u have*/
            Cursor cursor = db.query("address", null, null, null, null, null, null, null);
            dbHelper.logCursor(cursor, "Address");
//------
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pbInventory.setVisibility(View.INVISIBLE);
            btnInventory.setVisibility(View.VISIBLE);
        }
    }


    //-----Additional methods-------//

    /*connects to the given url and put the result in sqlite table*/
    protected void saveDataFromGetUrl(RestTemplate restTemplate, String url, String table_name) throws ResourceAccessException {
        log.info("----Getting data----");

        ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

            /*parsing*/
        List<Map<String, Object>> mapList = responseEntity.getBody();

            /*put into the given table (internal db)*/
        putMapListIntoTheTable(mapList, table_name);
    }


    /*Puts list of maps into the table (internal db)*/
    protected void putMapListIntoTheTable(List<Map<String, Object>> mapList, String table_name) {
        ContentValues cv;

        log.info("----Putting to sql----");

        /*transaction for safe operation*/
        db.beginTransaction();
        try {
            for (Map<String, Object> address : mapList) {

                /*converts map to the context value*/
                cv = mapToContextValueParser(address);

                /*insert to the internal db*/
                long insert_res = db.insert(table_name, null, cv);
//log track
                log.info("----loaded----: " + insert_res);

            }

            db.setTransactionSuccessful();
            log.info("----all is ok----");
        } finally {
            db.endTransaction();
            log.info("----End----");
        }

    }

    /*Converts map to a ContextValue obj*/
    private ContentValues mapToContextValueParser(Map<String, Object> mapToParse) {
        ContentValues cv = new ContentValues();
        log.info("-----Making cv---");

        for (Map.Entry<String, Object> map : mapToParse.entrySet()) {
            cv.put(map.getKey(), map.getValue().toString());
        }

        return cv;
    }
}

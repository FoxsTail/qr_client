package com.lis.qr_client.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log
public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private TextView tvDialogChange;

    private final int REQUEST_SCAN_QR = 1;
    private static final int DIALOG_EXIT = -1;
    private static final int DIALOG_SCANNED_CODE = 1;

    private HashMap<String, Object> scannedMap;

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
}

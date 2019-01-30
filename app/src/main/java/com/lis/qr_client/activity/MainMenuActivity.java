package com.lis.qr_client.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import com.badoo.mobile.util.WeakHandler;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.async_helpers.AsyncMultiDbManager;
import com.lis.qr_client.extra.dialog_fragment.ScanDialogFragment;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

@Log
public class MainMenuActivity extends BaseActivity {

    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private ProgressBar pbInventory;

    private final int REQUEST_SCAN_QR = 1;


    protected HashMap<String, Object> scannedMap;

    private WeakHandler dialogHandler;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);

        log.info("Main menu --- onCreate()");

        setContentView(R.layout.activity_main_menu);

        int id_user = PreferenceUtility.getIntegerDataFromPreferences(this, MyPreferences.PREFERENCE_ID_USER);
        log.info(String.valueOf(id_user));

      /*
        TODO: set dim only for the main window, exclude toolbar
        idea: get toolbar heidht, get window, set window dim\foreground-toolbar-height
         */

        //---get framing layout for dimming

        final FrameLayout frameLayout = findViewById(R.id.frame_main_layout);
        if (frameLayout != null) {
            frameLayout.getForeground().setAlpha(0);
        }
        //---set toolbar

        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar, getResources().getString(R.string.main_menu),
                    frameLayout, false);
        }

        //--------


        url = "http://" + getString(R.string.emu_ip) + ":"
                + getString(R.string.port) + getString(R.string.api_addresses_load);

        btnFormulyar = findViewById(R.id.btnFormulyar);
        btnFormulyar.setOnClickListener(onClickListener);
        btnInventory = findViewById(R.id.btnInventory);
        btnInventory.setOnClickListener(onClickListener);
        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(onClickListener);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnScanQR.setOnClickListener(onClickListener);

        pbInventory = findViewById(R.id.pbInventory);

        dialogHandler = new WeakHandler(callback);

    }

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            ScanDialogFragment dialogFragment = new ScanDialogFragment();
            Bundle bundle = new Bundle();

            String scanned_msg = ObjectUtility.scannedMapToMsg(scannedMap);

            log.info("Scanned msg: " + scanned_msg);

            dialogFragment.callDialog(getFragmentManager(), bundle, scanned_msg,
                    getString(R.string.inventory_scan_result), "qr_scan");
            return false;
        }
    };


    //---------------------------//

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnFormulyar: {

                    Intent intent = new Intent(QrApplication.getInstance(), TestActivity.class);
                    startActivity(intent);
                }
                break;
                case R.id.btnInventory: {

                    log.info("---Remove old saved data---");

                  /*clear old saved data*/
                    PreferenceUtility.removeOldPreferences(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                            MyPreferences.ADDRESS_ID_PREFERENCES,
                            MyPreferences.ROOM_ID_PREFERENCES);

                /*load all available strings from ext db, starts new Db*/
                    if (url != null) {

                        String table_name = DbTables.TABLE_ADDRESS;
                        new AsyncMultiDbManager(QrApplication.getInstance(), table_name, null, url, true,
                                InventoryParamSelectActivity.class, new int[]{Intent.FLAG_ACTIVITY_NEW_TASK}, null,
                                btnInventory, pbInventory, true).runAsyncLoader();
                    } else {
                        log.warning("---URL IS NULL!---");

                    }
                }
                break;
                case R.id.btnProfile: {
                    Intent intent = new Intent(QrApplication.getInstance(), ProfileActivity.class);
                    startActivity(intent);
                }
                break;

            /*call CameraActivity for scanning*/
                case R.id.btnScanQR: {
                    Intent intent = new Intent(QrApplication.getInstance(), CameraActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QR);
                }
                break;

            }
        }
    };


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

    //----------------------------------------//


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        log.info("---MainMenu onBackPressed()---");
/*
        finish();
*/
    }

    @Override
    protected void onStop() {
        log.info("---MainMenu -- onStop()---");
        super.onStop();


        dialogHandler.removeCallbacksAndMessages(null);


            /*set null buttons and pb*/
        btnScanQR = null;
        btnProfile = null;
        btnInventory = null;
        btnFormulyar = null;

        pbInventory = null;

            /*other ref's*/
        onClickListener = null;
        callback = null;

        toolbar = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---MainMenu -- onDestroy()---");
    }


}


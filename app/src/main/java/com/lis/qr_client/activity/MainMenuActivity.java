package com.lis.qr_client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.*;
import com.badoo.mobile.util.WeakHandler;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.extra.async_helpers.AsyncMultiDbManager;
import com.lis.qr_client.extra.dialog_fragment.ScanDialogFragment;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

import java.util.HashMap;

@Log
public class MainMenuActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private ProgressBar pbInventory;

    private final int REQUEST_SCAN_QR = 1;


    protected HashMap<String, Object> scannedMap;

    private WeakHandler dialogHandler;

    private String url;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);

        log.info("Main menu --- onCreate()");

        setContentView(R.layout.activity_main_menu);

        int id_user = PreferenceUtility.getIntegerDataFromPreferences(this, MyPreferences.PREFERENCE_ID_USER);
        log.info(String.valueOf(id_user));

        /*set toolbar icons and actions*/

        ImageView img_info = findViewById(R.id.img_btn_info);
        img_info.setOnClickListener(onClickListener);

        ImageView img_log_out = findViewById(R.id.img_btn_log_out);
        img_log_out.setOnClickListener(onClickListener);


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

        dialogHandler = new WeakHandler(showDialogHandler);

    }


    Handler.Callback showDialogHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            ScanDialogFragment dialogFragment = new ScanDialogFragment();
            Bundle bundle = new Bundle();

            String scanned_msg = ObjectUtility.scannedMapToMsg(scannedMap);

            log.info("Scanned msg: " + scanned_msg);

            dialogFragment.callDialog(getSupportFragmentManager(), bundle, scanned_msg,
                    getString(R.string.result_title), R.drawable.ic_check_circle,"qr_scan");
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
                    break;
                }
                case R.id.btnInventory: {

                    log.info("---Remove old saved data---");

                  /*clear old saved data*/
                    PreferenceUtility.removeOldPreferences(QrApplication.getInstance(), MyPreferences.PREFERENCE_FILE_NAME,
                            MyPreferences.ADDRESS_ID_PREFERENCES,
                            MyPreferences.ROOM_ID_PREFERENCES);

                /*load all available strings from ext db, starts new Db*/
                    if (url != null) {

                        String table_name = DbTables.TABLE_ADDRESS;
                        log.info("url is " + url);
                        new AsyncMultiDbManager(QrApplication.getInstance(), table_name, null, url,
                                true, InventoryParamSelectActivity.class,
                                new int[]{Intent.FLAG_ACTIVITY_NEW_TASK}, null,
                                btnInventory, pbInventory, true).runAsyncLoader();
                    } else {
                        log.warning("---URL IS NULL!---");

                    }
                    break;
                }
                case R.id.btnProfile: {
                    Intent intent = new Intent(QrApplication.getInstance(), ProfileActivity.class);
                    startActivity(intent);
                    break;
                }

            /*call CameraActivity for scanning*/
                case R.id.btnScanQR: {
                    Intent intent = new Intent(QrApplication.getInstance(), CameraActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QR);
                    break;
                }

                case R.id.img_btn_info: {
                    log.info("Getting the information...");
                    ScanDialogFragment dialogFragment = new ScanDialogFragment();
                    dialogFragment.callDialog(getSupportFragmentManager(), new Bundle(),
                                    getString(R.string.plain_info), getString(R.string.info), R.drawable.ic_info,
                            "info");
                    break;
                }
                case R.id.img_btn_log_out: {
                    log.info("Logging out...");
                    logout();
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

/*
                show alertDialog with scanned data
*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
/*
                        converts gotten data from json to map
*/
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
        showDialogHandler = null;

        toolbar = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---MainMenu -- onDestroy()---");
        activity = null;

    }

    //---Methods----

    public void logout() {

        /*remove saved user's data*/
        PreferenceUtility.removeLoginPreferences(QrApplication.getInstance(), MyPreferences.PREFERENCE_SAVE_USER,
                MyPreferences.PREFERENCE_IS_USER_SAVED);

        /*clear app's task and run login page*/
        Intent intent = new Intent(QrApplication.getInstance(), LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }


}


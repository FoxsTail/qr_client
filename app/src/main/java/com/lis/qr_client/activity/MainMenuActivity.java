package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.async_helpers.AsyncMultiDbManager;
import com.lis.qr_client.utilities.dialog_fragment.ScanDialogFragment;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

@Log
public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private TextView tvDialogChange;
    private ProgressBar pbInventory;
    private Toolbar toolbar;

    protected final int REQUEST_SCAN_QR = 1;
    public static final String PREFERENCE_FILE_NAME = "qr_preferences";


    protected HashMap<String, Object> scannedMap;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private String qr_hidden_key = "hidden";
    private Object qr_hidden_value;

    static Handler dialogHandler;
    private Context context = this;


    private String table_name = "address";
    private String url;

    private Utility utility = new Utility();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

      /*
        TODO: set dim only for the main window, exclude toolbar
        idea: get toolbar heidht, get window, set window dim\foreground-toolbar-height
         */

        //---get framing layout for dimming

        final FrameLayout frameLayout = findViewById(R.id.frame_main_layout);
        if(frameLayout != null) {
            frameLayout.getForeground().setAlpha(0);
        }
        //---set toolbar

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.main_menu);
            setSupportActionBar(toolbar);

            utility.toolbarSetter(getSupportActionBar(), frameLayout, false);
        }
        //--------


        url ="http://" + getString(R.string.emu_ip) + ":"
                + getString(R.string.port) + getString(R.string.api_addresses_load);

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

        dialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                ScanDialogFragment dialogFragment = new ScanDialogFragment();
                Bundle bundle = new Bundle();

                String scanned_msg = scannedMapToMsg(scannedMap);

                log.info("Scanned msg: "+ scanned_msg);

                dialogFragment.callDialog(context, bundle, scanned_msg, "qr_scan");
            }
        };


    }


    //-----------Menu------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.qr_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                log.info("---onMenuItemClick---");
                return onOptionsItemSelected(menuItem);
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_log_out:{

                /*clean user's shared preferences (or all preferences?)*/
                utility.removeLoginPrefernces(this, LogInActivity.PREFERENCE_SAVE_USER,
                        LogInActivity.PREFERENCE_IS_USER_SAVED);

                /*launch welcome page*/
                Intent intent = new Intent(this, WelcomeActivity.class);
                startActivity(intent);
                }
            }
        return super.onOptionsItemSelected(item);

    }


    //---------------------------//

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFormulyar: {

                Intent intent = new Intent(this, null);
                startActivity(intent);
            }
            break;
            case R.id.btnInventory: {

                log.info("---Remove old saved data---");

                  /*clear old saved data*/
                utility.removeOldPreferences(context,PREFERENCE_FILE_NAME,
                        InventoryParamSelectActivity.ADDRESS_ID_PREFERENCES,
                        InventoryParamSelectActivity.ROOM_ID_PREFERENCES);

                /*load all available strings from ext db, starts new Db*/
                if (url != null) {
                    new AsyncMultiDbManager(table_name, url, this, dbHelper, db, btnInventory, pbInventory,
                            InventoryParamSelectActivity.class, true, true, null)
                            .runAsyncMapListLoader();
                } else {
                    log.warning("---URL IS NULL!---");

                }
            }
            break;
            case R.id.btnProfile: {
                ScanDialogFragment dialogFragment = new ScanDialogFragment();

                Bundle bundle = new Bundle();
                bundle.putString(ScanDialogFragment.ARG_TITLE, "Scan");
                bundle.putString(ScanDialogFragment.ARG_MESSAGE, "Bla bla balala");
                dialogFragment.setArguments(bundle);

                dialogFragment.show(getFragmentManager(), "Profile");
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

                /*show alertDialog with scanned data*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       /* converts gotten data from json to map*/
                        scannedMap = utility.scannedJsonToMap(scan_result);
                        dialogHandler.sendEmptyMessage(1);
                    }

                }).start();

            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.mission_was_canceled), Toast.LENGTH_LONG).show();
        }
    }

    //----------------------------------------//

    /*
     Parsing scannedMap, hidden value saves in the global var,
     other data build into a plain string
     */
    String scannedMapToMsg(HashMap<String, Object> scannedMap) {
        StringBuilder message = new StringBuilder();

        if (scannedMap != null) {
            for (Map.Entry<String, Object> map : scannedMap.entrySet()) {

                /*saves hidden key value, for extended db search*/

                if (map.getKey().equals(qr_hidden_key)) {
                    qr_hidden_value = map.getValue();
                    message.append("------hidden---").append(qr_hidden_value).append("-------");

                    break;
                }

                message.append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
            }
            return message.toString();

        }
        return null;
    }


}


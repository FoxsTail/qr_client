package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.async_helpers.AsyncDbManager;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.dialog_fragment.ScanDialogFragment;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

@Log
public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnFormulyar, btnInventory, btnProfile, btnScanQR;
    private TextView tvDialogChange;
    private ProgressBar pbInventory;

    protected final int REQUEST_SCAN_QR = 1;
    protected static final int DIALOG_EXIT = -1;
    protected static final int DIALOG_SCANNED_CODE = 1;

    protected HashMap<String, Object> scannedMap;

    DBHelper dbHelper;
    SQLiteDatabase db;

    private String qr_hidden_key = "hidden";
    private Object qr_hidden_value;

    static Handler dialogHandler;
    private Context context = this;


    String table_name = "address";
    String url = "http://10.0.3.2:8090/addresses/only_address";

    private Utility utility = new Utility();


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

        dialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ScanDialogFragment dialogFragment = new ScanDialogFragment();
                Bundle bundle = new Bundle();

                String scanned_msg = scannedMapToMsg(scannedMap);

                dialogFragment.callDialog(context, dialogFragment, bundle, scanned_msg, "qr_scan");
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

                /*load all available strings from ext db, starts new Db*/

                new AsyncDbManager(table_name, url, this, dbHelper, db, btnInventory, pbInventory,
                        InventoryParamSelectActivity.class, true, true, null).runAsyncMapListLoader();

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
            Toast.makeText(this, "Great mission was canceled", Toast.LENGTH_LONG).show();
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


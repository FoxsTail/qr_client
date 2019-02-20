package com.lis.qr_client.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.extra.utility.Utility;
import com.lis.qr_client.pojo.Equipment;
import lombok.extern.java.Log;

@Log
public class EquipmentItemActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private Toolbar toolbar;
/*
    private Context context = this;
*/

    private String inventory_num;
    Equipment equipment;

    private TextView tvName;
    private TextView tvSerialNum;
    private TextView tvInventoryNum;
    private TextView tvAdditional;
    private TextView tvUser;
    private TextView tvAddress;
    private TextView tv_toolbar_title;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);
        log.info("---EquipmentItemActivity- onCreate()--");
        setContentView(R.layout.activity_equipment_item);


        /*get inventory_num from the last activity*/
        Bundle extras = getIntent().getExtras();
        inventory_num = extras.getString("inventory_num");


        //---set toolbar
        toolbar = findViewById(R.id.toolbar);
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title);


        if (toolbar != null) {
            Utility.toolbarSetterWhiteArrow(this, toolbar, "", null, true);
        }

        tvName = findViewById(R.id.tv_name);
        tvSerialNum = findViewById(R.id.tv_serial_num);
        tvInventoryNum = findViewById(R.id.tv_inventory_num);
        tvAdditional = findViewById(R.id.tv_additional);
        tvUser = findViewById(R.id.tv_user);
        tvAddress = findViewById(R.id.tv_address);



        /*set all for db access*/
        dbHelper = QrApplication.getDbHelper();
        db = dbHelper.getWritableDatabase();


        /*run thread with data loading from db*/
        thread = new Thread(runLoadEquipments);
        thread.start();

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            /*set toolbar name*/
            tv_toolbar_title.setText(equipment.getType());


            //--------
            tvName.setText(equipment.getVendor() + " " + equipment.getModel() + " " + equipment.getSeries());

            tvSerialNum.setText(equipment.getSerial_num());
            tvInventoryNum.setText(equipment.getInventory_num());


            /* set attributes*/
            String json_attributes = equipment.getAttributes();
            if (json_attributes != null) {

                String string_additional = ObjectUtility.jsonAttributesToString(json_attributes);
                if (string_additional != null) {
                    tvAdditional.setText(ObjectUtility.jsonAttributesToString(json_attributes));
                } else {
                    tvAdditional.setVisibility(View.GONE);
                }
            } else {
                tvAdditional.setVisibility(View.GONE);
            }

            /*set user*/
            String user_info = equipment.getUser_info();
            if (user_info != null && !user_info.equals("")) {
                tvUser.setText(user_info);
            } else {
                tvUser.setVisibility(View.GONE);

            }


            /*set address*/
            String address = equipment.getAddress();
            if (address != null && !address.equals("")) {
                tvAddress.setText(user_info);
            } else {
                tvAddress.setVisibility(View.GONE);

            }
            tvAddress.setText(equipment.getAddress());


        }
    };

    /**
     * Load equipments from the SQLite
     */
    private Runnable runLoadEquipments = new Runnable() {
        @Override
        public void run() {
            String table_to_select = DbTables.TABLE_EQUIPMENT;


            cursor = db.query(table_to_select, null, "inventory_num=?", new String[]{inventory_num},
                    null, null,
                    null);

            equipment = DbUtility.cursorToEquipment(cursor);

            cursor.close();
//--logs---
            if (equipment != null) {
                log.info("--Loaded equipment: " + equipment.toString() + " --");
            } else {
                log.info("--Loaded equipment is null--");
            }
//---------

            /*put equipment to ui*/
            handler.sendEmptyMessage(0);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Equipment Item -- onDestroy()---");
        handler.removeCallbacksAndMessages(null);

        if (thread != null) {
            thread = null;
        }
        handler = null;
        runLoadEquipments = null;

        dbHelper = null;
        db = null;
        cursor = null;

        toolbar = null;
        tvName = null;
        tvSerialNum = null;
        tvInventoryNum = null;
        tvAdditional = null;
        tvUser = null;
        tvAddress = null;
        tv_toolbar_title = null;


    }
}


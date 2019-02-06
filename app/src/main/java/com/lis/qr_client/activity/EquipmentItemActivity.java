package com.lis.qr_client.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

@Log
public class EquipmentItemActivity extends BaseActivity {
    private CollapsingToolbarLayout collapsingbar;


    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
/*
    private Context context = this;
*/

    private String inventory_num;
    Equipment equipment;

    private TextView info;
    private TextView tvAdditional;
    private TextView tvUser;
    private TextView tvAddress;
    private ImageView imageElement;

    //TODO: while loading make it circle


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

        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar, "", null, true);

        }


        /*get collapse bar, set title*/
        collapsingbar = findViewById(R.id.collBar_element);

        imageElement = findViewById(R.id.image_element);

        info = findViewById(R.id.tv_info);
        tvAdditional = findViewById(R.id.tv_additional);
        tvUser = findViewById(R.id.tv_user);
        tvAddress = findViewById(R.id.tv_address);

        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_element_layout);


        /*set all for db access*/
        dbHelper = QrApplication.getDbHelper();
        db = dbHelper.getWritableDatabase();


        /*run thread with data loading from db*/
        Thread thread = new Thread(runLoadEquipments);
        thread.start();

    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            /*stop waiting circle*/
            collapsingbar.setTitle(equipment.getType());
            imageElement.setImageResource(R.drawable.pic_videocard);

            //---style----
            //TODO: make it bold in th easy way
            SpannableStringBuilder span;
            StyleSpan bold = new StyleSpan(Typeface.BOLD);
            String label_name = getString(R.string.name)+" ";
            String label_inventory = R.string.inventory_number+": ";
            String label_serial = getString(R.string.serial_number)+" ";
            String label_additional = getString(R.string.additional_params)+" ";
            String label_user = getString(R.string.current_user)+" ";
            String label_tp = getString(R.string.tech_platform)+" ";


            //--------
            info.setText(label_name + equipment.getVendor() + " " + equipment.getModel() + " " + equipment.getSeries() + "\n"
                    + label_inventory + equipment.getInventory_num() + "\n"
                    + label_serial + equipment.getSerial_num());

            if (equipment.getAttributes() != null) {
                tvAdditional.setText(label_additional + equipment.getAttributes());
            }

            tvUser.setText(label_user + equipment.getUser_info());
            tvAddress.setText(label_tp + equipment.getAddress());


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
            ;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Equipment Item -- onDestroy()---");
        handler.removeCallbacksAndMessages(null);
    }
}


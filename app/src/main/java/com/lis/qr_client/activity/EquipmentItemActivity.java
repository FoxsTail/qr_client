package com.lis.qr_client.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.Equipment;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Log
public class EquipmentItemActivity extends AppCompatActivity {
    private CollapsingToolbarLayout collapsingbar;
    private TextView tvElement;


    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Context context = this;

    private String inventory_num;
    Equipment equipment;

    private Utility utility = new Utility();
    private TextView info;
    private TextView tvAdditional;
    private TextView tvUser;
    private TextView tvAddress;
    private ImageView imageElement;

    //TODO: while loading make it circle


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //----Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        log.info("---EquipmentItemActivity---");
        setContentView(R.layout.activity_equipment_item);


        /*get inventory_num from the last activity*/
        Bundle extras = getIntent().getExtras();
        inventory_num = extras.getString("inventory_num");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_element));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        /*get collapse bar, set title*/
        collapsingbar = findViewById(R.id.collBar_element);

        imageElement = findViewById(R.id.image_element);

        info = findViewById(R.id.tv_info);
        tvAdditional = findViewById(R.id.tv_additional);
        tvUser = findViewById(R.id.tv_user);
        tvAddress = findViewById(R.id.tv_address);

        /*set all for db access*/
        dbHelper = new DBHelper(this);
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
            imageElement.setImageResource(R.drawable.videocard);

            //---style----
            //TODO: make it bold in th easy way
            SpannableStringBuilder span;
            StyleSpan bold = new StyleSpan(Typeface.BOLD);
            String label_name = context.getResources().getString(R.string.name)+" ";
            String label_inventory = R.string.inventory_number+": ";
            String label_serial = getString(R.string.serial_number)+" ";
            String label_additional = getString(R.string.additional_params)+" ";
            String label_user = getString(R.string.current_user)+" ";
            String label_tp = getString(R.string.tech_platform)+" ";

            /*String[] labels = new String[]{label_name, label_additional, label_inventory, label_serial, label_serial,
                    label_tp, label_user};


            for (int i = 0; i < labels.length-1; i++) {
                span = new SpannableStringBuilder(labels[i]);
                span.setSpan(bold, 0, labels[i].length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                labels[i] = span.toString();
            }
            span = new SpannableStringBuilder(label_name);
            span.setSpan(bold, 0, label_name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            label_name = span.toString();*/


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
            String table_to_select = "equipment";


            cursor = db.query(table_to_select, null, "inventory_num=?", new String[]{inventory_num},
                    null, null,
                    null);

            equipment = Utility.cursorToEquipment(cursor);

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


}


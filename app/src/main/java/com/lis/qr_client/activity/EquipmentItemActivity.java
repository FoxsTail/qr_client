package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class EquipmentItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.info("---hello---");
        setContentView(R.layout.activity_equipment_item);
    }
}

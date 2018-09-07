package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.lis.qr_client.R;

public class InventoryParamSelectActivity extends AppCompatActivity {
    private Spinner spinSelectAdr, spinSelectRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_param_select);


    }
}

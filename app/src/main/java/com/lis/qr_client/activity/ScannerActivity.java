package com.lis.qr_client.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.qr_resolver.AsyncQRGenerator;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvScanFormat;
    TextView tvScanContent;
    Button btnScan, btnCreateQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        tvScanFormat = findViewById(R.id.tvScanFormat);
        tvScanContent = findViewById(R.id.tvScanContent);

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnCreateQr = findViewById(R.id.btnCreateQr);
        btnCreateQr.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateQr: {
                //TODO:here i can put anonymous class, im sure, not much, but sure:)
                /*Intent intent = new Intent(this, new AppCompatActivity(){
                    @Override
                    protected void onCreate(@Nullable Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);

                    }
                });
                startActivity(intent);*/

            }
            break;
        }
    }
}

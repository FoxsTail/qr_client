package com.lis.qr_client.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.qr_resolver.AsyncQRGenerator;
import net.sourceforge.zbar.*;

import java.io.File;

@lombok.extern.java.Log
public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {

    /*for Zbar*/
    static {
        System.loadLibrary("iconv");
    }

    final int TYPE_PHOTO = 1;
    final int REQUEST_SCAN_QR = 1;

    TextView tvScanFormat;
    TextView tvScanContent;
    Button btnScan, btnCreateQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        tvScanFormat = findViewById(R.id.tvScanFormat);
        tvScanContent = findViewById(R.id.tvScanContent);

        btnScan = findViewById(R.id.btnScanQr);
        btnScan.setOnClickListener(this);
        btnCreateQr = findViewById(R.id.btnCreateQr);
        btnCreateQr.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateQr: {

                Intent intent = new Intent(this, QrCreationActivity.class);
                startActivity(intent);

            }
            break;

            case R.id.btnScanQr: {
                Intent intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QR);
            }
            break;
        }
    }


    //TODO: Replace toast with small scan info window
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null) {
            log.info("intent is null");
            return;
        }

        log.info("-----------on result-------");

        if (resultCode == RESULT_OK) {
            if(requestCode == REQUEST_SCAN_QR) {

                String result = intent.getStringExtra("scan_result");

                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == RESULT_CANCELED) {

            Toast.makeText(this, "Great mission was canceled", Toast.LENGTH_LONG).show();
        }


    }

    //class for AsyncQRGenerator calling. We had to use intent. Otherwise
    // (with new AppCompactActivity) it throws null pointer during the attempt to get a context.

    public static class QrCreationActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String data = "Hello, Boss! Do u like my qr_code?";
            new AsyncQRGenerator(this).execute(data);
        }
    }
}

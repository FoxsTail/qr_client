package com.lis.qr_client.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.lis.qr_client.R;
import com.lis.qr_client.qr_resolver.AsyncQRGenerator;

import java.util.concurrent.TimeUnit;

public class QrCreationActivity extends AppCompatActivity {
    ProgressBar pb;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncQRGenerator(this, pb).execute();

    }

}

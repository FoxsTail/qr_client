package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.data.DBHelper;

public class TestActivity extends BaseActivity {
   // public DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //1dbHelper = QrApplication.getDbHelper();


    }
}

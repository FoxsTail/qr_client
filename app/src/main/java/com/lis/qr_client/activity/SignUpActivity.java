package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import com.lis.qr_client.R;
import com.lis.qr_client.extra.utility.Utility;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //---set toolbar

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.sign_up);
            setSupportActionBar(toolbar);

            Utility.toolbarSetter(getSupportActionBar(), null, true);
        }
        //--------
    }
}

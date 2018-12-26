package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.Utility;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar toolbar;

    Utility utility = new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //---set toolbar

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.sign_up);
            setSupportActionBar(toolbar);

            utility.toolbarSetter(getSupportActionBar(), null, true);
        }
        //--------
    }
}

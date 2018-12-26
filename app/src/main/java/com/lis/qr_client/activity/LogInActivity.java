package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;

@Log
public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private EditText et_email, et_password;
    private Button btn_log_in_the_app;

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
        setContentView(R.layout.activity_log_in);

        //---set toolbar

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.log_in);
            setSupportActionBar(toolbar);

            utility.toolbarSetter(getSupportActionBar(), null, true);
        }

        //----set views
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        btn_log_in_the_app = findViewById(R.id.btn_log_in_the_app);
        btn_log_in_the_app.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log_in_the_app: {
                log.info("---Trying to log in the system---");

                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                /*check if fields are not null*/
                if (email.equals("")) {
                    Toast.makeText(this, getString(R.string.toast_enter_email), Toast.LENGTH_SHORT).show();
                } else if (password.equals("")) {
                    Toast.makeText(this, getString(R.string.toast_enter_password), Toast.LENGTH_SHORT).show();

                } else {

                /*email and password validation*/

                /*prepare data*/
                    Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show();

                /*make a request*/

                }
                break;
            }
        }
    }
}
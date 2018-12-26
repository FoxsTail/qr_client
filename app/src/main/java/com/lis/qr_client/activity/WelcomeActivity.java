package com.lis.qr_client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

@Log
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_log_in, btn_sign_up;
    private ProgressBar pb_welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btn_log_in = findViewById(R.id.btn_log_in);
        btn_log_in.setOnClickListener(this);
        btn_sign_up = findViewById(R.id.btn_sign_up);
        btn_sign_up.setOnClickListener(this);

        pb_welcome = findViewById(R.id.pb_welcome);


        //TODO: add toolbar

    }

    @Override
    protected void onResume() {
        log.info("---Welcome OnResume---");
        super.onResume();
        hideLoading();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log_in: {
                log.info("---Button log in pressed---");

                showLoading();

                /*start log in page*/
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);

                break;
            }
            case R.id.btn_sign_up: {
                log.info("---Button sign up pressed---");

                showLoading();

                /*start sign up page*/
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);

                break;
            }
        }
    }

    /**
     * hide buttons, show progress bar
     */

    public void showLoading() {
        btn_log_in.setVisibility(View.INVISIBLE);
        btn_sign_up.setVisibility(View.INVISIBLE);
        pb_welcome.setVisibility(View.VISIBLE);
    }

    /**
     * show buttons, hide progress bar
     */
    public void hideLoading(){
        btn_log_in.setVisibility(View.VISIBLE);
        btn_sign_up.setVisibility(View.VISIBLE);
        pb_welcome.setVisibility(View.INVISIBLE);
    }
}

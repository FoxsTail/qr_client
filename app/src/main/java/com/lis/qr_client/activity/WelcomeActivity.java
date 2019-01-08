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
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

@Log
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_log_in, btn_sign_up;
    private ProgressBar pb_welcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);

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

        checkSavedUser();

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
     * If there any saved user's email and password -> go straight to the MainMenu
     **/

    public void checkSavedUser() {
        log.info("checkSavedUser");

        Boolean saved_user = PreferenceUtility.getBooleanDataFromPreferences
                (this, MyPreferences.PREFERENCE_IS_USER_SAVED);
        if (saved_user != null && saved_user) {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
        }else {
            log.info("Nothing was saved");
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
    public void hideLoading() {
        btn_log_in.setVisibility(View.VISIBLE);
        btn_sign_up.setVisibility(View.VISIBLE);
        pb_welcome.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        log.info("---Welcome -- onDestroy()---");
        super.onDestroy();
    }
}

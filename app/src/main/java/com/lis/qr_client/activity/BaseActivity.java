package com.lis.qr_client.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.lis.qr_client.R;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import lombok.extern.java.Log;

@Log
public class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    //-----------Menu------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(toolbar != null) {
            toolbar.inflateMenu(R.menu.qr_menu);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    log.info("---onMenuItemClick---");
                    return onOptionsItemSelected(menuItem);
                }
            });
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_log_out: {

                /*clean user's shared preferences (or all preferences?)*/
                PreferenceUtility.removeLoginPreferences(this, MyPreferences.PREFERENCE_SAVE_USER,
                        MyPreferences.PREFERENCE_IS_USER_SAVED);

                /*launch welcome page*/
                Intent intent = new Intent(this, WelcomeActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);

    }

}

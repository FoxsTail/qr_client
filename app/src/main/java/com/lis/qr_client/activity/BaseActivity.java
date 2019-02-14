package com.lis.qr_client.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.lis.qr_client.R;
import com.lis.qr_client.extra.utility.Utility;
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
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.params_menu);
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
            case R.id.menu_send_file: {
                log.info("Sending to server page...");
                break;
            }
            case R.id.menu_restore_page: {
                log.info("Restoring page...");
                break;
            }
        }
        return super.onOptionsItemSelected(item);

    }


}

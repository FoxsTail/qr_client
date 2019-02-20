package com.lis.qr_client.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.lis.qr_client.R;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Log
public class TestActivity extends BaseActivity {
    // public DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        toolbar = findViewById(R.id.toolbar);
        TextView tv_toolbar_title = findViewById(R.id.tv_toolbar_title);

        if (toolbar != null) {
            tv_toolbar_title.setText(getString(R.string.select_room));
            Utility.toolbarSetterDarkArrow(this, toolbar, "",
                    null, true);
        }


        //1dbHelper = QrApplication.getDbHelper();

        // Get reference of widgets from XML layout
        final Spinner spinner = (Spinner) findViewById(R.id.spinAddress);

        // Initializing a String Array
        String[] plants = new String[]{
                " ",
                "Laceflower",
                "California sycamore",
                "Mountain mahogany",
                "Butterfly weed",
                "Carrot weed"
        };

        final List<String> plantsList = new ArrayList<>(Arrays.asList(plants));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item, plantsList);

        /*
            public void setPrompt (CharSequence prompt)
                Sets the prompt to display when the dialog is shown.
         */
        spinner.setPrompt("Select an item");

        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.params_menu, menu);

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


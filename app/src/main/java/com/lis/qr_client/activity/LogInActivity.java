package com.lis.qr_client.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.utilities.Utility;
import com.lis.qr_client.utilities.async_helpers.AsyncOneDbManager;
import lombok.extern.java.Log;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Log
public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PREFERENCE_SAVE_USER = "save_user";
    public static final String PREFERENCE_IS_USER_SAVED= "is_user_saved";

    private Toolbar toolbar;
    private EditText et_email, et_password;
    private Button btn_log_in_the_app;

    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

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

        //----set db
        dbHelper = new DBHelper(this);
        sqLiteDatabase = dbHelper.getWritableDatabase();


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

                    String table_name = "user";

                //TODO: add is first logging
                /*check in sqlite*/
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.query(table_name, null, "email=?", new String[]{email},
                            null, null, null, null);

                    if (!cursor.moveToFirst() || cursor.getCount() == 0) {

                        log.info("Cursor is null");
                        /*if nothing found look at server*/
                        checkLoginAtServer(table_name, email, password);

                    } else {

                        log.info("Cursor is ok");
                        User user = Utility.cursorToUser(cursor);
                            if (user != null && user.getEmail().equals(email) && user.getPassword().equals(password)) {
                                log.info("User is ok");
                                log.info("From SQLite");
                                /*ok, load new page*/
                                Intent intent = new Intent(this, MainMenuActivity.class);
                                startActivity(intent);

                            } else {
                                log.info("User is null or data is incorrect, checking at the server");
                                 /*if nothing found look at server*/
                                checkLoginAtServer(table_name, email, password);
                            }
                        }
                    }

                }
                break;
            }
        }


    //----Methods----
    public void checkLoginAtServer(String table_name, String email, String password) {
        /*prepare data*/
        Resources resources = this.getResources();
        String url = "http://" + resources.getString(R.string.emu_ip) + ":" + resources.getString(R.string.port) +
                resources.getString(R.string.api_login_check);

                    /*make a request*/
        AsyncOneDbManager oneDbManager = new AsyncOneDbManager(true, table_name, url,
                null, this, dbHelper, MainMenuActivity.class, HttpMethod.POST,
                new User(email, password));
        oneDbManager.runAsyncOneDbManager();
    }
}
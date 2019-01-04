package com.lis.qr_client.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.async_helpers.AsyncOneDbManager;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.springframework.http.HttpMethod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PREFERENCE_SAVE_USER = "save_user";
    public static final String PREFERENCE_ID_USER = "id_user";
    public static final String PREFERENCE_IS_USER_SAVED = "is_user_saved";


    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    private Toolbar toolbar;
    private Button btn_log_in_the_app;

    DBHelper dbHelper;

    private TextInputLayout email_wrapper;
    private TextInputLayout password_wrapper;

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


        //---set toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.log_in);
            setSupportActionBar(toolbar);

            Utility.toolbarSetter(getSupportActionBar(), null, true);
        }

        //----set views
        email_wrapper = findViewById(R.id.email_wrapper);
        password_wrapper = findViewById(R.id.password_wrapper);


        btn_log_in_the_app = findViewById(R.id.btn_log_in_the_app);
        btn_log_in_the_app.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log_in_the_app: {

                log.info("---Trying to log in the system---");



                /*email password validation*/

                String email = email_wrapper.getEditText().getText().toString();
                String password = password_wrapper.getEditText().getText().toString();

                if (!validateEmail(email)) {
                    password_wrapper.setErrorEnabled(false);
                    email_wrapper.setError(getString(R.string.error_validation_email));
                } else if (!validatePassword(password)) {
                    email_wrapper.setErrorEnabled(false);
                    password_wrapper.setError(getString(R.string.error_validation_password));
                } else {
                    email_wrapper.setErrorEnabled(false);
                    password_wrapper.setErrorEnabled(false);

                    /*ok, check user data*/
                    String table_name = "user";

                     /*check in sqlite*/
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    Cursor cursor = db.rawQuery("select * from user where email=? and password=?",
                            new String[]{email, password});

                    if (!cursor.moveToFirst() || cursor.getCount() == 0) {

                        log.info("Cursor is null");
                        /*if nothing found look at server*/
                        checkLoginAtServer(table_name, email, password);

                    } else {
                        log.info("Cursor is ok");
                        DbUtility.logCursor(cursor, "test");
                                /*ok, load new page*/
                        Intent intent = new Intent(this, MainMenuActivity.class);
                        startActivity(intent);

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
        AsyncOneDbManager oneDbManager = new AsyncOneDbManager(this, table_name, null, url,
                true, MainMenuActivity.class, null, new User(email, password),
                HttpMethod.POST);
        oneDbManager.runAsyncLoader();
    }


    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validatePassword(String password) {
        return password.length() >= 5;
    }
}
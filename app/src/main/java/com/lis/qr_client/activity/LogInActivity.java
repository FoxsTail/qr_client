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
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.async_helpers.AsyncOneDbManager;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.ObjectUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.springframework.http.HttpMethod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    private Toolbar toolbar;
    private Button btn_log_in_the_app;

    private TextInputLayout email_wrapper;
    private TextInputLayout password_wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //----Full screen
        Utility.fullScreen(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //---set toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar, getString(R.string.log_in), null, false);
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
                    String table_name = DbTables.TABLE_USER;


                    //----set db
                    DBHelper dbHelper = QrApplication.getDbHelper();

                     /*check in sqlite*/
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    Cursor cursor = db.rawQuery("select * from user where email=? and password=?",
                            new String[]{email, password});

                    if (!cursor.moveToFirst() || cursor.getCount() == 0) {

                        log.info("Cursor is null");
                        /*if nothing found look at server*/
                        checkLoginAtServer(table_name, email, password);

                    } else {
//----log---
                        log.info("Cursor is ok");
                        DbUtility.logCursor(cursor, "test");
//-----Save user to preferences---
                        User user = (User) DbUtility.cursorToClass(cursor, User.class.getName());

                        if (user != null) {
                            log.info("Saving user to preferences...");
                            PreferenceUtility.saveUsersDataToPreference(user, QrApplication.getInstance(),
                                    MyPreferences.PREFERENCE_SAVE_USER,
                                    MyPreferences.PREFERENCE_ID_USER,
                                    MyPreferences.PREFERENCE_IS_USER_SAVED);
                        } else {
                            log.info("Can't save to preferences. User is null");
                        }
//------
                        cursor.close();
                                /*ok, load new page*/
                        Intent intent = new Intent(QrApplication.getInstance(), MainMenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
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
                true, MainMenuActivity.class, new int[]{Intent.FLAG_ACTIVITY_CLEAR_TASK,
                Intent.FLAG_ACTIVITY_NEW_TASK}, null, new User(email, password),
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        log.info("---Login -- onStop()---");
        Intent intent = new Intent(QrApplication.getInstance(), WelcomeActivity.class);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.info("---Login -- onStop()---");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Login -- onDestroy()---");

        matcher = null;

        toolbar = null;
        btn_log_in_the_app = null;

        email_wrapper = null;
        password_wrapper = null;
    }
}

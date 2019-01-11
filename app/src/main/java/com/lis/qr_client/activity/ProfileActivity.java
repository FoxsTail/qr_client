package com.lis.qr_client.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.badoo.mobile.util.WeakHandler;
import com.lis.qr_client.R;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.DbTables;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.pojo.*;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;

@Log
public class ProfileActivity extends BaseActivity {

    private TextView tv_fio, tv_workplace, tv_private_data, tv_phone_numbers, tv_address;
    private CheckBox is_remote_desktop;

    private int id_user;

    private SQLiteDatabase db;


    //   PreferenceUtility preferenceUtility = new PreferenceUtility();

    private PersonalData personalData;
    private Address address;
    private Workplace workplace;

    private WeakHandler handler = new WeakHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*full screen*/
        Utility.fullScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /*set toolbar*/
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            Utility.toolbarSetter(this, toolbar,
                    getResources().getString(R.string.profile), null, true);
        }


        /*get text views*/
        tv_fio = findViewById(R.id.tv_fio);
        tv_workplace = findViewById(R.id.tv_workplace);
        tv_private_data = findViewById(R.id.tv_private_data);
        tv_phone_numbers = findViewById(R.id.tv_phone_numbers);
        tv_address = findViewById(R.id.tv_address);

        is_remote_desktop = findViewById(R.id.is_remote_desktop);

        /*get user id from preferences*/

        id_user = PreferenceUtility.getIntegerDataFromPreferences(this, MyPreferences.PREFERENCE_ID_USER);

        if (id_user != 0) {
            log.info("User id is " + id_user);

            DBHelper dbHelper = QrApplication.getDbHelper();
            db = dbHelper.getWritableDatabase();

        /*load user data from db*/
            new Thread(runLoadUserData).start();

        } else {
            log.info("Logged user's id is 0. Can't show profile info. Log in and try again");
            Toast.makeText(this, getString(R.string.no_data_about_user), Toast.LENGTH_SHORT).show();
/*finish()?*/
        }
    }


    //---------------------------//


    private Runnable runLoadUserData = new Runnable() {

        @Override
        public void run() {

            /*find user in db by id*/
            Cursor cursor = getFromTableById(DbTables.TABLE_USER, "id=?", id_user);

            User user = (User) DbUtility.cursorToClass(cursor, User.class.getName());

            if (user == null) {
                log.info("User is null. Can't load the user");
/*finish()?*/

            } else {
                /*find his personal data*/
                Integer id_pd = user.getId_pd();

                if (id_pd == null) {
                    log.info("User has no personal data. Can't load the user");


                } else {
                    cursor = getFromTableById(DbTables.TABLE_PERSONAL_DATA, "id=?", id_pd);

                    log.info("Cursor is " + cursor.getCount());

                    personalData = (PersonalData) DbUtility.cursorToClass(cursor, PersonalData.class.getName());
                    if (personalData == null) {
                        log.info("User's personal data is null. Can't load the data");


                    } else {
                        Integer id_tp = personalData.getId_tp();

                        if (id_tp == null) {
                            log.info("Address is null");
                        } else {
                        /*find address*/
                            cursor = getFromTableById(DbTables.TABLE_ADDRESS, "id=?", id_tp);

                            address = (Address) DbUtility.cursorToClass(cursor, Address.class.getName());
                        }


                        Integer id_wp = personalData.getId_wp();

                        /*find workplace*/
                        if (id_wp == null) {
                            log.info("Workplace is null");
                        } else {
                            cursor = getFromTableById(DbTables.TABLE_WORKPLACE, "id=?", id_wp);
                            workplace = (Workplace) DbUtility.cursorToClass(cursor, Workplace.class.getName());
                        }


                        /*get phone numbers*/
                         /*cursor = getFromTableById("phone_number","id_pd=?", id_pd);
*/
                    }

                }

                cursor.close();
               /*set to text view*/
                handler.post(runSetTextViews);
            }

        }
    };

    public Cursor getFromTableById(String table_name, String selection, int selection_arg) {
        return db.query(table_name, null, selection, new String[]{String.valueOf(selection_arg)},
                null, null, null);
    }

    private Runnable runSetTextViews = new Runnable() {
        @Override
        public void run() {
            tv_fio.setText(personalData.getFio());
            tv_private_data.setText(personalData.getPrivateData());
            tv_phone_numbers.setText("(---) --- -- --");

            if (address != null) {
                tv_address.setText(address.getFullAddress());
            } else {
                tv_address.setVisibility(View.GONE);
            }

            if (workplace != null) {
                tv_workplace.setText(workplace.getWorkplace());
                is_remote_desktop.setChecked(workplace.getRemote_workstation());
            } else {
                tv_workplace.setVisibility(View.GONE);
                is_remote_desktop.setVisibility(View.GONE);
            }


        }
    };

    @Override
    protected void onStop() {
        log.info("---ProfileActivity -- onStop()---");
        super.onStop();

            /*set null buttons and pb*/
        tv_fio = null;
        tv_workplace = null;
        tv_private_data = null;
        tv_phone_numbers = null;
        tv_address = null;

        is_remote_desktop = null;

        toolbar = null;

        db = null;

        /*runnables*/
        runSetTextViews = null;
        runLoadUserData = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Profile -- onDestroy()---");
        handler.removeCallbacksAndMessages(null);
    }
}

package com.lis.qr_client.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.pojo.User;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Log
public class SyncActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvName, tvEmail, tvUsersTitle;
    Button btnNext, btnDel, btnTest;

    Cursor cursor;
    int nameColumnIndex;
    int emailColumnIndex;

    SQLiteDatabase db;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvUsersTitle = findViewById(R.id.tvUsersTitle);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        btnDel = findViewById(R.id.btnDel);
        btnDel.setOnClickListener(this);

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        initCursor(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sync: {
                new HttpClient(this).execute();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext: {
                setDataFromCursor(this);
            }
            break;
            case R.id.btnDel: {
                db.delete("user", null, null);
                initCursor(this);
                btnNext.setEnabled(false);
            }
            break;
            case R.id.btnTest: {
                User user = dbHelper.getByUsername("Testing");
                if (user != null) {
                    tvUsersTitle.setText("User found");
                    tvName.setText(user.getUsername());
                    tvEmail.setText(user.getEmail());
                    btnNext.setEnabled(false);
                }else{
                    tvUsersTitle.setText("User not found");

                }
            }
            break;

            /*case R.id.btnAll: {
                cursor = db.query("user", null, null, null, null, null, "username ASC");
                int tableName = cursor.getColumnIndex("username");
                int tableEmail = cursor.getColumnIndex("email");

                if (cursor.moveToFirst()) {
                    do {
                        log.info("--------");
                        log.info("name - " + cursor.getString(tableName));
                        log.info("email - " + cursor.getString(tableEmail));
                        log.info("--------");
                    } while (cursor.moveToNext());
                }

                log.info("----All----");
                Toast.makeText(this, "Total records " + cursor.getCount(), Toast.LENGTH_SHORT).show();
                cursor.close();
            }*/
        }
    }

    //---------------- Cursor --------------------//

    private void initCursor(Context mContext) {
        cursor = db.query("user", new String[]{"username", "email"},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            nameColumnIndex = cursor.getColumnIndex("username");
            emailColumnIndex = cursor.getColumnIndex("email");

            String titleText = "Users " + cursor.getCount();
            tvUsersTitle.setText(titleText);
            tvName.setText(cursor.getString(nameColumnIndex));
            tvEmail.setText(cursor.getString(emailColumnIndex));
            btnNext.setEnabled(true);
        } else {
            Toast.makeText(mContext, "DB is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDataFromCursor(Context mContext) {

        if (cursor.moveToNext() && !cursor.isLast()) {
            //log.info("----" + cursor.getCount() + "----");
            tvName.setText(cursor.getString(nameColumnIndex));
            tvEmail.setText(cursor.getString(emailColumnIndex));
            log.info("Pos is " + cursor.getPosition());
        } else {
            btnNext.setEnabled(false);
            Toast.makeText(mContext, "Last record", Toast.LENGTH_SHORT).show();
        }
    }

    //------------------------------------//


    class HttpClient extends AsyncTask<Void, Integer, Void> {
        private Context mContext;

        public HttpClient(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, "Start synchronization", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentValues cv = new ContentValues();
            String url = "http://10.0.3.2:8090/users/";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            ResponseEntity<List<User>> users = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<User>>() {
                    });

            for (User user : users.getBody()) {
                cv.put("username", user.getUsername());
                cv.put("password", user.getPassword());
                cv.put("email", user.getEmail());
                long i = db.insert("user", null, cv);
                if (i > 0) {
                    log.info("----" + user.toString() + "-- id -- " + i);
                }
            }

            //log.info("----" + user.getBody().toString() + "----");
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initCursor(mContext);
        }
    }
}

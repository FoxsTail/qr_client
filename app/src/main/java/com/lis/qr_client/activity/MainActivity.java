package com.lis.qr_client.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.lis.qr_client.R;
import com.lis.qr_client.data.DBHelper;
import lombok.extern.java.Log;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Log
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "MainActivity";
    EditText etName, etPassword, etEmail;
    Button btnAdd, btnSync, btnAll;

    DBHelper dbHelper;

    SQLiteDatabase db;

    SyncClient syncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        log.info("----onCreate: init fields----");
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnAll = findViewById(R.id.btnAll);
        btnAll.setOnClickListener(this);

        btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(this);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

    }

    @Override
    public void onClick(View view) {

        String name = etName.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();

        Cursor cursor;

        switch (view.getId()) {
            case R.id.btnAdd: {
                if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "All fields should be filled", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etPassword.setText("");
                    etEmail.setText("");
                    log.warning("----Empty field!----");
                    break;
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("username", name);
                    cv.put("password", password);
                    cv.put("email", email);

                    long i = db.insert("user", null, cv);

                    log.info("----User successfully added----" + i);
                    etName.setText("");
                    etPassword.setText("");
                    etEmail.setText("");
                    Toast.makeText(this, "User " + name + " has successfully been added", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.btnAll: {
                cursor = db.query("user", null, null, null, null, null, "username ASC");
                int tableName = cursor.getColumnIndex("username");
                int tablePassword = cursor.getColumnIndex("password");
                int tableEmail = cursor.getColumnIndex("email");

                if (cursor.moveToFirst()) {
                    do {
                        log.info("--------");
                        log.info("name - " + cursor.getString(tableName));
                        log.info("password - " + cursor.getString(tablePassword));
                        log.info("email - " + cursor.getString(tableEmail));
                        log.info("--------");
                    } while (cursor.moveToNext());
                }

                log.info("----All----");
                Toast.makeText(this, "Total records " + cursor.getCount(), Toast.LENGTH_SHORT).show();
                cursor.close();
            }
            break;
            case R.id.btnSync: {

                new SyncClient(this).execute();
                log.info("----Synchronizing----");
            }
            break;

        }


    }

    class SyncClient extends AsyncTask<Void, Void, Void>{
        private ProgressBar progressBar = findViewById(R.id.progressBar);
        private Context mContext;

        public SyncClient(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, "Synchronization", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(mContext, "Done!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

       /* @Override
        protected Void doInBackground(Context... contexts) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }*/

    }

   /* class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "qrdb", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            log.info("----Create Database----");
            sqLiteDatabase.execSQL("create table user (" +
                    "username text primary key, " +
                    "password text," +
                    "email text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }*/
}

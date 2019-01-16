package com.lis.qr_client.extra.async_helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Log
public class AsyncMultiDbManager extends AsyncAbstractManager {

    private Object extra_data;
    private Button btn;
    private ProgressBar pb;
    private Runnable runnableToStart;

    private boolean isMapList;


    /*button, next activity*/
    public AsyncMultiDbManager(Context context, String table_name, String column_name, String url,
                               boolean isNextActivityLauncher, Class activityToStart, int[] activityFlags, Object extra_data, Button btn,
                               ProgressBar pb, boolean isMapList) {

        super(context, table_name, column_name, url, isNextActivityLauncher, activityToStart, activityFlags);

        log.info("---AsyncMultiDbManager creation---");

        this.extra_data = extra_data;
        this.btn = btn;
        this.pb = pb;
        this.isMapList = isMapList;
    }

    /*simple loader with runnable*/
    public AsyncMultiDbManager(Context context, String table_name, String column_name, String url, boolean isNextActivityLauncher,
                               Class activityToStart, int[] activityFlags, Runnable runnableToStart, boolean isMapList) {
        super(context, table_name, column_name, url, isNextActivityLauncher, activityToStart, activityFlags);
        this.runnableToStart = runnableToStart;
        this.isMapList = isMapList;
    }

    @Override
    public void runAsyncLoader() {
        new AsyncMultiLoader().execute(this);
    }


    public class AsyncMultiLoader extends AsyncLoader {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (btn != null && pb != null) {
                btn.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Class doInBackground(AsyncAbstractManager... params) {
            log.info("----Async Multi Loader - Do in background----");


            context = params[0].context;
            String table_name = params[0].getTable_name();

            if (params[0].getTable_name() != null) {

                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();


                String url = params[0].getUrl();

                /*delete all data in table*/
                DbUtility.deleteTransaction(table_name, db);


            /*connect to the url and put the result in sqlite table*/
                saveListOrMapDataFromGetUrl(url, table_name, params[0].getColumn_name(), db);


//log track
            /*show me what u have*/
                Cursor cursor = db.query(table_name, null, null,
                        null, null, null, null, null);
                DbUtility.logCursor(cursor, table_name);
                cursor.close();
//------

            }

            if (params[0].isNextActivityLauncher()) {
                return params[0].getActivityToStart();
            } else return null;

        }

        @Override
        protected void onPostExecute(Class classToLaunch) {
            super.onPostExecute(classToLaunch);
              /*if we have buttons to change*/
            if (btn != null && pb != null) {
                pb.setVisibility(View.INVISIBLE);
                btn.setVisibility(View.VISIBLE);
            }

            /*if we have activity to run*/
            if (isNextActivityLauncher) {
                if (classToLaunch != null) {

                    Intent intent = new Intent(context, classToLaunch);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //TODO: make it not so hardcode ("room")
                    if (extra_data != null) {
                        intent.putExtra("room", extra_data.toString());
                    }
                    context.startActivity(intent);
                }
            }

            /*if we have a thread to run*/
            if (!isMapList) {
                if (runnableToStart != null) {
                    log.info("----Start runnable----");
                    Thread thread = new Thread(runnableToStart);
                    thread.start();
                }
            }
        }
    }

    //-----List----//

    /**
     * connects to the given url and put the result in sqlite table (list/ maplist)
     */

    private void saveListOrMapDataFromGetUrl(String url, String table_name, String column_name, SQLiteDatabase db) {
        log.info("----Getting data----");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        try {
            if (isMapList) {
                ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange
                        (url, HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                                });

            /*parsing*/
                List<Map<String, Object>> mapList = responseEntity.getBody();

            /*put into the given table (internal db)*/
                DbUtility.putMapListIntoTheTable(mapList, table_name, db);

            } else {

                ResponseEntity<List<Object>> responseEntity = restTemplate.exchange
                        (url, HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<Object>>() {
                                });

            /*parsing*/
                List<Object> list = responseEntity.getBody();

            /*put into the given table (internal db)*/
                DbUtility.putListToTableColumn(list, table_name, column_name, db);

            }
        } catch (Exception e) {
            Utility.handleServerError(context, e, handler);
            log.warning("Failed to connect to " + url);
            log.warning("Failure cause: " + e.getMessage() + "\n" + e.getStackTrace().toString());
        }

    }

}

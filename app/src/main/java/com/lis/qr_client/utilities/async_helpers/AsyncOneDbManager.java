package com.lis.qr_client.utilities.async_helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Pair;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Log
public class AsyncOneDbManager {
    private boolean isNextActivityLauncher = false;

    private String table_name;
    private String url;
    private Pair<String, Object> extra_data;

    private Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private Class activityToStart;
    private Utility utility = new Utility();

    public AsyncOneDbManager(boolean isNextActivityLauncher, String table_name, String url,
                             Pair<String, Object> extra_data, Context context, DBHelper dbHelper, SQLiteDatabase db,
                             Class activityToStart) {
        this.isNextActivityLauncher = isNextActivityLauncher;
        this.table_name = table_name;
        this.url = url;
        this.extra_data = extra_data;
        this.context = context;
        this.dbHelper = dbHelper;
        this.db = db;
        this.activityToStart = activityToStart;
    }

    public void runAsyncOneDbManager() {
        new AsyncMapLoader().execute(this);
    }

    class AsyncMapLoader extends AsyncTask<AsyncOneDbManager, Void, Class> {
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private Context context;


        @Override
        protected Class doInBackground(AsyncOneDbManager... params) {
            log.info("----Do in background----");

            dbHelper = params[0].getDbHelper();
            db = params[0].db;
            context = params[0].context;

            String table_name = params[0].getTable_name();
            String url = params[0].getUrl();


            /*delete old info from db table*/
            db.beginTransaction();
            try {
                db.delete(table_name, null, null);
                db.setTransactionSuccessful();

            } finally {
                db.endTransaction();
            }


            /*connect to the url and put the result in sqlite table*/
            try {

                /*request the api*/

                //TODO: handle if null
                Map<String, Object> response_map = getDataFromServer(url);
                utility.putMapToTable(response_map, table_name, db);

            } catch (ResourceAccessException e) {
                log.warning("Failed to connect to " + url);
                log.warning("Failure cause: " + e.getMessage() + "\n" + e.getStackTrace().toString());
            }


            //log track
            /*show me what u have*/
            Cursor cursor = db.query(table_name, null, null, null, null, null, null, null);
            dbHelper.getUtility().logCursor(cursor, table_name);
            cursor.close();
            //------

            return params[0].getActivityToStart();
        }

        @Override
        protected void onPostExecute(Class classToStart) {
            super.onPostExecute(classToStart);

            if (classToStart != null) {
                Intent intent = new Intent(context, classToStart);
                if (extra_data != null) {
                    intent.putExtra(extra_data.first, extra_data.second.toString());
                }
                context.startActivity(intent);
            }


        }
    }

    //-----Methods----

    /**
     * Get Map<String, Object> from server by url
     */

    public Map<String, Object> getDataFromServer(String url) {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());


        ResponseEntity<HashMap<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
                null, new ParameterizedTypeReference<HashMap<String, Object>>() {
                });


        return responseEntity.getBody();


    }


    //-----Getters-----

    public boolean isNextActivityLauncher() {
        return isNextActivityLauncher;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getUrl() {
        return url;
    }

    public Object getExtra_data() {
        return extra_data;
    }

    public Context getContext() {
        return context;
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public Class getActivityToStart() {
        return activityToStart;
    }

    public Utility getUtility() {
        return utility;
    }

    public static Logger getLog() {
        return log;
    }
}

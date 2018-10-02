package com.lis.qr_client.utilities.async_helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.utilities.Utility;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

    /*Gets city, street, number from api;
     parses;
     puts to the sqlite*/


@Log
/**cover class for AsyncMapListLoader; provides parameters from the inside class**/
public class AsyncDbManager {

    private boolean isNextActivityLauncher = false;
    private boolean isMapList;

    private String table_name;
    private String url;
    private String column_name;
    private Object extra_data;

    private Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private Button btn;
    private ProgressBar pb;
    private Class activityTostart;
    private Runnable runnableToStart;

    private Utility utility = new Utility();

    /*in case you need a button transformation and switch to the next activity*/

    public AsyncDbManager(String table_name, String url, Context context,
                          DBHelper dbHelper, SQLiteDatabase db, Button btn, ProgressBar pb, Class activityTostart,
                          boolean isNextActivityLauncher, boolean isMapList, Object extra_data) {
        this.isNextActivityLauncher = isNextActivityLauncher;
        this.isMapList = isMapList;
        this.table_name = table_name;
        this.url = url;
        this.context = context;
        this.dbHelper = dbHelper;
        this.db = db;
        this.btn = btn;
        this.pb = pb;
        this.activityTostart = activityTostart;
        this.extra_data = extra_data;
    }

    /*Simple db loader*/

    public AsyncDbManager(String table_name, String column_name, String url,
                          Context context, DBHelper dbHelper, SQLiteDatabase db, boolean isMapList, Runnable runnableToStart) {
        this.isMapList = isMapList;
        this.table_name = table_name;
        this.url = url;
        this.column_name = column_name;
        this.context = context;
        this.dbHelper = dbHelper;
        this.db = db;
        this.runnableToStart = runnableToStart;
    }

    public void runAsyncMapListLoader() {
        new AsyncMapListLoader(getContext()).execute(this);
    }


    /**
     * loads list of maps from an external db with the given url;
     * parses it, puts in the Context value and adds to the SQlite table (with the given table_name);
     * All params are in the cover class AsyncDbManager;
     */
    public class AsyncMapListLoader extends AsyncTask<AsyncDbManager, Void, Class> {

        DBHelper dbHelper;
        SQLiteDatabase db;
        Context context;

        public AsyncMapListLoader(Context context) {
            this.context = context;
        }

        public AsyncMapListLoader() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (btn != null && pb != null) {
                btn.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
            }
            //Toast.makeText(context, "Loading adresses...", LENGTH_SHORT).show();
        }


        @Override
        protected Class doInBackground(AsyncDbManager... params) {
            log.info("----Do in background----");

            dbHelper = params[0].getDbHelper();
            db = params[0].getDb();

            /*request to the main db to get multiple maps with "city, street, numbers", which are available*/
            String url = params[0].getUrl();
            String table_name = params[0].getTable_name();


            /*delete all data in table*/
            db.beginTransaction();
            log.info("----Delete all in table " + table_name + "----");

            try {
                db.delete(table_name, null, null);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }


            /*connect to the url and put the result in sqlite table*/
            try {
                /*choose method for list and mapList*/
                if (isMapList) {
                    saveMapListDataFromGetUrl(url, table_name);
                } else {
                    saveListDataFromGetUrl(url, table_name, column_name);
                }

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
            if (isNextActivityLauncher) {
                return params[0].getActivityTostart();
            } else return null;

        }

        @Override
        protected void onPostExecute(Class classTostart) {
            super.onPostExecute(classTostart);
            /*if we have buttons to change*/
            if (btn != null && pb != null) {
                pb.setVisibility(View.INVISIBLE);
                btn.setVisibility(View.VISIBLE);
            }

            /*if we have activity to run*/
            if (isNextActivityLauncher) {
                if (classTostart != null) {

                    Intent intent = new Intent(context, classTostart);

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

    //-----Additional methods-------//

    //-----List----//

    /**
     * connects to the given url and put the result in sqlite table (list)
     */

    private void saveListDataFromGetUrl(String url, String table_name, String column_name) throws ResourceAccessException {
        log.info("----Getting data----");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        ResponseEntity<List<Object>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Object>>() {
                });

            /*parsing*/
        List<Object> list = responseEntity.getBody();

            /*put into the given table (internal db)*/
        utility.putListToTableColumn(list, table_name, column_name, db);
    }


    //-----MapList-----//

    /**
     * connects to the given url and put the result in sqlite table (mapList)
     */

    private void saveMapListDataFromGetUrl(String url, String table_name) throws ResourceAccessException {
        log.info("----Getting data----");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

            /*parsing*/
        List<Map<String, Object>> mapList = responseEntity.getBody();

            /*put into the given table (internal db)*/
        utility.putMapListIntoTheTable(mapList, table_name, db);
    }


    //----Getters-----


    public Utility getUtility() {
        return utility;
    }

    public Button getBtn() {
        return btn;
    }

    public ProgressBar getPb() {
        return pb;
    }

    public Class getActivityTostart() {
        return activityTostart;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getUrl() {
        return url;
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

}


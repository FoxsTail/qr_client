package com.lis.qr_client.extra.async_helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.widget.Toast;
import com.lis.qr_client.activity.LogInActivity;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.pojo.User;
import com.lis.qr_client.extra.utility.Utility;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Log
public class AsyncOneDbManager {

    private final Object post_request;
    private HttpMethod httpMethod;

    private boolean isNextActivityLauncher = false;

    private String table_name;
    private String url;
    private Pair<String, Object> extra_data;

    private Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private Class activityToStart;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public AsyncOneDbManager(boolean isNextActivityLauncher, String table_name, String url,
                             Pair<String, Object> extra_data, Context context, DBHelper dbHelper,
                             Class activityToStart, HttpMethod httpMethod, Object post_request) {
        this.isNextActivityLauncher = isNextActivityLauncher;
        this.table_name = table_name;
        this.url = url;
        this.extra_data = extra_data;
        this.context = context;
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
        this.activityToStart = activityToStart;
        this.httpMethod = httpMethod;
        this.post_request = post_request;

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

            /*remove old data from db*/
            DbUtility.deleteTransaction(table_name, db);


            /*connect to the url and put the result in sqlite table*/
            try {

                /*request the api*/

                //TODO: handle if null

                switch (httpMethod) {
                    case GET: {
                        Map<String, Object> response_map = getDataFromServer(url);
                        if (response_map != null) {
                            DbUtility.putMapToTable(response_map, table_name, db);
                        }else{
                            return null;
                        }

                        break;
                    }
                    case POST: {

                        if (!postLoginCheck(url)) {
                            return null;
                        }

                    }
                }


            } catch (ResourceAccessException e) {
                log.warning("Failed to connect to " + url);
                log.warning("Failure cause: " + e.getMessage() + "\n" + e.getStackTrace().toString());
            }


            //log track
            /*show me what u have*/
            Cursor cursor = db.query(table_name, null, null, null,
                    null, null, null, null);
            DbUtility.logCursor(cursor, table_name);
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
     * Post request for logging in user
     */

    public boolean postLoginCheck(String url) {
        User user = postDataFromServer(url, post_request);


        if (user == null) {
            log.info("user is null");
            return false;
        }

                        /*save data to SQLite*/
        DbUtility.saveUserToDb(user, db);

                        /*save data to cache*/
        PreferenceUtility.saveUsersDataToPreference(user, context, LogInActivity.PREFERENCE_SAVE_USER,
                LogInActivity.PREFERENCE_ID_USER,
                LogInActivity.PREFERENCE_IS_USER_SAVED);

        return true;
    }

    /**
     * Get Map<String, Object> from server by url, method post
     */

    public Map<String, Object> getDataFromServer(String url) {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        ResponseEntity<HashMap<String, Object>> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<HashMap<String, Object>>() {
                    });
        } catch (Exception e) {
            Utility.handleServerError(context, e, handler);
            return null;
        }

        return responseEntity.getBody();
    }

    /**
     * Get User from server by url, method post
     */

    public User postDataFromServer(String url, Object post_request) {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        ResponseEntity<User> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(url, post_request, User.class);
        } catch (Exception e) {
            Utility.handleServerError(context, e, handler);
            return null;
        }

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

    public static Logger getLog() {
        return log;
    }
}

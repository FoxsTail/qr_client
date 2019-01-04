package com.lis.qr_client.extra.async_helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import com.lis.qr_client.activity.LogInActivity;
import com.lis.qr_client.data.DBHelper;
import com.lis.qr_client.extra.utility.DbUtility;
import com.lis.qr_client.extra.utility.PreferenceUtility;
import com.lis.qr_client.extra.utility.Utility;
import com.lis.qr_client.pojo.User;
import lombok.extern.java.Log;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Log
public class AsyncOneDbManager extends AsyncAbstractManager {
    private Pair<String, Object> extra_data;
    private final Object post_request;
    private HttpMethod httpMethod;


    public AsyncOneDbManager(Context context, String table_name, String column_name, String url,
                             boolean isNextActivityLauncher, Class activityToStart, Pair<String, Object> extra_data,
                             Object post_request, HttpMethod httpMethod) {
        super(context, table_name, column_name, url, isNextActivityLauncher, activityToStart);
        this.extra_data = extra_data;
        this.post_request = post_request;
        this.httpMethod = httpMethod;
    }

    @Override
    public void runAsyncLoader() {
        new AsyncOneLoader().execute(this);

    }

    public class AsyncOneLoader extends AsyncLoader {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Class doInBackground(AsyncAbstractManager... params) {
            log.info("----Do in background----");


            context = params[0].context;
            String table_name = params[0].getTable_name();

            if (table_name != null) {

                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                String url = params[0].getUrl();


            /*remove old data from db*/
                DbUtility.deleteTransaction(table_name, db);


            /*connect to the url and put the result in sqlite table*/
                try {
                /*request the api*/
                    switch (httpMethod) {
                        case GET: {
                            Object response = getDataFromServer(url, HttpMethod.GET,
                                    null);

                            /*check, cast and save*/
                            if (response != null) {
                                Map<String, Object> response_map = (Map<String, Object>) response;
                                DbUtility.putMapToTable(response_map, table_name, db);
                            } else {
                                log.info("Get response is null");
                                return null;
                            }

                            break;
                        }
                        case POST: {
                            Object response = getDataFromServer(url, HttpMethod.POST, post_request);

                            /*check, cast and save*/
                            if (response != null) {
                                User user = (User) response;
                                postLoginSave(user, db);
                            } else {
                                log.info("Post response is null");
                                return null;
                            }
                        }
                    }

                } catch (ResourceAccessException e) {
                    log.warning("Failed to connect to " + url);
                    log.warning("Failure cause: " + e.getMessage() + "\n" + e.getStackTrace().toString());
                }


//--log track
            /*show me what u have*/
                Cursor cursor = db.query(table_name, null, null, null,
                        null, null, null, null);
                DbUtility.logCursor(cursor, table_name);
                cursor.close();
//------
            }

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


    /**
     * Post request for logging in user
     */

    public boolean postLoginSave(User user, SQLiteDatabase db) {

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

    public Object getDataFromServer(String url, HttpMethod httpMethod, Object post_request) {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        try {
            switch (httpMethod) {
                case GET: {

                    ResponseEntity<HashMap<String, Object>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
                            null, new ParameterizedTypeReference<HashMap<String, Object>>() {
                            });
                    return responseEntity.getBody();
                }
                case POST: {

                    ResponseEntity<User> responseEntity = restTemplate.postForEntity(url, post_request, User.class);
                    return responseEntity.getBody();

                }
            }

        } catch (Exception e) {
            Utility.handleServerError(context, e, handler);
            return null;
        }
        return null;
    }

}

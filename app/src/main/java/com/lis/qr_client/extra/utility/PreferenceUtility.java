package com.lis.qr_client.extra.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lis.qr_client.application.QrApplication;
import com.lis.qr_client.constants.MyPreferences;
import com.lis.qr_client.pojo.User;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lis.qr_client.constants.MyPreferences.*;

import static android.content.Context.MODE_PRIVATE;

@Log
public class PreferenceUtility {


    /** remove previous inventory session data*/

    public static void removeOldInventorySessionData(){
        /*remove previous session data*/
        PreferenceUtility.removeOldPreferences(QrApplication.getInstance(),
                MyPreferences.PREFERENCE_FILE_NAME,
                MyPreferences.PREFERENCE_INVENTORY_STATE_BOOLEAN,
                MyPreferences.PREFERENCE_TO_SCAN_LIST,
                MyPreferences.PREFERENCE_SCANNED_LIST);
    }

    /**
     * remove data from preferences
     */

    public static void removeLoginPreferences(Context context, @Preferences String key_user_data,
                                              @Preferences String key_is_logged_in) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key_user_data);
        editor.putBoolean(key_is_logged_in, false);
        editor.apply();

        /**/
    }


    /**
     * remove data from preferences
     */

    public static void removePrefernces(Context context, @Preferences String preference_key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(preference_key);
        editor.apply();
    }

    /**
     * save user's email and password to Set<String> and then to the preferences
     */

    public static void saveUsersDataToPreference(User user, Context context,
                                                 @MyPreferences.Preferences String key_user_data,
                                                 @Preferences String key_id_user, @Preferences String key_is_logged_in) {
        Set<String> users_email_passwd = new HashSet<>();
        users_email_passwd.add(user.getEmail());
        users_email_passwd.add(user.getPassword());


        saveStringToPreferences(context, key_user_data, users_email_passwd);
        saveIntToPreferences(context, key_id_user, user.getId());
        saveBooleanTrueToPreferences(context, key_is_logged_in);
    }

    /**
     * save boolean to preferences
     */

    public static void saveBooleanTrueToPreferences(Context context, @Preferences String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(preference_key, true);
        editor.apply();

    }

    /**
     * save boolean to preferences
     */

    public static void saveIntToPreferences(Context context, @Preferences String preference_key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(preference_key, value);
        editor.apply();

    }

    /**
     * save data Set<String> to preferences
     */

    public static void saveStringToPreferences(Context context, @Preferences String preference_key,
                                               Set<String> strings_to_save) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(preference_key, strings_to_save);
        editor.apply();
        log.info("Preferences with key " + preference_key + " were saved");
    }


    /**
     * get boolean data from preferences
     */

    public static Boolean getBooleanDataFromPreferences(Context context, @Preferences String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        if (preference_key != null) {
            return sharedPreferences.getBoolean(preference_key, false);
        } else {
            return null;
        }
    }

    /**
     * get Set<String> data from preferences
     */

    public static int getIntegerDataFromPreferences(Context context, @Preferences String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        if (preference_key != null) {
            return sharedPreferences.getInt(preference_key, 0);
        } else {
            return 0;
        }
    }

    /**
     * get Set<String> data from preferences
     */

    public static Set<String> getDataFromPreferences(Context context, @Preferences String preference_key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (MyPreferences.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        if (preference_key != null) {
            return sharedPreferences.getStringSet(preference_key, null);
        } else {
            return null;
        }
    }

    /**
     * Save object (int, string, boolean, object) to preference
     */
    public static void savePreference(Context context, @Preferences String preferenceFileName, @Preferences String key,
                                      Object value) {
        log.info("---Save object to preferences---");
        log.info("---value---" + value);
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (value != null) {
            if (value instanceof Integer) {

                editor.putInt(key, (int) value);

            } else if (value instanceof String) {

                editor.putString(key, value.toString());

            } else if (value instanceof Boolean) {

                editor.putBoolean(key, (boolean) value);

            } else {

                Gson gson = new Gson();
                String jsonString = gson.toJson(value);
                editor.putString(key, jsonString);
            }
        }
        editor.apply();
    }

    public static List<Map<String, Object>> preferencesJsonToMapList(Context context,
                                                                     @Preferences String preferenceFileName,
                                                                     @Preferences String preferenceKey) {
        String jsonToScan = loadStringOrJsonPreference
                (context, preferenceFileName, preferenceKey);

        Gson gson = new Gson();
        return gson.fromJson(jsonToScan, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
    }

    /**
     * Load Boolean from preference
     */
    public static Boolean loadBooleanPreference(Context context, @Preferences String preferenceFileName,
                                                @Preferences String key) {
        log.info("---Load Boolean from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        boolean value = preferences.getBoolean(key, false);
        log.info("---value---" + value);
        return value;
    }

    /**
     * Load String or Json from preference
     */
    public static String loadStringOrJsonPreference(Context context, @Preferences String preferenceFileName,
                                                    @Preferences String key) {
        log.info("---Load String from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);

        String value = preferences.getString(key, "");

        log.info("---value---" + value);
        return value;
    }

    /**
     * Load int from preference
     */
    public static int loadIntPreference(Context context, @Preferences String preferenceFileName,
                                        @Preferences String key) {
        log.info("---Load int from preferences---");

        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        int value = preferences.getInt(key, -1);
        log.info("---value---" + value);
        return value;
    }

    /**
     * Clear old preferences (apply - async)
     */
    public static void clearOldReferences(Context context, @Preferences String pref_name) {
        SharedPreferences preferences = context.getSharedPreferences(pref_name, MODE_PRIVATE);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    /**
     * Remove old session data from preferences (apply - async)
     */

    public static void removeOldPreferences(Context context, @Preferences String preferenceFileName,
                                            @Preferences String... preferenceNames) {

        for (String name : preferenceNames) {
            removeOldReference(context, preferenceFileName, name);
        }

    }

    /**
     * Remove old preference (apply - async)
     */
    public static void removeOldReference(Context context, @Preferences String preferenceFileName,
                                          @Preferences String remove_key) {
        SharedPreferences preferences = context.getSharedPreferences(preferenceFileName, MODE_PRIVATE);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(remove_key);
            editor.apply();
        }
    }

    /**
     * Post request for logging in user
     */

    public static boolean postLoginSave(Context context, User user, SQLiteDatabase db) {

      /*save data to SQLite*/
        DbUtility.saveUserToDb(user, db);

                        /*save data to cache*/
        PreferenceUtility.saveUsersDataToPreference(user, context, MyPreferences.PREFERENCE_SAVE_USER,
                MyPreferences.PREFERENCE_ID_USER,
                MyPreferences.PREFERENCE_IS_USER_SAVED);

        return true;

    }
}

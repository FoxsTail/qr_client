package com.lis.qr_client.constants;


import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;




public class MyPreferences {

    @StringDef({PREFERENCE_SAVE_USER, PREFERENCE_ID_USER, PREFERENCE_IS_USER_SAVED, PREFERENCE_FILE_NAME,
    ADDRESS_ID_PREFERENCES, ROOM_ID_PREFERENCES, PREFERENCE_INVENTORY_STATE_BOOLEAN, PREFERENCE_TO_SCAN_LIST,
    PREFERENCE_SCANNED_LIST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Preferences {
    }

    public static final String PREFERENCE_FILE_NAME = "qr_preferences";
    public static final String PREFERENCE_SAVE_USER = "save_user";
    public static final String PREFERENCE_ID_USER = "id_user";
    public static final String PREFERENCE_IS_USER_SAVED = "is_user_saved";
    public static final String ADDRESS_ID_PREFERENCES = "address_id";
    public static final String ROOM_ID_PREFERENCES = "room";
    public static final String PREFERENCE_INVENTORY_STATE_BOOLEAN = "inventory_state";
    public static final String PREFERENCE_TO_SCAN_LIST = "to_scan_list";
    public static final String PREFERENCE_SCANNED_LIST = "scanned_list";

}
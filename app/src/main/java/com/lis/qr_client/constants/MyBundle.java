package com.lis.qr_client.constants;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MyBundle {

    @StringDef({SCANNED_LIST, TO_SCAN_LIST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Bundles{}

    public static final String SCANNED_LIST = "scanned_list";
    public static final String TO_SCAN_LIST = "to_scan_list";
    public static final String OTHER_ROOM_LIST = "other_room_list";
    public static final String ROOM = "room";
    public static final String RESULT_FILE_TITLES = "result_file_titles";
    public static final String EQUIPMENT_NAME = "Обладнання";
    public static final String EQUIPMENT_INVENTORY = "Інвентарний номер";
    public static final String EQUIPMENT_STATE_VALUE = "Стан";

}

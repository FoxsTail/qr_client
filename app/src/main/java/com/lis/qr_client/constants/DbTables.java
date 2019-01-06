package com.lis.qr_client.constants;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DbTables {

    @StringDef()
    @Retention(RetentionPolicy.SOURCE)
    public @interface Tables{}

    public static final String DB_NAME = "qr_db";
    public static final String TABLE_USER = "user";
    public static final String TABLE_ADDRESS = "address";
    public static final String TABLE_PERSONAL_DATA = "personal_data";
    public static final String TABLE_WORKPLACE = "workplace";
    public static final String TABLE_PHONE_NUMBER = "room";
    public static final String TABLE_EQUIPMENT = "equipment";
    public static final String TABLE_INVENTORY = "inventory";
    public static final String TABLE_ROOM = "room";

}

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
    public static final String ROOM = "room";

}

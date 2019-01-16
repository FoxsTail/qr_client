package com.lis.qr_client.application;

import android.app.Application;
import android.content.Context;
import com.lis.qr_client.data.DBHelper;
import com.squareup.leakcanary.LeakCanary;
import lombok.extern.java.Log;

@Log
public class QrApplication extends Application {

    private static Context instance;
    protected static DBHelper dbHelper;


    @Override
    public void onCreate() {
        super.onCreate();
        log.info("QrApplication  ---- OnCreate()");

        instance = this;

        dbHelper = new DBHelper(getInstance());

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    public static Context getInstance() {
        return instance;
    }

    public static DBHelper getDbHelper() {
        return dbHelper;
    }
}

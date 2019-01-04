package com.lis.qr_client.extra.async_helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.lis.qr_client.data.DBHelper;

public abstract class AsyncAbstractManager {
    Context context;

    private String table_name;
    private String column_name;
    private String url;


    protected boolean isNextActivityLauncher = false;
    protected Class activityToStart;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public AsyncAbstractManager(Context context, String table_name, String column_name, String url,
                                boolean isNextActivityLauncher, Class activityToStart) {
        this.context = context;
        this.table_name = table_name;
        this.column_name = column_name;
        this.url = url;
        this.isNextActivityLauncher = isNextActivityLauncher;
        this.activityToStart = activityToStart;
    }

    public abstract void runAsyncLoader();

    public abstract class AsyncLoader extends AsyncTask<AsyncAbstractManager, Void, Class> {
        DBHelper dbHelper;
        SQLiteDatabase db;
        Context context;

    }


    //----------//


    public Context getContext() {
        return context;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getColumn_name() {
        return column_name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isNextActivityLauncher() {
        return isNextActivityLauncher;
    }

    public Class getActivityToStart() {
        return activityToStart;
    }
}

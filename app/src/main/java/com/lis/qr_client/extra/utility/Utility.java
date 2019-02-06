package com.lis.qr_client.extra.utility;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lis.qr_client.R;
import com.lis.qr_client.activity.MainMenuActivity;
import com.lis.qr_client.pojo.*;
import com.lis.qr_client.extra.adapter.InventoryAdapter;
import lombok.extern.java.Log;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.security.Permission;
import java.util.*;

import static android.content.Context.MODE_PRIVATE;


/**
 * Utility class for different small, repetitive tasks
 */

@Log
public class Utility {

    //-------Front---------


    public static void fullScreen(Activity activity) {
        Window window = activity.getWindow();
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    /**
     * Toolbar setter dimOnMenu, back button, name
     */
    public static void toolbarSetter(AppCompatActivity activity, @NonNull Toolbar toolbar, String title,
                                     final FrameLayout frameLayout, boolean isChildActivity) {
        toolbar.setTitle(title);
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            if (isChildActivity) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
            if (frameLayout != null) {
                actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
                    @Override
                    public void onMenuVisibilityChanged(boolean b) {
                        if (b) {
                            log.info("visible");
                            frameLayout.getForeground().setAlpha(140);
                        } else {
                            log.info(" ne visible");
                            frameLayout.getForeground().setAlpha(0);

                        }
                    }
                });
            }
        }
    }


    //--------------Alerts----------------

    /**
     * Parse exception from server, send msg to handler
     */


    public static void handleServerError(Context context, Exception e, Handler handler) {
        String error_msg = e.getMessage();
        log.info("Some error occurred: " + error_msg);

        Message msg = new Message();
        msg.what = 3;
        switch (error_msg.trim()) {
            case "400": {
                msg.obj = context.getString(R.string.bad_request);
                break;
            }
            case "404": {
                msg.obj = context.getString(R.string.not_found);
                break;
            }
            case "500": {
                msg.obj = context.getString(R.string.server_error);
                break;
            }
            default: {
                msg.obj = context.getString(R.string.connection_error);
                break;
            }
        }

        handler.sendMessage(msg);
    }

    /**
     * hide keyboard
     */
    public static boolean hideSoftKeyboard(Activity activity) {
        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            if (activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
            return true;
        }
        return false;
    }

}
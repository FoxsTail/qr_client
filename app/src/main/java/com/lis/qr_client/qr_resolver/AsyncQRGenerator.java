package com.lis.qr_client.qr_resolver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Layout;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.lis.qr_client.R;


import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class AsyncQRGenerator extends AsyncTask<String, Integer, ArrayList<Bitmap>> {
    private Activity activity;
    //private LinearLayout layout;

    private ProgressBar pb;
    private TextView tv;


    public AsyncQRGenerator(Activity activity, ProgressBar pb) {
        this.activity = activity;
        this.pb = pb;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        LinearLayout linearLayout = new LinearLayout(new ContextThemeWrapper(activity.getApplicationContext(), R.style.Theme_AppCompat_DayNight_DarkActionBar));
        int wrap_content = ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(wrap_content, wrap_content);

        pb = new ProgressBar(activity.getApplicationContext());
        pb.setVisibility(View.VISIBLE);
        linearLayout.addView(pb);

        tv = new TextView(activity.getApplicationContext());
        tv.setText("Start creating...");
        tv.setTextColor(Color.BLACK);
        linearLayout.addView(tv);

      //  activity.setTheme(R.style.AppTheme);
        activity.setContentView(linearLayout);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(String... strings) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        super.onPostExecute(bitmaps);
        pb.setVisibility(View.INVISIBLE);
        Toast.makeText(activity.getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
       // activity.finish();
    }

}

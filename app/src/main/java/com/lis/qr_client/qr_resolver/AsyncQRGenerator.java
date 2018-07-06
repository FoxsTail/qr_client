package com.lis.qr_client.qr_resolver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lis.qr_client.R;


import java.io.*;
import java.util.ArrayList;

//TODO:сделать чтобы сохраняло в базу, а не только на телефон
//TODO: наследовать от этого генератора, чтобы сохранять в разные места

public class AsyncQRGenerator extends AsyncTask<String, Integer, ArrayList<Bitmap>> {
    private static final int QR_WIDTH = 400;
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private Activity activity;
    private ProgressBar pb;
    private TextView tv;


    public AsyncQRGenerator(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

       /*Prepares layout with views for the empty activity, sets as a content view*/

        LinearLayout linearLayout = new LinearLayout(new ContextThemeWrapper(activity.getBaseContext(), R.style.Theme_AppCompat_DayNight_DarkActionBar));
        int wrap_content = ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(wrap_content, wrap_content);

        pb = new ProgressBar(activity.getApplicationContext());
        pb.setVisibility(View.VISIBLE);
        linearLayout.addView(pb, layoutParams);

        tv = new TextView(activity.getApplicationContext());
        tv.setText("Start creating...");
        tv.setTextColor(Color.BLACK);
        linearLayout.addView(tv, layoutParams);

        activity.setContentView(linearLayout);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        tv.setText("Created: "+values[0]+" from " + values[1]);
    }


    @Override
    protected ArrayList<Bitmap> doInBackground(String... strings) {
        ArrayList<Bitmap> bitmapList = new ArrayList<>();
        String filename;

        /*Creates bitMatrix from the every transferred param,
         transforms it to bitmap (qr code pic),
         saves to the phone internal memory */

        for (int i = 0; i < strings.length; i++) {
                filename = String.valueOf(i+1) + "_qr";
            try {
                BitMatrix matrix = new QRCodeWriter().encode(strings[i], BarcodeFormat.QR_CODE, QR_WIDTH, QR_WIDTH);

                Bitmap bitmap = matrixToBitmap(matrix);
                bitmapList.add(bitmap);

                saveBitmapAsImgFile(bitmap, filename);

                publishProgress(i+1, strings.length);

            } catch (WriterException e) {
                e.printStackTrace();
            }

        }
        return bitmapList;
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        super.onPostExecute(bitmaps);
        pb.setVisibility(View.INVISIBLE);
        Toast.makeText(activity.getBaseContext(), "Done!", Toast.LENGTH_SHORT).show();
        activity.finish();
    }

    public Bitmap matrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int heigh = matrix.getHeight();

        /*Create bitmap from raw img from given size*/
        Bitmap bitmapImg = Bitmap.createBitmap(width, heigh, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < heigh; j++) {

                /*coloring img pixels from the given matrix*/
                bitmapImg.setPixel(i, j, matrix.get(i, j) ? BLACK : WHITE);
            }
        }
        return bitmapImg;
    }

    public boolean saveBitmapAsImgFile(Bitmap bitmap, String filename) {

        /*Create folder for saving*/
        String storagePath = Environment.getExternalStorageDirectory() + "/QR_CODES/";
        File sdFolder = new File(storagePath);
        sdFolder.mkdir();


        String imgName = sdFolder.getPath() + "/" + filename + ".png";
        try {

            //TODO: put in the resources
            /*Create file with the given name, put it in to the buffer*/
            FileOutputStream fos = new FileOutputStream(imgName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            /*Save file in the .png formate*/
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }


}

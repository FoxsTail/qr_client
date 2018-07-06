package com.lis.qr_client.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import com.lis.qr_client.R;
import net.sourceforge.zbar.*;

import java.io.IOException;

import static android.util.Log.d;

public class CameraActivity extends AppCompatActivity {

    /*Methods run in this order:
    onCreate, Previewer, OnResume, safeCameraOpen, SurfCreated,
    SurfChanged, (multiple) onPreviewFrame, (when found) OnPause,
    ReleaseCamera, SurfDestroyed*/

    private SurfaceView svScan;
    private Button btnDoScan;

    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private Previewer mPreview;
    private Handler autoFocusHandler;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private final int CAMERA_ID = 0;
    public static final String TAG = "Camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d(TAG, "---Create-----");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camera);


        svScan = findViewById(R.id.svScan);
        surfaceHolder = svScan.getHolder();

          /*Button for scanning*/
      /*  btnDoScan = findViewById(R.id.btnDoScan);
        btnDoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (barcodeScanned) {
                try{
                    barcodeScanned = false;
                    mCamera.setPreviewCallback(previewCallback);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCallback);
                }catch (Exception e) {
                    Log.d(TAG, "onClick failure");
                    e.printStackTrace();
            }
        });*/

        autoFocusHandler = new Handler();

        mPreview = new Previewer(this);
        surfaceHolder.addCallback(mPreview);

        /*Creates scanner*/
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

    }

    @Override
    protected void onResume() {
        super.onResume();
        d(TAG, "---onResume-----");
        safeCameraOpen(CAMERA_ID);
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        d(TAG, "---Safe camera Open-----");
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            d(TAG, "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }


    @Override
    protected void onPause() {
        super.onPause();
        d(TAG, "---OnPause-----");
        releaseCameraAndPreview();
    }

    private void releaseCameraAndPreview() {
        // mPreview.setCamera(null);
        d(TAG, "---Release Camera-----");
        if (mCamera != null) {
            previewing=false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /*autofocus setting*/

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                d(TAG, "---Runnable-----");
            mCamera.autoFocus(autoFocusCallback);
        }
    };

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            d(TAG, "---onAutoFocus-----");
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    /*read data from preview pic*/

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            d(TAG, "---OnPreviewFrame-----");
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

    /* Analyze image */
            int result = scanner.scanImage(barcode);

            if (result != 0) {
        /* ... stop preview ... */
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                previewing = false;


        /* Get and show scanned data*/
                SymbolSet symbols = scanner.getResults();
                String scanTextResult = null;
                for (Symbol sym : symbols) {
                    scanTextResult = sym.getData();
                    Toast toast = Toast.makeText(getApplicationContext(), scanTextResult, Toast.LENGTH_LONG);
                    toast.show();
                    barcodeScanned = true;
                }
/*Return scanned data to the parent activity*/
                if (barcodeScanned) {
                    Intent intent = new Intent();
                    intent.putExtra("scan", scanTextResult);
                    setResult(RESULT_OK, intent);
                    result = 0;
                    symbols = null;
                    barcode = null;
                    finish();
                }


            }
        }
    };


    /*Previewer setup*/

    class Previewer extends ViewGroup implements SurfaceHolder.Callback {

        public Previewer(Context context) {
            super(context);
            d(TAG, "---Previewer constructor-----");
        }


        @Override
        protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                d(TAG, "---Surface Created-----");
                mCamera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
            d(TAG, "---Surface Changed-----");

            if (surfaceHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.setDisplayOrientation(90);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
                mCamera.autoFocus(autoFocusCallback);

            } catch (IOException e) {
                Log.d("Camera", "Error starting camera preview: " + e.getMessage());

            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            d(TAG, "---Surface Destroyed-----");

        }


        public void setCamera(Camera camera) {
            if (mCamera == camera) {
                return;
            }
            camera.stopPreview();
            releaseCameraAndPreview();
            mCamera = camera;

            if (mCamera != null) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }

        }
    }

}

package com.lis.qr_client.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
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
    int skipFirstPreviewFrame;

    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
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

        svScan = null;
        svScan = findViewById(R.id.svScan);
        surfaceHolder = svScan.getHolder();

        autoFocusHandler = new Handler();

        surfaceHolder.addCallback(surfaceCallback);

        /*Creates scanner*/
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        skipFirstPreviewFrame = 0;

    }

    @Override
    protected void onResume() {
        super.onResume();
        d(TAG, "---onResume-----");
        safeCameraOpen(CAMERA_ID);
    }

    private void safeCameraOpen(int id) {
        d(TAG, "---Safe camera Open-----");
        try {
            //releaseCameraAndPreview();
            mCamera = Camera.open(id);
        } catch (Exception e) {
            d(TAG, "failed to open Camera");
            e.printStackTrace();
        }
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
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

        /*SurfaceHolder callback setup*/

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            d(TAG, "---Surface Created-----");

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            d(TAG, "---Surface Changed-----");


            if (surfaceHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }


            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            try {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
                previewing = true;
                mCamera.autoFocus(autoFocusCallback);

            } catch (IOException e) {
                Log.d("Camera", "Error starting camera preview: " + e.getMessage());

            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            d(TAG, "---Surface Destroyed-----");
        }
    };



    /*autofocus setting*/

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            d(TAG, "---onAutoFocus-----");
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                d(TAG, "---Runnable-----");
            mCamera.autoFocus(autoFocusCallback);
        }
    };



    /*read data from preview pic*/

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            d(TAG, "---OnPreviewFrame-----");

            /*skip the first preview frame, cause after second start it keeps
            * old info, thus duplicates result.
            *
            * Sometimes it works, sometimes no, idk how to fix this shit
            * */

            //TODO: try to replace with manual SurfaceView creation or PreviewCallbackWithBuffer, it may help

            if(skipFirstPreviewFrame < 3){
                skipFirstPreviewFrame ++;
                d(TAG, "---Magic happend-----");
                return;
            }

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

    /* Analyze image */
            int result = scanner.scanImage(barcode);

            if (result != 0) {
        /* ... stop preview ... */
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();


        /* Get and show scanned data*/
                SymbolSet symbols = scanner.getResults();
                String scanTextResult = null;
                for (Symbol sym : symbols) {
                    scanTextResult = sym.getData();
                    Toast.makeText(getApplicationContext(), scanTextResult, Toast.LENGTH_LONG).show();
                    barcodeScanned = true;
                }
        /*Return scanned data to the parent activity*/
                if (barcodeScanned) {
                    Intent intent = new Intent();
                    intent.putExtra("scan", scanTextResult);
                    setResult(RESULT_OK, intent);
                    finish();
                }


            }
        }
    };


}

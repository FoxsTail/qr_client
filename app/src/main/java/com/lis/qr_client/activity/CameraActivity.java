package com.lis.qr_client.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import com.lis.qr_client.R;
import net.sourceforge.zbar.*;

import java.io.IOException;
import java.security.Policy;
import java.util.List;

import static android.util.Log.d;
import static android.util.Log.e;

@lombok.extern.java.Log
public class CameraActivity extends AppCompatActivity {

    /*Methods run in this order:
    onCreate, Previewer, OnResume, safeCameraOpen, SurfCreated,
    SurfChanged, (multiple) onPreviewFrame, (when found) OnPause,
    ReleaseCamera, SurfDestroyed*/

    /*Scan every frame for qr-code existing, if there is one, parse for data and transfer to the parent activity*/

    private SurfaceView svScan;
    int skipFirstPreviewFrame;

    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private Handler autoFocusHandler;

    private CameraHandlerThread cameraHandlerThread;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private final int CAMERA_ID = 0;
    public static final String TAG = "Camera";
    private Surface surface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();

        d(TAG, "---Create-----");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camera);

        svScan = null;
        svScan = findViewById(R.id.svScan);

        autoFocusHandler = new Handler();

        /*Creates scanner*/
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        skipFirstPreviewFrame = 0;
        log.info("create Current thread is: " + Thread.currentThread());

    }

    @Override
    protected void onResume() {
        super.onResume();
        d(TAG, "---onResume-----");

        if (permissionsGranted) {

        /*prepare thread*/
            if (cameraHandlerThread == null) {
                cameraHandlerThread = new CameraHandlerThread("CameraHandlerThread");
            }

        /*prepare surface*/
            surfaceHolder = svScan.getHolder();
            surfaceHolder.addCallback(surfaceCallback);
        } else {
            log.info("No permission to use camera");
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        d(TAG, "---OnPause-----");
        releaseCameraAndPreview();

        if (cameraHandlerThread != null) {
            cameraHandlerThread.stopCameraThread();
            cameraHandlerThread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("---Camera -- onDestroy()---");
    }

    /*
    Run openCamera in the additional thread
    */
    class CameraHandlerThread extends HandlerThread {

        Handler handler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            handler = new Handler(getLooper());
        }


        public void stopCameraThread() {
            this.quitSafely();
            try {
                this.join();
                handler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void setupOnSurfaceChanged(final SurfaceHolder surfaceHolder) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    log.info("runnable surface changed Current thread is: " + Thread.currentThread());
                    surface = surfaceHolder.getSurface();
                    if (surface == null) {
                        // preview surface does not exist
                        return;
                    }

                    /*when surface is ready open camera*/
                    safeCameraOpen(CAMERA_ID);


                    try {
                        mCamera.stopPreview();
                    } catch (Exception e) {
                        // ignore: tried to stop a non-existent preview
                    }

                    try {
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(surfaceHolder);

                         /*
                         get the pic size for setting the buffer size
                          */
                        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                        int dataBufferSize = (int) (previewSize.height * previewSize.width *
                                (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8.0));
                        mCamera.addCallbackBuffer(new byte[dataBufferSize]);


                        mCamera.setPreviewCallbackWithBuffer(previewCallback);
                        mCamera.startPreview();
                        previewing = true;

                        /*setup autofocus*/
                        Camera.Parameters parameters = mCamera.getParameters();
                        List<String> focus_modes = parameters.getSupportedFocusModes();
                        if (focus_modes != null && focus_modes.contains(Camera.Parameters.FLASH_MODE_AUTO)) ;
                        {
                            mCamera.autoFocus(autoFocusCallback);
                        }


                    } catch (IOException |
                            NullPointerException e)

                    {
                        Log.d("Camera", "Error starting camera preview: " + e.getMessage());
                        d(TAG, e.getStackTrace().toString());
                        finish();
                    }
                }
            });

        }
    }


    //---------CameraOpen----------//

    /*
    * Usual safe camera open, HandlerThread that runs camera opening in the new thread;
    * thread camera open which init instance of cameraHandlerThread and runs openCamera method.
    * */

    private void safeCameraOpen(int id) {
        d(TAG, "---Safe camera Open-----");
        log.info("Current thread is: " + Thread.currentThread());

        try {
            releaseCameraAndPreview();

            d(TAG, "---Open camera----");
            mCamera = Camera.open(id);
        } catch (Exception e) {
            d(TAG, "failed to open Camera");
            e.printStackTrace();
            finish();
        }
    }

    private void releaseCameraAndPreview() {
        // mPreview.setCamera(null);
        d(TAG, "---Release Camera-----");
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallbackWithBuffer(null);
            try {
                mCamera.stopPreview();
            } catch (Exception e) {

            }
            /*release surface*/
            if (surface != null) {
                surface.release();
            }

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

            cameraHandlerThread.setupOnSurfaceChanged(surfaceHolder);

        }

        //--------------If u get a NullPointer at somewhere in methods above----------------//
        //Your camera object is instantiated only if there is no Exception.
        //So, if this exception happens, camera == null
        //------------------------------//


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
            if (mCamera != null) {
                mCamera.autoFocus(autoFocusCallback);
            }
        }
    };



    /*read data from preview pic*/

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            d(TAG, "---OnPreviewFrame-----");
            log.info("Current thread is: " + Thread.currentThread());

            /*skip the first preview frame, cause after second start it keeps
            * old info, thus duplicates result.
            *
            * Sometimes it works, sometimes no, idk how to fix this shit
            * */

            if (skipFirstPreviewFrame < 6) {
                skipFirstPreviewFrame++;
                /*return data to user to use the same buffer*/
                mCamera.addCallbackBuffer(data);
                d(TAG, "---Magic happend-----");
                return;
            }

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
/*
 Analyze image
*/

            int result = scanner.scanImage(barcode);

            if (result != 0) {
/*
 ... stop preview ...
*/
                previewing = false;
                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.stopPreview();


/*
 Get and show scanned data
*/

                SymbolSet symbols = scanner.getResults();
                String scanTextResult = null;
                for (Symbol sym : symbols) {
                    scanTextResult = sym.getData();
                    //Toast.makeText(getApplicationContext(), scanTextResult, Toast.LENGTH_LONG).show();
                    barcodeScanned = true;
                }
/*
Return scanned data to the parent activity
*/

                if (barcodeScanned) {
                    Intent intent = new Intent();
                    intent.putExtra("scan_result", scanTextResult);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
            /*return data to user to use the same buffer*/
            mCamera.addCallbackBuffer(data);
        }
    };

    //---Methods---
    public final int REQUEST_CAMERA_PERMISSION = 1;
    private boolean permissionsGranted = false;

    public void checkPermissions() {
        log.info("Checking permissions...");

         /*check or ask for permissions*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                log.info("Lack of permission. Allow? ");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    log.info("Camera permission is allowed");
                    permissionsGranted = true;
                } else {
                    log.info("Camera permission is denied ");
                    permissionsGranted = false;
                }
            } else {
                log.info("Camera permission is allowed");
                permissionsGranted = true;
            }
        } else {
            log.info("Camera permissions are ok");
            permissionsGranted = true;
        }
    }

}

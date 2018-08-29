package com.lis.qr_client.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import com.lis.qr_client.R;
import net.sourceforge.zbar.*;

import java.io.IOException;

import static android.util.Log.d;
import static android.util.Log.e;

//TODO:hardwork in ui thread, if instant run is off the app will crash. Change to AsyncTask the hardwork then u can turn off instant run
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
        threadCameraOpen(CAMERA_ID);
    }

    //---------CameraOpen----------//

    /*
    * Usual safe camera open, HandlerThread that runs camera opening in the new thread;
    * thread camera open which init instance of cameraHandlerThread and runs openCamera method.
    * */

    public void threadCameraOpen(int camera_id){
        if(cameraHandlerThread == null){
            cameraHandlerThread = new CameraHandlerThread("CameraHandlerThread");
        }
        d(TAG, "----Camera Thread handler----");

        cameraHandlerThread.openCamera(camera_id);
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

    /*
    Run openCamera in the additional thread
    */
    class CameraHandlerThread extends HandlerThread{

        Handler handler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            handler = new Handler(getLooper());
        }

        synchronized void cameraOpenNotify(){

            d(TAG, "----Notify!----");
            notify();
        }

        public void openCamera(final int camera_id) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    d(TAG, "----In the run----");
                    safeCameraOpen(camera_id);
                    cameraOpenNotify();

                }
            });

            try {
               synchronized (this){
                   d(TAG, "----Wait for it...----");
                   wait();

               }
            } catch (InterruptedException e) {
                e(TAG, "----Wait was Interrupted----");

            }
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
            mCamera.setPreviewCallbackWithBuffer(null);
            try{
                mCamera.stopPreview();
            }catch (Exception e){

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

            /*
            get the pic size for setting the buffer size
            */
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                int dataBufferSize=(int)(previewSize.height*previewSize.width*
                        (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat())/8.0));
                mCamera.addCallbackBuffer(new byte[dataBufferSize]);


                mCamera.setPreviewCallbackWithBuffer(previewCallback);
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

            if(skipFirstPreviewFrame < 1){
                skipFirstPreviewFrame ++;
                /*return data to user to use the same buffer*/
                mCamera.addCallbackBuffer(data);
                data = null;
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
}

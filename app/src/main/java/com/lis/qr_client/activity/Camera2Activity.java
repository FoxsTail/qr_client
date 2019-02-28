package com.lis.qr_client.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import com.lis.qr_client.R;
import lombok.extern.java.Log;
import net.sourceforge.zbar.ImageScanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log
public class Camera2Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    public static final int BACK_CAMERA_ID = 0;
    public static final int FRONT_CAMERA_ID = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 1;

    //TODO: add flash on button

    CameraManager mCameraManager = null;
    CameraHelper[] mCameraHelpers = null;
    CameraHelper mBackCamera = null;
    CameraHelper mFrontCamera = null;

    HandlerThread mBackgroundThread = null;
    Handler mBackgroundHandler = null;

    boolean mPermissionsGranted = false;

    TextureView mImageView = null;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*check or ask for permissions*/
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                log.info("Lack of permission. Allow? ");
                // return;
            } else {
                log.info("Camera permission is allowed");
                mPermissionsGranted = true;
            }
        } else {
            mPermissionsGranted = true;
        }

        setContentView(R.layout.activity_camera2);

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        mImageView = findViewById(R.id.view_camera);

        /*get all cameras and set textureView*/
        mCameraHelpers = getAllCameras(mCameraManager, mImageView);

        /*get back camera*/
        if (mCameraHelpers != null) {
            mBackCamera = mCameraHelpers[BACK_CAMERA_ID];
            mFrontCamera = mCameraHelpers[FRONT_CAMERA_ID];

            mImageView.setSurfaceTextureListener(this);
            // openBackCamera(mBackCamera, mFrontCamera);

        } else {
            log.info("Can't get cameras");
        }
    }


    /*get response from user "allow ar not to use" */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        log.info("onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGranted = true;
                    log.info("USE");

                } else {
                    mPermissionsGranted = false;
                    log.info("DON'T USE");
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.info("onResume");
        startBackgroundThread();
        if (mImageView != null) {
            if (mImageView.isAvailable() && mPermissionsGranted) {
                openBackCamera(mBackCamera, mFrontCamera);
            } else {
                mImageView.setSurfaceTextureListener(this);
            }
        } else {
            log.info("Can't get the textureView. Null the value is...");
        }
    }

    @Override
    protected void onPause() {
        log.info("onPause");
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.info("onStop");
        mBackCamera.closeCamera();
    }


    //---methods---

    /*Close front camera, open back camera if closed*/
    public void openBackCamera(CameraHelper backCamera, CameraHelper frontCamera) {
        log.info("openBackCamera");
        /*close front camera if open*/
        if (frontCamera.isOpen()) {
            frontCamera.closeCamera();
        }

        /*open back camera*/
        if (backCamera != null) {
            if (!backCamera.isOpen()) {
                backCamera.openCamera();
            }
        } else {
            log.info("Camera with id " + backCamera.getCameraId() + " is null");
        }
    }


    /*Get all the cameras*/
    public CameraHelper[] getAllCameras(CameraManager cameraManager, TextureView textureView) {
        log.info("getAllCameras");
        log.info("Current thread: " + Thread.currentThread());
        CameraHelper[] cameraHelpers = null;

        try {
            String[] cameraList = cameraManager.getCameraIdList();
            cameraHelpers = new CameraHelper[cameraList.length];

            for (String cameraId : cameraList) {
                int id = Integer.parseInt(cameraId);
                log.info("For camera: " + cameraId);
                cameraHelpers[id] = new CameraHelper(cameraManager, cameraId);
                cameraHelpers[id].viewFormatSize(ImageFormat.JPEG);
                cameraHelpers[id].setTextureView(textureView);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return cameraHelpers;
    }

    //---Threads---

    protected void startBackgroundThread() {
        log.info("Start background thread");
        mBackgroundThread = new HandlerThread("Camera background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        log.info("Stop background thread");
        mBackgroundThread.quitSafely();

        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //----SurfaceTexture---------
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        log.info("onSurfaceTextureAvailable");
        log.info("Current thread: " + Thread.currentThread());
        if (mPermissionsGranted) {
            openBackCamera(mBackCamera, mFrontCamera);
        } else {
/*
            Toast.makeText(this, "Lack of camera permissions", Toast.LENGTH_LONG).show();
*/
            log.info("Lack of camera permissions");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    class CameraHelper {

        CameraManager cameraManager = null;
        CameraDevice cameraDevice = null;

        ImageReader imageReader = null;

        TextureView cameraView = null;
        String cameraId = null;

        private Size bufferJpegSize = null;
        private StreamConfigurationMap configurationMap = null;
        private Size bufferYuvSize;

        public CameraHelper(@NonNull CameraManager cameraManager, @NonNull String cameraId) {
            this.cameraManager = cameraManager;
            this.cameraId = cameraId;
        }

        //----common-----

        public boolean isOpen() {
            log.info("Camera2 ------ isOpen camera " + cameraId);
            return cameraDevice != null;
        }

        public void openCamera() {
            log.info("Camera2 ------ openCamera");
            log.info("Current thread: " + Thread.currentThread());
            try {
                cameraManager.openCamera(cameraId, cameraCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        public void closeCamera() {
            log.info("Camera2 ------ closeCamera");
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }

            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        }


        //--------Callbacks-----


        private CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                cameraDevice = camera;
                log.info("Camera2 ------ onOpened -----" + cameraDevice.getId());
                cameraCreatePreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                log.info("Camera2 ------ onDisconnected");
                cameraDevice.close();
                cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                log.info("Camera2 ------ onError ----" + camera.getId() + " error " + error);
                Toast.makeText(context, "Error occurred", Toast.LENGTH_LONG).show();
                ((Camera2Activity) context).finish();


            }
        };

        private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
                log.info("onCaptureStarted");
            }

            @Override
            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                log.info("onCaptureProgressed");
            }

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                log.info("onCaptureCompleted");
            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                log.info("onCaptureFailed");
            }
        };

        private ImageReader.OnImageAvailableListener onImageListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                log.info("---onImageAvailable---");

                Image image = reader.acquireNextImage();
                if (image != null) {
                    log.info("-var-");
                    image.close();
                }else{
                    log.info("-yok-");
                }

            }

        };

        //---------methods-----

        public void cameraCreatePreviewSession() {
            log.info("Camera2 --- createPreviewSession");
            log.info("Current thread: " + Thread.currentThread());


            List<Surface> surfaces = new ArrayList<>();


            SurfaceTexture texture = cameraView.getSurfaceTexture();
            texture.setDefaultBufferSize(bufferJpegSize.getWidth(), bufferJpegSize.getHeight());
            Surface previewSurface = new Surface(texture);

            imageReader = ImageReader.newInstance(bufferYuvSize.getWidth(), bufferYuvSize.getHeight(),
                    ImageFormat.JPEG, 2);

            imageReader.setOnImageAvailableListener(onImageListener, mBackgroundHandler);


            Surface readerSurface = imageReader.getSurface();


            surfaces.add(readerSurface);
            surfaces.add(previewSurface);


            try {
                /*create request and give it surface to output*/
                log.info("...form CaptureRequest...");

                final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                /*output surface*/
                //requestBuilder.addTarget(previewSurface);
                requestBuilder.addTarget(readerSurface);
                requestBuilder.addTarget(previewSurface);

                /*set auto focus*/
                //requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                //requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
                // ???? requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);


                log.info("...createCaptureSession...");
                cameraDevice.createCaptureSession(Arrays.asList(readerSurface, previewSurface), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                log.info("Current thread: " + Thread.currentThread());
                                try {
                                    session.setRepeatingRequest(requestBuilder.build(), null, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {

                            }
                        },
                        null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }


        }

        public void setTextureView(TextureView textureView) {
            cameraView = textureView;
        }

        public void viewFormatSize(int formatSize) {
            CameraCharacteristics characteristics = null;
            try {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);

                configurationMap = characteristics.get
                        (CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size[] jpeg_size = configurationMap.getOutputSizes(ImageFormat.JPEG);
                Size[] yuv_size = configurationMap.getOutputSizes(ImageFormat.JPEG);

                if (yuv_size != null) {
                    log.info("YUV: " );
                    bufferYuvSize = yuv_size[0];
                   /* for (Size size : yuv_size) {
                        log.info("Width: " + size.getWidth() + ", Height: " + size.getHeight());
//check
                    }*/

                }

                if (jpeg_size != null) {
                    log.info("JPEG: " );
                    bufferJpegSize = jpeg_size[0];
/*
                    for (Size size : jpeg_size) {
                        log.info("Width: " + size.getWidth() + ", Height: " + size.getHeight());
//check
                    }

       */         } else {
                    log.info("Camera " + cameraId + "doesn't support jpeg");

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


        //---getters


        public String getCameraId() {
            return cameraId;
        }


    }
}

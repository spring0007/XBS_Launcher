package com.sczn.wearlauncher.camera;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.util.ThreadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CandidCameraDialog extends Dialog {

    private final String TAG = "lqq";
    //public static final String WEARABLE_ROMOTE_CAMERA = "WEARABLE_ROMOTE_CAMERA";
    //public static final String WEARABLE_ROMOTE_CAMERA_OPTUSERID = "WEARABLE_ROMOTE_CAMERA_OPTUSERID";
    //public static final String WEARABLE_SOS_CAMERA = "WEARABLE_SOS_CAMERA";
    private SurfaceView mySurfaceView;
    private SurfaceHolder myHolder;
    private Camera myCamera;
    //private String optUserId;
    private Context mContext;

    /*style name="dialogtheme" parent="Theme.AppCompat.Dialog.MinWidth">
        <item name="android:windowFrame">@null</item>
        <item name="android:windowIsFloating">true</item>
        <item name="android:windowIsTranslucent">false</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowBackground">@color/transparent</item>
        <!-- 这儿也很重要啊，我这儿用了一张透明的.9.png的图，当然用#00000000也是可以的，否则的话这儿出来后有一个黑色的背景 -->
        <item name="android:backgroundDimEnabled">false</item>
    </style>

    * */

    public CandidCameraDialog(@NonNull Context context) {
        this(context,R.style.dialogtheme);
        mContext = context;
    }

    public CandidCameraDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_camera);
        Log.d(TAG, "onCreate");
        initSurface();
        //initParam();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initCamera();
            }
        }).start();
    }



    /**
     *
     */
    @SuppressWarnings("deprecation")
    private void initSurface() {
        mySurfaceView = findViewById(R.id.camera_surfaceview);
        myHolder = mySurfaceView.getHolder();
        //myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

   /* private void initParam() {
        if (getIntent() != null) {
            optUserId = getIntent().getStringExtra(WEARABLE_ROMOTE_CAMERA_OPTUSERID);
            Log.d(TAG, "optUserId = " + optUserId);
        }
    }*/

    /**
     *
     */
    private void initCamera() {
        if (checkCameraHardware(mContext)) {
            if (openFacingFrontCamera()) {
                Log.d(TAG, "openFrontCameraSuccess");
                autoFocus();
            } else {
                Log.d(TAG, "openFrontCameraFailed");
                dismiss();
            }
        }
    }


    /**
     * 聚焦
     */
    private void autoFocus() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myCamera.autoFocus(myAutoFocus);
       // String doWhat = getIntent().getStringExtra("dowhat");
        //Log.d(TAG, "--->>>" + doWhat);
        //if (doWhat.equals(WEARABLE_ROMOTE_CAMERA)) {
            myCamera.takePicture(null, null, remotePicCallback);
        //} else if (doWhat.equals(WEARABLE_SOS_CAMERA)) {
            // myCamera.takePicture(null, null, sosPicCallback);
        //}
    }


    /**
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    private boolean openFacingFrontCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Log.i(TAG, "-->>" + cameraInfo.facing);
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    Log.d(TAG, "tryToOpenFrontCamera");
                    myCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.d(TAG, "CAMERA_FACING_FRONT" + e.toString());
                    return false;
                }
            }
        }
        if (myCamera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        Log.d(TAG, "tryToOpenBackCamera");
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        Log.d(TAG, "CAMERA_FACING_BACK" + e.toString());
                        return false;
                    }
                }
            }
        }
        try {
            myCamera.setPreviewDisplay(myHolder);
            myCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
        return true;
    }

    private void releaseCamera() {
        if (myCamera != null) {
            myCamera.release();
            myCamera = null;
        }
    }

    private Camera.AutoFocusCallback myAutoFocus = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    /**
     * 远程拍照上传
     */
    private Camera.PictureCallback remotePicCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.preRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            final File pictureFile = new File(getRemoteDir(), getPhotoFileName());
            String filePath = pictureFile.getAbsolutePath();
            Log.i(TAG, "pictureFile = " + filePath);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.close();
            } catch (Exception error) {
                Log.d(TAG, "error:" + error.toString());
                error.printStackTrace();
                myCamera.stopPreview();
                myCamera.release();
                myCamera = null;
            }
            Log.d(TAG, "stopPreview");
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;

            /**
             * 需要执行上传图片的操作
             */
            Log.w(TAG, "咔嚓,拍照了:" + filePath);
            ThreadUtil.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    uploadImage(pictureFile, pictureFile.getName());
                }
            });
        }
    };

    /**
     * 执行上传图片的操作
     *compile 'com.squareup.okhttp:okhttp:2.4.0'
     * compile 'com.squareup.okio:okio:1.5.0'
     *
     * @param file
     * @param fileName
     */
    public String uploadImage(File file, String fileName) {
        String uploadResult="";
        /*try {
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", fileName,
                            RequestBody.create(MediaType.parse("image/jpg"), file))
                    .addFormDataPart("imei", Config.IMEI)
                    .addFormDataPart("optUserId", optUserId)
                    .addFormDataPart("uploadTime", TimeUtil.getCurrentTimeSec() + "")
                    .build();
            Request request = new Request.Builder()
                    .url(SocketConstant.fileUpload)
                    .post(requestBody)
                    .build();
            Response response = OkhttpClientManager.getInstance().getmOkHttpClient().newCall(request).execute();
            // 把response转换成string
            uploadResult = response.body().string();
        } catch (IOException e) {
            uploadResult = e.toString();
        }*/
        Log.w(TAG, "<<<--" + uploadResult);
        return uploadResult;
    }

    //
    /**
     * SOS拍照上传
     */
    int count = 0;
    String picPath1 = null;
    String picPath2 = null;
    String picPath3 = null;
    /*private PictureCallback sosPicCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.preRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            //
            File pictureFile = new File(getSOSDir(), getPhotoFileName());
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.close();
            } catch (Exception error) {
                Log.d("Demo", "error:" + error.toString());
                error.printStackTrace();
                myCamera.stopPreview();
                myCamera.release();
                myCamera = null;
            }

            Log.d("Demo", "stopPreview");
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
            if (++count < 3) {
                if (count == 1) {
                    picPath1 = pictureFile.toString();
                } else if (count == 2) {
                    picPath2 = pictureFile.toString();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //camera
                        initCamera();
                    }
                }).start();
            } else {
                picPath3 = pictureFile.toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        final String[] picPaths = new String[]{picPath1, picPath2, picPath3};
                        final String urlstr = "http://s1.zjrt9999.com:8090/Document/UploadAlarmData?deviceId="
                                + Util.getIMEI(CameraActivity.this) +
                                "&alarmType=" + 1 +
                                "&uniqueId=" + System.currentTimeMillis() +
                                "&locations=" + "";
                        try {
                            Boolean upload = HttpUtil.uploadFile(picPaths, urlstr);
                            if (upload) {
                                //删除文件夹
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
                CameraActivity.this.finish();
            }
        }
    };*/

    /**
     * sos照片 存放地址
     *
     * @return
     */
    private File getSOSDir() {
        File dir = new File(Environment.getExternalStorageDirectory().toString() + "/SOSCamera");
        if (dir.exists()) {
            return dir;
        } else {
            dir.mkdirs();
            return dir;
        }
    }

    /**
     * 远程拍照片 存放地址
     *
     * @return
     */
    private File getRemoteDir() {
        File dir = new File(Environment.getExternalStorageDirectory().toString() + "/RemoteCamera");
        if (dir.exists()) {
            return dir;
        } else {
            dir.mkdirs();
            return dir;
        }
    }

    /**
     * 拍照相片的名称
     *
     * @return
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.getDefault());
        return dateFormat.format(date) + ".jpg";
    }
}

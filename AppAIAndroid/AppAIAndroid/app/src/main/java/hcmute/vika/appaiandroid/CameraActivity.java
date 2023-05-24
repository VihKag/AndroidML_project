package hcmute.vika.appaiandroid;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

public class CameraActivity extends org.opencv.android.CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private  static  final String TAG="activityMain";
    private  Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpencvCameraView;
    private DetectionModel detectionModel;
    private ImageButton imgSwitchCamera;
    private static final int CAMERA_FRONT = 98;
    private static final int CAMERA_BACK =99;
    private int currentCamera = CAMERA_BACK;
    private final BaseLoaderCallback mLoaderCallBack= new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i(TAG,"Opencv is loaded");
                    mOpencvCameraView.enableView();
                }
                break;
                default:{
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    public  CameraActivity(){
        Log.i(TAG,"Open camera"+this.getClass());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        int PERMISSION_CAMERA=1;
        //Cấp quyền camera
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this,new String[] {Manifest.permission.CAMERA},PERMISSION_CAMERA);
        }
        try {
            //inputsize là kích thước model

            detectionModel=new DetectionModel(getAssets(), "ssd_mobilenet_v1_1_metadata_1.tflite", "labelmap.txt",300);
            detectionModel.setCamera(currentCamera);
            Log.d("MainActivity","Model is successfully loaded");
        }
        catch (Exception e){
            Log.d("MainActivity","ERROR");

            e.printStackTrace();
        }

        mOpencvCameraView=(CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        mOpencvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpencvCameraView.setCameraIndex(currentCamera);
        mOpencvCameraView.setCvCameraViewListener(this);
        imgSwitchCamera=findViewById(R.id.switch_camera_button);
        imgSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

    }
    private void switchCamera() {
        if (currentCamera == CAMERA_BACK) {
            mOpencvCameraView.disableView();
            mOpencvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            mOpencvCameraView.enableView();
            currentCamera = CAMERA_FRONT;
            mOpencvCameraView.setCameraIndex(currentCamera);
            detectionModel.setCamera(currentCamera);
        } else {
            mOpencvCameraView.disableView();
            mOpencvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
            mOpencvCameraView.enableView();
            currentCamera = CAMERA_BACK;
            mOpencvCameraView.setCameraIndex(currentCamera);
            detectionModel.setCamera(currentCamera);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV initializer");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            if (currentCamera == CAMERA_FRONT) {
                mOpencvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            } else {
                mOpencvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
            }
            mOpencvCameraView.enableView();
        } else {
            Log.d(TAG, "OpenCV initializer not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallBack);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpencvCameraView!=null){
            mOpencvCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpencvCameraView!=null){
            mOpencvCameraView.disableView();

        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray=new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        Mat out=new Mat();
        out=detectionModel.recognizeImage(mRgba);
        return out;
    }
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections. singletonList(mOpencvCameraView);
    }

}
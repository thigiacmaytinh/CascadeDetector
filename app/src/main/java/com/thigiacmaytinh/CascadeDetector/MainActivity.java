package com.thigiacmaytinh.CascadeDetector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener{

    CameraView mOpenCvCameraView;
    TextView lblCount;
    private ImageView imgView;
    private ImageButton btnFlash, btnCapture, btnSetting;
    Spinner cbCascade;

    private Mat mRgba, mGray;
    private boolean isFlashEnable = false;
    RelativeLayout mContainerView;
    LayoutInflater inflater;
    View settingView;
    EditText txtScaleFactor, txtMinNeighbors, txtMinSize, txtMaxSize;
    float m_scaleFactor = 0;
    CascadeClassifier  mJavaDetector;
    int mAbsoluteFaceSize   = 0;
    float mRelativeFaceSize   = 0.2f;

    private static final String TAG = "TGMT";
    public static final int CAMERA_REQUEST = 1;
    ArrayList cascadeList = new ArrayList<String>();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RequestCameraPermission();

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setFocusable(true);

        lblCount = findViewById(R.id.lblCount);
        imgView = findViewById(R.id.imageView);
        btnCapture  = findViewById(R.id.btnCapture);
        btnFlash = findViewById(R.id.btnFlash);
        btnSetting = findViewById(R.id.btnSetting);

        //set size of camera view
        int width = 1280;
        int height = 720;
        mOpenCvCameraView.setMinimumHeight(height);
        mOpenCvCameraView.setMinimumWidth(width);
        mOpenCvCameraView.setMaxFrameSize(width, height);

        CheckFlashlight();
        CheckEnableCheat();
        LoadSetting();

        mContainerView = findViewById(R.id.mainview);
        inflater =(LayoutInflater)getSystemService(this.LAYOUT_INFLATER_SERVICE);
        settingView = inflater.inflate(R.layout.activity_setting, null);

        cascadeList.add("lbpcascade_frontalface");
        cascadeList.add("Bike plate");
        cascadeList.add("Car plate");
        //TODO: add ads
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    InitCascade(0);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
                        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void RequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        inputFrame.rgba().copyTo(mRgba);
        inputFrame.gray().copyTo(mGray);
        return mRgba;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean onTouch(View v, MotionEvent event)
    {
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnCapture_onClick(View v) {

        if(imgView.getVisibility() == View.INVISIBLE)
        {
            imgView.setVisibility(View.VISIBLE);
            SetImage();
        }
        else
        {
            imgView.setVisibility(View.INVISIBLE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnFlash_onClick(View v)
    {
        if(isFlashEnable)
        {
            btnFlash.setImageResource(R.drawable.noflash256);
        }
        else
        {
            btnFlash.setImageResource(R.drawable.flash256);
        }
        mOpenCvCameraView.setupCameraFlashLight();
        isFlashEnable = !isFlashEnable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnSetting_onClick(View v)
    {
        mContainerView.addView(settingView);

        SetMainViewVisibility(false);

//        if(m_scaleFactor == 0) {
//            Display display = getWindowManager().getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            m_scaleFactor = size.x / 640;
//        }

        if(cbCascade == null)
            cbCascade = findViewById(R.id.cbCascade);
        if(txtScaleFactor == null)
            txtScaleFactor = findViewById(R.id.txtScaleFactor);
        if(txtMinNeighbors == null)
            txtMinNeighbors = findViewById(R.id.txtMinNeighbors);
        if(txtMinSize == null)
            txtMinSize = findViewById(R.id.txtMinSize);
        if(txtMaxSize == null)
            txtMaxSize = findViewById(R.id.txtMaxSize);

        if(cbCascade != null)
        {
            InitCascadeList();
            cbCascade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    InitCascade(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnBack_onClick(View v)
    {
        HideSettingLayout();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void HideSettingLayout()
    {
        SaveSetting();

        mContainerView.removeView(settingView);
        SetMainViewVisibility(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void SetImage()
    {
        Bitmap bitmap;
        try
        {
            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }


            MatOfRect faces = new MatOfRect();
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            Rect[] facesArray = faces.toArray();

            lblCount.setText("Detected: " + facesArray.length);

            Scalar colorGreen = new Scalar(0, 0, 255, 255);
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), colorGreen, 2);
            }

            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
            imgView.setImageBitmap(bitmap);
            imgView.invalidate();
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed()
    {
        if(imgView.getVisibility()==View.VISIBLE)
        {
            imgView.setVisibility(View.INVISIBLE);
            return;
        }
        if(mContainerView.getChildCount() > 7)
        {
            HideSettingLayout();
            return;
        }
        ShowExitDialog();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void ShowExitDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn muốn thoát chương trình?") //
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO
                        System.exit(0);
                        dialog.dismiss();
                    }
                }) //
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void CheckFlashlight()
    {
        if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            btnFlash.setVisibility(View.GONE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void CheckEnableCheat()
    {
        Registry.enableCheat = FileProcess.Exist(Common.path + "/debug.txt");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void LoadSetting()
    {
        SharedPreferences pre=getSharedPreferences(Common.prefName, MODE_PRIVATE);

        Registry.inverse = pre.getBoolean("inverse", Registry.DEFAULT_INVERSE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void lblIntro_onClick(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("App cascade detector dùng để phát hiện vật thể bằng cascade model " +
                        "Phần mềm được viết bởi tác giả Võ Hùng Vĩ.\n\n" +
                        "vohungvi@vohungvi.com \n" +
                        "http://thigiacmaytinh.com" )
                .setPositiveButton("Xem trang web", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Uri uriUrl = Uri.parse("https://thigiacmaytinh.com/");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }) //
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void SaveSetting()
    {
        SharedPreferences pre=getSharedPreferences(Common.prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor=pre.edit();

        editor.putBoolean("inverse", Registry.inverse);
        editor.commit();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void SetMainViewVisibility(boolean enable)
    {
        if(enable)
        {
            btnFlash.setVisibility(View.VISIBLE);
            btnSetting.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);
        }
        else
        {
            btnFlash.setVisibility(View.INVISIBLE);
            btnSetting.setVisibility(View.INVISIBLE);
            btnCapture.setVisibility(View.INVISIBLE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void InitCascade(int position)
    {
        int rawResource = R.raw.lbpcascade_frontalface;
        switch (position)
        {
            case 1:
                rawResource = R.raw.bike_plate;
                break;
            case 2:
                rawResource = R.raw.bike_plate;
                break;
            default:
                break;
        }
        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(rawResource);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnReset_onClick(View view) {
        txtScaleFactor.setText("1.1");
        txtMinNeighbors.setText("3");
        txtMinSize.setText("0");
        txtMaxSize.setText("0");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void lblTitle_onClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse("https://thigiacmaytinh.com"));
        startActivity(browserIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void InitCascadeList()
    {
        ArrayAdapter<String> adaptCascade = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, cascadeList);

        cbCascade.setAdapter(adaptCascade);
    }
}

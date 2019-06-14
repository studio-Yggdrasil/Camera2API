package yggdrasil.camerasee.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.SleepThread;
import yggdrasil.camerasee.adpater.EffectAdapter;
import yggdrasil.camerasee.adpater.SenseAdapter;
import yggdrasil.camerasee.callback.PreviewSessionCallback;
import yggdrasil.camerasee.converter.GPS;
import yggdrasil.camerasee.listener.AwbSeekBarChangeListener;
import yggdrasil.camerasee.listener.EffectItemClickListener;
import yggdrasil.camerasee.listener.FlashItemClickListener;
import yggdrasil.camerasee.listener.GPSLocationListener;
import yggdrasil.camerasee.listener.SenseItemClickListener;
import yggdrasil.camerasee.listener.TextureViewTouchEvent;
import yggdrasil.camerasee.listener.TimerItemClickListener;
import yggdrasil.camerasee.settings.SettingsActivity;
import yggdrasil.camerasee.view.AnimationFocusView;
import yggdrasil.camerasee.view.AnimationTextView;
import yggdrasil.camerasee.view.GridView;
import yggdrasil.camerasee.view.MyTextureView;
import yggdrasil.camerasee.view.SeekBar_Awb;
import yggdrasil.camerasee.view.SeekBar_Vertical;


public class DisplayFragment extends Fragment implements View.OnClickListener {

    String TAG = "로그 displayfragment";

    Context context;
    View inflate;

    static int DSI_height, DSI_width;


    private static final int STATE_PREVIEW = 0;//Camera state: Showing camera preview.
    private static final int STATE_WAITING_LOCK = 1;//Camera state: Waiting for the focus to be locked.
    private static final int STATE_WAITING_PRECAPTURE = 2;//Camera state: Waiting for the exposure to be precapture state.
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;//Camera state: Waiting for the exposure state to be something other than precapture.
    private static final int STATE_PICTURE_TAKEN = 4;//Camera state: Picture was taken.

    static int StateFocus = 1;
    public static final int FOCUS_MANUAL = 0;
    public static final int FOCUS_AUTO = 1;
    public static final int FOCUS_CONTINOUS = 2;
    public static final int FOCUS_HALF_SHUTTER = 3;

    private int mState = 0;



    public static final int FOCUS_SUCCESS = 100;
    public static final int FOCUS_DISAPPEAR = 101;
    public static final int WINDOW_TEXT_DISAPPEAR = 102;
    public static final int FOCUS_AGAIN = 103;

    private static final int SHOW_AF = 1;
    private static final int SHOW_AWB = 3;
    private static final int SHOW_ISO = 4;
    private static final int SHOW_ZOOM = 5;


    private SharedPreferences sharedPref, sharedPrefcamera;
    private SharedPreferences.Editor mEditor;

    private File mFile;
    static String mPath;



    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private MyTextureView mTextureView;
    private Size mPreviewSize;


    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder mCaptureBuilder;


    private CameraManager mCameraManager;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private CameraCharacteristics mCameraCharacteristics;


    //-------camera 변수-------
    private String mCameraId = "0";
    static String size_camera_0;
    static String size_camera_1;
    //-------camera 변수-------


    private int mFormat;

    private ImageReader mImageReader;


    private CameraCaptureSession mCameraCaptureSession;
    private PreviewSessionCallback mPreviewSessionCallback;

    private boolean showAfFlag = false;
    private LinearLayout mLayoutAf;

    private boolean showAeFlag = false;
    private boolean showAwbFlag = false;
    private boolean showIsoFlag = false;

    private LinearLayout mLayoutIso;
    private boolean showZoomFlag = false;




    private RelativeLayout mControlBottom;
    private RelativeLayout mLayoutCapture;

    private List<View> mLayoutList;

    private TranslateAnimation mShowAction;
    private ScaleAnimation mScaleFocusAnimation;


    static Animation Close_Full, Open_Full,Rotate_Clockwise, Rotate_AntiClockwise, animation_Grid_In, animation_Grid_Out, alpha_In, alpha_out, Rotate_Icon_Clock, Rotate_Icon_Anticlock;


    private Surface mSurface;
    private TextView mSeekBarTextView;


    private AnimationTextView mWindowTextView;
    private SeekBar_Awb mAwbSb;
    private SeekBar_Vertical mSeekbar_Exposure;

    private TextView mText_exposure;

    private ImageView mBtnEffect;
    private ImageView mBtnFlash;
    private ImageView mBtnSense;
    private ImageView mBtnChangeCamera;
    private ImageView thumFrame;
    private ImageView btnFocus;
    private ImageView btnAwb;
    private ImageView btnIso;
    private ImageView mCamereMenu;
    private ImageView btnSetting;
    private ImageView btnGrid;
    private ImageView btnTimer;


    private GridView gridView;
    private AnimationFocusView mManualFocusImage;
    private int mToPreviewWidth;
    private int mToPreviewHeight;
    private List<Surface> mOutputSurfaces;
    private Size mlargest;


    private SeekBar seekBar_AF;
    //----------------------------seekbar------------------------
    //Initialization of the words are practical value.
    private float valueAF;
    private int valueAE;
    private long valueAETime;
    private int valueISO;
    //----------------------------seekbar-------------------------

    //preference boolean------------------------------------------
    static boolean SoundShutterOn;
    static boolean SoundFocusOn;
    static boolean TagGPSOn;
    //preference boolean------------------------------------------


    GPSLocationListener gpsLocationListener;
    static LocationManager locationManager;
    static Location location;

    OrientationEventListener orientEventListener;

    static TextView Orientation;


    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //TODO 핸들러로 포커스 상태제어... HOW???
                case FOCUS_SUCCESS:
                    Log.d(TAG,"포커스 success!");
                    if(isSoundFocusOn()) {
                        soundPool.play(soundIds[1], 1, 1, 1, 0, 1f);
                    }
                    break;
                case FOCUS_CONTINOUS:
                    Log.d(TAG,"포커스 컨티뉴어스로 전환 !");

                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    updatePreview();

                    break;
                case FOCUS_DISAPPEAR:
                    Log.d(TAG,"FOCUS_DISAPPEAR");
                    if (msg.obj == null) {
                        mManualFocusImage.stopFocus();
                        break;
                    }
                    Integer valueTimes = (Integer) msg.obj;
                    if (mManualFocusImage.mTimes == valueTimes.intValue()) {
                        mManualFocusImage.stopFocus();
                    }
                    break;

                case WINDOW_TEXT_DISAPPEAR:
                    if (msg.obj == null) {
                        break;
                    }
                    Integer valueTimes2 = (Integer) msg.obj;
                    if (mWindowTextView.mTimes == valueTimes2.intValue()) {
                        mWindowTextView.stop();
                    }
                    break;

                case FOCUS_AGAIN:
                    Log.i("FOCUS_AGAIN", "FOCUS_AGAINFOCUS_AGAINFOCUS_AGAIN");
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                    updatePreview();
                    break;

            }
        }
    };


    static final String ARG_PARAM1 = "param1";
    static final String ARG_PARAM2 = "param2";


    static String LagistSizeCamera0, LagistSizeCamera1;

    public static DisplayFragment newInstance(String param1, String param2) {
        DisplayFragment fragment = new DisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    public static String getLagistSizeCamera0() {
        return LagistSizeCamera0;
    }

    public static String getLagistSizeCamera1() {
        return LagistSizeCamera1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            LagistSizeCamera0 = getArguments().getString(ARG_PARAM1);
            LagistSizeCamera1 = getArguments().getString(ARG_PARAM2);
        }



        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);


        size_camera_0 =  sharedPref.getString("selected_size_camera_0",LagistSizeCamera0);
        size_camera_1 =  sharedPref.getString("selected_size_camera_1",LagistSizeCamera1);
        Log.d("로그 카메라사이즈 노 번들","camera0 : "+size_camera_0+"camera1 : "+size_camera_1);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;




    }



    @Override
    public void onStart() {
        super.onStart();

        initSoundPool();
        initPreparence();


    }


    public void initPreparence() {
        SoundShutterOn = sharedPref.getBoolean("SoundShutter", true);
        SoundFocusOn = sharedPref.getBoolean("SoundFocus", true);
        TagGPSOn  = sharedPref.getBoolean("TagGPS", false);
    }

    public static boolean isSoundShutterOn() {
        return SoundShutterOn;
    }

    public static boolean isSoundFocusOn() {
        return SoundFocusOn;
    }

    public static boolean isTagGPSOn() { return TagGPSOn; }

    static int[] soundIds;


    static SoundPool soundPool;
    public void initSoundPool() {
        //AudioAttributes audioAttributes = new AudioAttributes.Builder()
        //        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        //        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        //        .build();
        soundPool = new SoundPool.Builder().setMaxStreams(3).build();

        soundIds = new int[3];
        // fill your sounds
        soundIds[0] = soundPool.load(getActivity(), R.raw.camera_shutter_click, 1);
        soundIds[1] = soundPool.load(getActivity(), R.raw.camera_focus_beep, 1);
        soundIds[2] = soundPool.load(getActivity(), R.raw.camera_timer_1, 1);


    }


    public void closeSoundPool() {
        for (int i = 0 ; i < soundIds.length ; i++) {
            soundIds[i] = 0;
        }

        soundPool.release();
        soundPool = null;
    }

    static int ScreenOrientation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.display_camera_frag, null);




        initAnimation();
        initUIAndListener(inflate);
        initSeekBarValue();
        initFocusImage();


        thumFrame = inflate.findViewById(R.id.thumnail);


        Orientation = inflate.findViewById(R.id.textViewOrientation);


        orientEventListener = new OrientationEventListener(getActivity(),
                SensorManager.SENSOR_DELAY_NORMAL) {
//TODO Orientation Listener
            @Override
            public void onOrientationChanged(int arg0) {
                //Orientation.setText("Orientation: " + String.valueOf(arg0));
                //arg0: 기울기 값
//TODO 애니메이션 및 딜레이 설정하여 자연스럽게 UI변경이 되도록 설정해 주어야함.
                // 0˚ (portrait)
                if(arg0 >= 315 || arg0 < 45) {
                    ScreenOrientation = 0;

                }

                // 90˚
                else if(arg0 >= 45 && arg0 < 135) {
                    ScreenOrientation = 270;
                }

                // 180˚
                else if(arg0 >= 135 && arg0 < 225) {
                    ScreenOrientation = 180;
                }

                // 270˚ (landscape).
                else if(arg0 >= 225 && arg0 < 315) {
                    ScreenOrientation = 90;
                }
                if (mTextureView.isAvailable()) {
                    RotateImage();
                }
                //Log.d("로그 회전 확인",String.valueOf(ScreenOrientation));
                //Log.d("로그 위치 확인",String.valueOf(location));
            }

        };

        if (orientEventListener.canDetectOrientation()) {
            //Toast.makeText(getActivity(), "Can DetectOrientation", Toast.LENGTH_LONG).show();
            orientEventListener.enable();
        } else {
            //Toast.makeText(getActivity(), "Can't DetectOrientation", Toast.LENGTH_LONG).show();

        }
        return inflate;
    }


    static int m_nOldRotation;

    private void RotateImage() {
        Animation anim_left = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_anticlockwise2);
        Animation anim_right = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_clockwise2);
        Animation anim_180 = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_180);
        Animation anim_180_re = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_180_reverse);
        int nCurrentRotation = ScreenOrientation;
        if (((m_nOldRotation == 0) && (nCurrentRotation == 90))) {

            RotateIcons(Rotate_Icon_Clock);

        } else if (((m_nOldRotation == 0) && (nCurrentRotation == 270))) {

            RotateIcons(anim_left);
        }
        if (((m_nOldRotation == 270) && (nCurrentRotation == 0))) {

            RotateIcons(anim_right);

        } else if (((m_nOldRotation == 270) && (nCurrentRotation == 180))) {

            RotateIcons(anim_left);
        }
        if (((m_nOldRotation == 180) && (nCurrentRotation == 270))) {

            RotateIcons(Rotate_Icon_Clock);

        } else if (((m_nOldRotation == 180) && (nCurrentRotation == 90))) {

            RotateIcons(anim_left);
        }
        if (((m_nOldRotation == 90) && (nCurrentRotation == 180))) {

            RotateIcons(anim_right);

        } else if (((m_nOldRotation == 90) && (nCurrentRotation == 0))) {

            RotateIcons(Rotate_Icon_Anticlock);
        }


        m_nOldRotation = nCurrentRotation;


        //Log.d("로그 RotateImage",String.valueOf(m_nOldRotation));
        //Log.d("로그 타이머셋트",String.valueOf(stateTimer));
    }


    public static void setStateTimer(int stateTimer) {
        DisplayFragment.stateTimer = stateTimer;
    }




    public void RotateIcons(Animation anim) {
        mBtnFlash.startAnimation(anim);
        mBtnChangeCamera.startAnimation(anim);
        thumFrame.startAnimation(anim);
        btnFocus.startAnimation(anim);
        btnGrid.startAnimation(anim);
        btnTimer.startAnimation(anim);
        mCamereMenu.startAnimation(anim);


        //textTimer.startAnimation(anim);
        //btnAwb.startAnimation(anim);
        //btnIso.startAnimation(anim);
        //mBtnSense.startAnimation(anim);
        //mBtnEffect.startAnimation(anim);
        //btnSetting.startAnimation(anim);

    }

    private void initAnimation() {
        Open_Full = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.open_full);
        Close_Full = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.close_full);

        Rotate_Clockwise = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_clockwise);
        Rotate_AntiClockwise = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_anticlockwise);

        animation_Grid_In = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);
        animation_Grid_Out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);

        alpha_In = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.alpha_in);
        alpha_out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.alpha_out);

        Rotate_Icon_Clock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_clockwise);
        Rotate_Icon_Anticlock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_anticlockwise);

        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);

        mScaleFocusAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleFocusAnimation.setDuration(300);

    }




    private void initUIAndListener(View v) {
        mTextureView = (MyTextureView) v.findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mSeekBarTextView = (TextView) v.findViewById(R.id.txt_sb_txt);
        mSeekBarTextView.setVisibility(View.INVISIBLE);

        mWindowTextView = (AnimationTextView) v.findViewById(R.id.txt_window_txt);
        mWindowTextView.setVisibility(View.INVISIBLE);
        mWindowTextView.setmAnimation(alpha_In);
        mWindowTextView.setmMainHandler(mMainHandler);

        mManualFocusImage = (AnimationFocusView) v.findViewById(R.id.img_focus);
        mManualFocusImage.setVisibility(View.INVISIBLE);
        mManualFocusImage.setmMainHandler(mMainHandler);
        mManualFocusImage.setmAnimation(mScaleFocusAnimation);

        mControlBottom = (RelativeLayout) v.findViewById(R.id.control_bottom);

        mLayoutAf = (LinearLayout) v.findViewById(R.id.layout_focus);
        seekBar_AF = (SeekBar) v.findViewById(R.id.seekbar_focus);

        LinearLayout layoutAe = (LinearLayout) v.findViewById(R.id.layout_ae);
        Switch switchAe = (Switch) v.findViewById(R.id.switch_ae);
        SeekBar sbAe = (SeekBar) v.findViewById(R.id.sb_ae);

        LinearLayout layoutAwb = (LinearLayout) v.findViewById(R.id.layout_awb);
        mAwbSb =  v.findViewById(R.id.sb_awb);

        mLayoutIso = (LinearLayout) v.findViewById(R.id.layout_iso);
        Switch switchIso = (Switch) v.findViewById(R.id.switch_iso);
        SeekBar sbIso = (SeekBar) v.findViewById(R.id.sb_iso);

        LinearLayout layoutZoom = (LinearLayout) v.findViewById(R.id.layout_zoom);
        SeekBar sbZoom = (SeekBar) v.findViewById(R.id.sb_zoom);

        TextView mTextExposure = v. findViewById(R.id.text_exposure);
        mSeekbar_Exposure = v.findViewById(R.id.seekbar_exposure);
        mText_exposure = v.findViewById(R.id.text_exposure);
//TODO seekbar 리스너 부분

        mLayoutCapture = (RelativeLayout) v.findViewById(R.id.layout_capture);


        final ImageView btnCapture = (ImageView) v.findViewById(R.id.btn_capture);

        mBtnChangeCamera = (ImageView) v.findViewById(R.id.btn_change_camera);

        mBtnFlash = (ImageView) v.findViewById(R.id.btn_flash);
        mBtnFlash.setImageResource(R.drawable.btn_flash_off);

        btnTimer = (ImageView) v.findViewById(R.id.btn_timer);
        btnTimer.setImageResource(R.drawable.ic_timer);

        btnFocus = (ImageView) v.findViewById(R.id.btn_focus);

        mCamereMenu = (ImageView) v.findViewById(R.id.btn_menu);

        btnSetting = (ImageView) v.findViewById(R.id.btn_setting);

        btnAwb = (ImageView) v.findViewById(R.id.btn_awb);
        btnIso = (ImageView) v.findViewById(R.id.btn_iso);
        mBtnEffect = (ImageView) v.findViewById(R.id.btn_effect);
        mBtnSense = (ImageView) v.findViewById(R.id.btn_sense);
        btnGrid = (ImageView) v.findViewById(R.id.btn_grid);


        gridView = v.findViewById(R.id.grid_frame);




        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

//TODO 반셔터 기능을 구현하기 위한 DSLR 반셔터 작동 매커니즘 참조. 구글카메라 앱에서는 반셔터가 작동하는것으로 확인.
// 삼성과 LG 기본 카메라의 경우에는 반셔터 대신 연사기능이 작동.



                        Log.i("setOnTouchListener", "MotionEvent.ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_UP:

                        takePictureTimer();

                        Log.i("setOnTouchListener", "MotionEvent.ACTION_UP");
                        break;

                }

                return true;
            }
        });

        //button
        mCamereMenu.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        mBtnChangeCamera.setOnClickListener(this);
        btnFocus.setOnClickListener(this);
        btnAwb.setOnClickListener(this);
        btnIso.setOnClickListener(this);

        mBtnEffect.setOnClickListener(this);
        mBtnFlash.setOnClickListener(this);
        btnTimer.setOnClickListener(this);


        mBtnSense.setOnClickListener(this);
        btnGrid.setOnClickListener(this);

        //switch
        MyOnCheckedChangeListener myOnClickChangeListener = new MyOnCheckedChangeListener();

        switchAe.setOnCheckedChangeListener(myOnClickChangeListener);
        switchIso.setOnCheckedChangeListener(myOnClickChangeListener);

        switchAe.setChecked(true);
        switchIso.setChecked(true);


        //seekbar
        MySeekBarListener listener = new MySeekBarListener();
        sbAe.setOnSeekBarChangeListener(listener);
        sbIso.setOnSeekBarChangeListener(listener);
        sbZoom.setOnSeekBarChangeListener(listener);
        mSeekbar_Exposure.setOnSeekBarChangeListener(listener);



        seekBar_AF.setOnSeekBarChangeListener(listener);

        sbIso.setEnabled(false);
        seekBar_AF.setMax(100);

        sbAe.setMax(100);
        sbIso.setMax(100);
        sbZoom.setMax(300);

        mSeekbar_Exposure.setMax(100);
        seekBar_AF.setProgress(50);
        sbAe.setProgress(50);
        sbIso.setProgress(50);
        sbZoom.setProgress(0);
        mSeekbar_Exposure.setProgress(50);

        textTimer = v.findViewById(R.id.text_timer);
        //list ,add
        mLayoutList = new ArrayList<View>();
        mLayoutList.add(mControlBottom);
        mLayoutList.add(mLayoutAf);
        mLayoutList.add(layoutAe);
        mLayoutList.add(layoutAwb);
        mLayoutList.add(mLayoutIso);
        mLayoutList.add(layoutZoom);
    }

    static TextView textTimer;
    public static void setFocusStateTouch(int Focusstate) {
        StateFocus = Focusstate;
        Log.d("로그 setFocusStateTouch",String.valueOf(StateFocus));
    }



    @Override
    public void onResume() {
        super.onResume();
        size_camera_0 = sharedPref.getString("selected_size_camera_0",LagistSizeCamera0);
        size_camera_1 = sharedPref.getString("selected_size_camera_1",LagistSizeCamera1);



        if (mTextureView.isAvailable()) {
            Log.d("로그 onResume","mTextureView.isAvailable()");
            //TODO 카메라를 열때 사이즈를 설정해주는 부분 확인함, 하단의 변수를 사용가능한 사진크기로 치환해줄것
                if (mCameraId.equals("0")) {
                    mBtnChangeCamera.setImageResource(R.drawable.btn_camera_front);
                    try {
                        Log.d("로그 리오픈 카메라0", size_camera_0);

                        openCamera(Size.parseSize(size_camera_0).getWidth(), Size.parseSize(size_camera_0).getHeight());


                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (mCameraId.equals("1")) {
                    mBtnChangeCamera.setImageResource(R.drawable.btn_camera_rear);
                    try {
                        Log.d("로그 리오픈 카메라1", size_camera_1);
                        openCamera(Size.parseSize(size_camera_1).getWidth(), Size.parseSize(size_camera_1).getHeight());


                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

        } else {
            Log.d("로그 onResume","else");
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        if (isTagGPSOn()) {
            gpsLocationListener = new GPSLocationListener(locationManager, location);
            location = gpsLocationListener.getLocation();

            if (location !=null) {
                beforeLocation = location;
            }
        }

        Log.d("로그 mTextureView 사이즈",String.valueOf(mTextureView.getWidth())+"  x  "+String.valueOf(mTextureView.getHeight()));
        Log.d("로그 셔터사운드 ",String.valueOf(isSoundShutterOn()));
        Log.d("로그 포커스사운드 ",String.valueOf(isSoundFocusOn()));

        Log.d("로그 로케이션 ",String.valueOf(location));
    }


    public static void setLocation(Location location) {
        DisplayFragment.location = location;
    }

    public static void setBeforeLocation(Location beforeLocation) {
        DisplayFragment.beforeLocation = beforeLocation;
    }

    public static void setStateFlash(int stateFlash) {
        DisplayFragment.stateFlash = stateFlash;
    }

    private void intflashstate() {
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        stateFlash = 0;
    }

    private void initSeekBarValue() {
        valueAF = 5.0f;
        valueAETime = (214735991 - 13231) / 2;
        valueISO = (10000 - 100) / 2;
        valueAE = 0;
    }



    private void initFocusImage() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mManualFocusImage.setLayoutParams(layoutParams);
        mManualFocusImage.initFocus();
    }




    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            //startBackgroundThread();

            //new Thread(new ImageSaver(reader.acquireNextImage(), mFile)).start();
            startBackgroundThread();
            Image image = null;
            image = reader.acquireLatestImage();
            mBackgroundHandler.post(new ImageSaver(image, mFile));
            //mHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));


        }

    };

    private HandlerThread mBackgroundThread;//An additional thread for running tasks that shouldn't block the UI.
    private Handler mBackgroundHandler;//A {@link Handler} for running tasks in the background.


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    static boolean GridOn = false;






    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);





            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private CameraCaptureSession.CaptureCallback mCaptureCallback//A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            //Log.d(TAG,"mCaptureCallback");
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }

                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }

                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private void captureStillPicture() {
        Log.d(TAG,"captureStillPicture");
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            ApplyPhotoEffect(captureBuilder);//이미지 효과를 적용하는 부분

            // Use the same AE and AF modes as the preview.
            //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //setAutoFlash(captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {


                    unlockFocus();
                }
            };

            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
            mCameraCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public int mTimes = 0;
    private CaptureRequest mPreviewRequest;

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            //TODO 수동 조절값을 그대로 받아올때 어떻게 할것인지?
            //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            //setAutoFlash(mPreviewRequestBuilder);

            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);

            if (StateFocus == FOCUS_CONTINOUS) {
                new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_CONTINOUS, 50, Integer.valueOf(mTimes))).start();
            } else if (StateFocus == FOCUS_MANUAL) {

            }
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mHandler);
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }


    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private  class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;


        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                new SaveImageTask().execute(bytes);

                //output = new FileOutputStream(mFile);
                //output.write(bytes);

            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }





    private  class SaveImageTask extends AsyncTask<byte[], Void, Void> {


        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/TESTCam");
                dir.mkdirs();

                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                mPath = dir.toString() + "/TESTCam" + now + ".jpg";

                //thumFrame = inflate.findViewById(R.id.thumnail);
                //Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Images.Thumbnails.MINI_KIND);
                //thumFrame.setImageBitmap(thumb);

                File outFile = new File(mPath);

                outStream = new FileOutputStream(mPath);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();


                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outFile.getAbsolutePath());

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//스크린샷 생성 파일을 미디어 스캐닝
                Uri contentUri = Uri.fromFile(outFile);
                mediaScanIntent.setData(contentUri);
                getActivity().sendBroadcast(mediaScanIntent);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            AddThumnail();
            if (isTagGPSOn()) {
                intTagGPS();
            }
        }
    }


static Location beforeLocation;



    public void intTagGPS() {
        if (location == null) {
            location = beforeLocation;
            gpsLocationListener.onLocationChanged(location);
        } else {
        }


        try {
            ExifInterface exif = new ExifInterface(mPath);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(location.getLongitude()));
            exif.saveAttributes();


            Log.d(TAG,"로그 exif 기록 GPS"+String.valueOf(exif));
            Log.d(TAG,"로그 exif 기록 GPS"+String.valueOf(location));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Bitmap getThumbnailBitmap(String path, int thumbnailSize) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
            return null;
        }
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;




        return BitmapFactory.decodeFile(path, opts);
    }


    private void AddThumnail() {
        Log.d(TAG,mPath);

        thumFrame = inflate.findViewById(R.id.thumnail);

        Bitmap thumb = getThumbnailBitmap(mPath,300);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Latest_Photo_Path", mPath).apply();

        Bitmap resize = ThumbnailUtils.extractThumbnail(thumb, 36,36);

        Bitmap roundthum = getRoundedCornerBitmap(resize,5);



        thumFrame.setImageBitmap(roundthum);
        thumFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File imageFile = new File(mPath);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri ThumnailURI = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".fileprovider", imageFile);
                intent.setDataAndType(ThumnailURI,"image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });

    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    static Size sizeImageReader;

    private void initOutputSurface() {
        size_camera_0 = sharedPref.getString("selected_size_camera_0",LagistSizeCamera0);
        size_camera_1 = sharedPref.getString("selected_size_camera_1",LagistSizeCamera1);

        if(mCameraId=="0") {
            sizeImageReader = new Size(Size.parseSize(size_camera_0).getWidth(), Size.parseSize(size_camera_0).getHeight());

        }
        if(mCameraId=="1") {
            sizeImageReader = new Size(Size.parseSize(size_camera_1).getWidth(), Size.parseSize(size_camera_1).getHeight());

        }
        mImageReader = ImageReader.newInstance(sizeImageReader.getWidth(), sizeImageReader.getHeight(), ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);
        //mFormat = sharedPrefcamera.getInt("format", 256);
        //Log.d("로그 initOutputSurface","initOutputSurface");
        //int sizeWidth = sharedPrefcamera.getInt("format_" + mFormat + "_pictureSize_width", 1280);
        //int sizeHeight = sharedPrefcamera.getInt("format_" + mFormat + "_pictureSize_height", 960);

        Log.d("로그 initOutputSurface",String.valueOf(sizeImageReader));


        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mSurface = new Surface(texture);

        mOutputSurfaces = new ArrayList<Surface>(2);
        mOutputSurfaces.add(mImageReader.getSurface());

        mOutputSurfaces.add(mSurface);
    }

    public void takePictureTimer() {
        Log.d("로그 timerState",String.valueOf(stateTimer));
        if(stateTimer==0) {
            try {
                takePicture();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            startTime = System.currentTimeMillis();
            int PostTime = 0;
            if (stateTimer==1) { PostTime= 3000; }
            else if (stateTimer==2) { PostTime= 10000; }
            textTimer.setVisibility(View.VISIBLE);
            timerHandler.postDelayed(timerRunnable, 0);

            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        takePicture();
                        timerHandler.removeCallbacks(timerRunnable);

                        if (stateTimer==1) { textTimer.setText(String.valueOf(3));}
                        else if (stateTimer==2) { textTimer.setText(String.valueOf(10)); }
                        textTimer.setVisibility(View.INVISIBLE);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, PostTime);
        }

    }

    long startTime = 0;

    Handler timerHandler = new Handler(Looper.getMainLooper());
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);

            seconds = seconds % 60;
            if (stateTimer==1) {

                textTimer.setText(String.valueOf(3 - seconds));
                for (int i = 3; i > 0; i--) {
                    if (textTimer.getText() == String.valueOf(i))
                        soundPool.play(soundIds[2], 1, 1, 1, 0, 1f);
                }
            }
            else if (stateTimer==2) {
                textTimer.setText(String.valueOf(10 - seconds));
                //for (int i = 0; i < 1; i++) {
                for (int i = 3; i > 0; i--) {
                    if (textTimer.getText() == String.valueOf(i))
                soundPool.play(soundIds[2], 1, 1, 1, 0, 1f);
                }
            }


            timerHandler.postDelayed(this, 500);
        }
    };



    private void takePicture() throws CameraAccessException {
        if(SoundShutterOn) {
            soundPool.play(soundIds[0], 1, 1, 1, 0, 1f);
        }

        //mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        mCaptureBuilder.addTarget(mImageReader.getSurface());
        mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        Range<Integer> fps[] = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));




        //previewBuilder2CaptureBuilder();//수동 설정들을 반영하는 과정
        //mState = STATE_CAPTURE;
        lockFocus();

        //mCameraDevice.createCaptureSession(mOutputSurfaces, mSessionCaptureStateCallback, mHandler);
    }



    private void ApplyPhotoEffect(CaptureRequest.Builder captureBuilder) {

        Log.d(TAG, "로그 ApplyPhotoEffect");

        captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AWB_MODE));
        captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mPreviewRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME));

        captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION));
        captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mPreviewRequestBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE));//FocusDistance
        captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE));//effects
        captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mPreviewRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY));//ISO
        captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS)); //AF REGIONS
        captureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_REGIONS));//AE REGIONS
        captureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_SCENE_MODE));//SCENSE
        captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mPreviewRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION));//zoom
        captureBuilder.set(CaptureRequest.FLASH_MODE, mPreviewRequestBuilder.get(CaptureRequest.FLASH_MODE));
    }


    private void initHandler() {
        mHandlerThread = new HandlerThread("Android_L_Camera");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }


    int sizeWidth;
    int sizeHeight;

    @SuppressLint("MissingPermission")
    private void openCamera(int viewWidth, int viewHeight) throws CameraAccessException, InterruptedException {
        sizeWidth = viewWidth;
        sizeHeight = viewHeight;

        if (viewWidth> viewHeight) {
            sizeWidth = viewHeight;
            sizeHeight = viewWidth;
        }

        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        initHandler();
        Log.d("로그 openCamera",String.valueOf(sizeWidth) +"  "+ String.valueOf(sizeHeight));

        setUpCameraOutputs(sizeWidth, sizeHeight);

        configureTransform(sizeWidth, sizeHeight);


        Log.d("로그 initOutputSurface",String.valueOf(sizeWidth) +"  "+ String.valueOf(sizeHeight));
        initOutputSurface();
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);


        //TODO 프리뷰세션이 어떤코드인가???????
        mPreviewSessionCallback = new PreviewSessionCallback(mManualFocusImage, mMainHandler, mTextureView);// 터치 포커스를 위한 핸들러
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }




    private int mSensorOrientation;//Orientation of the camera sensor

    private void setUpCameraOutputs(int width, int height) throws CameraAccessException {

        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);




        mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);



        mPreviewSize = initPreviewRatio();



        mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        gridView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());








    }
    Size AbsolutePreviewsize;




    private Size initPreviewRatio() {

        if (mCameraId =="0") {
            AbsolutePreviewsize =  Size.parseSize(sharedPref.getString("selected_size_camera_0", String.valueOf(LagistSizeCamera0)));
        }
        if (mCameraId =="1") {
            AbsolutePreviewsize =  Size.parseSize(sharedPref.getString("selected_size_camera_1", String.valueOf(LagistSizeCamera1)));
        }
        int height = AbsolutePreviewsize.getHeight();
        int width = AbsolutePreviewsize.getWidth();

        if(height>width) {
            double ratio = (double) height / width;
            double sizeRatio = Math.round(ratio * 100) / 100.0;
            int resizeH = (int) Math.round(DSI_width * sizeRatio);


            mPreviewSize = new Size(DSI_width,resizeH);


            Log.d("로그 initPreviewRatio h>w","라티오 :"+String.valueOf(ratio)+"사이즈라티오 :"+String.valueOf(sizeRatio));
            Log.d("로그 initPreviewRatio h>w","가로 : " +DSI_width+ "세로 : "+ String.valueOf(resizeH));

            return mPreviewSize;

        } else {
            double ratio = (double) width / height;
            double sizeRatio = Math.round(ratio * 100) / 100.0;
            int resizeH = (int) Math.round(DSI_width * sizeRatio);
            mPreviewSize = new Size(DSI_width,resizeH);

            Log.d("로그 initPreviewRatio","라티오 :"+String.valueOf(ratio)+"사이즈라티오 :"+String.valueOf(sizeRatio));
            Log.d("로그 initPreviewRatio","가로 : " +DSI_width+ "세로 : "+ String.valueOf(resizeH));
            return mPreviewSize;
        }
    }




    private void configureTransform(int viewWidth, int viewHeight) {

        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        //TODO 프리뷰 사이즈 부분, 하단의 변수를 사용가능한 사진크기로 치환해줄것
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    //TODO 프리뷰 사이즈 부분, 하단의 변수를 사용가능한 사진크기로 치환해줄것
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }






    private void startPreview() {


        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewRequestBuilder.addTarget(mSurface);

        initPreviewBuilder();

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        StateFocus = FOCUS_AUTO;

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);




        try {
            mState = STATE_PREVIEW;


// Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCameraCaptureSession  = cameraCaptureSession;
                            try {
                                setListener();
                                //Auto focus should be continuous for camera preview.
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.


                                //setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCameraCaptureSession .setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );





        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void initPreviewBuilder() {
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
        mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, valueAF);
        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, valueAETime);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, valueAE);
        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, valueISO);
    }


    @SuppressLint("MissingPermission")
    private void reOpenCamera() {

        mManualFocusImage.stopFocus();

        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }


        if (mCameraId.equals("1")) {
            mCameraId = "0";
            mBtnChangeCamera.setImageResource(R.drawable.btn_camera_front);
            try {
                Log.d("로그 리오픈 카메라0",size_camera_0);

                openCamera(Size.parseSize(size_camera_0).getWidth(),Size.parseSize(size_camera_0).getHeight());

                //setUpCameraOutputs(Size.parseSize(size_camera_0).getWidth(),Size.parseSize(size_camera_0).getHeight());
                //configureTransform(Size.parseSize(size_camera_0).getWidth(),Size.parseSize(size_camera_0).getHeight());
                //mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (mCameraId.equals("0")) {
            mCameraId = "1";
            mBtnChangeCamera.setImageResource(R.drawable.btn_camera_rear);
            try {
                Log.d("로그 리오픈 카메라1",size_camera_1);
                openCamera(Size.parseSize(size_camera_1).getWidth(),Size.parseSize(size_camera_1).getHeight());

                //setUpCameraOutputs(Size.parseSize(size_camera_1).getWidth(),Size.parseSize(size_camera_1).getHeight());
                //configureTransform(Size.parseSize(size_camera_1).getWidth(),Size.parseSize(size_camera_1).getHeight());
                //mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }

    public static int getStateFocus() {
        return StateFocus;
    }

    //TODO TextureView의 리스너를 달아주는 곳.
    private void setListener() {
        mTextureView.setmMyTextureViewTouchEvent(new TextureViewTouchEvent(mCameraCharacteristics, mTextureView, mPreviewRequestBuilder, mCameraCaptureSession, mHandler, mPreviewSessionCallback, soundPool, soundIds));

        mAwbSb.setmOnAwbSeekBarChangeListener(new AwbSeekBarChangeListener(getActivity(), mSeekBarTextView, mPreviewRequestBuilder, mCameraCaptureSession, mHandler, mPreviewSessionCallback));
    }

    @Override
    public void onStop() {
        super.onStop();



        closeSoundPool();
        if (mHandler != null) {
            try {

                mHandlerThread.quitSafely();
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();

            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }

    }





    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureAvailable");
            mToPreviewWidth = width;
            mToPreviewHeight = height;
            try {
                openCamera(width, height);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureSizeChanged");
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            //Log.i("SurfaceTextureListener", "onSurfaceTextureUpdated");
        }
    };


    private Semaphore mCameraOpenCloseLock = new Semaphore(1);// A {@link Semaphore} to prevent the app from exiting before closing the camera.

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onClosed(CameraDevice camera) {

        }

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.i("Thread", "onOpened---->" + Thread.currentThread().getName());
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            startPreview();
            intflashstate();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };








    static boolean isOpen = false;
    static int stateFlash = 0;
    static int stateTimer = 0;
    @Override
    public void onClick(View view) {
        mManualFocusImage.stopFocus();
        switch (view.getId()) {
            case R.id.btn_menu:
                if (!isOpen) {
                    mCamereMenu.startAnimation(Rotate_Clockwise);
                    btnAwb.startAnimation(Open_Full);
                    btnIso.startAnimation(Open_Full);
                    mBtnEffect.startAnimation(Open_Full);
                    mBtnSense.startAnimation(Open_Full);
                    btnSetting.startAnimation(Open_Full);
                    mCamereMenu.setImageResource(R.drawable.btn_menu_close);

                    btnAwb.setClickable(true);
                    btnIso.setClickable(true);
                    mBtnEffect.setClickable(true);
                    mBtnSense.setClickable(true);
                    btnSetting.setClickable(true);

                    isOpen = true;
                } else {

                    btnAwb.startAnimation(Close_Full);
                    btnIso.startAnimation(Close_Full);
                    mBtnEffect.startAnimation(Close_Full);
                    mBtnSense.startAnimation(Close_Full);
                    btnSetting.startAnimation(Close_Full);

                    mCamereMenu.startAnimation(Rotate_Clockwise);
                    mCamereMenu.setImageResource(R.drawable.btn_menu);


                    btnAwb.setClickable(false);
                    btnIso.setClickable(false);
                    mBtnEffect.setClickable(false);
                    mBtnSense.setClickable(false);
                    btnSetting.setClickable(false);


                    isOpen = false;
                }
                break;
            case R.id.btn_grid:
                if(!GridOn) {
                    //gridView.invalidate();
                    gridView.startAnimation(animation_Grid_In);
                    gridView.setVisibility(View.VISIBLE);
                    //gridView = new GridView(getActivity());
                    //getActivity().setContentView(gridView);
                    GridOn = true;
                } else {
                    gridView.startAnimation(animation_Grid_Out);
                    gridView.setVisibility(View.INVISIBLE);
                    GridOn = false;
                }
                break;

            case R.id.btn_setting:
                Intent SettingActivity = new Intent(getActivity(), SettingsActivity.class);
                startActivity(SettingActivity);


                break;
            case R.id.btn_change_camera:
                reOpenCamera();
                break;

            case R.id.btn_focus:

                if (!showAfFlag) {
                    btnFocus.setImageResource(R.drawable.ic_focus_manual);
                    mLayoutAf.setVisibility(View.VISIBLE);
                    seekBar_AF.setEnabled(true);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                    updatePreview();

                    StateFocus = FOCUS_MANUAL;
                    showAfFlag = true;

                } else {
                    btnFocus.setImageResource(R.drawable.ic_focus_auto);
                    seekBar_AF.setEnabled(false);
                    mLayoutAf.setVisibility(View.GONE);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    updatePreview();
                    //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    StateFocus = FOCUS_AUTO;
                    showAfFlag = false;
                }

                break;



            case R.id.btn_awb:
                showAwbFlag = !showAwbFlag;
                showLayout(SHOW_AWB, showAwbFlag);
                break;

            case R.id.btn_iso:
                showIsoFlag = !showIsoFlag;
                showLayout(SHOW_ISO, showIsoFlag);
                break;

            case R.id.btn_effect:
                ListView lv = new ListView(getActivity());
                lv.setBackgroundColor(44000000);
                SimpleAdapter listItemAdapter = EffectAdapter.getAdapter(getActivity());
                lv.setAdapter(listItemAdapter);
                PopupWindow window = createPopupWindow(getActivity(), lv);
                lv.setOnItemClickListener(new EffectItemClickListener(mPreviewRequestBuilder, mCameraCaptureSession, mHandler, window, mPreviewSessionCallback, mWindowTextView));
                int xoff = window.getWidth() / 2 - mBtnEffect.getWidth() / 2;
                window.update();
                window.showAsDropDown(mBtnEffect, -xoff, 0);
                break;

            case R.id.btn_flash:
                new FlashItemClickListener(mPreviewRequestBuilder, mCameraCaptureSession, mHandler, mBtnFlash, mPreviewSessionCallback, stateFlash).onFlashClick(stateFlash);
                break;

            case R.id.btn_timer:

                new TimerItemClickListener(mHandler, btnTimer,  textTimer, stateTimer).onTimerClick(stateTimer);
                break;
            case R.id.btn_sense:
                ListView lv3 = new ListView(getActivity());
                lv3.setBackgroundColor(44000000);
                SimpleAdapter listItemAdapter3 = SenseAdapter.getAdapter(getActivity());
                lv3.setAdapter(listItemAdapter3);
                PopupWindow window3 = createPopupWindow(getActivity(), lv3);
                lv3.setOnItemClickListener(new SenseItemClickListener(mPreviewRequestBuilder, mCameraCaptureSession, mHandler, window3, mPreviewSessionCallback, mWindowTextView));
                int xoff3 = window3.getWidth() / 2 - mBtnSense.getWidth() / 2;
                window3.update();
                window3.showAsDropDown(mBtnSense, -xoff3, 0);
                break;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeCamera();


        if (isOpen) {
            closeMenu();
        }
    }





    private void closeMenu() {
        btnSetting.startAnimation(Close_Full);

        btnAwb.startAnimation(Close_Full);
        btnIso.startAnimation(Close_Full);
        mBtnEffect.startAnimation(Close_Full);
        mBtnSense.startAnimation(Close_Full);
        mCamereMenu.startAnimation(Rotate_Clockwise);
        mCamereMenu.setImageResource(R.drawable.btn_menu);


        btnSetting.setClickable(false);

        btnAwb.setClickable(false);
        btnIso.setClickable(false);
        mBtnEffect.setClickable(false);
        mBtnSense.setClickable(false);
        isOpen = false;
    }
//todo seekbar 텍스트 설정

    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

            if (mPreviewRequestBuilder == null || getView() == null) {
                return;
            }
            switch (seekBar.getId()) {
                //TODO 포커스를 조절하는 부분
                case R.id.seekbar_focus:
                    float minimumLens = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                    float maximumLens = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
                    if (progress < 100) {
                        float num = (((float) (100 - progress)) * minimumLens / 100);
                        mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, num);

                    } else if (progress == 100) {
                        mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, maximumLens);
                    }

                    break;
                case R.id.seekbar_exposure:
                    Range<Integer> range1 = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                    int maxmax = range1.getUpper();
                    int minmin = range1.getLower();
                    int all = (-minmin) + maxmax;
                    int time = 100 / all;
                    int ae = ((progress / time) - maxmax) > maxmax ? maxmax : ((progress / time) - maxmax) < minmin ? minmin : ((progress / time) - maxmax);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                    //mSeekBarTextView.setText("노출 보정：" + ae);

                    int vertical = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                    mText_exposure.setY(seekBar.getY() + vertical + seekBar.getThumbOffset() / 2);
                    mText_exposure.setText(String.valueOf(ae));
                    valueAE = ae;

                    break;
                /**
                 case R.id.sb_ae:
                 Switch switchAE = (Switch) getView().findViewById(R.id.switch_ae);
                 if (switchAE.isChecked()) {

                 Range<Integer> range1 = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                 int maxmax = range1.getUpper();
                 int minmin = range1.getLower();
                 int all = (-minmin) + maxmax;
                 int time = 100 / all;
                 int ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                 mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                 mSeekBarTextView.setText("노출 보정：" + ae);
                 valueAE = ae;
                 } else {

                 Range<Long> range = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                 long max = range.getUpper();
                 long min = range.getLower();
                 long ae = ((i * (max - min)) / 100 + min);
                 mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
                 mSeekBarTextView.setText("노출시간：" + ae);
                 valueAETime = ae;
                 }
                 break;
                 **/
                case R.id.sb_iso:
                    Range<Integer> range = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    int max1 = range.getUpper();//10000
                    int min1 = range.getLower();//100
                    int iso = ((progress * (max1 - min1)) / 100 + min1);
                    mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                    valueISO = iso;
                    mSeekBarTextView.setText("감도：" + iso);
                    break;

                case R.id.sb_zoom:
                    Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int radio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 2;
                    int realRadio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                    int centerX = rect.centerX();
                    int centerY = rect.centerY();
                    int minWidth = (rect.right - ((progress * centerX) / 100 / radio) - 1) - ((progress * centerX / radio) / 100 + 8);
                    int minHeight = (rect.bottom - ((progress * centerY) / 100 / radio) - 1) - ((progress * centerY / radio) / 100 + 16);
                    if (minWidth < rect.right / realRadio || minHeight < rect.bottom / realRadio) {
                        Log.i("sb_zoom", "sb_zoomsb_zoomsb_zoom");
                        return;
                    }
//                    Rect newRect = new Rect(20, 20, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
//                    Log.i("sb_zoom", "left--->" + "20" + ",,,top--->" + "20" + ",,,right--->" + (rect.right - ((i * centerX) / 100 / radio) - 1) + ",,,bottom--->" + (rect.bottom - ((i * centerY) / 100 / radio) - 1));
                    Rect newRect = new Rect((progress * centerX / radio) / 100 + 40,
                            (progress * centerY / radio) / 100 + 40,
                            rect.right - ((progress * centerX) / 100 / radio) - 1,
                            rect.bottom - ((progress * centerY) / 100 / radio) - 1);

                    // Log.i("sb_zoom", "left--->" + ((i * centerX / radio) / 100 + 8) + ",,,top--->" + ((i * centerY / radio) / 100 + 16) + ",,,right--->" + (rect.right - ((i * centerX) / 100 / radio) - 1) + ",,,bottom--->" + (rect.bottom - ((i * centerY) / 100 / radio) - 1));
                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
                    mSeekBarTextView.setText("확대：" + progress + "%");
                    break;

            }
            updatePreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mSeekBarTextView.setVisibility(View.VISIBLE);
            mSeekBarTextView.startAnimation(alpha_In);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mSeekBarTextView.startAnimation(alpha_out);
            mSeekBarTextView.setVisibility(View.INVISIBLE);
        }


    }


    private class MyOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (mPreviewRequestBuilder == null || getView() == null) {
                return;
            }
            switch (buttonView.getId()) {

                case R.id.switch_ae:
                    Switch switchISO = (Switch) getView().findViewById(R.id.switch_iso);
                    switchISO.setChecked(isChecked);
                    if (isChecked) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        mLayoutIso.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        mLayoutIso.getChildAt(1).setEnabled(true);
                    }
                    break;
                case R.id.switch_iso:
                    Switch switchAE = (Switch) getView().findViewById(R.id.switch_ae);
                    switchAE.setChecked(isChecked);
                    if (isChecked) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        mLayoutIso.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        mLayoutIso.getChildAt(1).setEnabled(true);
                    }
                    break;
            }
            updatePreview();
        }
    }


    private void showLayout(int showWhat, boolean showOrNot) {
        View v = mLayoutList.get(showWhat);
        if (showOrNot) {
            for (int i = 0; i < mControlBottom.getChildCount(); i++) {
                if (mControlBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mControlBottom.getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            v.startAnimation(alpha_In);
            v.setVisibility(View.VISIBLE);

        } else {

            for (int i = 0; i < mControlBottom.getChildCount(); i++) {
                if (mControlBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mControlBottom.getChildAt(i).setVisibility(View.GONE);
                }
            }
            v.startAnimation(alpha_out);
            v.setVisibility(View.GONE);
        }
    }


    private PopupWindow createPopupWindow(Context cx, ListView lv) {
        PopupWindow window = new PopupWindow(cx);
        window.setContentView(lv);
        Resources res = cx.getResources();
        window.setWidth(res.getDimensionPixelOffset(R.dimen.popupwindow_width));
        window.setHeight(res.getDimensionPixelOffset(R.dimen.popupwindow_height) * (lv.getAdapter().getCount()));
        window.setFocusable(true);
        window.setTouchable(true);
        window.setOutsideTouchable(true);
        return window;
    }


}

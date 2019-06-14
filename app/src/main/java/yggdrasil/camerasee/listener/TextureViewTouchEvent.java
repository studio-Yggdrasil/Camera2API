package yggdrasil.camerasee.listener;

import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;

import yggdrasil.camerasee.callback.PreviewSessionCallback;
import yggdrasil.camerasee.ui.DisplayFragment;
import yggdrasil.camerasee.view.MyTextureView;


public class TextureViewTouchEvent implements MyTextureView.MyTextureViewTouchEvent {

    public int mTimes = 0;

    private static final int FOCUS_MANUAL = 0;
    private static final int FOCUS_AUTO = 1;
    private static final int FOCUS_CONTINOUS = 2;
    private static final int FOCUS_HALF_SHUTTER = 3;

    private CameraCharacteristics mCameraCharacteristics;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mMainHandler;
    private PreviewSessionCallback mPreviewSessionCallback;
    private SoundPool soundPool;
    private int[] soundIds;
    private int stateFocus;

    public TextureViewTouchEvent(CameraCharacteristics mCameraCharacteristics, TextureView mTextureView, CaptureRequest.Builder mPreviewBuilder,
                                 CameraCaptureSession mCameraCaptureSession, Handler mMainHandler, PreviewSessionCallback mPreviewSessionCallback, SoundPool soundPool, int[] soundIds) {
        this.mCameraCharacteristics = mCameraCharacteristics;
        this.mTextureView = mTextureView;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mMainHandler = mMainHandler;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        this.soundPool = soundPool;
        this.soundIds = soundIds;
        this.stateFocus = stateFocus;
    }



    @Override
    public boolean onAreaTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (DisplayFragment.getStateFocus() == FOCUS_AUTO) {
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    ManualFocus(event);

                } else {

                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void ManualFocus(MotionEvent event) {


        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.i("onAreaTouchEvent", "SENSOR_INFO_ACTIVE_ARRAY_SIZE,,,,,,,,rect.left--->" + rect.left + ",,,rect.top--->" + rect.top + ",,,,rect.right--->" + rect.right + ",,,,rect.bottom---->" + rect.bottom);
        Size size = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        Log.i("onAreaTouchEvent", "mCameraCharacteristics,,,,size.getWidth()--->" + size.getWidth() + ",,,size.getHeight()--->" + size.getHeight());
        int areaSize = 200;
        int right = rect.right;
        int bottom = rect.bottom;
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        int ll, rr;
        Rect newRect;
        int centerX = (int) event.getX();
        int centerY = (int) event.getY();
        ll = ((centerX * right) - areaSize) / viewWidth;
        rr = ((centerY * bottom) - areaSize) / viewHeight;
        int focusLeft = clamp(ll, 0, right);
        int focusBottom = clamp(rr, 0, bottom);
        Log.i("focus_position", "focusLeft--->" + focusLeft + ",,,focusTop--->" + focusBottom + ",,,focusRight--->" + (focusLeft + areaSize) + ",,,focusBottom--->" + (focusBottom + areaSize));
        newRect = new Rect(focusLeft, focusBottom, focusLeft + areaSize, focusBottom + areaSize);
        MeteringRectangle meteringRectangle = new MeteringRectangle(newRect, 500);
        MeteringRectangle[] meteringRectangleArr = {meteringRectangle};


        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, meteringRectangleArr);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);


        updatePreview();



    }


    private int clamp(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }



    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }
}

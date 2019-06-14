package yggdrasil.camerasee.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import yggdrasil.camerasee.view.AnimationFocusView;
import yggdrasil.camerasee.view.MyTextureView;


public class PreviewSessionCallback extends CameraCaptureSession.CaptureCallback implements MyTextureView.FocusPositionTouchEvent {
    private int mAfState = CameraMetadata.CONTROL_AF_STATE_INACTIVE;
    private AnimationFocusView mFocusImage;
    private Handler mMainHandler;
    private int mRawX;
    private int mRawY;
    private boolean mFlagShowFocusImage = false;

        public PreviewSessionCallback(AnimationFocusView mFocusImage, Handler mMainHandler, MyTextureView mMyTextureView) {
        this.mFocusImage = mFocusImage;
        this.mMainHandler = mMainHandler;
        mMyTextureView.setmFocusPositionTouchEvent(this);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, final TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Integer nowAfState = result.get(CaptureResult.CONTROL_AF_STATE);

        if (nowAfState == null) {
            return;
        }
        if (nowAfState.intValue() == mAfState) {
            return;
        }
        mAfState = nowAfState.intValue();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                judgeFocus();
            }
        });
    }
//TODO 여기서 continous focus의 마킹 처리 및 터치포커스와의 구분을 수행해주어야함.
    private void judgeFocus() {
        if(mAfState == CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN ||
                mAfState == CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN) {
            focusFocusing();

        } else  if(mAfState == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                mAfState == CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
            focusSucceed();

        } else  if(mAfState == CameraMetadata.CONTROL_AF_STATE_INACTIVE) {
            focusInactive();

        } else  if(mAfState == CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED ||
                    mAfState == CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED) {
            focusFailed();
        }

    }

    private void focusFocusing() {

        int width = mFocusImage.getWidth();
        int height = mFocusImage.getHeight();

        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(mFocusImage.getLayoutParams());
        margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        mFocusImage.setLayoutParams(layoutParams);

        if (mFlagShowFocusImage == false) {
            mFocusImage.startFocusing();
            mFlagShowFocusImage = true;
        }
    }

    private void focusSucceed() {
        if (mFlagShowFocusImage == true) {
            mFocusImage.focusSuccess();
            mFlagShowFocusImage = false;


        }
    }

    private void focusInactive() {
        mFocusImage.stopFocus();
        mFlagShowFocusImage = false;
    }

    private void focusFailed() {
        if (mFlagShowFocusImage == true) {
            mFocusImage.focusFailed();
            mFlagShowFocusImage = false;
        }
    }

    @Override
    public void getPosition(MotionEvent event) {
        mRawX = (int) event.getRawX();
        mRawY = (int) event.getRawY();
    }

}

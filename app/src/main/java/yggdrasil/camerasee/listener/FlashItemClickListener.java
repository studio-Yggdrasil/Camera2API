package yggdrasil.camerasee.listener;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.callback.PreviewSessionCallback;
import yggdrasil.camerasee.ui.DisplayFragment;


public class FlashItemClickListener  {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private ImageView mBtnFlash;
    private PreviewSessionCallback mPreviewSessionCallback;

    private int stateFlash;

    public FlashItemClickListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, ImageView mBtnFlash,
                                  PreviewSessionCallback mPreviewSessionCallback, int stateFlash) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mBtnFlash = mBtnFlash;
        this.mPreviewSessionCallback = mPreviewSessionCallback;

        this.stateFlash = stateFlash;
    }


    public void onFlashClick(int flashState) {

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

       if (flashState==3) {
           mBtnFlash.setImageResource(R.drawable.btn_flash_off);

           mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
           mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);

           DisplayFragment.setStateFlash(0);


       } else if(flashState == 0) {
                mBtnFlash.setImageResource(R.drawable.btn_flash_all);
                //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);

                //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                //mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                DisplayFragment.setStateFlash(1);


       } else if(flashState == 1) {
                mBtnFlash.setImageResource(R.drawable.btn_flash_auto);
                //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);

                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.FLASH_MODE_OFF);

                DisplayFragment.setStateFlash(2);

       } else if(flashState == 2) {
           mBtnFlash.setImageResource(R.drawable.btn_flash_red);
           //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);

           mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
           //mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.FLASH_MODE_OFF);

           DisplayFragment.setStateFlash(3);
       }


        updatePreview();
    }


    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }
}

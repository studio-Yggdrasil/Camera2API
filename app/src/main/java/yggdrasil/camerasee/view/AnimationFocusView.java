package yggdrasil.camerasee.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.SleepThread;
import yggdrasil.camerasee.ui.DisplayFragment;


public class AnimationFocusView extends android.support.v7.widget.AppCompatImageView {
    private Handler mMainHandler;
    private Animation mAnimation;
    private Context mContext;


    public int mTimes = 0;

    public AnimationFocusView(Context context) {
        super(context);
        mContext = context;
    }

    public AnimationFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AnimationFocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    public void setmMainHandler(Handler mMainHandler) {
        this.mMainHandler = mMainHandler;
    }

    public void setmAnimation(Animation mAnimation) {
        this.mAnimation = mAnimation;
    }

    public void initFocus() {
        this.setVisibility(VISIBLE);
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 1000, null)).start();
    }

    public void startFocusing() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.startAnimation(mAnimation);
        this.setBackground(mContext.getDrawable(R.drawable.focus));

        //new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 1000, Integer.valueOf(mTimes))).start();
    }

    public void focusFailed() {
        mTimes++;
        this.setBackground(mContext.getDrawable(R.drawable.focus_failed));
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 800, Integer.valueOf(mTimes))).start();
    }

    public void focusSuccess() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.setBackground(mContext.getDrawable(R.drawable.focus_succeed));

        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_SUCCESS, 50, Integer.valueOf(mTimes))).start();
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 1000, Integer.valueOf(mTimes))).start();
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_CONTINOUS, 2500, Integer.valueOf(mTimes))).start();
    }

    public void stopFocus() {
        this.setVisibility(INVISIBLE);
    }
}

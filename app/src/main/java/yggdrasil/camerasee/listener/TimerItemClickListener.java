package yggdrasil.camerasee.listener;

import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import yggdrasil.camerasee.R;
import yggdrasil.camerasee.ui.DisplayFragment;


public class TimerItemClickListener {

    private int stateTimer;
    private Handler mHandler;
    private ImageView btnTimer;
    private TextView textTimer;

    private SharedPreferences sharedPref;

    public TimerItemClickListener(Handler mHandler, ImageView btnTimer,  TextView textTimer, int stateTimer) {

        this.textTimer = textTimer;
        this.mHandler = mHandler;
        this.btnTimer = btnTimer;
        this.stateTimer = stateTimer;

        this.sharedPref = sharedPref;
    }


    public void onTimerClick(int flashState) {


       if(stateTimer==2) {
           btnTimer.setImageResource(R.drawable.ic_timer);
           textTimer.setVisibility(View.GONE);
           DisplayFragment.setStateTimer(0);

       } else if(stateTimer == 0) {
           btnTimer.setImageResource(R.drawable.ic_timer_3s);

           textTimer.setText("3");
           textTimer.setVisibility(View.INVISIBLE);
           DisplayFragment.setStateTimer(1);

       } else if(stateTimer == 1) {
           btnTimer.setImageResource(R.drawable.ic_timer_10s);

           textTimer.setText("10");
           textTimer.setVisibility(View.INVISIBLE);
           DisplayFragment.setStateTimer(2);

       }



    }



}

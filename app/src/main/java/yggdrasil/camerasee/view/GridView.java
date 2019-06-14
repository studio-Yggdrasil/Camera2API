package yggdrasil.camerasee.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by dlrud on 2018-01-27.
 */

public class GridView extends View {
    Paint paint = new Paint();
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;



    private void init() {
        //  Set paint options
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(255, 255, 255, 255));
    }

    public GridView(Context context) {
        super(context);
        init();
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    public void onDraw(Canvas canvas) {

        //  Find Screen size first
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();



        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        //int screenHeight = metrics.heightPixels;
        int resizeHeight = metrics.widthPixels * mRatioHeight / mRatioWidth;

        canvas.drawLine((screenWidth/3)*2,0,(screenWidth/3)*2,screenHeight,paint);
        canvas.drawLine((screenWidth/3),0,(screenWidth/3),screenHeight,paint);


        canvas.drawLine(0,(resizeHeight/3)*2,screenWidth,(resizeHeight/3)*2,paint);
        canvas.drawLine(0,((resizeHeight)/3),screenWidth,((resizeHeight)/3),paint);

    }

}

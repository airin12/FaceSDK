package com.example.marcin.facesdkvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.widget.VideoView;

/**
 * Created by Marcin on 2015-10-11.
 */
public class ShowVideoView extends VideoView {

    private Paint paint = new Paint();
    private int i = 0;
    private int j = 0;

    private void initialize(){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);
        paint.setTextSize(25);
        setWillNotDraw(false);
    }

    public ShowVideoView(Context context) {
        super(context);
        initialize();
    }

    public ShowVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ShowVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("DRAW", "onDraw");
        drawText(canvas);
        super.onDraw(canvas);
        drawText(canvas);
            invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.e("DRAW", "dispatch draw");
        drawText(canvas);
        super.dispatchDraw(canvas);
        drawText(canvas);
        i++;
        j++;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Drawable drawable = getBackground();
//        //super.onMeasure(100,100);
//        setMeasuredDimension(100,100);
//        Rest = getClipBounds()
//        Log.e("MEASURE","BEFORE CHECK");
//        if (drawable != null) {
//            int width = MeasureSpec.getSize(widthMeasureSpec);
//            int height = (int) Math.ceil((float) width * (float) drawable.getIntrinsicHeight() / (float) drawable.getIntrinsicWidth());
//            setMeasuredDimension(width, height);
//            Log.e("MEASURE",width+" "+height);
//        } else {
//            Log.e("MEASURE","NULL");
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
    }

    private void drawText(Canvas canvas){
        canvas.drawText("Jestem Aga",i,j,paint);
    }
}

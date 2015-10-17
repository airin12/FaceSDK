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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin on 2015-10-11.
 */
public class ShowVideoView extends VideoView {

    private Paint paint = new Paint();
    private int i = 0;
    private int j = 0;
    private long videoDuration;
    private long actualDuration;
    private long startTime;
    private LinkedHashMap<Long,Map<String,List<Integer>>> framesWithFaceCoords =
            new LinkedHashMap<>();
    private Long actualFrame;
    private Iterator<Long> it;
    private Integer frameWidth;
    private double ratio =1.0;



    private void initialize(){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);
        paint.setTextSize(25);
        paint.setTextAlign(Paint.Align.CENTER);
//        setWillNotDraw(false);
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
    public void start() {
        super.start();
        Log.e("SIZEW", getWidth() + " " + frameWidth);
        ratio = (double) getWidth()/frameWidth;
        Log.e("SIZEW",ratio+"");
    }

    //    @Override
//    public void start() {
//        actualDuration = 0;
//        startTime = System.currentTimeMillis();
//        Log.e("TIME","Start time "+startTime);
//        super.start();
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("DRAW", "onDraw");
        drawText(canvas);
        super.onDraw(canvas);
        drawText(canvas);
//        if(actualDuration == 0)
//            startTime = System.currentTimeMillis();
//        actualDuration = System.currentTimeMillis() - startTime;
//        if(actualDuration < videoDuration)
//            invalidate();
        if(getCurrentPosition()<videoDuration)
            invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.e("DRAW", "dispatch draw");
        if(getCurrentPosition()*1000 > actualFrame){
            if(it.hasNext())
                actualFrame = it.next();
        }


        drawText(canvas);
        super.dispatchDraw(canvas);
        drawText(canvas);
        i++;
        j++;
//        actualDuration = System.currentTimeMillis() - startTime;
//        Log.e("TIME","actual "+actualDuration);
//        if(actualDuration < videoDuration)
        Log.e("TIME","current "+getCurrentPosition());
        if(getCurrentPosition() < videoDuration)
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
        Map<String, List<Integer>> templateToCoords = framesWithFaceCoords.get(actualFrame);
        for(Map.Entry<String,List<Integer>> entry : templateToCoords.entrySet()){
            String name = entry.getKey();
            List<Integer> coords = entry.getValue();
            int x = (int)  (coords.get(0)* ratio);
            int y = (int) (coords.get(1) * ratio);
            int w = (int) (coords.get(2) * ratio);
            Log.e("LOCTEX",x+" "+y+ " "+w);
            Log.e("LOCTEX",ratio+"");
            canvas.drawText(name, x , y+w/2 + paint.getTextSize(), paint);
            canvas.drawRect(x - w / 2, y - w / 2, x + w / 2, y + w / 2, paint);
        }

    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public long getActualDuration() {
        return actualDuration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public void setActualDuration(long actualDuration) {
        this.actualDuration = actualDuration;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setFramesWithFaceCoords(LinkedHashMap<Long, Map<String, List<Integer>>> framesWithFaceCoords) {
        this.framesWithFaceCoords = framesWithFaceCoords;
        it = this.framesWithFaceCoords.keySet().iterator();
        actualFrame = it.next();
    }

    public void setFrameWidth(Integer frameWidth) {
        this.frameWidth = frameWidth;
    }
}

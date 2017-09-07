package com.example.myclock;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by zuying on 2017/6/23.
 */

public class MyClock extends View {
    private Canvas mCanvas;

    private Paint mTextPaint;

    private Rect mTextRect;

    private Paint mCirclePaint;

    private float mCircleStrokeWidth = 5;

    private RectF mCircleRectF;

    private Paint mScaleArcPaint;

    private RectF mScaleArcRectF;

    private Paint mScaleLinePaint;

    private Paint mHourHandPaint;

    private Paint mMinuteHandPaint;

    private Paint mSecondHandPaint;

    private Path mHourHandPath;

    private Path mMinuteHandPath;



    private int mLightColor;
    private int mScaleLine;

    private int mDarkColor;

    private int mBackgroundColor;

    private float mTextSize;

    private float mRadius;

    private float mScaleLength;


    private float mHourDegree;

    private float mMinuteDegree;

    private float mSecondDegree;


    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;


    private SweepGradient mSweepGradient;
    private Matrix mGradientMatrix;

    private Matrix mCameraMatrix;
    private Camera mCamera;
    private float mCameraRotateX;
    private float mCameraRotateY;
    private float mMaxCameraRotate = 10;
    private ValueAnimator mShakeAnim;

    public MyClock(Context context) {
        this(context, null);
    }

    public MyClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyClock(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyClock, defStyleAttr, 0);
        mBackgroundColor = ta.getColor(R.styleable.MyClock_backgroundColor, Color.parseColor("#237EAD"));
        setBackgroundColor(mBackgroundColor);
        mScaleLine = ta.getColor(R.styleable.MyClock_ScaleLine, Color.parseColor("#515151"));
        mLightColor = ta.getColor(R.styleable.MyClock_lightColor, Color.parseColor("#ffffff"));
        mDarkColor = ta.getColor(R.styleable.MyClock_darkColor, Color.parseColor("#80ffffff"));
        mTextSize = ta.getDimension(R.styleable.MyClock_textSize, DensityUtils.sp2px(context, 20));
        ta.recycle();

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mDarkColor);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setColor(mLightColor);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(mLightColor);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mBackgroundColor);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mLightColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStrokeWidth(mCircleStrokeWidth);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setColor(mDarkColor);

        mTextRect = new Rect();
        mCircleRectF = new RectF();
        mScaleArcRectF = new RectF();
        mHourHandPath = new Path();
        mMinuteHandPath = new Path();
        mGradientMatrix = new Matrix();
        mCameraMatrix = new Matrix();
        mCamera = new Camera();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
    }
    private int measureDimension(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 800;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius=Math.min(w-getPaddingLeft()-getPaddingRight(),h-getPaddingBottom()-getPaddingTop())/2;
        mDefaultPadding = 0.12f * mRadius;
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft();
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop();
        mPaddingRight=mPaddingLeft;
        mPaddingBottom=mPaddingTop;
        mScaleLength = 0.12f * mRadius;
        mScaleArcPaint.setStrokeWidth(mScaleLength);
        mScaleLinePaint.setStrokeWidth(0.04f * mRadius);
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{mScaleLine, mLightColor}, new float[]{0.95f, 1});
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        setCameraRotate();
        getTimeDegree();
        drawTimeText();
        drawScaleLine();
        drawSecondHand();
        drawHourHand();
        drawMinuteHand();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getCameraRotate(event);
                break;
            case MotionEvent.ACTION_MOVE:
                getCameraRotate(event);
                break;
            case MotionEvent.ACTION_UP:
                startShakeAnim();
                break;
        }
        return true;
    }
    private void getCameraRotate(MotionEvent event) {
        if (mShakeAnim != null && mShakeAnim.isRunning()) {
            mShakeAnim.cancel();
        }
        float rotateX = -(event.getY() - getHeight() / 2);
        float rotateY = (event.getX() - getWidth() / 2);
        float percentX = rotateX / mRadius;
        float percentY = rotateY / mRadius;
        if (percentX > 1) {
            percentX = 1;
        } else if (percentX < -1) {
            percentX = -1;
        }
        if (percentY > 1) {
            percentY = 1;
        } else if (percentY < -1) {
            percentY = -1;
        }
        mCameraRotateX = percentX * mMaxCameraRotate;
        mCameraRotateY = percentY * mMaxCameraRotate;
    }
    private void setCameraRotate() {
        mCameraMatrix.reset();
        mCamera.save();
        mCamera.rotateX(mCameraRotateX);
        mCamera.rotateY(mCameraRotateY);
        mCamera.getMatrix(mCameraMatrix);
        mCamera.restore();
        mCameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mCameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
        mCanvas.concat(mCameraMatrix);
    }
    private void startShakeAnim() {
        final String cameraRotateXName = "cameraRotateX";
        final String cameraRotateYName = "cameraRotateY";
        PropertyValuesHolder cameraRotateXHolder =
                PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0);
        PropertyValuesHolder cameraRotateYHolder =
                PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0);
        mShakeAnim = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder, cameraRotateYHolder);
        mShakeAnim.setInterpolator(new OvershootInterpolator(10));
        mShakeAnim.setDuration(500);
        mShakeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCameraRotateX = (float) animation.getAnimatedValue(cameraRotateXName);
                mCameraRotateY = (float) animation.getAnimatedValue(cameraRotateYName);
            }
        });
        mShakeAnim.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTimeDegree(){
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 12 * 360;
    }
    private void drawTimeText(){
        String timeText="12";
        mTextPaint.getTextBounds(timeText,0,timeText.length(),mTextRect);
        int textLargeWidth = mTextRect.width();
        mCanvas.drawText("12", getWidth() / 2 - textLargeWidth / 2, mPaddingTop + mTextRect.height(), mTextPaint);
        timeText = "3";
        mTextPaint.getTextBounds(timeText,0,timeText.length(),mTextRect);
        int textSmallWidth = mTextRect.width();
        mCanvas.drawText("3", getWidth() - mPaddingRight-textSmallWidth, getHeight()/2+mTextRect.height()/2, mTextPaint);
        mCanvas.drawText("6", getWidth() / 2 - textSmallWidth / 2, getHeight()-mPaddingBottom, mTextPaint);
        mCanvas.drawText("9", mPaddingLeft, getHeight()/2+mTextRect.height()/2, mTextPaint);

        mCircleRectF.set(mPaddingLeft+textSmallWidth/2+mCircleStrokeWidth / 2,mPaddingTop+mTextRect.height()/2+mCircleStrokeWidth / 2,getWidth()
        -mPaddingRight-textSmallWidth/2+mCircleStrokeWidth / 2,getHeight()-mPaddingBottom-mTextRect.height()/2+mCircleStrokeWidth / 2);
        for (int i = 0; i < 4; i++) {
            mCanvas.drawArc(mCircleRectF, 5 + 90 * i, 80, false, mCirclePaint);
        }

    }
    private void drawScaleLine(){
        mCanvas.save();
        mScaleArcRectF.set(mPaddingLeft + 1.5f * mScaleLength + mTextRect.height() / 2,
                mPaddingTop + 1.5f * mScaleLength + mTextRect.height() / 2,
                getWidth() - mPaddingRight - mTextRect.height() / 2 - 1.5f * mScaleLength,
                getHeight() - mPaddingBottom - mTextRect.height() / 2 - 1.5f * mScaleLength);
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        mScaleArcPaint.setShader(mSweepGradient);
        mCanvas.drawArc(mScaleArcRectF, 0, 360, false, mScaleArcPaint);
        mCanvas.rotate(3f, getWidth() / 2, getHeight() / 2);
        for (int i = 0; i < 60; i++) {
            mCanvas.drawLine(getWidth() / 2, mPaddingTop + mScaleLength + mTextRect.height() / 2,
                    getWidth() / 2, mPaddingTop + 2 * mScaleLength + mTextRect.height() / 2, mScaleLinePaint);
            mCanvas.rotate(6f, getWidth() / 2, getHeight() / 2);
        }
        mCanvas.restore();
    }
    private void drawSecondHand(){
        mCanvas.save();
        mCanvas.rotate(mSecondDegree,getWidth()/2,getHeight()/2);
        mCanvas.drawLine(getWidth()/2,mPaddingTop + mTextRect.height()+ 0.26f * mRadius / 2,getWidth()/2,getHeight()/2-0.03f*mRadius,mSecondHandPaint);
        mCanvas.restore();

    }
    private void drawHourHand() {
        mCanvas.save();
        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);
        mHourHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mHourHandPath.moveTo(getWidth() / 2 - 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 - 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.46f * mRadius,
                getWidth() / 2 + 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(0.01f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mHourHandPaint);
        mCanvas.restore();
    }
    private void drawMinuteHand() {
        mCanvas.save();
        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);
        mMinuteHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.345f * mRadius,
                getWidth() / 2 + 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.close();
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mMinuteHandPaint);
        mCanvas.restore();
    }
}

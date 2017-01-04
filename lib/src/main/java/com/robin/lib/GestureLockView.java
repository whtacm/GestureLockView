package com.robin.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by haitao on 1/4/17.
 */
public class GestureLockView extends View {

    public final static int OUT_OF_MAX_LOCK_LENGTH = 0x1;//= "OutOfMaxSupportLockLength";

    public final static int LOWER_THAN_MIN_LOCK_LENGTH = 0x2;//= "LowerThanMinLockLength";

    public final static int REPEAT_NOT_ALLOWED = 0x3;//= "RepeatNotAllowed";


    enum Status {
        SUCCESS,
        FAIL,
        ORIGIN,
        LOCKING
    }

    //
    GestureLockListener mGestureLockListener;

    //
    private int mMinLockLength = 4;

    //
    private int mMaxLockLength = 4;

    //
    private boolean mRepeatAllowed = false;

    //
    private boolean mShowPath = false;

    //
    private int mLockCircleStrokeWidth;

    //
    private int mLockCircleStrokeColor;

    //
    private int mLockPointStrokeColor;

    //
    private int mLockPathStrokeColor;

    //
    private int mErrorColor;

    //
    private int mSuccessColor;

    //
    private int mLockPathStrokeWidth;

    //
    private List<Integer> mLockList;


    Paint mLockInnerCirclePaint;
    Paint mLockCirclePaint;
    Paint mLockPathPaint;
    Paint mLockPointPaint;

    int mHeight;
    int mWidth;
    int mCirclrRadius;
    int mStartCx;
    int mStartCy;
    int marginLeft;
    int marginTop;
    int marginBetweenCircles;

    Status status;

    public void setGestureLockListener(GestureLockListener gestureLockListener) {
        this.mGestureLockListener = gestureLockListener;
    }

    public GestureLockView(Context context) {
        super(context);
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mLockList = new LinkedList<>();
        status = Status.ORIGIN;

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.com_robin_gesturelockview, defStyleAttr, 0);
        this.mMinLockLength = ta.getInteger(R.styleable.com_robin_gesturelockview_mMinLockLength, 4);
        this.mMaxLockLength = ta.getInteger(R.styleable.com_robin_gesturelockview_mMaxLockLength, 4);
        this.mRepeatAllowed = ta.getBoolean(R.styleable.com_robin_gesturelockview_mRepeatAllowed, false);
        this.mShowPath = ta.getBoolean(R.styleable.com_robin_gesturelockview_mShowPath, false);
        this.mLockCircleStrokeWidth = ta.getDimensionPixelSize(R.styleable.com_robin_gesturelockview_mLockCircleStrokeWidth, 4);
        this.mLockCircleStrokeColor = ta.getColor(R.styleable.com_robin_gesturelockview_mLockCircleStrokeColor, Color.BLACK);
        this.mLockPointStrokeColor = ta.getColor(R.styleable.com_robin_gesturelockview_mLockPointStrokeColor, Color.BLACK);
        this.mLockPathStrokeColor = ta.getColor(R.styleable.com_robin_gesturelockview_mLockPathStrokeColor, Color.BLACK);
        this.mLockPathStrokeWidth = ta.getDimensionPixelSize(R.styleable.com_robin_gesturelockview_mLockPathStrokeWidth, 2);
        this.mErrorColor = ta.getColor(R.styleable.com_robin_gesturelockview_mErrorColor, Color.RED);
        this.mSuccessColor = ta.getColor(R.styleable.com_robin_gesturelockview_mSuccessColor, Color.BLUE);
        ta.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mWidth == 0) {
            mWidth = getWidth();
            mHeight = getHeight();
        }

        /**
         *
         */
        mLockInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockInnerCirclePaint.setColor(Color.WHITE);

        mLockCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockCirclePaint.setColor(mLockCircleStrokeColor);

        mLockPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockPointPaint.setColor(mLockPointStrokeColor);

        mLockPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLockPathPaint.setColor(mLockPathStrokeColor);
        mLockPathPaint.setStrokeWidth(mLockPathStrokeWidth);

        if (status == Status.FAIL) {
            mLockPointPaint.setColor(mErrorColor);
            mLockPathPaint.setColor(mErrorColor);
        }
        if (status == Status.SUCCESS) {
            mLockPointPaint.setColor(mSuccessColor);
            mLockPathPaint.setColor(mSuccessColor);
        }

        mCirclrRadius = mWidth / 12;
        marginLeft = (mWidth - mCirclrRadius * 6) / 3;
        marginTop = (mHeight - mCirclrRadius * 6 - marginLeft) / 2;
        marginBetweenCircles = marginLeft / 2;

        mStartCx = marginLeft + mCirclrRadius;
        mStartCy = marginTop + mCirclrRadius;

        for (int i = 0; i < 9; i++) {
            int cx = mStartCx + (mCirclrRadius * 2 + marginBetweenCircles) * (i % 3);
            int cy = mStartCy + (mCirclrRadius * 2 + marginBetweenCircles) * (i / 3);
            canvas.drawCircle(cx, cy, mCirclrRadius, mLockCirclePaint);
            canvas.drawCircle(cx, cy, mCirclrRadius - mLockCircleStrokeWidth, mLockInnerCirclePaint);
        }


        if (mShowPath) {
            int lastcx = mStartCx;
            int lastcy = mStartCy;
            //
            for (int i = 0; i < mLockList.size(); i++) {
                int idx = mLockList.get(i);
                int cx = mStartCx + (mCirclrRadius * 2 + marginBetweenCircles) * (idx % 3);
                int cy = mStartCy + (mCirclrRadius * 2 + marginBetweenCircles) * (idx / 3);
                if (i > 0) {
                    canvas.drawLine(lastcx, lastcy, cx, cy, mLockPathPaint);
                }
                lastcx = cx;
                lastcy = cy;
            }

            //
            for (Integer idx : mLockList) {
                int cx = mStartCx + (mCirclrRadius * 2 + marginBetweenCircles) * (idx % 3);
                int cy = mStartCy + (mCirclrRadius * 2 + marginBetweenCircles) * (idx / 3);
                canvas.drawCircle(cx, cy, mCirclrRadius / 3, mLockPointPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (status == Status.ORIGIN || status == Status.LOCKING) {
            int idx = getPoint(event);

            if (event.getAction() == MotionEvent.ACTION_DOWN)
                status = Status.LOCKING;

            if (idx != -1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLockList.add(idx);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mLockList.size() > 0 && mLockList.get(mLockList.size() - 1) == idx) {
                            //do nothing
                        } else {
                            if (mMaxLockLength == mLockList.size()) {
                                if (mGestureLockListener != null) {
                                    status = Status.FAIL;
                                    invalidate();
                                    mGestureLockListener.onError(mLockList, OUT_OF_MAX_LOCK_LENGTH);
                                }
                                return true;
                            }

                            if (mRepeatAllowed) {
                                mLockList.add(idx);
                                invalidate();
                            } else {
                                HashSet<Integer> set = new HashSet<>();
                                set.addAll(mLockList);
                                if (set.contains(idx)) {
                                    if (mGestureLockListener != null) {
                                        status = Status.FAIL;
                                        invalidate();
                                        mGestureLockListener.onError(mLockList, REPEAT_NOT_ALLOWED);
                                    }
                                    return true;
                                }

                                mLockList.add(idx);
                                invalidate();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mLockList.size() > 0 && mLockList.get(mLockList.size() - 1) == idx) {
                            //
                        } else {
                            mLockList.add(idx);
                        }
                        int code = -1;
                        if (mLockList.size() < mMinLockLength) {
                            code = LOWER_THAN_MIN_LOCK_LENGTH;
                        }
                        if (mMaxLockLength < mLockList.size()) {
                            code = OUT_OF_MAX_LOCK_LENGTH;
                        }

                        if (mGestureLockListener != null) {
                            if (code != -1) {
                                status = Status.FAIL;
                                mGestureLockListener.onError(mLockList, code);
                            } else {
                                status = Status.SUCCESS;
                                mGestureLockListener.onNext(mLockList);
                            }
                        }
                        invalidate();
                        break;
                }
            } else {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mLockList.size() < mMinLockLength && mGestureLockListener != null) {
                        status = Status.FAIL;
                        invalidate();
                        mGestureLockListener.onError(mLockList, LOWER_THAN_MIN_LOCK_LENGTH);
                        return true;
                    }

                    if (mGestureLockListener != null) {
                        status = Status.SUCCESS;
                        invalidate();
                        mGestureLockListener.onNext(mLockList);
                    }

                }
            }

        }

        return true;
    }

    /***
     * @param event
     */
    private int getPoint(MotionEvent event) {
        int idx = -1;
        int dist = mCirclrRadius * mCirclrRadius;
        for (int i = 0; i < 9; i++) {
            int cx = mStartCx + (mCirclrRadius * 2 + marginBetweenCircles) * (i % 3);
            int cy = mStartCy + (mCirclrRadius * 2 + marginBetweenCircles) * (i / 3);
            if (dist > Math.pow(event.getX() - cx, 2.0) + Math.pow(event.getY() - cy, 2.0)) {
                idx = i;
                break;
            }
        }

        return idx;
    }


    /**
     *
     */
    public void reset() {
        mLockList.clear();
        status = Status.ORIGIN;
        invalidate();
    }

    public interface GestureLockListener {
        /**
         * @param lockArray
         */
        public void onNext(List<Integer> lockArray);

        /**
         * @param lockArray
         * @param errCode
         */
        public void onError(List<Integer> lockArray, int errCode);
    }
}

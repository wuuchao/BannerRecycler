package wuuchao.github.com.libarary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import wuuchao.github.com.bannerrecycler.R;
import wuuchao.github.com.libarary.utils.DeviceUtil;

/**
 * 描述:
 * 作者: wuuchao 2
 * 时间: 2016/07/17 11:21
 */
public class BannerRecycler extends View {
    private static final int TOUCH_STATE_SCROLLING = 1;
    private static final int TOUCH_STATE_SCROLLED = 2;
    private static final int SNAP_VELOCITY = 1000;
    private static final int SCROLL_TO_LEFT = 101;
    private static final int SCROLL_TO_RIGHT = 102;
    private static final int SCROLL_TO_NONE = 103;
    public static final int MAX_DRAW_BITMAPS = 3; //在屏幕上绘制的图片数量
    private Context mContext;
    private Scroller mScroller;
    private int[] drawBitmaps = new int[3];
    private int mCurX;
    private boolean startScroll;
    private int mTouchSlop;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private int mTouchState; //滚动的状态
    private int mScrollDirect; //滚动的方向
    private int mIndex;
    private int mRealIndex;//根据mIndex计算的当前页码
    private float mLastMotionX;
    private BitmapDrawable[] d = new BitmapDrawable[3];
    private Bitmap[] b = new Bitmap[MAX_DRAW_BITMAPS];
    private RectF[] mWidgetRect = new RectF[MAX_DRAW_BITMAPS];
    private Paint mPaint;
    private List<Bitmap> mData = new ArrayList<>();

    public BannerRecycler(Context context) {
        super(context);
        init(context);
    }

    public BannerRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BannerRecycler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public BannerRecycler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drawBitmaps[0] = R.mipmap.splash1;
        drawBitmaps[1] = R.mipmap.ic_launcher;
        drawBitmaps[2] = R.mipmap.psu;
        ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mScroller = new Scroller(context);
        b[0] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[0]);
        b[1] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[1]);
        b[2] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[2]);
        mData.add(b[0]);
        mData.add(b[1]);
        mData.add(b[2]);
        d[0] = new BitmapDrawable(mContext.getResources(), b[drawBitmaps.length - 1]);
        for (int index = 1; index < d.length; index++) {
            d[index] = new BitmapDrawable(mContext.getResources(), b[index - 1]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidgetRect[0] = new RectF(-getMeasuredWidth(), 0, 0, getMeasuredHeight());
        mWidgetRect[1] = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mWidgetRect[2] = new RectF(getMeasuredWidth(), 0, getMeasuredWidth() * 2, getMeasuredHeight());
    }

    /**
     * 计算实际的页码
     */
    private void resetPageIndex() {
        if (mScrollDirect == SCROLL_TO_LEFT) {
            mRealIndex--;
        } else if (mScrollDirect == SCROLL_TO_RIGHT) {
            mRealIndex++;
        }
        if (mRealIndex < 0) {
            mRealIndex = mData.size() - 1;
        } else if (mRealIndex > mData.size() - 1) {
            mRealIndex = 0;
        }
    }

    private void snapToScreen(int index) {
        int destination = index * getWidth();
        int deltaX = destination - getScrollX();
        smoothScrollBy(getScrollX(), deltaX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        float x = event.getX();
        Log.e("event.getAction()", event.getAction() + "");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                boolean xMoved = xDiff > mTouchSlop;
                if (xMoved) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final int deltaX = (int) (mLastMotionX - x);
                    mLastMotionX = x;
                    scrollBy(deltaX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    if (velocityX > SNAP_VELOCITY) {
                        mScrollDirect = SCROLL_TO_LEFT;
                        snapToScreen(--mIndex);
                    } else if (velocityX < -SNAP_VELOCITY) {
                        mScrollDirect = SCROLL_TO_RIGHT;
                        snapToScreen(++mIndex);
                    } else {
                        mScrollDirect = SCROLL_TO_NONE;
                        snapToScreen(mIndex);
                    }
                    if (null != mVelocityTracker) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                mTouchState = TOUCH_STATE_SCROLLED;
                break;
        }
        return true;
    }

    private void smoothScrollBy(int startX, int x) {
        mScroller.startScroll(startX, 0, x, 0, 500);
        startScroll = true;
        invalidate();
    }

    private void resetView() {
        if (mScrollDirect != SCROLL_TO_NONE) {
            mWidgetRect[0] = new RectF(getMeasuredWidth() * (mIndex - 1), 0, getMeasuredWidth() * mIndex, getMeasuredHeight());
            mWidgetRect[1] = new RectF(getMeasuredWidth() * mIndex, 0, getMeasuredWidth() * (mIndex + 1), getMeasuredHeight());
            mWidgetRect[2] = new RectF(getMeasuredWidth() * (mIndex + 1), 0, getMeasuredWidth() * (mIndex + 2), getMeasuredHeight());
            resetPageIndex();
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        Log.e("computeScroll", "" + mScroller.computeScrollOffset() + " " + mScroller.getCurrX());
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        } else if (startScroll) {
            startScroll = false;
            resetView();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int index = 0; index < MAX_DRAW_BITMAPS; index++) {
            int drawPosition = mRealIndex + index - 1;
            if (drawPosition < 0) {
                drawPosition = mData.size() - 1;
            } else if (drawPosition > mData.size() - 1) {
                drawPosition = 0;
            }
            Log.e("onDraw", "drawPosition=" + drawPosition + "index=" + index + "mData.get(drawPosition)=" + mData.get(drawPosition).toString());
            canvas.drawBitmap(mData.get(drawPosition), null, mWidgetRect[index], mPaint);
        }
    }
}

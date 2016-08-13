package wuuchao.github.com.libarary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
    private Context mContext;
    private Scroller mScroller;
    private int[] drawBitmaps = new int[2];
    private int mCurX;
    private boolean startScroll;
    private int mTouchSlop;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private int mTouchState;
    private int mPreIndex, mIndex;
    private float mLastMotionX;
    private BitmapDrawable[] d = new BitmapDrawable[3];
    private Bitmap[] b = new Bitmap[3];

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
        drawBitmaps[0] = R.mipmap.splash1;
        drawBitmaps[1] = R.mipmap.ic_launcher;
        ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mScroller = new Scroller(context);
        b[0] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[drawBitmaps.length - 1]);
        b[1] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[0]);
        b[2] = BitmapFactory.decodeResource(mContext.getResources(), drawBitmaps[1]);
        d[0] = new BitmapDrawable(mContext.getResources(), b[drawBitmaps.length - 1]);
        for (int index = 1; index < d.length; index++) {
            d[index] = new BitmapDrawable(mContext.getResources(), b[index - 1]);
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
                    mPreIndex = mIndex;
                    if (velocityX > SNAP_VELOCITY) {
                        snapToScreen(--mIndex);
                    } else if (velocityX < -SNAP_VELOCITY) {
                        snapToScreen(++mIndex);
                    } else {
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
        mCurX = mScroller.getCurrX();
        int curIndex = Math.abs(mIndex % drawBitmaps.length);
        d[0] = new BitmapDrawable(mContext.getResources(), b[curIndex - 1 < 0 ? b.length - 1 : curIndex - 1]);
        for (int index = 1; index < d.length; index++) {
            d[1] = new BitmapDrawable(mContext.getResources(), b[curIndex + index - 1]);
        }
        invalidate();
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
        for (int index = 0; index < d.length; index++) {
            d[index].setBounds(mCurX - (-1 * (index - 1) * getWidth()), 0, mCurX + index * getWidth(), getHeight());
            d[index].draw(canvas);
        }
    }
}

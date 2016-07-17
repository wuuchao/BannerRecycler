package wuuchao.github.com.libarary;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 描述:
 * 作者: wuuchao 2
 * 时间: 2016/07/17 11:21
 */
public class BannerRecycler extends ViewGroup {
    public BannerRecycler(Context context) {
        super(context);
    }

    public BannerRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerRecycler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public BannerRecycler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}

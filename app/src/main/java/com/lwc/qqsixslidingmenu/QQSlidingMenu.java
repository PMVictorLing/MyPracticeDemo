package com.lwc.qqsixslidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;


/**
 * Created by lingwancai on
 * 2018/9/5 16:28
 */
public class QQSlidingMenu extends HorizontalScrollView {
    private static final String TAG = "QQSlidingMenu";

    //菜单的宽度
    private int mMenuWidth;

    private View mMenuView;

    private View mContentView;

    //GestureDetector 处理快速滑动
    private GestureDetector mGestureDetector;

    //菜单是否打开
    private boolean mMenuIsOpen = false;

    //是否拦截事件
    private boolean isInterceptTouch = false;
    private View mShadowView;

    public QQSlidingMenu(Context context) {
        this(context, null);
    }

    public QQSlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQSlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //解析自定义属性
        TypedArray array = context.obtainStyledAttributes(R.styleable.QQSlidingMenu);
        int rightMarin = (int) array.getDimension(R.styleable.QQSlidingMenu_rightMargin, Tool.dip2px(context, 50));
        //菜单页的宽度 = 屏幕宽度 - 右边一小部分距离（自定义属性）
        mMenuWidth = (getScreenWidth(context) - rightMarin);

        //回收
        array.recycle();

        //这种方式不可取
        /*mGestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });*/
        //优先选择该 监听listener
        mGestureDetector = new GestureDetector(context, mGestureListerner);
    }


    private GestureDetector.OnGestureListener mGestureListerner = new GestureDetector.SimpleOnGestureListener() {
        //只关注快速滑动 实现自己要处理的方法 adapter设计模式
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //只关注快速滑动，只要快速滑动就会回调
            //条件 打开的时候往右边快速滑动切换（对应关闭）
            //     关闭的时候往左边快速滑动切换 （对应打开状态）
            Log.e(TAG, "velocityX ->" + velocityX);
            // 快速往左边滑动的时候是一个负数，往右边滑动的时候是一个正数
            if (mMenuIsOpen) {
                //打开的时候往右边快速滑动切换（对应关闭）
                if (velocityX < 0) {
                    closeMenu();
                    return true;//拦截事件
                }
            } else {
                // 关闭的时候往左边快速滑动切换 （对应打开）
                if (velocityX > 0) {
                    openMenu();
                    return true;//拦截事件
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };

    /**
     * 宽度不对（乱套了），指定宽高
     */
    @Override
    protected void onFinishInflate() {
        //这个方法是布局解析完毕 也是xml布局解析完毕
        super.onFinishInflate();
        //指定宽高 内容页的宽度是屏幕的宽度
        //获取LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2)
            throw new RuntimeException("只能放置两个子View");

        //获取菜单
        //菜单页的宽度 = 屏幕宽度 - 右边一小部分距离（自定义属性）
        mMenuView = container.getChildAt(0);
        //设置参数只能通过 LayoutParams
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams();
        menuParams.width = mMenuWidth;
        //7.0 以下的手机必须采用下面的方式
        mMenuView.setLayoutParams(menuParams);

        //获取内容布局-----在布局外添加一层阴影
        //把内容布局单独提取出来;
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams();
        container.removeView(mContentView);
        //然后在外面套层阴影;
        RelativeLayout contentOutView = new RelativeLayout(getContext());
        contentOutView.addView(mContentView);
        mShadowView = new View(getContext());
        mShadowView.setBackgroundColor(Color.parseColor("#55000000"));
        contentOutView.addView(mShadowView);
        //最后在把内容放到原来位置;
        contentParams.width = getScreenWidth(getContext());
//        mContentView.setLayoutParams(contentParams);
        contentOutView.setLayoutParams(contentParams);
        container.addView(contentOutView);
        //设置透明度
        mShadowView.setAlpha(0.0f);

    }

    /**
     * 事件拦截
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        isInterceptTouch = false;
        // 2. 处理事件拦截 + ViewGroup 事件分发的源码实践
        //    当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，
        //    还需要拦截事件（打开情况下点击内容页不会相应点击事件）
        if (mMenuIsOpen) {
            float currentX = ev.getX();
            if (currentX > mMenuWidth) {
                //关闭菜单
                closeMenu();
                //拦截子view事件 就是子View 不需要相应任何事件（点击和触摸）
                //如果返回true 代表我会拦截子View的事件，但是会响应自己的onTouch事件
                isInterceptTouch = true;
                return true;
            }

        }


        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 3.手指抬起是二选一，要么关闭要么打开
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 1. 获取手指滑动的速率，当大于一定值就认为是快速滑动 ，
        //    使用GestureDetector,手势检测
        if (mGestureDetector.onTouchEvent(ev)) {
            // mGestureDetector.onTouchEvent(ev);//如果快速滑动执行了，下面的 MotionEvent.ACTION_UP 事件就不要执行了
            return true;//拦截事件
        }
        // 2. 处理事件拦截 + ViewGroup 事件分发的源码实践
        //    当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，
        //    还需要拦截事件（打开情况下点击内容页不会相应点击事件）
        if (isInterceptTouch) {
            //如果有拦截就不需要执行自动的onTouch事件
            return true;
        }

        // 3.手指抬起是二选一，要么关闭要么打开
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int currentScrollX = getScrollX();
            if (currentScrollX > mMenuWidth / 2) {
                closeMenu();
            } else {
                openMenu();
            }
            //对事件进行拦截
            //确保  super.onTouchEvent(ev); 不会执行
            return true;
        }


        return super.onTouchEvent(ev);
    }

    // 4.处理右边的缩放，左边的缩放和透明度，需要不断的获取当前滚动的位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 算一个梯度值  scale 变化是 1 - 0
        float scale = 1f * l / mMenuWidth;

        //控制阴影 0 - 1
        float alphaScale = 1 - scale;
        mShadowView.setAlpha(alphaScale);

         /*//int l 的变化是:mMenuWidth - 0
        Log.e(TAG, "l -> " + l);
        // 算一个梯度值
        // scale 变化是 1 - 0
        float scale = 1f * l / mMenuWidth;
        //右边的缩放：最小是 0.7f, 最大是 1f
        float rightScale = 0.7f + 0.3f * scale;
        //设置右边的缩放，默认是以中心点缩放
        //设置缩放的中心点位置
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView, rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        //菜单的缩放和透明度
        //透明度是 半透明到完全透明 0.5f - 1.0f
        float leftAlpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(mMenuView, leftAlpha);
        //缩放 0.7f - 1.0f
        float leftScale = 0.7f + (1 - scale) * 0.3f;
        ViewCompat.setScaleX(mMenuView, leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);*/

        // 最后一个效果 退出这个按钮刚开始是在右边，按照我们目前的方式永远都是在左边
        // 设置平移 先看一个抽屉效果
//        ViewCompat.setTranslationX(mMenuView,l);
        ViewCompat.setTranslationX(mMenuView, 0.6f * l);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 初始化是关闭菜单
        scrollTo(mMenuWidth, 0);
    }

    /**
     * 打开菜单 滚动到0的位置
     */
    public void openMenu() {
        //smoothScrolto 有动画
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }

    /**
     * 关闭菜单 滚动到mMenuWidth的位置
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}

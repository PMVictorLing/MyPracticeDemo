package com.lwc.qqsixslidingmenu;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.WindowManager;


/**
 * Created by Administrator on 2016/9/19.
 */
public class Tool {

    /**
     * 判断输入对象不为空或不为空字符
     *
     * @param object
     * @return
     */
    public static boolean isNull(Object object) {
        if (object == null || object.equals("") || object.equals("null")) {
            return true;
        }
        return false;
    }

    /**
     * 设置界面透明度
     *
     * @param activity
     * @param alpha
     */
    public static void setWindow(Activity activity, float alpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 判断要分享的应用是否存在
     *
     * @param packageName 应用包名
     * @return
     */
    public static boolean checkInstallation(String packageName) {
        return false;
    }

    /**
     * 判断网络是否连接
     * * @param context
     * * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据手机分辨率从DP转成PX
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }



}

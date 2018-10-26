package com.eric.http.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * @author li
 * @Package com.eric.http.utils
 * @Title: NetworkUtils
 * @Description: Copyright (c)
 * Create DateTime: 2017/11/29
 */
public class NetworkUtils {
    private static boolean wasDialogShow = false;

    /**
     * 该方法只在模板中使用
     *
     * @param context 上下文
     */
    public static void setNetworkMethod(final Context context) {
        if (wasDialogShow) {// 如果网络设置对话框已经显示，则不再显示
            return;
        } else {
            wasDialogShow = true;
            setNetWorkMethodWithDismiss(context, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    wasDialogShow = false;
                }
            }, null);
        }
    }

    /**
     * 判断wifi 是否可用
     *
     * @param context 上下文
     * @return wifi 是否可用
     * @throws Exception 异常信息
     */
    public static boolean isWifiDataEnable(Context context) throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiDataEnable = false;
        isWifiDataEnable = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        return isWifiDataEnable;
    }

    /**
     * 监听Dialog dismiss事件和keyClick事件
     *
     * @param context           上下文
     * @param onDismissListener 弹窗消失回调监听
     * @param onKeyListener     按键监听
     */
    public static void setNetWorkMethodWithDismiss(final Context context, final DialogInterface.OnDismissListener
            onDismissListener, final DialogInterface.OnKeyListener onKeyListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setOnKeyListener(onKeyListener);
        builder.setTitle("网络连接失败！").setMessage("没有网络, 请检查网络设置。").setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(onDismissListener);
        dialog.show();
    }

    /**
     * 打开设置网络界面
     *
     * @param context         上下文
     * @param dismissListener 取消监听回调
     */
    public static void setNetworkMethod(final Context context, final DialogInterface.OnClickListener dismissListener) {
        // 提示对话框
        setNetworkMethodWithKey(context, dismissListener, null);
    }

    /**
     * 打开设置网络界面
     *
     * @param context         上下文
     * @param dismissListener 取消监听回调
     * @param onKeyListener   按键监听
     */
    public static void setNetworkMethodWithKey(final Context context, final DialogInterface.OnClickListener dismissListener,
                                               DialogInterface.OnKeyListener onKeyListener) {
        // 提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setOnKeyListener(onKeyListener);
        builder.setTitle("网络连接失败！").setMessage("没有网络, 请检查网络设置。").setPositiveButton("设置", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent intent = null;
                // intent = new
                // Intent(android.provider.Settings.ACTION_SETTINGS);
                intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (dismissListener != null) {
                    dismissListener.onClick(dialog, which);
                }
            }
        }).show();
    }

    /**
     * 打开设置网络界面
     *
     * @param context          上下文
     * @param positiveListener 确认监听回调
     * @param negativeListener 取消监听回调
     */
    public static void setNetworkMethod(final Context context, DialogInterface.OnClickListener positiveListener,
                                        DialogInterface.OnClickListener negativeListener) {
        // 提示对话框
        setNetworkMethod(context, positiveListener, negativeListener, null);
    }

    /**
     * 打开设置网络界面
     *
     * @param context          上下文
     * @param positiveListener 确认监听回调
     * @param negativeListener 取消监听回调
     * @param onKeyListener    按键监听回调
     */
    public static void setNetworkMethod(final Context context, DialogInterface.OnClickListener positiveListener,
                                        DialogInterface.OnClickListener negativeListener, DialogInterface.OnKeyListener
                                                onKeyListener) {
        // 提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setOnKeyListener(onKeyListener);
        builder.setTitle("网络连接失败！").setMessage("没有网络, 请检查网络设置。").setPositiveButton("设置", positiveListener).setNegativeButton
                ("取消", negativeListener).show();
    }

    /**
     * check if network is available
     *
     * @param cxt 上下文
     * @return check if network is available
     */
    public static boolean isNetworkAvailable(Context cxt) {
        if (cxt == null) {
            return false;
        }
        Context context = cxt instanceof Activity ? cxt.getApplicationContext() : cxt;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * make true current connect service is wifi
     *
     * @param mContext 上下文
     * @return make true current connect service is wifi
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 移除所有cookie
     *
     * @param context 上下文
     */
    public static void removeAllCookies(Context context) {
        getCookieManager(context).removeAllCookie();
    }

    /**
     * 获取CookieManager实例
     *
     * @param context 上下文
     * @return CookieManager实例
     */
    public static CookieManager getCookieManager(Context context) {
        CookieSyncManager.createInstance(context);
        return CookieManager.getInstance();
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }


    /**
     * Network type is unknown
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;
    /**
     * Current network is GSM {@hide}
     */
    public static final int NETWORK_TYPE_GSM = 16;


    /**
     * 手机网络制式号
     *
     * @param context 上下文
     * @return 手机网络制式号
     */
    public static String getNetworkType(Context context) {
        String strNetworkType = "";
        if (context == null) {
            return strNetworkType;
        }
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                Log.e("Network", "Network getSubtypeName : " + _strSubTypeName);
                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case NETWORK_TYPE_GPRS:
                    case NETWORK_TYPE_EDGE:
                    case NETWORK_TYPE_CDMA:
                    case NETWORK_TYPE_1xRTT:
                    case NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case NETWORK_TYPE_UMTS:
                    case NETWORK_TYPE_EVDO_0:
                    case NETWORK_TYPE_EVDO_A:
                    case NETWORK_TYPE_HSDPA:
                    case NETWORK_TYPE_HSUPA:
                    case NETWORK_TYPE_HSPA:
                    case NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:                    // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName == null) {
                            strNetworkType = "null";
                        } else if ("TD-SCDMA".equalsIgnoreCase(_strSubTypeName) || "WCDMA".equalsIgnoreCase(_strSubTypeName) ||
                                "CDMA2000".equalsIgnoreCase(_strSubTypeName)) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }
                        break;
                }
                Log.e("Network", "Network getSubtype : " + Integer.toString(networkType));
            }
        }
        Log.e("Network", "Network Type : " + strNetworkType);
        return strNetworkType;
    }
}

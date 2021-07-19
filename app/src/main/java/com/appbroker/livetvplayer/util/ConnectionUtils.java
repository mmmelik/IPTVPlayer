package com.appbroker.livetvplayer.util;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;

public class ConnectionUtils {

    public static boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public static boolean isInternetAvailable(Context context) {
        if (isNetworkConnected(context)){
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");//TODO: Bad approach.
                //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                return false;
            }
        }else {
            return false;
        }
    }
}

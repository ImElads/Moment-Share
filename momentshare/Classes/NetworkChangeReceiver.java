package com.example.momentshare.Classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface NetworkChangeCallback {
        void onNetworkLost();
        void onNetworkAvailable();
    }

    private static NetworkChangeCallback callback;

    public static void setNetworkChangeCallback(NetworkChangeCallback cb) {
        callback = cb;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkConnected(context)) {
            if (callback != null) callback.onNetworkAvailable();
        } else {
            if (callback != null) callback.onNetworkLost();
        }
    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
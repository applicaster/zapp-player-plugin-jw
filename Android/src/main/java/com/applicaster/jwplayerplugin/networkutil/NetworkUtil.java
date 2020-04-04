package com.applicaster.jwplayerplugin.networkutil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

public class NetworkUtil {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int NETWORK_STATUS_MOBILE = 2;

    public interface ConnectionAvailabilityCallback {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        int status = 0;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void checkConnectionAvailability(
            final ConnectionAvailabilityCallback availabilityCallback,
            final Context context
    ) {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        availabilityCallback.onNetworkAvailable();
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        availabilityCallback.onNetworkLost();
                    }
                });
            }
        };

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }
}

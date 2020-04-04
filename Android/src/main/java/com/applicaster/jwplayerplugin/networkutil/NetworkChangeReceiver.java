package com.applicaster.jwplayerplugin.networkutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkUtil.ConnectionAvailabilityCallback connectionAvailabilityCallback;

    public NetworkChangeReceiver(NetworkUtil.ConnectionAvailabilityCallback callback) {
        this.connectionAvailabilityCallback = callback;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                connectionAvailabilityCallback.onNetworkLost();
            } else {
                connectionAvailabilityCallback.onNetworkAvailable();
            }
        }
    }
}

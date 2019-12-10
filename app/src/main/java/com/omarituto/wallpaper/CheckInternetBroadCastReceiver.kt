package com.omarituto.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.google.android.material.snackbar.Snackbar
import android.widget.Toast

class CheckInternetBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent!!.action)) {
            val noConnectivity: Boolean = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            )
            if (noConnectivity) {
                isConnected = false
                Toast.makeText(context, "No internet connexion", Toast.LENGTH_SHORT).show()
            } else {
                isConnected = true
            }
        }
    }

    companion object {
        var isConnected: Boolean = false
    }
}
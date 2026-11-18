package com.josh.overlayhome

import android.content.Context
import android.content.Intent
import android.util.Log

object AppLauncher {
    private const val PREFS = "state"
    private const val KEY_LAST = "last_pkg"

    /** Opens an app, bringing its existing task (and playback state) forward if it's already running. */
    fun launch(context: Context, pkg: String): Boolean {
        val pm = context.packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
            ?: pm.getLaunchIntentForPackage(pkg)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(intent)
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST, pkg).apply()
            true
        } catch (e: Exception) {
            Log.w("OverlayHome", "Could not launch $pkg", e)
            false
        }
    }

    fun lastLaunched(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST, null)
}

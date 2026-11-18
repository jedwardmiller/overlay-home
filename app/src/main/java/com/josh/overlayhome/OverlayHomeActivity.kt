package com.josh.overlayhome

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * The "home screen". It has a transparent background, so the app you came
 * from stays visible behind the row of app tiles.
 *
 * - Home pressed inside an app  -> overlay appears
 * - Home or Back on the overlay -> returns to the app you were in
 * - First start after a reboot  -> opens Config.DEFAULT_APP
 */
class OverlayHomeActivity : Activity() {

    private data class TvApp(val pkg: String, val label: String, val art: Drawable?)

    private lateinit var row: LinearLayout
    private var showAll = false
    private var visibleNow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)
        row = findViewById(R.id.app_row)

        WakeService.start(this)
        openDefaultIfFreshBoot()
    }

    override fun onStart() {
        super.onStart()
        visibleNow = true
    }

    override fun onStop() {
        super.onStop()
        visibleNow = false
        showAll = false
    }

    override fun onResume() {
        super.onResume()
        buildRow()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home pressed while the overlay is already showing -> toggle it off.
        if (visibleNow) returnToPreviousApp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (showAll) {
            showAll = false
            buildRow()
        } else {
            returnToPreviousApp()
        }
    }

    // ---------------------------------------------------------------------

    private fun openDefaultIfFreshBoot() {
        val bootCount = Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, -1)
        val prefs = getSharedPreferences("state", MODE_PRIVATE)
        if (prefs.getInt("handled_boot", Int.MIN_VALUE) != bootCount) {
            prefs.edit().putInt("handled_boot", bootCount).apply()
            AppLauncher.launch(this, Config.DEFAULT_APP)
        }
    }

    private fun returnToPreviousApp() {
        val target = AppLauncher.lastLaunched(this) ?: Config.DEFAULT_APP
        if (!AppLauncher.launch(this, target)) AppLauncher.launch(this, Config.DEFAULT_APP)
    }

    private fun loadApps(): List<TvApp> {
        val pm = packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val installed = pm.queryIntentActivities(query, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { ri ->
                val ai = ri.activityInfo
                TvApp(
                    pkg = ai.packageName,
                    label = ri.loadLabel(pm).toString(),
                    art = ai.loadBanner(pm) ?: ai.applicationInfo.loadBanner(pm) ?: ri.loadIcon(pm)
                )
            }

        return if (showAll || Config.ALLOWED_APPS.isEmpty()) {
            installed.sortedBy { it.label.lowercase() }
        } else {
            Config.ALLOWED_APPS.mapNotNull { p -> installed.find { it.pkg == p } }
        }
    }

    private fun buildRow() {
        row.removeAllViews()

        loadApps().forEach { app ->
            row.addView(makeTile(app.label, app.art) { AppLauncher.launch(this, app.pkg) })
        }

        if (Config.SHOW_ALL_APPS_TILE && !showAll && Config.ALLOWED_APPS.isNotEmpty()) {
            row.addView(makeTile("All apps", getDrawable(android.R.drawable.ic_menu_more)) {
                showAll = true
                buildRow()
            })
        }

        if (Config.SHOW_SETTINGS_TILE) {
            row.addView(makeTile("Settings", getDrawable(android.R.drawable.ic_menu_preferences)) {
                startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
        }

        // Put focus on the app you came from, so OK/Back feels natural.
        val last = AppLauncher.lastLaunched(this)
        val index = loadApps().indexOfFirst { it.pkg == last }.coerceAtLeast(0)
        row.post { row.getChildAt(index)?.requestFocus() }
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

    private fun makeTile(label: String, art: Drawable?, onClick: () -> Unit): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            isFocusable = true
            isClickable = true
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = getDrawable(R.drawable.tile_bg)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(18) }
        }

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(176), dp(99))
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageDrawable(art)
        }

        val text = TextView(this).apply {
            this.text = label
            setTextColor(Color.WHITE)
            textSize = 15f
            alpha = 0f
            setPadding(0, dp(8), 0, 0)
        }

        tile.addView(image)
        tile.addView(text)

        tile.setOnFocusChangeListener { v, hasFocus ->
            val s = if (hasFocus) 1.12f else 1f
            v.animate().scaleX(s).scaleY(s).setDuration(120).start()
            text.animate().alpha(if (hasFocus) 1f else 0f).setDuration(120).start()
        }
        tile.setOnClickListener { onClick() }
        return tile
    }
}

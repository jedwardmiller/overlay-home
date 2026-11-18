package com.josh.overlayhome

/**
 * Edit this file to customise the launcher.
 * Find a package name with:  adb shell pm list packages | grep -i <name>
 */
object Config {

    /** Opens at boot, on every wake, and when Back is pressed with nothing to return to. */
    const val DEFAULT_APP = "com.google.android.youtube.tvunplugged" // YouTube TV

    /**
     * Apps shown in the overlay, in this order. Apps that aren't installed are skipped.
     * Leave the list empty to show every installed TV app alphabetically.
     */
    val ALLOWED_APPS = listOf(
        "com.google.android.youtube.tvunplugged", // YouTube TV
        "com.plexapp.android",                    // Plex
        "org.jellyfin.androidtv",                 // Jellyfin
        "com.google.android.youtube.tvkids",      // YouTube Kids
        "com.google.android.youtube.tv",          // YouTube
        "com.netflix.ninja",                      // Netflix
        "com.disney.disneyplus",                  // Disney+
    )

    /** Adds an "All apps" tile that expands the row to every installed app. */
    const val SHOW_ALL_APPS_TILE = true

    /** Adds a Settings tile (needed — the stock launcher's gear icon is gone). */
    const val SHOW_SETTINGS_TILE = true

    /** Re-open DEFAULT_APP whenever the TV wakes from standby. */
    const val OPEN_DEFAULT_ON_WAKE = true
}

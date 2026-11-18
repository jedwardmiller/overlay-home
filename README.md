# Overlay Home for Google TV

A minimal home app: YouTube TV opens on boot and wake, Home shows a row of
your apps over whatever is playing, and Home or Back returns you to that app.

Customise everything in `app/src/main/java/com/josh/overlayhome/Config.kt`.

## 1. Build
Open the `OverlayHome` folder in Android Studio, let it sync (it will offer to
add the Gradle wrapper), then **Build > Build APK(s)**.
The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## 2. Turn on ADB on the TV
Settings > System > About > click **Android TV OS build** 7 times.
Then Settings > System > Developer options > turn on **USB debugging**
(on some models also **Network/Wireless debugging**). Note the TV's IP address.

## 3. Install and switch over (from your PC)
```bash
adb connect <TV-IP>:5555            # accept the prompt on the TV
adb install -r app-debug.apk

# Let it open YouTube TV from the background on wake
adb shell appops set com.josh.overlayhome SYSTEM_ALERT_WINDOW allow

# Google TV ignores the "default home" choice, so the stock launcher has to be disabled
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith

adb reboot
```

## Undo (restores the stock Google TV home screen)
```bash
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
adb uninstall com.josh.overlayhome
```

## Notes
- If a Google TV update brings the stock home screen back, re-run the two `disable-user` lines.
- Back on the overlay returns to the last app *opened from the overlay*. Apps opened with the
  remote's shortcut buttons (e.g. Netflix) aren't tracked.
- Find a package name for Config.kt: `adb shell pm list packages | grep -i plex`

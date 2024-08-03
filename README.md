## Train Application
This Train Application is currently under development. 
The app was inspired by existing navigation solutions like Google Maps and Citymapper, and in particular, the "Get-off alert" function of Citymapper. I intended to explore whether device sensors could improve Get-off alerts for commuters.

The original motivation behind the app was to use accelerometer sensors on an individual's mobile device to predict the commuter's position in areas where GPS reception is poor, such as in train tunnels. However, I later realised that this was an inherent limitation of mobile device sensors -- they do not detect ACTUAL acceleration, but rather RELATIVE acceleration.

Having to rely on GPS, I then attempted to offer personalisation for get-off alerts in app settings.

## Features
Several features of the app include:
- Choice of nearest station based on current location
- Customisable get-off alerts based on number of stations apart, "reaching" distance, and "reached" distance
- Get-off alerts will be sent when the device is within the "reaching" distance of qualifying stations, i.e. stations that are within the set number of stations apart from the destination station. 
- Get-off alerts will be paused once the device is within the "reached" distance of qualifying stations.

## Problems
Known problems include:
- Status resets when device orientation is changed.
- Notifications don't redirect to the original app activity.

## A note
The app is built on Kotlin, and available as an APK on Android Devices.
The APK can be downloaded under get-app/app-debug.apk
The current database only includes selected EW and DT stations I live near to, for testing on my commutes.

## Required Permissions
Precise Location - For accurate location determining
Notifications - For alerting
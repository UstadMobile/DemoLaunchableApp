
# Launchable App Manifest URL 

Install by downloading the APK from Github Releases then use this manifest Url:

https://demo.openeel.org/appmanifest.json

# Command line launch

```shell

adb shell "am start -W -a android.intent.action.VIEW -d 'https://demo.openeel.org/grade/1/learningunits/2/learningunit.html?endpoint=abc&actor=xyz&activity_id=bad&auth=secret'"
```

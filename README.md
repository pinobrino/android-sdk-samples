android-sdk-samples
===================

Samples projects for the Snapback SDK.

Visit http://developer.snapback.io and join in the Early Adopter Program to get Our SDK and the full documentation.

In order to compile the projects properly, first remember to add our SDK library jar file in *libs* project directory. Second, add support library v7 dependency where required.
Usually a project called *appcompat_v7* should be already in your workspace if you already handled with Android application which support older Android API Level. If not, you can refer to https://developer.android.com/tools/support-library/setup.html.
Third, add Google Play Services dependency where required (see http://developer.android.com/google/play-services/setup.html).
If you are using Android Studio, you have to edit your app/build.gradle adding in dependecies section the followings:

``` java
dependencies {
	compile fileTree(dir: 'libs', include: ['*.jar'])

	# our SDK
	compile files ('/libs/snapback-android-sdk-current-0.3.jar');

	# Support library
        compile 'com.android.support:appcompat-v7:21.0.3'

	# Google Analytics
	compile 'com.google.android.gms:play-services:6.5.87';
}
```


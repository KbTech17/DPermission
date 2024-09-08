# DPermission

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
[![AGPL Android](https://www.vectorlogo.zone/logos/android/android-ar21.svg)]()

DPermission is an extension Android library that makes Android runtime permission request in an efficient and easy way. You can use it for permission request occasions or handle more complex conditions, like showing rationale dialog or go to app settings for allowance manually.

## Setup Guide

Add the following dependency to your build.gradle.

```groovy
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {
    implementation 'com.github.KbTech17:DPermission:2.1.8'
}
```
or
```groovy
repositories {
  google()
  mavenCentral()
}

dependencies {
    implementation 'com.github.KbTech17:DPermission:2.1.8'
}
```

Then you are ready to go.

## Usage Guide

Use DPermission to request Android runtime permissions is extremely simple.

Step 1. Declared permissions in the AndroidManifest.xml first.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.telephony"
        android:required="false" />

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" /><!--API 33-->
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

........

</manifest>
```

Then just request DPermissions as follow.

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
```java
  DPermission.with(this)
    .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
    .request(new OnPermissionCallback() {
      @Override
      public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
          if (isGranted) {
            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
          }
      }
    });
```


[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
```kotlin
  DPermission.with(this)
	.permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
	.request(OnPermissionCallback { isGranted, grantedList, deniedList ->
		if (isGranted) {
			Toast.makeText(this, "All permissions are granted",Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "These permissions are denied", Toast.LENGTH_LONG).show()
		}
  })
```

This is **Basic** concept, just to ask runtime permission without any dialog.

Pass any instance or Context into **with** method, and specify the permissions that you want to request in the **permissions** method, then call **request** method for requesting.

The **OnPermissionCallback** will return the result. If all permissions are granted or not by the user will be returned by bolean **isGranted** true or false. **grantedList** : all granted permissions | **deniedList** : all denied permissions.



## More Usage

As you know, Android provide **shouldShowRequestPermissionRationale** method to indicate us if we should show a rationale dialog to explain to user why we need this permission. Otherwise user may deny the permissions and may checked **never ask again** option.

To simplify this process, DPermission provide **onReasonToRequest** method. This method before **request** method, If user deny one of the permissions, **onReasonToRequest** method will get call first. Then you can call **showReasonDialog** method to explain to user why these permissions are necessary like below.

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
```java
DPermission.with(this)
    .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
    .onReasonToRequest(new RequestReasonCallback() {
      @Override
      public void onExplainReason(@NonNull ReasonToAsk reason, @NonNull List<String> deniedList) {
          reason.showReasonDialog(deniedList, "These permissions are required!", "OK", "Cancel");
      }
  }).request(new OnPermissionCallback() {
      @Override
      public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
          if (isGranted) {
              Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
          } else {
              Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
          }
      }
  });
```

[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
```kotlin
  DPermission.with(this)
	.permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
	.onReasonToRequest(RequestReasonCallback { reason, deniedList ->
		reason.showReasonDialog(deniedList,"These permissions are required!","OK","Cancel")})
	.request(OnPermissionCallback { isGranted, grantedList, deniedList ->
		if (isGranted) {
			Toast.makeText(this, "All permissions are granted",Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "These permissions are denied",Toast.LENGTH_LONG).show()
		}
	})
```
**showReasonDialog** method will prompt a rationale dialog with the information that second parameter provide. If user click positive button which shows text as third parameter provide, DPermission will request again with the permissions that first parameter provide.

The fourth parameter as text for negative button is optional. If the denied permissions are necessary, you can ignore the fourth parameter and the dialog will be uncancelable. Which means user must allow these permissions for further usage.

Of course, user still may deny some permissions and checked **never ask again** option. In this case, each time we request these permissions again will be denied automatically. The only thing we could do is prompt to users they need to allow these permissions manually in app settings for continuation usage. But DPermission do it better.

DPermission provide **onManualSettings** method for handling this condition. If some permissions are still "denied and never ask again" by user, **onManualSettings** method will be call. Then you can call **showManualSettingDialog** method.

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
```java
DPermission.with(this)
    .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
    .onReasonToRequest(new RequestReasonCallback() {
      @Override
      public void onExplainReason(@NonNull ReasonToAsk reason, @NonNull List<String> deniedList) {
          reason.showReasonDialog(deniedList, "These permissions are required!", "OK", "Cancel");
      }
    }).onManualSettings(new ManualSettingCallback() {
      @Override
      public void onManualSettings(@NonNull ManualScope manual, @NonNull List<String> deniedList) {
          manual.showManualSettingDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel");
      }
    }).request(new OnPermissionCallback() {
      @Override
      public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
          if (isGranted) {
            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
          }
      }
    });
```

[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
```kotlin
  DPermission.with(this)
	.permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
	.onReasonToRequest(RequestReasonCallback { reason, deniedList ->
		reason.showReasonDialog(deniedList, "These permissions are required!", "OK", "Cancel")})
	.onManualSettings(ManualSettingCallback { manual, deniedList ->
		manual.showManualSettingDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel") })
	.request(OnPermissionCallback { isGranted, grantedList, deniedList ->
		if (isGranted) {
			Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
		} else {
			Toast.makeText(this, "These permissions are denied", Toast.LENGTH_LONG).show()
		}
	})
```
The parameters in **onManualSettings** method are similar with **onReasonToRequest** method. When user click positive button, DPermission will forward to the App settings in device and user can turn on the necessary permissions again. When user switch back to app, DPermission will request the necessary permissions automatically again.

## Explain Before Request

It is always a good practice to show the rationale dialog and explain to users why App needs those permissions before requesting.

To do that with DPermission is quite simple. Just use **explainReasonBeforeRequest** method like below.

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
```java
DPermission.with(this)
    .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
    .explainBeforeRequest()
//..................YOUR_CODE......................
  ;
```

[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
```kotlin
DPermission.with(this)
    .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
    .explainBeforeRequest()
//..................YOUR_CODE......................
  
```
Now **Good to go**.

## Dark Theme

DPermission support Android dark theme automatically or you do not need to write any additional code. Just change your device into dark theme, everything works great.


## License

```
Copyright (C) DPermission Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


## Do your code fly ?
Buy a cup of coffee for me:- </br>
dpaeppac@gmail.com</br>
[![MIT PayPal](https://www.vectorlogo.zone/logos/paypal/paypal-ar21.svg)]()


## Platform

You can use it for developing a Android Platform

[![MIT Java](https://www.vectorlogo.zone/logos/java/java-ar21.svg)]()
[![GPLv3 Kotlin](https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-ar21.svg)]()
[![AGPL Android](https://www.vectorlogo.zone/logos/android/android-ar21.svg)]()


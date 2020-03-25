# myket_flutter

MyKet In-App Billing package for flutter this package is just for android applications and not work on ios

This plugin is just for Android applications

این نسخه از پلایگن به صورت کامل تست نشده است  و ممکن است خطای وجود داشته باشد در صورت وجود هر گونه خطا لطفا از طریق صفحه گیت هاب برای ما ارسال نماید 

## Getting Started

### Import this line in Flutter pubspec
```dart
myket_flutter: <Last Version>
```

### Updating Your Application's Manifest
Adding the ir.mservices.market.BILLING permission to your AndroidManifest.xml file for In-App Billing
Adding ir.mservices.market.CHECK_LICENSE permission to your AndroidManifest.xml file for CHECK LICENSE
```dart

<uses-permission android:name="ir.mservices.market.CHECK_LICENSE" />
    <uses-permission android:name="ir.mservices.market.BILLING" />

```

### To Use myket plugin import below code to your class(your payment class)
```dart
import 'package:myket_flutter/myket_flutter.dart';
```

* All method be static and don't need to initialize any class


### To check License (نمایش صفحه برنامه نویس)
# for more info visit http://kb.myket.ir/pages/viewpage.action?pageId=1900832
```dart
MyketFlutter.checkLicense() 

```


### To show developer applications page in myket use this code(نمایش صفحه برنامه نویس)
```dart
MyketFlutter.showDeveloperApplicationsPage("packageName")  // Replace "package Name" with Your application Package Name. you can find package name on gradle file

```

### To show comment and privilege in myket use this code(ثبت نظر و امتیاز به برنامه)
```dart
MyketFlutter.setComment("packageName")  // Replace "package Name" with Your application Package Name. you can find package name on gradle file

```

### Access to app details page(دسترسی به صفحه‌ی جزئیات برنامه)
```dart
MyketFlutter.showApplicationDetails("packageName")  // Replace "package Name" with Your application Package Name. you can find package name on gradle file

```
### If you want to start downloading the app when you open the app's details page on Myket, you can use the below pattern(در صورت به‌ کار‌بردن الگوی زیر، همراه با بازشدن صفحه‌ اطلاعات برنامه در مایکت، دانلود آن هم آغاز می‌شود)
```dart
MyketFlutter.showApplicationDetails("packageName")  // Replace "package Name" with Your application Package Name. you can find package name on gradle file

```


### You can view an app's video directly by using the below pattern(با استفاده از الگوی زیر می‌توان به صورت مستقیم، ویدئو یک برنامه را مشاهده کرد)
```dart
MyketFlutter.showApplicationVideo("packageName")  // Replace "package Name" with Your application Package Name. you can find package name on gradle file

```




For use In-app purchases on your application use below code
جهت استفاده از پرداخت درون برنامه ای به صورت زیر عملکنید.

### First of all initialize payment and ras Key in `initState`
use async and await
```dart
await MyketFlutter.initPay(rsaKey:"Your RSA Key From myket");

```



### To start a purchase request from your app, call the `launchPurchaseFlow` method on the In-app Billing plugin
```dart
Map<String,dynamic> result = await MyketFlutter.launchPurchaseFlow(
                        sku: "wm2", consumption: false,payload:"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
 // after pay you get some data from myket, if you get response code -1005 the payment is canceled by the user and  if get code 0  the payment is Success

```
1. `sku` : your product id on MyketFlutter

2. `consumption` : if your product is not a subscriber type you must consume it, For product consumption, set the consumption to true

 `consumption` : زمانی که محصول شما از نوع اشتراکی نباشد و از نوع مصرفی می باشد  باید ان را مصرف کنید تا در خرید ها بعدی کاربر بتواند ان را خریداری کند بجت مصرف یک محصول consumption را به صورت true تنظیم کنید

4.`payload` : The `developerPayload` String is used to specify any additional arguments that you want Myket to send back along with the purchase information.


* Result If pay is success
```dart
'{
    "isSuccess": "true",
    "response":"0",
    "message":"Success (response: 0:OK)",
    "purchase":{
       "orderId":"12999763169054705758.1371079406387615",
       "packageName":"com.example.app",
       "productId":"exampleSku",
       "purchaseTime":1345678900000,
       "purchaseState":0,
       "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
       "purchaseToken":"rojeslcdyyiapnqcynkjyyjh"
     }
}'
````


### To get Purchase details use below code
```dart
Map<String,dynamic> result = MyketFlutter.getPurchase(sku:"your product sku") // you can find sku(product id) in your application in-app section
// you get the payment details if you consumption the product result is null
```

Result
```dart
'{
   "orderId":"12999763169054705758.1371079406387615",
   "packageName":"com.example.app",
   "productId":"exampleSku",
   "purchaseTime":1345678900000,
   "purchaseState":0,
   "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
   "purchaseToken":"rojeslcdyyiapnqcynkjyyjh"
 }'
````

* Security Recommendation: When you send a purchase request, create a String token that uniquely identifies this purchase request and include this token in the developerPayload.You can use a randomly generated string as the token. When you receive the purchase response from Myket, make sure to check the returned data signature, the orderId, and the developerPayload String. For added security, you should perform the checking on your own secure server. Make sure to verify that the orderId is a unique value that you have not previously processed, and the developerPayload String matches the token that you sent previously with the purchase request.
### After Payment result check developerPayload result true or false
```dart
bool result = await MyketFlutter.verifyDeveloperPayload("your developerPayload");
```

* Important: Remember to unbind from the In-app Billing service when you are done with your Activity. If you don’t unbind, the open service connection could cause your device’s performance to degrade. This example shows how to perform the unbind operation on a service connection to In-app Billing called mServiceConn by overriding the activity’s onDestroy method.

```dart
await MyketFlutter.dispose();
```
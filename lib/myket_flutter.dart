import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';
import 'dart:convert';

class MyketFlutter {
  static const MethodChannel _channel = const MethodChannel('myket_flutter');

  static Future<dynamic> initPay({@required String base6PublicKey}) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("BASE64PUBLICKEY", () => base6PublicKey);
    dynamic result = await _channel.invokeMethod("initPay", args);
    return result;
  }

  static Future<Null> showDeveloperApplicationsPage(String packageName) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("packageName", () => packageName);
    await _channel.invokeMethod("referralToDeveloper", args);
    return null;
  }

  static Future<dynamic> checkLicense() async {
    dynamic result = await _channel.invokeMethod("checkLicense");
    return result;
  }

  static Future<Null> showApplicationDetails(String packageName) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("packageName", () => packageName);
    await _channel.invokeMethod("referralToApplication", args);
    return null;
  }

  static Future<Null> showApplicationVideo(String packageName) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("packageName", () => packageName);
    await _channel.invokeMethod("referralToApplicationVideo", args);
    return null;
  }

  static Future<Null> setComment(String packageName) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("packageName", () => packageName);
    await _channel.invokeMethod("referralToComment", args);
    return null;
  }

  static Future<Null> downloadApplication(String packageName) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("packageName", () => packageName);
    await _channel.invokeMethod("referralToDownloadApplication", args);
    return null;
  }

  static Future<dynamic> getPurchase({@required String sku}) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("sku", () => sku);
    dynamic result = await _channel.invokeMethod("getPurchase", args);
    return jsonDecode(result);
  }

  static Future<dynamic> queryInventoryAsync({@required String sku}) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("sku", () => sku);
    dynamic result = await _channel.invokeMethod("queryInventoryAsync", args);
    return result;
  }

  static Future<dynamic> launchPurchaseFlow(
      {@required String sku,
      bool consumption = false,
      String payload = ""}) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("productKey", () => sku);
    args.putIfAbsent("payload", () => payload);
    args.putIfAbsent("consumption", () => consumption);
    dynamic result = await _channel.invokeMethod("launchPurchaseFlow", args);
    return jsonDecode(result);
  }

  static Future<bool> verifyDeveloperPayload({@required String payload}) async {
    Map<String, dynamic> args = <String, dynamic>{};
    String result = await _channel.invokeMethod("verifyDeveloperPayload", args);
    print(result);
    return result == payload;
  }

  static Future<Null> dispose() async {
    await _channel.invokeMethod("dispose");
    return null;
  }
}

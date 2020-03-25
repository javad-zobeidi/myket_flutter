import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:myket_flutter/myket_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    MyketFlutter.initPay(
        base6PublicKey:"");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            RaisedButton(
              onPressed: () async {
                try {
                  Map<String,dynamic> result = await MyketFlutter.launchPurchaseFlow(
                        sku: "p_one", consumption: false,payload:"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                } catch (e) {
                  print(e.message);
                }
              },
              child: Text("Ok"),
            )
          ],
        ),
      ),
    );
  }
}

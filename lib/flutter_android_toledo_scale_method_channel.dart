import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_android_toledo_scale_platform_interface.dart';

/// An implementation of [FlutterAndroidToledoScalePlatform] that uses method channels.
class MethodChannelFlutterAndroidToledoScale extends FlutterAndroidToledoScalePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_android_toledo_scale');

  @visibleForTesting
  final eventChannel = const EventChannel('flutter_android_toledo_scale_events');

  @override
  Future<double?> getWeight() async {
    final weight = await methodChannel.invokeMethod<double>(
      'getWeight',
    );
    return weight;
  }

  @override
  Stream<double> getWeightStream(String port, int baudRate, int flags) {
    return eventChannel.receiveBroadcastStream({
      'port': port,
      'baudRate': baudRate,
      'flags': flags,
    }).map((dynamic event) => event as double);
  }
}

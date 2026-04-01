import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_android_toledo_scale_method_channel.dart';

abstract class FlutterAndroidToledoScalePlatform extends PlatformInterface {
  /// Constructs a FlutterAndroidToledoScalePlatform.
  FlutterAndroidToledoScalePlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAndroidToledoScalePlatform _instance = MethodChannelFlutterAndroidToledoScale();

  /// The default instance of [FlutterAndroidToledoScalePlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAndroidToledoScale].
  static FlutterAndroidToledoScalePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAndroidToledoScalePlatform] when
  /// they register themselves.
  static set instance(FlutterAndroidToledoScalePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<double?> getWeight() {
    throw UnimplementedError('getWeight() has not been implemented.');
  }

  Stream<double> getWeightStream(String port, int baudRate, int flags) {
    throw UnimplementedError('getWeightStream() has not been implemented.');
  }
}

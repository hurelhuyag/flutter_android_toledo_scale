import 'flutter_android_toledo_scale_platform_interface.dart';

class FlutterAndroidToledoScale {

  Future<double?> getWeight() {
    return FlutterAndroidToledoScalePlatform.instance.getWeight();
  }

  Stream<double> getWeightStream(String port, int baudRate, int flags) {
    return FlutterAndroidToledoScalePlatform.instance.getWeightStream(port, baudRate, flags);
  }
}

package io.github.hurelhuyag.flutter_android_toledo_scale;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;

import android_serialport_api.SerialPort;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlutterAndroidToledoScalePlugin */
public class FlutterAndroidToledoScalePlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  private static final String TAG = "ToledoScalePlugin";
  private MethodChannel methodChannel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private SerialPort serialPort;
  private Thread readThread;
  private ScaleRunnable readRunnable;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_android_toledo_scale");
    methodChannel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_android_toledo_scale_events");
    eventChannel.setStreamHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getWeight":
        if (readRunnable != null) {
          result.success(readRunnable.getLatestStableValue());
        } else {
          result.success(0.0);
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    if (this.eventSink != null) {
      events.error("ALREADY_LISTENING", "Already listening for weight changes.", null);
      return;
    }
    this.eventSink = events;
    if (arguments instanceof Map) {
      Map<String, Object> args = (Map<String, Object>) arguments;
      String portPath = (String) args.get("port");
      Number baudRateNum = (Number) args.get("baudRate");
      Number flagsNum = (Number) args.get("flags");

      if (portPath != null && baudRateNum != null && flagsNum != null) {
        int baudRate = baudRateNum.intValue();
        int flags = flagsNum.intValue();

        try {
          serialPort = new SerialPort(new File(portPath), baudRate, flags);
          readRunnable = new ScaleRunnable(serialPort.getInputStream(), value -> {
            mainHandler.post(() -> {
              if (eventSink != null) {
                Log.d(TAG, "Reporting stable weight: " + value);
                eventSink.success(value);
              }
            });
          });
          readThread = new Thread(readRunnable);
          readThread.start();
        } catch (Exception e) {
          Log.e(TAG, "Error opening serial port", e);
          events.error("SERIAL_PORT_ERROR", e.getMessage(), null);
        }
      } else {
        events.error("INVALID_ARGUMENTS", "Port, baudrate or flags missing", null);
      }
    }
  }

  @Override
  public void onCancel(Object arguments) {
    stopReading();
  }

  private void stopReading() {
    if (readRunnable != null) {
      readRunnable.stop();
      readRunnable = null;
    }
    if (readThread != null) {
      readThread.interrupt();
      readThread = null;
    }
    if (serialPort != null) {
      serialPort.close();
      serialPort = null;
    }
    eventSink = null;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    methodChannel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
    stopReading();
  }
}

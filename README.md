# flutter_android_toledo_scale

A Flutter plugin for reading weight data from Mettler Toledo scales via serial port on Android.

## Features

- Read current stable weight.
- Listen to a continuous stream of weight updates.
- Configure serial port settings (port, baudrate, flags).

## Installation

Add `flutter_android_toledo_scale` to your `pubspec.yaml` dependencies.

## Usage

### 1. Create an instance

```dart
final _toledoScale = FlutterAndroidToledoScale();
```

### 2. Listen to weight changes (Stream)

This is the recommended way to get continuous updates. You need to specify the serial port path, baudRate, and flags.

#### Using `StreamSubscription`

```dart
StreamSubscription<double>? _subscription;

void startListening() {
  _subscription = _toledoScale
      .getWeightStream('/dev/ttyS0', 9600, 0)
      .listen((double weight) {
    print('Current weight: $weight');
  }, onError: (error) {
    print('Error: $error');
  });
}

void stopListening() {
  _subscription?.cancel();
}
```

#### Using `StreamBuilder` (UI Example)

`StreamBuilder` is excellent for demo purposes as it automatically handles the stream lifecycle (listening and cancelling) based on the widget's lifecycle.

```dart
StreamBuilder<double>(
  stream: _toledoScale.getWeightStream('/dev/ttyS0', 9600, 0),
  builder: (context, snapshot) {
    if (snapshot.hasError) {
      return Text('Error: ${snapshot.error}');
    }
    
    if (!snapshot.hasData) {
      return const Text('Waiting for stable weight...');
    }

    return Text(
      '${snapshot.data} kg',
      style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
    );
  },
)
```

### 3. Get the latest stable weight

If you are already listening to the stream, you can get the last known stable value.

```dart
Future<void> fetchWeight() async {
  double? weight = await _toledoScale.getWeight();
  print('Last stable weight: $weight');
}
```

## Android Configuration

Ensure your app has the necessary permissions to access serial ports. On some devices, you might need root access or specific hardware configurations.

## Protocol Details

The plugin is designed to parse the standard Toledo serial protocol:
- Start: `0x02` (STX)
- Data: ASCII weight characters.
- Terminator: `0x0D 0x0A` (CR LF)
- Includes stability and sign detection.

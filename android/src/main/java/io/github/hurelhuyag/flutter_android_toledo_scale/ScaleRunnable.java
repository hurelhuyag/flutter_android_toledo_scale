package io.github.hurelhuyag.flutter_android_toledo_scale;

import android.util.Log;
import java.io.InputStream;

public class ScaleRunnable implements Runnable {
    private static final String TAG = "ScaleRunnable";
    private static final int STABLE_VALUE_COUNT = 5;
    private static final int PACKET_SIZE = 18;

    private final ScaleCallback callback;
    private final InputStream inputStream;
    private volatile boolean running = true;

    private double currentCandidateValue = 0.0;
    private double latestStableValue = 0.0;
    private int stabilityCounter = 0;

    public ScaleRunnable(InputStream inputStream, ScaleCallback scaleCallback) {
        this.inputStream = inputStream;
        this.callback = scaleCallback;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        final byte[] packet = new byte[PACKET_SIZE];
        while (running) {
            try {
                // 1. Sync to Start of Text (0x02)
                int head = inputStream.read();
                if (head == -1) break;
                if (head != 0x02) continue;

                packet[0] = (byte) head;
                // 2. Read the remaining 17 bytes of the Toledo frame
                int read = 1;
                while (read < PACKET_SIZE) {
                    int n = inputStream.read(packet, read, PACKET_SIZE - read);
                    if (n == -1) return;
                    read += n;
                }

                // 3. Validate Terminator (CR LF)
                if (packet[16] != 0x0D || packet[17] != 0x0A) continue;

                // 4. Parse Multiplier (SWA bits 0-2)
                double multiplier;
                switch (packet[1] & 0x07) {
                    case 1: multiplier = 10.0; break;
                    case 2: multiplier = 1.0; break;
                    case 3: multiplier = 0.1; break;
                    case 4: multiplier = 0.01; break;
                    case 5: multiplier = 0.001; break;
                    default:
                        multiplier = 0.001;
                        Log.w(TAG, "Unknown multiplier id: " + (packet[1] & 0x07));
                        break;
                }

                // 5. Parse Status (SWB)
                // Bit 1: Sign (1 = Negative), Bit 3: Motion (1 = Unstable)
                final int sign = ((packet[2] >> 1) & 1) == 1 ? -1 : 1;
                final boolean isStable = ((packet[2] >> 3) & 1) == 0;

                // 6. Parse Weight ASCII (Bytes 4-9)
                int rawWeight = 0;
                for (int i = 4; i <= 9; i++) {
                    rawWeight = rawWeight * 10 + (packet[i] - '0');
                }

                final var weight = rawWeight * multiplier * sign;
                //Log.d(TAG, "raw weight: " + weight);

                // 7. Stability Logic (Require 5 consecutive matches)
                if (isStable && Math.abs(currentCandidateValue - weight) < 0.01) {
                    stabilityCounter++;
                    if (stabilityCounter >= STABLE_VALUE_COUNT) {
                        stabilityCounter = 0;
                        if (weight != latestStableValue) {
                            latestStableValue = weight;
                            if (callback != null) {
                                callback.onWeightStabilized(weight);
                            }
                        }
                    }
                } else {
                    currentCandidateValue = weight;
                    stabilityCounter = 0;
                }
            } catch (Exception e) {
                Log.e(TAG, "Serial read error", e);
            }
        }
    }

    public double getLatestStableValue() {
        return latestStableValue;
    }
}

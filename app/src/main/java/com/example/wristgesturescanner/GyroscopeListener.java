package com.example.wristgesturescanner;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class GyroscopeListener implements SensorEventListener {

    private String contents = "";

    @Override
    public void onSensorChanged(SensorEvent event) {

        Long tsLong = System.currentTimeMillis();
        String ts = String.format("%.3f", tsLong/1000.000);

        String line =  ts
                + "," + String.format("%.8f", event.values[0])
                + "," + String.format("%.8f", event.values[1])
                + "," + String.format("%.8f", event.values[2])
                + "\n";

        contents = contents + line;

//        Log.e("LOG", line);

    }

    public String getContents(){
        return contents;
    }

    public String popContents(){
        String retval = contents;
        contents = "";
        return retval;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

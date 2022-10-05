package com.example.wristgesturescanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.wristgesturescanner.databinding.ActivityMainBinding;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;

    private String gyrocontents = "";
    private String acccontents = "";
    private SensorManager sensorManager;
    private Sensor gyrosensor, accsensor;
    private AccelorlateListener AccelorlateListener;
    private GyroscopeListener GyroscopeListener;
    private Boolean recording = false;

    private Long tsLong;
    private String ts;

    private String GyroFileName, AccFileName;
    private File GyroFile, AccFile;
    private OutputStream gyroOutput, accOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();

        File appDirectory = new File( getFilesDir().toString()+"/data" );
        if ( !appDirectory.exists() ) {
            appDirectory.mkdirs();
            Log.e("INFO", "Created ... "+ appDirectory.getAbsolutePath());
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyrosensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroscopeListener = new GyroscopeListener();
        AccelorlateListener = new AccelorlateListener();


        Timer timer = new Timer();
        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                // 반복실행할 구문
                try {
                    gyrocontents = GyroscopeListener.popContents();
                    gyroOutput.write(gyrocontents.getBytes(StandardCharsets.UTF_8));
                    gyroOutput.flush();
                    acccontents = AccelorlateListener.popContents();
                    accOutput.write(acccontents.getBytes(StandardCharsets.UTF_8));
                    accOutput.flush();
                    Log.i("INFO", "Saved at " + AccFile);
                    Log.i("INFO", "Saved at " + GyroFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };


        Button startButton = findViewById(R.id.startButton);
        Button endButton = findViewById(R.id.endButton);
        TextView statusText = findViewById(R.id.StatusText);

        startButton.setOnClickListener(view -> {
            recording = true;
            sensorManager.registerListener(GyroscopeListener, gyrosensor, 500);
            sensorManager.registerListener(AccelorlateListener, accsensor, 500);
            tsLong = System.currentTimeMillis()/1000;
            ts = tsLong.toString();
            GyroFileName = "gyroscope_" + ts + ".csv";
            GyroFile = new File(appDirectory, GyroFileName);
            AccFileName = "accelerate_" + ts + ".csv";
            AccFile = new File(appDirectory, AccFileName);
            try {
                gyroOutput = new BufferedOutputStream(new FileOutputStream(GyroFile, true));
                accOutput = new BufferedOutputStream(new FileOutputStream(AccFile, true));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            timer.schedule(TT, 0, 3000);
            if (recording){
                statusText.setText("scan : O");
            }else{
                statusText.setText("scan : x");
            }
        });
        endButton.setOnClickListener(view -> {
            if (recording) {
                timer.cancel();
                sensorManager.unregisterListener(GyroscopeListener);
                sensorManager.unregisterListener(AccelorlateListener);

                try {
                    gyrocontents = GyroscopeListener.popContents();
                    gyroOutput.write(gyrocontents.getBytes(StandardCharsets.UTF_8));
                    gyroOutput.flush();
                    acccontents = AccelorlateListener.popContents();
                    accOutput.write(acccontents.getBytes(StandardCharsets.UTF_8));
                    accOutput.flush();
                    Log.i("INFO", "Saved at " + AccFile);
                    Log.i("INFO", "Saved at " + GyroFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                gyrocontents = "";
                acccontents = "";
                recording = false;
                try {
                    gyroOutput.close();
                    accOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (recording){
                    statusText.setText("scan : O");
                }else{
                    statusText.setText("scan : x");
                }

            }
        });
    }
}
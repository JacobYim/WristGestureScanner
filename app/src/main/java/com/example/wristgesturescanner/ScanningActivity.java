package com.example.wristgesturescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.TextView;
import android.os.Vibrator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanningActivity extends AppCompatActivity {

    private String contents = "X,Y,Z,pitch,roll,yaw,dt\n";
    private String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    private String FileName = timestamp+".csv";

    private static final int CREATE_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        TextView gesture = findViewById(R.id.gesture);
        TextView remainTime = findViewById(R.id.remainTime);

        Intent intent = getIntent();
        String str = intent.getStringExtra("gesture");
        gesture.setText(str);

        File appDirectory = new File( "/data/data/com.example.wristgesturescanner/data" );
        if ( !appDirectory.exists() ) {
            appDirectory.mkdirs();
            Log.e("INFO", "Created ... "+ appDirectory.getAbsolutePath());
        }else{
            Log.e("INFO", "Already Existed ... "+ appDirectory.getAbsolutePath());
            for (int i = 0; i < appDirectory.listFiles().length; i++) {
                Log.e("INFO", "the list is  ... " + appDirectory.listFiles()[i].getAbsolutePath());
            }
        }

        int measuringTime = 20;


        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // gyroscope start
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        SensorEventListener mGyroLis = new GyroscopeListener();;
        Sensor mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_UI);

        new CountDownTimer((measuringTime+1)*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                remainTime.setText(String.valueOf((int)(millisUntilFinished / 1000))+"\n");
                if ((int)(millisUntilFinished / 1000)%10 == 0 && (int)(millisUntilFinished / 1000)!=0){
                    vibrator.vibrate(1000*10);
                }
            }
            public void onFinish() {
//                timeout = true;
                remainTime.setText("done!\n");
                mSensorManager.unregisterListener(mGyroLis);
                File file = new File(appDirectory, FileName);
//                File file = new File(FileName);
                OutputStream myOutput;
                try {
                    myOutput = new BufferedOutputStream(new FileOutputStream(file,true));
                    myOutput.write(contents.getBytes());
                    myOutput.flush();
                    myOutput.close();
                    Log.i("INFO", "Successfully Saved at "+file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(ScanningActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }.start();

    }

    private class GyroscopeListener implements SensorEventListener {

        //Roll and Pitch
        private double pitch;
        private double roll;
        private double yaw;

        //timestamp and dt
        private double timestamp;
        private double dt;

        // for radian -> dgree
        private double RAD2DGR = 180 / Math.PI;
        private static final float NS2S = 1.0f/1000000000.0f;

        @Override
        public void onSensorChanged(SensorEvent event) {

            /* ??? ?????? ????????? ????????? ?????????. */
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            /* ???????????? ???????????? ???????????? ???????????? ?????? ?????? ??????(dt)??? ?????????.
             * dt : ????????? ?????? ????????? ???????????? ?????? ??????
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            /* ??? ?????? ????????? ????????? ?????? ?????? timestamp??? 0????????? dt?????? ???????????? ???????????? ????????????. */
            if (dt - timestamp*NS2S != 0) {

                /* ????????? ????????? ?????? -> ?????????(pitch, roll)?????? ??????.
                 * ??????????????? pitch, roll??? ????????? '?????????'??????.
                 * SO ?????? ?????? ?????????????????? ???????????? 'RAD2DGR'??? ???????????? degree??? ????????????.  */
                pitch = pitch + gyroY*dt;
                roll = roll + gyroX*dt;
                yaw = yaw + gyroZ*dt;

                contents = contents + String.format("%.8f", event.values[0])
                        + "," + String.format("%.8f", event.values[1])
                        + "," + String.format("%.8f", event.values[2])
                        + "," +  String.format("%.8f", pitch*RAD2DGR)
                        + "," + String.format("%.8f", roll*RAD2DGR)
                        + "," +  String.format("%.8f", yaw*RAD2DGR)
                        + "," +  String.format("%.8f", dt)+"\n";


                Log.e("LOG", "GYROSCOPE           [X]:" + String.format("%.8f", event.values[0])
                        + "           [Y]:" + String.format("%.8f", event.values[1])
                        + "           [Z]:" + String.format("%.8f", event.values[2])
                        + "           [Pitch]: " + String.format("%.8f", pitch*RAD2DGR)
                        + "           [Roll]: " + String.format("%.8f", roll*RAD2DGR)
                        + "           [Yaw]: " + String.format("%.8f", yaw*RAD2DGR)
                        + "           [dt]: " + String.format("%.8f", dt));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
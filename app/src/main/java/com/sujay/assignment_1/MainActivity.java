package com.sujay.assignment_1;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int VIDEO_RECORD_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static Uri fileUri;
    private SensorManager accelManage;
    private Sensor senseAccel;
    public long start;
    public long end;

    MediaMetadataRetriever metadata;
    ArrayList<Double> accelValuesZ=new ArrayList<>();
    private int heartRate=0;
    Vibrator vibrator;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button SLP = findViewById(R.id.uploadSignsSubmit);
        Button heartRateSubmit= findViewById(R.id.heartRateSubmit);
        Button respiratoryRateSubmit= findViewById(R.id.respiratoryRateSubmit);
        TextView heartRate = findViewById(R.id.heartRate);

        TextView respiratoryRate = findViewById(R.id.respiratoryRate);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        SLP.setOnClickListener(v -> {
            Intent intent;
            intent = new Intent(getApplicationContext(), SymptomLoggingPage.class);

            try{
                ContentValues values = new ContentValues();
                Bundle b=new Bundle();
                values.put("heartRate", String.valueOf(heartRate.getText()));
                values.put("respiratoryRate", String.valueOf(respiratoryRate.getText()));
                b.putParcelable("Initial",values);
                intent.putExtras(b);
            }catch (SQLException e){
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            startActivity(intent);
        });



        respiratoryRateSubmit.setOnClickListener(v -> {
            Thread thread = new Thread(new CalcRespRateThread());
            Toast.makeText(getApplicationContext(),"Please Lie Down and Place the phone on chest.", Toast.LENGTH_SHORT).show();
            thread.start();

        });

        heartRateSubmit.setOnClickListener(v -> {
            getCameraPermission();
            File mediaFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.sujay.assignment_1/files/fingerTip.mp4");
            fileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", mediaFile);

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, VIDEO_RECORD_CODE);
        });

    }

    private void getCameraPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {
                // Use the fileUri to access the video path instead of data.getData()
                Uri videoUri = fileUri;
                System.out.println("Video recorded at: " + videoUri.getPath());

                TextView heartRate = findViewById(R.id.heartRate);
                heartRate.setText("Calculating...");
                Thread thread = new Thread(new CalcHeartRateThread());
                thread.start();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Recording Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Recording Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public static int noisypeakdetect(ArrayList<Double> signal) {
        System.out.println("Noisy peak detection");
        ArrayList<Double> smoothed=new ArrayList<>();
        for(int i=2;i<signal.size()-2;i++){
            smoothed.add(average(signal.get(i-2),signal.get(i-1),signal.get(i),signal.get(i+1),signal.get(i+2)));
        }
        ArrayList<Integer> peakIndices=new ArrayList<>();
        int peakIndex=0;
        double peakValue=0;
        double sum=0;
        double max=0;
        for(int i=0;i<smoothed.size();i++){
            sum=sum+smoothed.get(i);
            if(max < smoothed.get(i)){
                max = smoothed.get(i);
            }
        }
        double baseline=sum/smoothed.size();
        double threshHold = (double) (0.5*(max-baseline));
        System.out.println(">>>>>" + smoothed);
        for(int i=1;i<smoothed.size()-1;i++){
            if(smoothed.get(i)>(baseline + threshHold)){
                //     if(peakValue==0 || smoothed.get(i)>peakValue){
                //         peakIndex=i;
                //         peakValue=smoothed.get(i);
                //     }
                // }else if(smoothed.get(i)< (baseline + threshHold) && peakIndex!=0){
                //     peakIndices.add(peakIndex);
                //     peakIndex=0;
                //     peakValue=0;
                if(smoothed.get(i) >smoothed.get(i-1) && smoothed.get(i)>smoothed.get(i+1)){
                    peakIndices.add(i);
                }
            }
        }
        if(peakIndex!=0){
            peakIndices.add(peakIndex);
        }
        return peakIndices.size();
    }

    public static double average(double a,double b,double c,double d,double e){

        return (a+b+c+d+e)/5;
    }

    public static int callGestureRecognition(ArrayList<Double> accelValuesZ){

        double avgZ = 0;

        for(int i=0;i<accelValuesZ.size();i++){
            avgZ = avgZ + accelValuesZ.get(i);

        }
        avgZ = avgZ/accelValuesZ.size();

        boolean left = true;

        int zeroCrossingZ = 0;
        if(accelValuesZ.get(0) >= avgZ){
            left = true;
            for(int i=0;i<accelValuesZ.size();i+=8){
                if(left){
                    if(accelValuesZ.get(i) < avgZ){
                        zeroCrossingZ++;
                        left = false;
                    }
                }else{
                    if(accelValuesZ.get(i) >= avgZ){
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }else{
            left = false;
            for(int i=0;i<accelValuesZ.size();i+=8){
                if(left){
                    if(accelValuesZ.get(i) < avgZ){
                        zeroCrossingZ++;
                        left = false;
                    }
                }else{
                    if(accelValuesZ.get(i) >= avgZ){
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }

        return zeroCrossingZ;


    }


    private class CalcHeartRateThread implements Runnable {

        @Override
        public void run() {
            String uri = Environment.getExternalStorageDirectory()+"/Android/data/com.sujay.assignment_1/files/fingerTip.mp4";



            metadata=new MediaMetadataRetriever();
            metadata.setDataSource(uri);
            String Duration= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            ArrayList<Bitmap> frameList=new ArrayList<>();
            int duration_millisecond=Integer.parseInt(Duration);
            int duration_second=duration_millisecond/1000;
            int fps = 30;
            int numFramesCapture = fps * duration_second;
            for(int i=0;i<numFramesCapture;i++){
                Bitmap bmp=metadata.getFrameAtTime(1000*i);
                Bitmap resizebitmap = Bitmap.createBitmap(bmp,
                        bmp.getWidth() / 2, bmp.getHeight() / 2, 60, 60);
                frameList.add(resizebitmap);
            }

            int avgRedCountPerFrame = 0;
            int avgRedCount = 0;
            ArrayList<Double> redIntensity = new ArrayList<Double>();
            for (Bitmap bitmap: frameList){
                int redCount = 0;
                for (int i = 0; i < bitmap.getWidth(); i++){
                    for (int j = 0; j < bitmap.getHeight(); j++){
                        int pixel = bitmap.getPixel(i,j);
                        redCount += Color.red(pixel);
                    }
                }
                avgRedCountPerFrame += redCount/3600;
                redIntensity.add((double) (redCount/3600));
            }
            avgRedCount = avgRedCountPerFrame / frameList.size();




            for(int i = 0, j=35; j<redIntensity.size(); i++,j++) {
                float sum = 0;
                for(int k=i; k<j; k++){
                    sum += redIntensity.get(k);
                }
                redIntensity.set(i, (double) (sum / 35));
            }

            List<Integer> ext = new ArrayList<>();
            for (int i = 0; i<redIntensity.size()-38; i++) {
                if ((redIntensity.get(i + 1) - redIntensity.get(i))*(redIntensity.get(i + 2) - redIntensity.get(i + 1)) <= 0) {
                    ext.add(i+1);
                }
            }

            int heartRateSmoothing = 0;
            for (int i = 0; i<ext.size()-1; i++) {
                if(ext.get(i)/10 != ext.get(i++)) heartRateSmoothing++;
            }

            heartRate = heartRateSmoothing * 2;

            System.out.println("#################################");
            System.out.println(heartRate);



            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    TextView heartRateTextView = (TextView) findViewById(R.id.heartRate);
                    heartRateTextView.setText(String.valueOf(heartRate/2)+" bpm");

                }
            });


        }
    }

    private class CalcRespRateThread implements Runnable {
        @Override
        public void run() {

            start=System.currentTimeMillis();
            end=start+ 45*1000;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            vibrator.vibrate(500);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            SensorEventListener set=new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    Sensor mySensor = sensorEvent.sensor;

                    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        accelValuesZ.add((double)sensorEvent.values[2]);
                    }

                    if(System.currentTimeMillis()>end) {
                        accelManage.unregisterListener(this);

                        count=noisypeakdetect(accelValuesZ);
                        //count=callGestureRecognition(accelValuesZ);
                        // System.out.println(accelValuesZ);
                        for(double i: accelValuesZ){
                            System.out.println(i);
                        }


                        System.out.println("################################## : "+count);
                        vibrator.vibrate(500);
                        runOnUiThread(() -> {
                            TextView respiratoryRate = findViewById(R.id.respiratoryRate);
                            respiratoryRate.setText(Integer.toString(count/2));
                        });


                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };


            accelManage.registerListener(set, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);


        }
    }
}

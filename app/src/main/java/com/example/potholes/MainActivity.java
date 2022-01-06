package com.example.potholes;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    TextView xView;
    TextView yView;
    TextView zView;
    TextView latView;
    TextView longView;
    TextView statusView;
    TextView linearView;
    TextView avgView;
    TextView stdView;
    TextView maxView;
    Button startRecording;
    Button stopRecording;
    int loopId = 0;
    AtomicBoolean currentlyRecording = new AtomicBoolean();
    SensorManager sensorManager;
    Sensor accelerometer;
    AccelerometerListener accelerometerListener;
    LocationManager locationManager;
    GPSListener gpsListener;
    StringBuilder currentCsv;
    long startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getAndroidResources();
        stopRecording.setEnabled(false);
        accelerometerListener = new AccelerometerListener();
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        gpsListener = new GPSListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            askPermissions();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, gpsListener);
        }


    }
    @TargetApi(23)
    public void askPermissions(){
        //note - i had to grant app storage permissions from android settings itself
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, perms, 0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("no perms");
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, gpsListener);
        }
    }

    private void getAndroidResources(){
        xView = (TextView) findViewById(R.id.xView);
        yView = (TextView) findViewById(R.id.yView);
        zView = (TextView) findViewById(R.id.zView);
        latView = (TextView) findViewById(R.id.latView);
        longView = (TextView) findViewById(R.id.longView);
        statusView = (TextView) findViewById(R.id.statusView);
        linearView = (TextView) findViewById(R.id.linearView);
        avgView = (TextView) findViewById(R.id.avgView);
        stdView = (TextView) findViewById(R.id.stdView);
        maxView = (TextView) findViewById(R.id.maxView);

        startRecording = (Button) findViewById(R.id.startButton);
        stopRecording = (Button) findViewById(R.id.stopButton);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    public void startRecordingClicked(View view) {
        performRecording();
    }
    private void performRecording(){
        setUpRecording();
        //here's android's version of a loop with a sleep
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                processLoop();
                if(currentlyRecording.get()){
                    handler.postDelayed(this, 1000l);  // 1 second delay
                }
                else{
                    finishRecording();
                }

            }
        };
        handler.post(runnable);
    }
    public void setUpRecording(){
        currentCsv = new StringBuilder();
        //accel is in m/s^2 according to android docs
        currentCsv.append("avg accel,std accel,max accel,latitude,longitude,elapsed seconds,\n");
        currentlyRecording.set(true);
        statusView.setText("recording!");
        startRecording.setEnabled(false);
        stopRecording.setEnabled(true);
        startTime=System.currentTimeMillis();
    }
    public void finishRecording(){
        statusView.setText("not recording");
        stopRecording.setEnabled(false);
        startRecording.setEnabled(true);
        String csv = currentCsv.toString();
        System.out.println(csv);
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String fileName = "Pothole-Analysis-"+timestamp+".csv";
        String filePath = baseDir + File.separator + fileName;
        //the file ends up in the rootdir of the internal storage, when you open up the thing over usb
        File f = new File(filePath);
        try {
            PrintWriter pw = new PrintWriter(f);
            pw.println(csv);
            pw.close();
            statusView.setText(filePath+ " recorded");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void processLoop(){
        xView.setText(""+accelerometerListener.lastX);
        yView.setText(""+accelerometerListener.lastY);
        zView.setText(""+accelerometerListener.lastZ);
        linearView.setText(""+accelerometerListener.lastLinear);
        double[] avgStdMax = accelerometerListener.getAvgStdMax();
        avgView.setText(""+avgStdMax[0]);
        stdView.setText(""+avgStdMax[1]);
        maxView.setText(""+avgStdMax[2]);
        latView.setText(""+gpsListener.latitude);
        longView.setText(""+gpsListener.longitude);

        currentCsv.append(avgStdMax[0]);
        currentCsv.append(",");
        currentCsv.append(avgStdMax[1]);
        currentCsv.append(",");
        currentCsv.append(avgStdMax[2]);
        currentCsv.append(",");
        currentCsv.append(gpsListener.latitude);
        currentCsv.append(",");
        currentCsv.append(gpsListener.longitude);
        currentCsv.append(",");
        currentCsv.append((System.currentTimeMillis()-startTime)/1000d);
        currentCsv.append(",\n");

    }

    public void stopRecordingClicked(View view){
        currentlyRecording.set(false);
    }
}
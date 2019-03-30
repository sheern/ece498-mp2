package com.example.shaanp2.mp2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }




    class SensorLogger implements SensorEventListener
    {

        long START_TIME;

        Sensor acc, gyr, mag, lig;
        private ArrayList<float[]> accData = new ArrayList<>();
        private ArrayList<Float> accDataZ = new ArrayList<>();
//        private float[] gyrData = new float[3];
//        private float[] magData = new float[3];
//        private float ligData = 0;

        private FileWriter writer;

        private SensorLogger()
        {
            acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//            mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//            lig = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        }

        void start() {
            //START_TIME = SystemClock.elapsedRealtime();
            //createLoggingFile();

            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
//            sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_FASTEST);
//            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_FASTEST);
//            sensorManager.registerListener(this, lig, SensorManager.SENSOR_DELAY_FASTEST);

        }

        void stop() {
            sensorManager.unregisterListener(this, acc);
//            sensorManager.unregisterListener(this, gyr);
//            sensorManager.unregisterListener(this, mag);
//            sensorManager.unregisterListener(this, lig);

           // closeLoggingFile();
        }



        @Override
        public void onSensorChanged(SensorEvent sensorEvent)
        {
            float currZ;
             if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {

                currZ = sensorEvent.values[2];
                int steps = calculate_steps();


                accDataZ.add(currZ);
            }

        }

        int calculate_steps(){
           ArrayList<Float> clean = low_pass(accDataZ, 100);
           return numPeaks;
        }

       ArrayList<Float> low_pass(ArrayList<Float> accDataZ, int smooth){
            float acc = accDataZ.get(0);
            int len = accDataZ.size();
            for(int i=0; i<len; ++i){
                float curr = accDataZ.get(i);
                acc += (curr - acc) / smooth;
                accDataZ.set(i, acc);
            }
            return accDataZ;
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int i)
        {
        }

    }

}

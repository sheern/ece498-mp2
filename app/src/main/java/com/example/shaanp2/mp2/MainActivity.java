package com.example.shaanp2.mp2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private TextView stepCountText, distanceText, rotationText;
    private Button resetButton;

    private SensorManager sensorManager;
    private SensorLogger sensorLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorLogger = new SensorLogger();

        stepCountText = findViewById(R.id.stepCountText);
        distanceText = findViewById(R.id.distanceText);
        rotationText = findViewById(R.id.rotationText);

        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sensorLogger.reset();
            }
        });

        sensorLogger.start();
    }


    class SensorLogger implements SensorEventListener
    {

        Sensor acc, gyr;

        // Accelerometer
        private ArrayList<Float> accZ = new ArrayList<>();
        private int baseSteps = 0, totalSteps = 0;
        private final float STEP_LENGTH = 0.635f;

        // Gyroscope
        private long gyrTimestamp = 0;
        private double totalRotation = 0;

        private SensorLogger()
        {
            acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        void start()
        {
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
        }

        void stop()
        {
            sensorManager.unregisterListener(this, acc);
            sensorManager.unregisterListener(this, gyr);
        }


        @Override
        public void onSensorChanged(SensorEvent sensorEvent)
        {
            int sensorType = sensorEvent.sensor.getType();
            boolean shouldUpdate = false;

            if (sensorType == Sensor.TYPE_ACCELEROMETER)
            {
                float currZ = sensorEvent.values[2];
                accZ.add(currZ);
                int steps = calculateSteps();

                // Update view only if step count has changed
                if (baseSteps + steps != totalSteps)
                {
                    totalSteps = baseSteps + steps;
                    shouldUpdate = true;
                }

                // Reset the data array if it gets too large
                if (accZ.size() > 2500)
                {
                    baseSteps += steps;
                    accZ = new ArrayList<>();
                }
            }
            else if (sensorType == Sensor.TYPE_GYROSCOPE)
            {
                // Calculate angular speed and stuff here
                float aX = sensorEvent.values[0], aY = sensorEvent.values[1], aZ = sensorEvent.values[2];

                // We only consider z-axis rotation since we hold the phone flat
                double angularSpeed = Math.sqrt(aZ * aZ);
                if (gyrTimestamp != 0)
                {
                    double dt = (sensorEvent.timestamp - gyrTimestamp) / 1_000_000_000.0;
                    double currRotation = angularSpeed * dt;

                    if (currRotation != 0)
                    {
                        totalRotation += currRotation;
                        shouldUpdate = true;
                    }
                }

                gyrTimestamp = sensorEvent.timestamp;
            }

            if (shouldUpdate)
            {
                updateView();
            }
        }

        int calculateSteps()
        {
            int steps = 0;
            float PEAK_THRESHOLD = 10;
            ArrayList<Float> smoothedZ = lowPass(accZ, 100);

            for (int i = 1; i < smoothedZ.size() - 1; ++i)
            {
                boolean isPeak = smoothedZ.get(i) > smoothedZ.get(i - 1) && smoothedZ.get(i) > smoothedZ.get(i + 1);
                boolean passesThreshold = smoothedZ.get(i) > PEAK_THRESHOLD;
                if (isPeak && passesThreshold)
                {
                    steps++;
                }
            }

            return steps;
        }

        ArrayList<Float> lowPass(ArrayList<Float> arr, int smoothing)
        {
            ArrayList<Float> result = new ArrayList<>();
            float value = arr.get(0);
            result.add(value);

            for (int i = 1; i < arr.size(); ++i)
            {
                float curr = arr.get(i);
                value += (curr - value) / smoothing;
                result.add(value);
            }

            return result;
        }

        private void updateView()
        {
            int totalDegrees = (int) Math.toDegrees(totalRotation);
            float totalDistance = STEP_LENGTH * totalSteps;

            stepCountText.setText(getString(R.string.steps, totalSteps));
            distanceText.setText(getString(R.string.distance, totalDistance));
            rotationText.setText(getString(R.string.rotation, (int) totalDegrees));
        }

        private void reset()
        {
            accZ = new ArrayList<>();
            baseSteps = 0;
            totalSteps = 0;
            totalRotation = 0;
            gyrTimestamp = 0;

            updateView();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i)
        {
        }

    }

}

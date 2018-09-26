package com.bakerapps.stepcounter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView counter;
    private TextView stepsSaved;

    DatabaseReference userRef;

    private SensorManager sensorManager;
    private Runnable stepsLogger;
    private Thread loggerThread;
    private boolean runLogger = false;
    private int previousSteps;
    private int currentSteps;
    private SharedPreferences prefs;
    private DatabaseReference myDatabase;
    private Animation fadeOut;

    //checks if user has picked a username and goes to WelcomeActivity if he/she hasn't
    //otherwise registers a listener for the sensors, gets reference to database and starts a thread that updates the user's steps in the database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("StepCounter_Preferences", MODE_PRIVATE);

        getSharedPreferences("StepCounter_Preferences", 0).edit().clear().commit();

        if(prefs.getString("userName", null) == null){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        setContentView(R.layout.activity_main);
        counter = (TextView) findViewById(R.id.stepCounter);
        stepsSaved = (TextView) findViewById(R.id.lblStepsSaved);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);


        getSupportActionBar().hide();

        prefs = getSharedPreferences("StepCounter_Preferences", MODE_PRIVATE);

        myDatabase = FirebaseDatabase.getInstance().getReference();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(countSensor!=null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else{
            Toast.makeText(this, "Step counter not available", Toast.LENGTH_LONG).show();
        }

        stepsLogger = new Runnable() {
            @Override
            public void run() {
                while(true){
                if (runLogger) {
                    if (stepsNotChanged()) {
                        userRef = myDatabase.child("users").child(prefs.getString("userName", null).toLowerCase());

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userRef.child("steps").setValue(currentSteps + ((Long) dataSnapshot.child("steps").getValue()).intValue());
                                currentSteps = 0;
                                stepsSaved.startAnimation(fadeOut);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        //wait until device gets new sensor input before running again
                        runLogger = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                counter.setText("0");
                            }
                        });

                    }

                    previousSteps = currentSteps;
                }
                //Run every 5 seconds
                    try {
                        Thread.currentThread().sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }

                }
            }
        };
            loggerThread = new Thread(stepsLogger);
            loggerThread.start();
    }
    //starts the LeaderboardActivity
    public void viewLeaderboard(View view){
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //interrupts the thread when the application is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        loggerThread.interrupt();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    //checks if the user has taken any steps
    private boolean stepsNotChanged() {
        return currentSteps - previousSteps == 0;
    }

    //gets called when the sensor detects a step. Updates the GUI
    @Override
    public void onSensorChanged(SensorEvent event) {
            currentSteps += (int) event.values[0];
            runLogger = true;
            counter.setText(String.valueOf(currentSteps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}


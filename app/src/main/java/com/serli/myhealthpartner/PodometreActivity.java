package com.serli.myhealthpartner;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import com.serli.myhealthpartner.controller.ProfileController;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;


public class PodometreActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
//  private static final String PREFERENCES_NAME = "Values";
    private static final String PREFERENCES_VALUES_THRESHOLD_KEY = "threshold";
    private SharedPreferences preferences;
    private int mStepCount = 0;
    private AccelerometerDetector mAccelDetector;
    private TextView mThreshValTextView;
    private TextView mStepCountTextView;
    private TextView mTimeValTextView;


    private TimeCounter mTimer;
    private Handler mHandler = new Handler();

    /***************/
    private ProfileController profController ;
    int height;
    int weight;
    int gender;
    double resultE = 0;
    String resultCalories, resultDistance;
    TextView counterCalories, counterDistance;
    TextView numberCalories, distanceTravelled;
    double resCalories = 0;
    double resDistance = 0;
    /***************/
    // constant reference
    private final AccelerometerProcessing mAccelerometerProcessing = AccelerometerProcessing.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podometre);

        // set default locale:
        Locale.setDefault(Locale.ENGLISH);
        // get and configure text views
        mThreshValTextView = (TextView)findViewById(R.id.threshval_textView);
        formatThreshTextView(AccelerometerProcessing.THRESH_INIT_VALUE);
        mStepCountTextView = (TextView)findViewById(R.id.stepcount_textView);
        mStepCountTextView.setText(String.valueOf(0));
        mTimeValTextView = (TextView)findViewById(R.id.timeVal_textView);

        /****************/
        counterCalories = (TextView) findViewById(R.id.calorie_affiche);
        numberCalories=(TextView)findViewById(R.id.number_calories);
        counterDistance=(TextView) findViewById(R.id.distance_affiche);
        distanceTravelled=(TextView)findViewById(R.id.number_distance);
        profController = new ProfileController(this);
        height = profController.getProfile().getHeight();
        weight = profController.getProfile().getWeight();
        gender = profController.getProfile().getGender();
        /****************/

        // timer counter
        mTimer = new TimeCounter(mHandler,mTimeValTextView);
        mTimer.start();

        // initialize accelerometer
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelDetector = new AccelerometerDetector(sensorManager);
        mAccelDetector.setStepCountChangeListener(new OnStepCountChangeListener() {
            @Override
            public void onStepCountChange(long eventMsecTime) {
                ++mStepCount;
                resCalories = getCalories()*mStepCount;
                resultCalories = resCalories + " cal";
                numberCalories.setText(resultCalories);
                resDistance = (double) Math.round((calculateDistance() * mStepCount) * 100) / 100; // 4.248 --> 4.25
                resultDistance = resDistance + " km";
                distanceTravelled.setText(resultDistance);
                mStepCountTextView.setText(String.valueOf(mStepCount));
            }
        });

        initializeSeekBar();

    }

    /**
     * SeekBar is the publisher.
     *
     */
    private void initializeSeekBar() {
        final SeekBar seekBar = (SeekBar)findViewById(R.id.offset_seekBar);
        seekBar.setMax(130 - 90);
        seekBar.setProgress((int) AccelerometerProcessing.getInstance().getThresholdValue());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double threshold = AccelerometerProcessing.THRESH_INIT_VALUE * (progress + 90) / 100;
                mAccelerometerProcessing.onThresholdChange(threshold);
                formatThreshTextView(threshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void formatThreshTextView(double v) {
        final DecimalFormat df = new DecimalFormat("#.##");
        mThreshValTextView.setText(String.valueOf(df.format(v)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
             return true;
    }

    private void saveThreshold() {
        preferences.edit().putFloat(
                PREFERENCES_VALUES_THRESHOLD_KEY,
                (float) AccelerometerProcessing.getInstance().getThresholdValue()).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAccelDetector.startDetector();

        mTimer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        mAccelDetector.stopDetector();
        mTimer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
    }

    public int getAge() {
        Calendar curr = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();

        birth.setTime(profController.getProfile().getBirthday());
        int age = curr.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        curr.add(Calendar.YEAR, -age);
        if (birth.after(curr)) {
            age = age - 1;
        }
        return age;
    }

    public double getCalories(){
        if (gender == 0) {
            resultE = height * 0.415 * 0.00001 * (weight*2.02);
        }
        if (gender==1) {
            resultE = height * 0.413 * 0.00001 * (weight*2.02);
        }

        return resultE;
    }

    /**
     * Calculates the distance travelled
     * @return The distance travalled
     */
    public double calculateDistance(){
        double distance = 0;
        if (gender == 0) {
            distance = height * 0.415 * 0.00001;
        }
        if (gender == 1) {
            distance = height * 0.413 * 0.00001;
        }
        return distance;
    }
}

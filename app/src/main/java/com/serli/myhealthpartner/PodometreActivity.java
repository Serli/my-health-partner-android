package com.serli.myhealthpartner;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by kahina on 26/01/2017.
 */

public class PodometreActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PREFERENCES_NAME = "Values";
    private static final String PREFERENCES_VALUES_THRESHOLD_KEY = "threshold";
    private SharedPreferences preferences;
    private int mStepCount = 0;
    private AccelerometerDetector mAccelDetector;
    private TextView mThreshValTextView;
    private TextView mStepCountTextView;

    private TextView mTimeValTextView;
    //private MusicCreator mMusicCreator;

    private TimeCounter mTimer;
    private Handler mHandler = new Handler();

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
        //  mTempoValTextView = (TextView)findViewById(R.id.tempoval_textView);
        mTimeValTextView = (TextView)findViewById(R.id.timeVal_textView);

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
                mStepCountTextView.setText(String.valueOf(mStepCount));
            }
        });

        initializeSeekBar();

    }

    /**
     * SeekBar is the publisher.
     * The subscribers are: AccelerometerGraph and AccelerometerProcessing instances.
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
        // Inflate the menu items for use in the action bar
     /*   MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);


        // set string values for menu
        String[] titles = getResources().getStringArray(R.array.nav_drawer_items);
        for (int i = 0; i < titles.length; i++) {
            menu.getItem(i).setTitle(titles[i]);
        }
        return super.onCreateOptionsMenu(menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      /*  switch (item.getItemId()) {
            case R.id.action_threshold:
                saveThreshold();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
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
        // mMusicCreator.startCSound();
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

    }
}

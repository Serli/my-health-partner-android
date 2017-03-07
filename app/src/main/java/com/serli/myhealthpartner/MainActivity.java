package com.serli.myhealthpartner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.serli.myhealthpartner.controller.MainController;
import com.serli.myhealthpartner.controller.ProfileController;
import com.serli.myhealthpartner.model.AccelerometerData;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * View of the main activity..<br/>
 * in this view we allow the user choose de duration and type  of his activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private MainController mainController;
    private ProfileController profileController;

    private boolean acquisitionStarted;

    private Messenger serviceMessenger = null;
    private final Messenger messenger = new Messenger(new IncomingMessageHandler());
    private boolean serviceBound = false;



    @Override
    public void onClick(View view) {

    }

    /**
     * Ask the controller for the accelerometer data stored in the database and display them in a
     * listView.
     */
    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        serviceMessenger = new Messenger(iBinder);
        try {
            Message msg = Message.obtain(null, AccelerometerService.MSG_REGISTER_CLIENT  );
            msg.replyTo = messenger;
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        serviceMessenger = null;
    }

    /**
     * Bind to the AccelerometerService if its already running.
     */
    private void autoBindService() {
        if (AccelerometerService.isRunning()) {
            doBindService();
        }
    }

    /**
     * Bind to the AccelerometerService.
     */
    private void doBindService() {
        bindService(new Intent(this, AccelerometerService.class), this, Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    /**
     * Unbind from the AccelerometerService
     */
    private void doUnbindService() {
        if (serviceBound) {
            if (serviceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, AccelerometerService.MSG_UNREGISTER_CLIENT  );
                    msg.replyTo = messenger;
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(this);
            serviceBound = false;
        }
    }

    /**
     * Handle message incoming from {@link AccelerometerService}.
     */
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AccelerometerService.MSG_ACQUISITION_START  :
                    acquisitionStarted = true;
                    //startStopButton.setText(R.string.button_stop);
                    break;
                case AccelerometerService.MSG_ACQUISITION_STOP  :
                    doUnbindService();
                    acquisitionStarted = false;
                    //startStopButton.setText(R.string.button_start);
                    //displayAlertDialog();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    //Requesting permission
    private void requestPhoneStatePermission(){
        //Ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == 0){
            //If permission is granted
            if(!(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                //Displaying this toast if permission is not granted
                Toast.makeText(this,"You denied the permission",Toast.LENGTH_LONG).show();
                this.finish();
            }
            else{
                verifyExistingProfile();
            }
        }
    }

    public void verifyExistingProfile(){
        if (profileController.getProfile() == null){
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    //////////partie podometre
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
    // private ProfileController profController ;
    int height;
    int weight;
    int gender;
    double resultE = 0;
    String result;
    TextView counter;
    TextView number_calorie;
    double res = 0;
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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        requestPhoneStatePermission();

        // set default locale:
        Locale.setDefault(Locale.ENGLISH);
        // get and configure text views
        mThreshValTextView = (TextView)findViewById(R.id.threshval_textView);
        formatThreshTextView(AccelerometerProcessing.THRESH_INIT_VALUE);
        mStepCountTextView = (TextView)findViewById(R.id.stepcount_textView);
        mStepCountTextView.setText(String.valueOf(0));
        mTimeValTextView = (TextView)findViewById(R.id.timeVal_textView);

        /****************/
        counter = (TextView) findViewById(R.id.calorie_affiche);
        number_calorie=(TextView)findViewById(R.id.number_calorie);
        //dist
        counterDistance=(TextView) findViewById(R.id.distance_affiche);
        distanceTravelled=(TextView)findViewById(R.id.number_distance);//

        profileController = new ProfileController(this);
        if (profileController.getProfile() != null) {
            height = profileController.getProfile().getHeight();
            weight = profileController.getProfile().getWeight();
            gender = profileController.getProfile().getGender();
        }
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
                res = getCaloris()*mStepCount;
                result = res + " cal";
                number_calorie.setText(result);
                mStepCountTextView.setText(String.valueOf(mStepCount));
                ///dist
                resDistance = (double) Math.round((calculateDistance() * mStepCount) * 100) / 100; // 4.248 --> 4.25
                resultDistance = resDistance + " km";
                distanceTravelled.setText(resultDistance);//
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if (item.getItemId() == R.id.action_podometre) {
            startActivity(new Intent(this, PodometreActivity.class));
        }
        if (item.getItemId() == R.id.action_repport) {
            startActivity(new Intent(this, RepportActivity.class));
        }
        //action_repport
       /* if (item.getItemId() == R.id.action_show_data) {
            RelativeLayout dataLayout = (RelativeLayout) findViewById(R.id.data_list_layout);
            if (dataLayout.getVisibility() == RelativeLayout.INVISIBLE) {
                populateDataListView();
                dataLayout.setVisibility(RelativeLayout.VISIBLE);
            }
            else
                dataLayout.setVisibility(RelativeLayout.INVISIBLE);
            startActivity(new Intent(this, ProfileActivity.class));
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

        birth.setTime(profileController.getProfile().getBirthday());
        int age = curr.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        curr.add(Calendar.YEAR, -age);
        if (birth.after(curr)) {
            age = age - 1;
        }
        return age;
    }

    public double getCaloris(){
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

package com.serli.myhealthpartner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.serli.myhealthpartner.controller.MainController;
import com.serli.myhealthpartner.controller.ProfileController;

import java.util.ArrayList;

/**
 * View of the main activity..<br/>
 * in this view we allow the user choose de duration and type  of his activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private MainController mainController;
    private ProfileController profileController;

    private Button profileButton;
    private Button trackerButton;
    private TextView runningCalorieText;
    private TextView walkingCalorieText;
    private PieChart pieChart;

    private boolean acquisitionStarted;

    private Messenger serviceMessenger = null;
    private final Messenger messenger = new Messenger(new IncomingMessageHandler());
    private boolean serviceBound = false;
    private TextView runningStepText;
    private TextView walkingStepText;

    private Thread updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        Typeface font = Typeface.createFromAsset(getAssets(), "Poppins-Regular.ttf");
        Typeface fontSemiBold = Typeface.createFromAsset(getAssets(), "Poppins-SemiBold.ttf");

        pieChart = (PieChart) findViewById(R.id.piechart_podometer);
        pieChart.setHoleColor(Color.argb(255, 64, 134, 237));
        pieChart.setCenterTextTypeface(font);
        pieChart.setCenterTextColor(Color.argb(255, 255, 255, 255));
        ArrayList<PieEntry> pedometerEntries = new ArrayList<>();
        pedometerEntries.add(new PieEntry(0));
        pedometerEntries.add(new PieEntry(0));
        PieDataSet dataSet = new PieDataSet(pedometerEntries, "Pedometer");

        IValueFormatter formatter = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return ((int) value + "");
            }
        };
        dataSet.setValueFormatter(formatter);

        dataSet.setColors(Color.argb(255, 255, 255, 255), Color.argb(255, 255, 193, 9));
        PieData data = new PieData(dataSet);
        data.setValueTypeface(fontSemiBold);
        data.setValueTextSize(15f);
        pieChart.setData(data);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        mainController = new MainController(this);
        profileController = new ProfileController(this);

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (result != PackageManager.PERMISSION_GRANTED){
            requestPhoneStatePermission();
        }
        else{
            verifyExistingProfile();
        }

        acquisitionStarted = AccelerometerService.isRunning();

        profileButton = (Button) findViewById(R.id.button_profile);
        profileButton.setOnClickListener(this);

        trackerButton = (Button) findViewById(R.id.button_tracking);
        trackerButton.setOnClickListener(this);

        runningCalorieText = (TextView) findViewById(R.id.textView_running_today_calories);
        runningStepText = (TextView) findViewById(R.id.textView_running_today_steps);
        walkingCalorieText = (TextView) findViewById(R.id.textView_walking_today_calories);
        walkingStepText = (TextView) findViewById(R.id.textView_walking_today_steps);

        runningCalorieText.setTypeface(fontSemiBold);
        runningStepText.setTypeface(font);
        walkingCalorieText.setTypeface(fontSemiBold);
        walkingStepText.setTypeface(font);

        autoBindService();

        // Test acquisition without button
        if (!acquisitionStarted && profileController.getProfile() != null) {
            mainController.startAcquisition();
            doBindService();
        }

        updateObjective();
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
        return true;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == profileButton.getId()) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if(view.getId() == trackerButton.getId()) {
            //TODO: afficher le gestionnaire de suivi.
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        serviceMessenger = new Messenger(iBinder);
        try {
            Message msg = Message.obtain(null, AccelerometerService.MSG_REGISTER_CLIENT);
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
     * Update periodically the information displayed in the UI.
     */
    private void updateObjective() {
        if(updateThread != null)
            updateThread.interrupt();
        updateThread = new Thread() {
            @Override
            public void run() {
                try {
                    while(!isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int stepPlanned = mainController.getStepPlanned();
                                int stepDone = mainController.getStepDone();
                                walkingCalorieText.setText(mainController.getCalorieWalking() + "");
                                walkingStepText.setText(mainController.getStepWalking() + "");
                                runningCalorieText.setText(mainController.getCalorieRunning() + "");
                                runningStepText.setText(mainController.getStepRunning() + "");
                                ArrayList<PieEntry> pedometerEntries = new ArrayList<>();
                                pedometerEntries.add(new PieEntry(stepDone));
                                pedometerEntries.add(new PieEntry(stepPlanned - stepDone));
                                PieDataSet dataSet = new PieDataSet(pedometerEntries, "Pedometer");
                                dataSet.setColors(Color.argb(255, 255, 255, 255), Color.argb(255, 255, 193, 9));
                                pieChart.getData().setDataSet(dataSet);
                                pieChart.setCenterText(Math.round(stepDone) + " / " + Math.round(stepPlanned));
                                pieChart.notifyDataSetChanged();
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        updateThread.start();
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
                    break;
                case AccelerometerService.MSG_ACQUISITION_STOP  :
                    doUnbindService();
                    acquisitionStarted = false;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Requesting permission
     */
    private void requestPhoneStatePermission(){
        //Ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
    }

    /**
     * This method will be called when the user will tap on allow or deny
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Verify if a profile is already existing.
     * If none was existing, open the {@link ProfileActivity} and ask to the user to complete the profile.
     */
    public void verifyExistingProfile(){
        if (profileController.getProfile() == null){
            Toast.makeText(this, getResources().getText(R.string.toast_profile_missing).toString(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        doUnbindService();
        updateThread.interrupt();
        super.onPause();
    }

    @Override
    protected void onResume() {
        doBindService();
        updateObjective();
        super.onResume();
    }
}

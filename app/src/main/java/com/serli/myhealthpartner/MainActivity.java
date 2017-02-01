package com.serli.myhealthpartner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.serli.myhealthpartner.controller.MainController;
import com.serli.myhealthpartner.controller.ProfileController;
import com.serli.myhealthpartner.model.AccelerometerData;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * View of the main activity..<br/>
 * in this view we allow the user choose de duration and type  of his activity
 */
// TODO : Add send and delete acquisition.
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private MainController mainController;
    private ProfileController profileController;

    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private Spinner activitySpinner;
    private Button startStopButton;

    private boolean acquisitionStarted;

    private Messenger serviceMessenger = null;
    private final Messenger messenger = new Messenger(new IncomingMessageHandler());
    private boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);


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

        minutePicker = (NumberPicker) findViewById(R.id.minute_picker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        secondPicker = (NumberPicker) findViewById(R.id.second_picker);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        activitySpinner = (Spinner) findViewById(R.id.activity_spinner);
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sport_activity));
        activitySpinner.setAdapter(stringArrayAdapter);

        startStopButton = (Button) findViewById(R.id.button_start_stop);
        startStopButton.setOnClickListener(this);

        autoBindService();

        RelativeLayout dataLayout = (RelativeLayout) findViewById(R.id.data_list_layout);
        dataLayout.setVisibility(RelativeLayout.INVISIBLE);

        Button updateButton = (Button) findViewById(R.id.button_update);
        updateButton.setOnClickListener(this);

        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(this);

        // Test acquisition without button
        if (!acquisitionStarted && profileController.getProfile() != null) {
            mainController.startAcquisition();
            doBindService();
        }
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

        if (item.getItemId() == R.id.action_show_data) {
            RelativeLayout dataLayout = (RelativeLayout) findViewById(R.id.data_list_layout);
            if (dataLayout.getVisibility() == RelativeLayout.INVISIBLE) {
                populateDataListView();
                dataLayout.setVisibility(RelativeLayout.VISIBLE);
            }
            else
                dataLayout.setVisibility(RelativeLayout.INVISIBLE);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        /*if (view.getId() == R.id.button_start_stop) {
            if (acquisitionStarted) {
                doUnbindService();
                controller.stopAcquisition();
                startStopButton.setText(R.string.button_start);
                acquisitionStarted = false;
                displayAlertDialog();
            } else {
                long duration = minutePicker.getValue() * 60000L + secondPicker.getValue() * 1000L;
                if (duration > 0) {
                    controller.startAcquisition(duration, activitySpinner.getSelectedItemPosition());
                    doBindService();
                }
            }
        }
        if (view.getId() == R.id.button_update || view.getId() == R.id.button_clear) {
            if (view.getId() == R.id.button_clear) {
                controller.DeleteAcquisition();
            }
            populateDataListView();
        }*/
    }

    /**
     * Ask the controller for the accelerometer data stored in the database and display them in a
     * listView.
     */
    private void populateDataListView() {
        ListView listView = (ListView) findViewById(R.id.data_list_view);
        final List<AccelerometerData> dataList = mainController.getData();
        ArrayAdapter<AccelerometerData> dataArrayAdapter = new ArrayAdapter<AccelerometerData>(this, android.R.layout.simple_list_item_2, android.R.id.text1, dataList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                AccelerometerData data = dataList.get(position);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss,S", Locale.getDefault());

                text1.setText(dateFormat.format(new Timestamp(data.getTimestamp())));
                String strX = String.format("%.2f", data.getX());
                String strY = String.format("%.2f", data.getY());
                String strZ = String.format("%.2f", data.getZ());
                text2.setText("x=" + strX + " y=" + strY + " z=" + strZ + " " + MainActivity.this.getResources().getTextArray(R.array.sport_activity)[data.getActivity()]);

                return view;
            }
        };
        listView.setAdapter(dataArrayAdapter);
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

    /**
     * Asks user if he wants to send accelerometer data acquired to the database
     */
    /*private void displayAlertDialog(){;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.sending_data_message);
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //controller.sendAcquisition(); ==> A EFFECTUER DES QUE LA VERITABLE URL DE L'ACCES AU SERVEUR EST VALIDE !
                Toast.makeText(MainActivity.this, R.string.acquisition_sent,Toast.LENGTH_LONG).show();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                controller.DeleteAcquisition();
                Toast.makeText(MainActivity.this, R.string.acquisition_deleted, Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/

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
}

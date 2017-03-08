package com.serli.myhealthpartner;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;

import com.serli.myhealthpartner.controller.MainController;
import com.serli.myhealthpartner.controller.PedometerController;
import com.serli.myhealthpartner.model.AccelerometerDAO;

/**
 * This service store the accelerometer data.
 */
public class AccelerometerService extends Service {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_ACQUISITION_START = 3;
    public static final int MSG_ACQUISITION_STOP = 4;

    private MainController mainController;
    private PedometerController pedometerController;

    private final Messenger messenger = new Messenger(new IncomingMessageHandler());
    private Messenger clientMessenger = null;

    private static boolean isRunning = false;

    private AccelerometerDAO dao;

    private int activity = -1;
    private long previousTimestamp = 0;

    private Handler handler = new Handler();

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            long timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + sensorEvent.timestamp / 1000000;
            if (previousTimestamp == 0 || timestamp - previousTimestamp >= 99){
                dao.addEntry(x, y, z, timestamp, activity);
                previousTimestamp = timestamp;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private Runnable startRun = new Runnable() {
        @Override
        public void run() {
            startAcquisition();
        }
    };

    private Runnable sendRun = new Runnable() {
        @Override
        public void run() {
            sendAcquisition();
        }
    };

    /**
     * The method onCreate is called when the activity AccelerometerService is created
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (!isRunning) {
            isRunning = true;

            mainController = new MainController(this);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            dao = new AccelerometerDAO(this);
        }
    }

    /**
     * The method onStartCommand will open the connection with the mobile database and then start the acquisition.
     * * @return START_STICKY, and int which indicates that the service have to continue after the application finished
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dao.open();
        startRun.run();
        return START_STICKY;
    }

    /**
     * Start the acquisition of the accelerometer data.
     */
    private void startAcquisition() {
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        handler.postDelayed(sendRun, 5000);
    }

    /**
     * Stop the acquisition of the accelerometer data.
     */
    private void sendAcquisition() {
        mainController.sendAcquisition();
        handler.postDelayed(sendRun, 5000);
    }

    /**
     * The final call  receive before the activity is destroyed..
     */
    @Override
    public void onDestroy() {
        handler.removeCallbacks(startRun);
        handler.removeCallbacks(sendRun);
        sensorManager.unregisterListener(sensorEventListener, accelerometerSensor);
        dao.close();
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    /**
     * Tell if the service is already running.
     *
     * @return true if the service is already running.
     */
    public static boolean isRunning() {
        return isRunning;
    }

    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT  :
                    clientMessenger = msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT  :
                    clientMessenger = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}

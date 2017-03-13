package com.serli.myhealthpartnerdevelopper;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;

import com.serli.myhealthpartnerdevelopper.controller.MainController;
import com.serli.myhealthpartnerdevelopper.model.AccelerometerDAO;

/**
 * This service stores the accelerometer data for the duration specified in the intent.<br/>
 * The intent given in the start command must contain 2 extra data :<br/>
 * &nbsp;&nbsp;&nbsp;<b>duration</b> : a long which specify the duration of the acquisition.<br/>
 * &nbsp;&nbsp;&nbsp;<b>activity</b> : an int which specify the sport activity performed during the acquisition.<br/> <br/>
 * When the service is started, it will play a beep sound as start signal for the user and another one at the end of the acquisition as stop signal for the user.
 */
public class AccelerometerService extends Service {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_ACQUISITION_START = 3;
    public static final int MSG_ACQUISITION_STOP = 4;


    private final Messenger messenger = new Messenger(new IncomingMessageHandler());
    private Messenger clientMessenger = null;

    private SoundPool soundPool;
    private int soundID;

    private static boolean isRunning = false;

    private AccelerometerDAO dao;

    private int activity;
    private long duration;

    private Handler handler = new Handler();

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        /**
         * We add a new AccelerometerData in the accelerometer data table each time the sensor changes.
         * @param sensorEvent
         */
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            long timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + sensorEvent.timestamp / 1000000;
            dao.addEntry(x, y, z, timestamp, activity);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private Runnable startRun = new Runnable() {
        @Override
        public void run() {
            startAcquisition(duration);
        }
    };

    private Runnable stopRun = new Runnable() {
        @Override
        public void run() {
            stopAcquisition();
        }
    };

    /**
     * The method onCreate is called when the activity AccelerometreService is first created. A beep sound is played to signal it.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;

        soundPool = new SoundPool(4, AudioManager.STREAM_ALARM, 100);
        soundID = soundPool.load(this, R.raw.beep, 1);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        dao = new AccelerometerDAO(this);
    }

    /**
     * The method onStartCommand is called from the alarm, it schedules a new alarm for N minutes later, and spawns a thread to do its networking.
     * @return START_NOT_STICKY Constant to return from onStartCommand(Intent, int, int): if this service's process is killed while it is started, and there are no new start intents to deliver to it.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dao.open();

        activity = intent.getIntExtra("activity", 0);

        duration = intent.getLongExtra("duration", 0);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPool.play(soundID, 1, 1, 1, 0, 1);

                if (clientMessenger != null) {
                    try {
                        Message msg = Message.obtain(null, MSG_ACQUISITION_START );
                        msg.replyTo = messenger;
                        clientMessenger.send(msg);
                    } catch (RemoteException e) {
                    }
                }

                handler.postDelayed(startRun, AccelerometerService.this.getResources().getInteger(R.integer.start_delay));
            }
        });
        return START_NOT_STICKY;
    }

    /**
     * Start the acquisition of the accelerometer data for the duration given.
     *
     * @param duration The duration of the acquisition.
     */
    private void startAcquisition(long duration) {
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        handler.postDelayed(stopRun, duration);
    }

    /**
     * Stop the acquisition of the accelerometer data.
     */
    private void stopAcquisition() {
        soundPool.play(soundID, 1, 1, 1, 0, 1);
        if (clientMessenger != null) {
            try {
                Message msg = Message.obtain(null, MSG_ACQUISITION_STOP  );
                msg.replyTo = messenger;
                clientMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }
        AccelerometerService.this.stopSelf();

        MainController controller = new MainController(this);
        controller.sendAcquisition();
    }

    /**
     * The final call receive before the activity is destroyed..
     */
    @Override
    public void onDestroy() {
        handler.removeCallbacks(startRun);
        handler.removeCallbacks(stopRun);
        soundPool.release();
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

package com.serli.myhealthpartner;

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

import com.serli.myhealthpartner.model.AccelerometerDAO;

/**
 * This service store the accelerometer data for the duration specified in the intent.<br/>
 * The intent given in the start command must contain 2 extra data :<br/>
 * &nbsp;&nbsp;&nbsp;<b>duration</b> : an long which specify the duration of the acquisition.<br/>
 * &nbsp;&nbsp;&nbsp;<b>activity</b> : an int which specify the sport activity performed during the acquisition.<br/> <br/>
 * When the service is started, it will play a beep sound as start signal for the user and an other at the end of the acquisition as stop signal for the user.
 */
public class AccelerometerService extends Service {

    public static final int msgRegisterClient = 1;
    public static final int msgUnregistredClient = 2;
    public static final int msgAcquisitionSart = 3;
    public static final int msgAcquisitionStop = 4;

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
                        Message msg = Message.obtain(null, msgAcquisitionSart);
                        msg.replyTo = messenger;
                        clientMessenger.send(msg);
                    } catch (RemoteException e) {
                    }
                }

                handler.postDelayed(startRun, 2000);
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
                Message msg = Message.obtain(null, msgAcquisitionStop );
                msg.replyTo = messenger;
                clientMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }
        AccelerometerService.this.stopSelf();
    }

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
                case msgRegisterClient :
                    clientMessenger = msg.replyTo;
                    break;
                case msgUnregistredClient :
                    clientMessenger = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}

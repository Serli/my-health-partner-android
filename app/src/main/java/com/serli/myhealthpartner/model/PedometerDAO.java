package com.serli.myhealthpartner.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * DAO for the pedometer table.
 */
public class PedometerDAO {

    private Database database;
    private SQLiteDatabase db;
    private String[] allColumns = {database.PEDOMETER_TIMESTAMP, database.PEDOMETER_DURATION, database.PEDOMETER_STEPS,
            database.PEDOMETER_CALORIES, database.PEDOMETER_DISTANCE, database.PEDOMETER_ACTIVITY};

    /**
     * Build the DAO on the given context.
     *
     * @param context The context where the dao is called
     */
    public PedometerDAO(Context context) {
        this.database = new Database(context);
    }

    /**
     * Open the connection with the database.
     */
    public void open() {
        db = database.getWritableDatabase();
    }

    /**
     * Close the connection with the database.
     */
    public void close() {
        database.close();
    }

    /**
     * Add an entry in the pedometer table.
     *
     * @param data the data object containing the pedometer information
     */
    public void addEntry(PedometerData data) {
        ContentValues values = new ContentValues();
        values.put(Database.PEDOMETER_TIMESTAMP, data.getTimestamp());
        values.put(Database.PEDOMETER_DURATION, data.getDuration());
        values.put(Database.PEDOMETER_STEPS, data.getSteps());
        values.put(Database.PEDOMETER_CALORIES, data.getCalories());
        values.put(Database.PEDOMETER_DISTANCE, data.getDistance());
        values.put(Database.PEDOMETER_ACTIVITY, data.getActivity());
        db.insert(Database.PEDOMETER_TABLE, null, values);
        System.out.println("data inserted !");
    }

    /**
     * Return object containing the pedometer stored.
     *
     * @return the pedometer
     */
    public PedometerData getPedometer() {
        Cursor c = db.query(Database.PEDOMETER_TABLE, allColumns, null, null, null, null, null);
        c.moveToFirst();
        PedometerData data = cursorToData(c);
        c.close();
        return data;
    }

    public List<PedometerData> getTodayPedometer() {
        List<PedometerData> resultList = new ArrayList<>();
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY,0);
        today.set(Calendar.MINUTE,0);
        today.set(Calendar.SECOND,0);
        today.set(Calendar.MILLISECOND,0);

        Cursor c = db.query(Database.PEDOMETER_TABLE, allColumns, Database.PEDOMETER_TIMESTAMP + " >= " + today.getTimeInMillis(), null, null, null, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            resultList.add(cursorToData(c));
            c.moveToNext();
        }
        c.close();
        return resultList;
    }

    /**
     * Transform a {@link Cursor} in a {@link PedometerData}.
     *
     * @param cursor the {@link Cursor} to transform
     * @return the corresponding {@link PedometerData}
     */
    private PedometerData cursorToData(Cursor cursor) {
        PedometerData pedData = null;
        if (!cursor.isAfterLast()) {
            pedData = new PedometerData();
            pedData.setTimestamp(cursor.getLong(0));
            pedData.setDuration(cursor.getLong(1));
            pedData.setSteps(cursor.getInt(2));
            pedData.setCalories(cursor.getDouble(3));
            pedData.setDistance(cursor.getDouble(4));
            pedData.setActivity(cursor.getInt(5));
        }
        return pedData;
    }

}


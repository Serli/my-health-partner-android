package com.serli.myhealthpartner.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        if (data.getSteps() != 0 && data.getTimestamp() != 0) {
            db.delete(Database.PEDOMETER_TABLE, null, null);

            ContentValues values = new ContentValues();
            values.put(Database.PEDOMETER_TIMESTAMP, data.getTimestamp());
            values.put(Database.PEDOMETER_DURATION, data.getDuration());
            values.put(Database.PEDOMETER_STEPS, data.getSteps());
            values.put(Database.PEDOMETER_CALORIES, data.getCalories());
            values.put(Database.PEDOMETER_DISTANCE, data.getDistance());
            values.put(Database.PEDOMETER_ACTIVITY, data.getActivity());

            db.insert(Database.PEDOMETER_TABLE, null, values);
        }
    }

    /**
     * Return object containing the pedometer stored.
     *
     * @return the pedometer
     */
    public PedometerData getPedometer() {
        Cursor c = db.query(Database.PEDOMETER_TABLE, allColumns, null, null, null, null, null);
        c.moveToFirst();
        return cursorToData(c);
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
            pedData.setTimestamp(cursor.getInt(0));
            pedData.setDuration(cursor.getLong(1));
            pedData.setSteps(cursor.getInt(2));
            pedData.setCalories(cursor.getInt(3));
            pedData.setDistance(cursor.getInt(4));
            pedData.setActivity(cursor.getInt(5));
        }
        return pedData;
    }

}


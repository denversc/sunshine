package org.sleepydragon.sunshine.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class TestWeatherOpenHelper extends AndroidTestCase {

    private static final String DATABASE_NAME = "weather.db";

    public void testCreate() {
        mContext.deleteDatabase(DATABASE_NAME);
        final WeatherOpenHelper x = new WeatherOpenHelper(mContext);
        final SQLiteDatabase db = x.getReadableDatabase();
        // make sure the tables are created
        db.query(WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);
        db.query(WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
    }

}

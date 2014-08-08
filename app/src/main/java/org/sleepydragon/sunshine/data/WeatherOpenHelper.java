package org.sleepydragon.sunshine.data;

import org.sleepydragon.sunshine.data.WeatherContract.LocationEntry;
import org.sleepydragon.sunshine.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public WeatherOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public WeatherOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(generateCreateLocationTableSQL());
        db.execSQL(generateCreateWeatherTableSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static String generateCreateLocationTableSQL() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(LocationEntry.TABLE_NAME);
        sb.append('(');
        sb.append(LocationEntry._ID).append(" INTEGER PRIMARY KEY, ");
        sb.append(LocationEntry.COL_CITY_ID).append(" TEXT NOT NULL, ");
        sb.append(LocationEntry.COL_DISPLAY_NAME).append(" TEXT, ");
        sb.append(LocationEntry.COL_LATITUDE).append(" REAL, ");
        sb.append(LocationEntry.COL_LONGITUDE).append(" REAL");
        sb.append(')');
        return sb.toString();
    }

    private static String generateCreateWeatherTableSQL() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(WeatherEntry.TABLE_NAME);
        sb.append('(');
        sb.append(WeatherEntry._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(WeatherEntry.COL_LOCATION_ID).append(" TEXT NOT NULL, ");
        sb.append(WeatherEntry.COL_DATE).append(" INTEGER NOT NULL, ");
        sb.append(WeatherEntry.COL_DESCRIPTION).append(" TEXT, ");
        sb.append(WeatherEntry.COL_ICON_ID).append(" TEXT, ");
        sb.append(WeatherEntry.COL_TEMP_HI).append(" REAL, ");
        sb.append(WeatherEntry.COL_TEMP_LO).append(" REAL, ");
        sb.append(WeatherEntry.COL_HUMIDITY).append(" REAL, ");
        sb.append(WeatherEntry.COL_PRESSURE).append(" REAL, ");
        sb.append(WeatherEntry.COL_WIND_SPEED).append(" REAL, ");
        sb.append(WeatherEntry.COL_WIND_DIRECTION).append(" TEXT, ");
        sb.append("FOREIGN KEY (").append(WeatherEntry.COL_LOCATION_ID)
                .append(") REFERENCES ").append(LocationEntry.TABLE_NAME)
                .append(" (").append(LocationEntry._ID).append("), ");
        sb.append("UNIQUE (").append(WeatherEntry.COL_DATE).append(',')
                .append(WeatherEntry.COL_LOCATION_ID)
                .append(") ON CONFLICT REPLACE");
        sb.append(')');
        return sb.toString();
    }

}

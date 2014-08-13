package org.sleepydragon.sunshine.data;

import org.sleepydragon.sunshine.data.WeatherContract.WeatherEntry;
import org.sleepydragon.sunshine.data.WeatherContract.LocationEntry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.sleepydragon.sunshine.data.WeatherContract.LocationEntry;
import org.sleepydragon.sunshine.data.WeatherContract.WeatherEntry;

public class TestWeatherProvider extends AndroidTestCase {

    private static final String DATABASE_NAME = "weather.db";

    public void testGetType_Weather() {
        doTestGetType(WeatherEntry.CONTENT_URI, WeatherEntry.CONTENT_TYPE);
    }

    public void testGetType_WeatherFromCityId() {
        doTestGetType(WeatherEntry.buildUriFromCityId("Kitchener"), WeatherEntry.CONTENT_TYPE);
    }

    public void testGetType_WeatherFromCityIdAndDate() {
        doTestGetType(WeatherEntry.buildUriFromCityIdAndDate("Kitchener", "Jan 01, 2014")
                , WeatherEntry.CONTENT_TYPE_ITEM);
    }

    public void testGetType_Location() {
        doTestGetType(LocationEntry.CONTENT_URI, LocationEntry.CONTENT_TYPE);
    }

    public void testGetType_LocationFromId() {
        doTestGetType(LocationEntry.buildUriFromId(123), LocationEntry.CONTENT_TYPE_ITEM);
    }

    private void doTestGetType(Uri uri, String expected) {
        final ContentResolver cr = getContext().getContentResolver();
        final String actual = cr.getType(uri);
        assertEquals(expected, actual);
    }

    public void testInsert() {
        deleteDatabase();

        // TODO: update to use content provider
        final WeatherOpenHelper x = new WeatherOpenHelper(mContext);
        final SQLiteDatabase db = x.getReadableDatabase();

        final ContentValues cvLocation = new ContentValues();
        cvLocation.put(LocationEntry.COL_CITY_ID, "TestCityID");
        cvLocation.put(LocationEntry.COL_DISPLAY_NAME, "TestDisplayName");
        cvLocation.put(LocationEntry.COL_LATITUDE, -54.321);
        cvLocation.put(LocationEntry.COL_LONGITUDE, 12.345);
        final long locationId = db.insert(LocationEntry.TABLE_NAME, null, cvLocation);
        assertTrue(locationId >= 0);

        final ContentValues cvWeather = new ContentValues();
        cvWeather.put(WeatherEntry.COL_DATE, "TestDate");
        cvWeather.put(WeatherEntry.COL_DESCRIPTION, "TestDescription");
        cvWeather.put(WeatherEntry.COL_ICON_ID, "TestIconId");
        cvWeather.put(WeatherEntry.COL_LOCATION_ID, locationId);
        cvWeather.put(WeatherEntry.COL_HUMIDITY, 12.34);
        cvWeather.put(WeatherEntry.COL_PRESSURE, 23.45);
        cvWeather.put(WeatherEntry.COL_TEMP_HI, 34.56);
        cvWeather.put(WeatherEntry.COL_TEMP_LO, 45.67);
        cvWeather.put(WeatherEntry.COL_WIND_SPEED, 56.78);
        cvWeather.put(WeatherEntry.COL_WIND_DIRECTION, "NNW");
        final long weatherId = db.insert(WeatherEntry.TABLE_NAME, null, cvWeather);
        assertTrue(weatherId >= 0);

        final ContentResolver cr = getContext().getContentResolver();

        final Cursor curLocation = cr.query(LocationEntry.CONTENT_URI, null, null, null, null);
        assertEquals(curLocation.getCount(), 1);
        assertTrue(curLocation.moveToFirst());
        assertColValue(curLocation, LocationEntry._ID, Long.toString(locationId));
        assertColValue(curLocation, LocationEntry.COL_CITY_ID, "TestCityID");
        assertColValue(curLocation, LocationEntry.COL_DISPLAY_NAME, "TestDisplayName");
        assertColValue(curLocation, LocationEntry.COL_LATITUDE, "-54.321");
        assertColValue(curLocation, LocationEntry.COL_LONGITUDE, "12.345");
        curLocation.close();

        final Uri locationByIdQueryUri = LocationEntry.buildUriFromId(locationId);
        final Cursor curLocationById = cr.query(locationByIdQueryUri, null, null, null, null);
        assertEquals(curLocationById.getCount(), 1);
        assertTrue(curLocationById.moveToFirst());
        assertColValue(curLocationById, LocationEntry._ID, Long.toString(locationId));
        assertColValue(curLocationById, LocationEntry.COL_CITY_ID, "TestCityID");
        assertColValue(curLocationById, LocationEntry.COL_DISPLAY_NAME, "TestDisplayName");
        assertColValue(curLocationById, LocationEntry.COL_LATITUDE, "-54.321");
        assertColValue(curLocationById, LocationEntry.COL_LONGITUDE, "12.345");
        curLocation.close();

        final Cursor curWeather = cr.query(WeatherEntry.CONTENT_URI, null, null, null, null);
        assertEquals(curWeather.getCount(), 1);
        assertTrue(curWeather.moveToFirst());
        assertColValue(curWeather, WeatherEntry._ID, Long.toString(weatherId));
        assertColValue(curWeather, WeatherEntry.COL_DATE, "TestDate");
        assertColValue(curWeather, WeatherEntry.COL_DESCRIPTION, "TestDescription");
        assertColValue(curWeather, WeatherEntry.COL_ICON_ID, "TestIconId");
        assertColValue(curWeather, WeatherEntry.COL_LOCATION_ID, locationId);
        assertColValue(curWeather, WeatherEntry.COL_HUMIDITY, "12.34");
        assertColValue(curWeather, WeatherEntry.COL_PRESSURE, "23.45");
        assertColValue(curWeather, WeatherEntry.COL_TEMP_HI, "34.56");
        assertColValue(curWeather, WeatherEntry.COL_TEMP_LO, "45.67");
        assertColValue(curWeather, WeatherEntry.COL_WIND_SPEED, "56.78");
        assertColValue(curWeather, WeatherEntry.COL_WIND_DIRECTION, "NNW");
        curWeather.close();
    }

    private void deleteDatabase() {
        mContext.deleteDatabase(DATABASE_NAME);
    }

    private static void assertColValue(Cursor cursor, String colName, long expected) {
        assertColValue(cursor, colName, Long.toString(expected));
    }

    private static void assertColValue(Cursor cursor, String colName, String expected) {
        final int columnIndex = cursor.getColumnIndex(colName);
        final String actual = cursor.getString(columnIndex);
        assertEquals(expected, actual);
    }
}

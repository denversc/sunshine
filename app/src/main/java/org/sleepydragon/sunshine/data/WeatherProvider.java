package org.sleepydragon.sunshine.data;

import org.sleepydragon.sunshine.data.WeatherContract.WeatherEntry;
import org.sleepydragon.sunshine.data.WeatherContract.LocationEntry;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_CITY_ID = 101;
    private static final int WEATHER_WITH_CITY_ID_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private WeatherOpenHelper mOpenHelper;
    private UriMatcher mUriMatcher;
    private SQLiteQueryBuilder mWeatherWithLocationQueryBuilder;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherOpenHelper(getContext());
        mUriMatcher = buildUriMatcher();

        mWeatherWithLocationQueryBuilder = new SQLiteQueryBuilder();
        mWeatherWithLocationQueryBuilder.setTables(
                WeatherEntry.TABLE_NAME + " INNER JOIN " + LocationEntry.TABLE_NAME + " ON "
                + WeatherEntry.TABLE_NAME + "." + WeatherEntry.COL_LOCATION_ID + "="
                + LocationEntry.TABLE_NAME + "." + LocationEntry._ID);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int queryType = mUriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final Cursor cursor;

        switch (queryType) {
            case WEATHER:
                cursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WEATHER_WITH_CITY_ID: {
                final String cityId = WeatherEntry.getCityIdFromUri(uri);
                final String curSelection = LocationEntry.COL_CITY_ID + "=?";
                final String[] curSelectionArgs = new String[] {cityId};
                cursor = mWeatherWithLocationQueryBuilder.query(db, projection, curSelection,
                        curSelectionArgs, null, null, sortOrder);
                break;
            }
            case WEATHER_WITH_CITY_ID_AND_DATE:
                final String cityId = WeatherEntry.getCityIdFromUri(uri);
                final String date = WeatherEntry.getDateFromUri(uri);
                final String curSelection = LocationEntry.COL_CITY_ID + " = ?"
                        + " AND " + WeatherEntry.COL_DATE + " >= ?";
                final String[] curSelectionArgs = new String[] {cityId, date};
                cursor = mWeatherWithLocationQueryBuilder.query(db, projection, curSelection,
                        curSelectionArgs, null, null, sortOrder);
                break;
            case LOCATION:
                cursor = db.query(LocationEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case LOCATION_ID: {
                final long id = ContentUris.parseId(uri);
                final String idSelection = LocationEntry._ID + "=?";
                final String[] idSelectionArgs = new String[] {Long.toString(id)};
                cursor = db.query(LocationEntry.TABLE_NAME, projection, idSelection,
                        idSelectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("unsupported URI: " + uri);
        }

        final ContentResolver cr = getContext().getContentResolver();
        cursor.setNotificationUri(cr, uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int urlId = mUriMatcher.match(uri);
        switch (urlId) {
            case WEATHER:
            case WEATHER_WITH_CITY_ID:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_CITY_ID_AND_DATE:
                return WeatherEntry.CONTENT_TYPE_ITEM;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_CITY_ID_AND_DATE);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_CITY_ID);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER, WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION, LOCATION);
        return uriMatcher;
    }
}

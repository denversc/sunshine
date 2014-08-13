package org.sleepydragon.sunshine.data;

import org.sleepydragon.sunshine.data.WeatherContract.WeatherEntry;
import org.sleepydragon.sunshine.data.WeatherContract.LocationEntry;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private WeatherOpenHelper mOpenHelper;
    private UriMatcher mUriMatcher;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherOpenHelper(getContext());
        mUriMatcher = buildUriMatcher();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int queryType = mUriMatcher.match(uri);
        final Cursor cursor;
        switch (queryType) {
            case WEATHER:
                final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                cursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                cursor = null;
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                cursor = null;
                break;
            case LOCATION:
                cursor = null;
                break;
            case LOCATION_ID:
                cursor = null;
                break;
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
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
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
                WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER, WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION, LOCATION);
        return uriMatcher;
    }
}

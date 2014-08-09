package org.sleepydragon.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "org.sleepydragon.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    private WeatherContract() {
    }

    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WEATHER).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";

        /**
         * ID of the Location entry to which this weather data entry pertains.
         * Column type is INTEGER that references the ID column of a LocationEntry.
         */
        public static final String COL_LOCATION_ID = "location_id";

        /**
         * Date to which this weather data pertains.
         * Column type is TEXT and is the human-friendly format of the date.
         */
        public static final String COL_DATE = "date";

        /**
         * The ID of the icon to display for this weather entry.
         * This ID is that provided by the OpenWeatherMap API for the icon.
         * Column type is INTEGER.
         */
        public static final String COL_ICON_ID = "icon_id";

        /**
         * The high temperature for the day.
         * Column type is: REAL
         */
        public static final String COL_TEMP_HI = "temp_hi";

        /**
         * The low temperature for the day.
         * Column type is: REAL
         */
        public static final String COL_TEMP_LO = "temp_lo";

        /**
         * A very short description of the weather for the day (e.g. "Cloudy")
         * Column type is: TEXT
         */
        public static final String COL_DESCRIPTION = "description";

        /**
         * The humidity, as a percentage
         * Column type is: FLOAT
         */
        public static final String COL_HUMIDITY = "humidity";

        /**
         * The barometric pressure, as kilopascals (kPa)
         * Column type is: REAL
         */
        public static final String COL_PRESSURE = "pressure";

        /**
         * The wind speed, in kilometers per hour (kPh)
         * Column type is: REAL
         */
        public static final String COL_WIND_SPEED = "wind_speed";

        /**
         * The wind direction (e.g. "N" for north, "SSW" for south-south-west)
         * Column type is: TEXT
         */
        public static final String COL_WIND_DIRECTION = "wind_direction";

        private WeatherEntry() {
        }

        public static Uri buildUriFromId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriFromCityId(String cityId) {
            return CONTENT_URI.buildUpon().appendPath(cityId).build();
        }

        public static Uri buildUriFromCityIdAndStartDate(String cityId, String date) {
            final Uri baseUri = buildUriFromCityId(cityId);
            return baseUri.buildUpon().appendQueryParameter(COL_DATE, date).build();
        }

        public static Uri buildUriFromCityIdAndDate(String cityId, String date) {
            final Uri baseUri = buildUriFromCityId(cityId);
            return baseUri.buildUpon().appendPath(date).build();
        }

        public static String getCityIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COL_DATE);
        }
    }

    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LOCATION).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        /**
         * The name of this location, as it is displayed to users.
         * Column type is TEXT
         */
        public static final String COL_DISPLAY_NAME = "display_name";

        /**
         * The ID of this location's city, as sent to OpenWeatherMap.
         * Column type is TEXT
         */
        public static final String COL_CITY_ID = "city_id";

        /**
         * The latitude of this location.
         * Column type is INTEGER that references the ID column of a LocationEntry.
         */
        public static final String COL_LATITUDE = "latitude";

        /**
         * ID of the Location entry to which this weather data entry pertains.
         * Column type is INTEGER that references the ID column of a LocationEntry.
         */
        public static final String COL_LONGITUDE = "longitude";


        private LocationEntry() {
        }

        public static Uri buildUriFromId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriFromCityId(String cityId) {
            return CONTENT_URI.buildUpon().appendPath(cityId).build();
        }
    }

}

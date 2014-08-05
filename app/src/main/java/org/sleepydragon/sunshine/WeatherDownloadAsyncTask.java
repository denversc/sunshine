package org.sleepydragon.sunshine;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Downloads the weather data from the Internet.
 */
public class WeatherDownloadAsyncTask extends
        AsyncTask<Void, WeatherDownloadAsyncTask.Progress, WeatherDownloadAsyncTask.Result> {

    private final String mLocation;
    private final MeasurementUnits mMeasurementUnits;

    private String mErrorMessage;
    private String[] mWeatherData;
    private int mNumDownloadedChars;
    private String mLocationLatitude;
    private String mLocationLongitude;

    public WeatherDownloadAsyncTask(String location, MeasurementUnits measurementUnits) {
        if (location == null) {
            throw new NullPointerException("location==null");
        } else if (measurementUnits == null) {
            throw new NullPointerException("measurementUnits==null");
        }
        mLocation = location;
        mMeasurementUnits = measurementUnits;
    }

    @Override
    protected Result doInBackground(Void... params) {
        if (isCancelled()) {
            return null;
        }

        final String urlString = getUrl();
        final URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            mErrorMessage = e.getMessage();
            return Result.INVALID_URL;
        }

        publishProgress(Progress.CONNECTING);
        final URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
            return Result.CONNECT_FAILED;
        }

        final HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        return doInBackground(httpURLConnection);
    }

    private Result doInBackground(HttpURLConnection con) {
        if (isCancelled()) {
            return null;
        }

        final InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
            return Result.CONNECT_FAILED;
        }

        if (isCancelled()) {
            return null;
        }

        final Result result;
        try {
            result = doInBackground(in);
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
            return Result.DOWNLOAD_FAILED;
        } finally {
            con.disconnect();
        }

        return result;
    }

    private Result doInBackground(InputStream in) throws IOException {
        final InputStreamReader reader = new InputStreamReader(in, "utf8");
        final StringBuilder sb = new StringBuilder();
        final char[] buf = new char[1024];

        while (true) {
            if (isCancelled()) {
                break;
            }
            mNumDownloadedChars = sb.length();
            publishProgress(Progress.DOWNLOADING);
            final int readCount = reader.read(buf, 0, buf.length);
            if (readCount < 0) {
                break;
            }
            sb.append(buf, 0, readCount);
        }

        if (isCancelled()) {
            return null;
        }

        final String weatherDataJSONEncoded = sb.toString();
        try {
            mWeatherData = parseJSONEncodedWeatherData(weatherDataJSONEncoded);
        } catch (JSONException e) {
            mErrorMessage = e.getMessage();
            return Result.INVALID_DATA;
        }

        return Result.OK;
    }

    /**
     * Assembles and returns the URL that will be used to download the weather data.
     * @return the URL that will be used to download the weather data; never returns null.
     */
    public String getUrl() {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("api.openweathermap.org");
        builder.path("/data/2.5/forecast/daily");
        builder.appendQueryParameter("q", mLocation);
        builder.appendQueryParameter("mode", "json");
        builder.appendQueryParameter("units", "metric");
        builder.appendQueryParameter("cnt", "7");
        final Uri uri = builder.build();
        return uri.toString();
    }

    /**
     * The date/time conversion code is going to be moved outside the AsyncTask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        final Date date = new Date(time * 1000);
        final SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        final long roundedHigh = Math.round(high);
        final long roundedLow = Math.round(low);
        return formatTemperature(roundedHigh) + "/" + formatTemperature(roundedLow);
    }

    private String formatTemperature(long tempInCelsius) {
        final String suffix;
        final long value;
        switch (mMeasurementUnits) {
            case METRIC:
                suffix = "C";
                value = tempInCelsius;
                break;
            case IMPERIAL:
                suffix = "F";
                value = (tempInCelsius * 9 / 5) + 32;
                break;
            default:
                throw new RuntimeException("unsupported measurement units: " + mMeasurementUnits);
        }
        return value + "\u00B0" + suffix;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] parseJSONEncodedWeatherData(String forecastJsonStr) throws JSONException {
        final JSONObject forecastJson = new JSONObject(forecastJsonStr);
        final JSONArray weatherArray = forecastJson.getJSONArray("list");

        final JSONObject city = forecastJson.getJSONObject("city");
        if (city != null) {
            final JSONObject cityCoordinates = city.getJSONObject("coord");
            if (cityCoordinates != null) {
                mLocationLatitude = cityCoordinates.getString("lat");
                mLocationLongitude = cityCoordinates.getString("lon");
            }
        }

        final List<String> resultStrs = new ArrayList<>();
        for(int i = 0; i < weatherArray.length(); i++) {
            // Get the JSON object representing the day
            final JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            final long dateTime = dayForecast.getLong("dt");
            final String day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            final JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
            final String description = weatherObject.getString("main");

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            final JSONObject temperatureObject = dayForecast.getJSONObject("temp");
            final double high = temperatureObject.getDouble("max");
            final double low = temperatureObject.getDouble("min");

            final String highAndLow = formatHighLows(high, low);
            final String resultStr = day + " - " + description + " - " + highAndLow;
            resultStrs.add(resultStr);
        }

        return resultStrs.toArray(new String[resultStrs.size()]);
    }

    /**
     * Returns the message associated with an error that occurred in doInBackground().
     * <p>
     * This method is designed to be called by {@link #onPostExecute} if any result other than
     * {@link Result#OK} is passed in.  The message depends on the specific error and is not
     * generally suitable for displaying directly to the user, but rather is suitable for logging
     * to somewhere, such as to the logcat log.
     * @return the message associated with an error that occurred in doInBackground();
     * returns null if no error occurred.
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Returns the weather data downloaded by doInBackground().
     * <p>
     * This method is designed to be called by {@link #onPostExecute} if and only if
     * {@link Result#OK} is passed in.
     * @return the weather data downloaded by doInBackground(); returns null if no weather data
     * was downloaded.
     */
    public String[] getWeatherData() {
        return mWeatherData;
    }

    /**
     * Returns the latitude of the location whose weather data was retrieved by doInBackground().
     * <p>
     * This method is designed to be called by {@link #onPostExecute} if and only if
     * {@link Result#OK} is passed in.
     * @return the latitude of the location whose weather data was retrieved by doInBackground();
     * returns null no weather data was downloaded or the latitude could not be parsed.
     * @see #getLocationLongitude
     */
    public String getLocationLatitude() {
        return mLocationLatitude;
    }


    /**
     * Returns the longitude of the location whose weather data was retrieved by doInBackground().
     * <p>
     * This method is designed to be called by {@link #onPostExecute} if and only if
     * {@link Result#OK} is passed in.
     * @return the longitude of the location whose weather data was retrieved by doInBackground();
     * returns null no weather data was downloaded or the longitude could not be parsed.
     * @see #getLocationLatitude
     */
    public String getLocationLongitude() {
        return mLocationLongitude;
    }

    /**
     * Returns the number of characters (not bytes) that have been downloaded so far from the
     * weather service.
     * <p>
     * This method is designed to be called by {@link #onProgressUpdate} when specified the event
     * {@link Progress#DOWNLOADING}.  It can, however, be called at any point and will return the
     * correct value.
     * @return the amount of data, in the number of Unicode characters, that have been downloaded
     * so far from the weather service; returns 0 if download has not yet started.
     */
    public int getNumDownloadedChars() {
        return mNumDownloadedChars;
    }

    public enum Progress {
        CONNECTING,
        DOWNLOADING,
    }

    public enum Result {
        OK,
        INVALID_URL,
        CONNECT_FAILED,
        DOWNLOAD_FAILED,
        INVALID_DATA,
    }

}

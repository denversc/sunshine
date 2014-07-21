package org.sleepydragon.sunshine;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Downloads the weather data from the Internet.
 */
public class WeatherDownloadAsyncTask extends
        AsyncTask<Void, WeatherDownloadAsyncTask.Progress, WeatherDownloadAsyncTask.Result> {

    private String mErrorMessage;
    private String mWeatherData;
    private int mNumDownloadedChars;

    public WeatherDownloadAsyncTask() {
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
        final Result result = doInBackground(httpURLConnection);
        return result;
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

        mWeatherData = sb.toString();
        return Result.OK;
    }

    /**
     * Assembles and returns the URL that will be used to download the weather data.
     * @return the URL that will be used to download the weather data; never returns null.
     */
    public String getUrl() {
        return "http://api.openweathermap.org/data/2.5/forecast/daily?" +
                "q=Kitchener,ca&mode=json&units=metric&cnt=7";
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
    public String getWeatherData() {
        return mWeatherData;
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
    }

}

package org.sleepydragon.sunshine;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static org.sleepydragon.sunshine.Utils.LOG_TAG;

/**
 * A fragment showing the weather forecast.
 */
public class ForecastFragment extends Fragment {

    private WeatherDownloadAsyncTask mWeatherDownloadAsyncTask;
    private ArrayAdapter<String> mForecastAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        try {
            final WeatherDownloadAsyncTask task = mWeatherDownloadAsyncTask;
            mWeatherDownloadAsyncTask = null;
            if (task != null) {
                task.cancel(true);
            }
        } finally {
            super.onDestroy();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (mWeatherDownloadAsyncTask == null) {
                    mWeatherDownloadAsyncTask = new MyWeatherDownloadAsyncTask("Kitchener,ca");
                    mWeatherDownloadAsyncTask.execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setWeatherForecast(String[] weatherForecasts) {
        mForecastAdapter.clear();
        mForecastAdapter.addAll(weatherForecasts);
    }

    private class MyWeatherDownloadAsyncTask extends WeatherDownloadAsyncTask {

        public MyWeatherDownloadAsyncTask(String location) {
            super(location);
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            for (final Progress progress : values) {
                switch (progress) {
                    case CONNECTING:
                        Log.d(LOG_TAG, "WeatherDownloadAsyncTask connecting to " + getUrl());
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            mWeatherDownloadAsyncTask = null;
            if (result == null || isCancelled()) {
                Log.i(LOG_TAG, "WeatherDownloadAsyncTask download cancelled");
                return;
            }
            switch (result) {
                case OK:
                    Log.i(LOG_TAG, "WeatherDownloadAsyncTask download completed successfully");
                    final String[] weatherData = getWeatherData();
                    setWeatherForecast(weatherData);
                    break;
                default:
                    final String errorMessage = getErrorMessage();
                    Log.w(LOG_TAG, "WeatherDownloadAsyncTask download failed: " + errorMessage);
                    final String format = getText(R.string.msg_weather_download_failed).toString();
                    final String message = String.format(format, errorMessage);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    break;
            }
        }

    }

}
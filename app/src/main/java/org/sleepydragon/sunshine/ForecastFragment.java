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

import java.util.ArrayList;
import java.util.List;

import static org.sleepydragon.sunshine.Utils.LOG_TAG;

/**
 * A fragment showing the weather forecast.
 */
public class ForecastFragment extends Fragment {

    private WeatherDownloadAsyncTask mWeatherDownloadAsyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);
        populateListViewData(rootView);
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

    private void populateListViewData(View rootView) {
        List<String> items = new ArrayList<String>();
        items.add("Today - Sunny - 88 / 63");
        items.add("Tomorrow - Foggy - 70 / 46");
        items.add("Weds - Cloudy - 72 / 63");
        items.add("Thurs - Rainy - 64 / 51");
        items.add("Fri - Foggy - 70 / 46");
        items.add("Sat - Sunny - 76 / 68");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, items);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
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
                        final String url = getUrl();
                        Log.i(LOG_TAG, "WeatherDownloadAsyncTask connecting to " + url);
                        break;
                    case DOWNLOADING:
                        final int numDownloadedChars = getNumDownloadedChars();
                        Log.i(LOG_TAG, "WeatherDownloadAsyncTask downloaded " + numDownloadedChars);
                        break;
                }
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            mWeatherDownloadAsyncTask = null;
            if (isCancelled()) {
                Log.i(LOG_TAG, "WeatherDownloadAsyncTask download cancelled");
            }
            if (result == null) {
                return;
            }
            switch (result) {
                case OK:
                    final String weatherData = getWeatherData();
                    Log.i(LOG_TAG, "WeatherDownloadAsyncTask downloaded data: " + weatherData);
                    break;
                default:
                    final String errorMessage = getErrorMessage();
                    Log.w(LOG_TAG, "WeatherDownloadAsyncTask download failed: " + errorMessage);
                    break;
            }
        }

    }

}
package org.sleepydragon.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static org.sleepydragon.sunshine.Utils.LOG_TAG;

/**
 * A fragment showing the weather forecast.
 */
public class ForecastFragment extends Fragment {

    private final MyOnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    private static final int WHAT_SHOW_MAP = 1;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private WeatherDownloadAsyncTask mWeatherDownloadAsyncTask;
    private LoadSharedPreferencesAsyncTask mLoadSharedPreferencesAsyncTask;
    private ArrayAdapter<String> mForecastAdapter;
    private SharedPreferences mSharedPreferences;
    private MeasurementUnits mMeasurementUnits;
    private String mKeyMeasurementUnits;
    private String mKeyLocation;
    private LocationCoordinates mLocationCoordinates;
    private boolean mDestroyed;

    public ForecastFragment() {
        mOnSharedPreferenceChangeListener = new MyOnSharedPreferenceChangeListener();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mKeyMeasurementUnits = getString(R.string.pref_units_key);
        mKeyLocation = getString(R.string.pref_location_key);
        mLoadSharedPreferencesAsyncTask = new LoadSharedPreferencesAsyncTask();
        mLoadSharedPreferencesAsyncTask.execute();

        mHandlerThread = new HandlerThread("ForecastFragment Handler Thread");
        mHandlerThread.start();
        final Looper handlerLooper = mHandlerThread.getLooper();
        final Handler.Callback handlerCallback = new MyHandlerCallback();
        mHandler = new Handler(handlerLooper, handlerCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new ForecastListItemClickedListener());

        return rootView;
    }

    @Override
    public void onDestroy() {
        try {
            mDestroyed = true;
            if (mWeatherDownloadAsyncTask != null) {
                mWeatherDownloadAsyncTask.cancel(true);
            }
            if (mLoadSharedPreferencesAsyncTask != null) {
                mLoadSharedPreferencesAsyncTask.cancel(true);
            }
            if (mSharedPreferences != null) {
                mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
            }
            if (mHandlerThread != null) {
                mHandlerThread.quit();
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
                onOptionItemRefreshSelected(false);
                return true;
            case R.id.action_show_location_on_map:
                onOptionItemShowLocationOnMapSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onOptionItemRefreshSelected(boolean force) {
        if (mSharedPreferences == null) {
            return;
        }
        if (mWeatherDownloadAsyncTask == null || force) {
            if (mWeatherDownloadAsyncTask != null) {
                mWeatherDownloadAsyncTask.cancel(true);
            }
            final String location = mSharedPreferences.getString(mKeyLocation, "Kitchener,on");
            mWeatherDownloadAsyncTask = new MyWeatherDownloadAsyncTask(location, mMeasurementUnits);
            mWeatherDownloadAsyncTask.execute();
        }
    }

    private void onOptionItemShowLocationOnMapSelected() {
        mHandler.sendEmptyMessage(WHAT_SHOW_MAP);
    }

    private void doShowLocationOnMap() {
        final LocationCoordinates coordinates = mLocationCoordinates;
        if (coordinates == null) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("geo:0,0?q=");
        sb.append(coordinates.latitude);
        sb.append(',');
        sb.append(coordinates.longitude);

        final String location;
        if (mSharedPreferences == null) {
            location = null;
        } else {
            final String locationKey = getString(R.string.pref_location_key);
            location = mSharedPreferences.getString(locationKey, null);
        }
        if (location != null) {
            sb.append('(').append(Uri.encode(location)).append(')');
        }

        final String uriString = sb.toString();
        final Uri uri = Uri.parse(uriString);
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.msg_no_maps_app, Toast.LENGTH_LONG).show();
        }
    }


    private void setWeatherForecast(String[] weatherForecasts, LocationCoordinates locationCoordinates) {
        mLocationCoordinates = locationCoordinates;
        mForecastAdapter.clear();
        mForecastAdapter.addAll(weatherForecasts);
    }

    private class MyWeatherDownloadAsyncTask extends WeatherDownloadAsyncTask {

        public MyWeatherDownloadAsyncTask(String location, MeasurementUnits measurementUnits) {
            super(location, measurementUnits);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().setProgressBarIndeterminateVisibility(true);
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
            if (mDestroyed) {
                return;
            }
            getActivity().setProgressBarIndeterminateVisibility(false);
            mWeatherDownloadAsyncTask = null;
            if (result == null || isCancelled()) {
                Log.i(LOG_TAG, "WeatherDownloadAsyncTask download cancelled");
                return;
            }
            switch (result) {
                case OK:
                    Log.i(LOG_TAG, "WeatherDownloadAsyncTask download completed successfully");
                    final String[] weatherData = getWeatherData();
                    final String lat = getLocationLatitude();
                    final String lon = getLocationLongitude();
                    final LocationCoordinates locationCoordinates;
                    if (lat == null || lon == null) {
                        locationCoordinates = null;
                    } else {
                        locationCoordinates = new LocationCoordinates(lat, lon);
                    }
                    setWeatherForecast(weatherData, locationCoordinates);
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

    private class ForecastListItemClickedListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final CharSequence forecastText = ((TextView) view).getText();
            final Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, forecastText);
            startActivity(intent);
        }
    }

    private class LoadSharedPreferencesAsyncTask extends AsyncTask<Void, Void, SharedPreferences> {

        @Override
        protected SharedPreferences doInBackground(Void... params) {
            return PreferenceManager.getDefaultSharedPreferences(getActivity());
        }

        @Override
        protected void onPostExecute(SharedPreferences prefs) {
            if (! mDestroyed) {
                mLoadSharedPreferencesAsyncTask = null;
                mSharedPreferences = prefs;
                prefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
                mOnSharedPreferenceChangeListener.onSharedPreferenceChanged(prefs, mKeyMeasurementUnits);
            }
        }
    }

    private class MyOnSharedPreferenceChangeListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(mKeyMeasurementUnits)) {
                final String value = prefs.getString(key, null);
                final String valueImperial = getString(R.string.prefs_units_entry_value_imperial);
                final MeasurementUnits origMeasurementUnits = mMeasurementUnits;
                if (valueImperial.equals(value)) {
                    mMeasurementUnits = MeasurementUnits.IMPERIAL;
                } else {
                    mMeasurementUnits = MeasurementUnits.METRIC;
                }
                if (mMeasurementUnits != origMeasurementUnits) {
                    onOptionItemRefreshSelected(true);
                }
            } else if (key.equals(mKeyLocation)) {
                onOptionItemRefreshSelected(true);
            }
        }
    }

    private static class LocationCoordinates {
        public final String latitude;
        public final String longitude;

        public LocationCoordinates(String latitude, String longitude) {
            if (latitude == null) {
                throw new NullPointerException("latitude==null");
            } else if (longitude == null) {
                throw new NullPointerException("longitude==null");
            }
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private class MyHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SHOW_MAP:
                    doShowLocationOnMap();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }
}
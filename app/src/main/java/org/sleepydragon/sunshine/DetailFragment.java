package org.sleepydragon.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailFragment extends Fragment {

    private CharSequence mForecastText;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_detail, menu);

        final MenuItem shareMenuItem = menu.findItem(R.id.action_share);
        final StrictMode.ThreadPolicy origThreadPolicy = StrictMode.allowThreadDiskReads();
        try {
            StrictMode.allowThreadDiskWrites();
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
            mShareActionProvider.setShareHistoryFileName("shared_weather_history.xml");
            setShareIntent();
        } finally {
            StrictMode.setThreadPolicy(origThreadPolicy);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        final TextView textView = (TextView) rootView.findViewById(R.id.text);
        final CharSequence forecastText = getActivity().getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
        textView.setText(forecastText);
        mForecastText = forecastText;
        return rootView;
    }

    private void setShareIntent() {
        if (mShareActionProvider == null || mForecastText == null) {
            return;
        }
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastText);
        mShareActionProvider.setShareIntent(shareIntent);
    }
}

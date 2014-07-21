package org.sleepydragon.sunshine;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment showing the weather forecast.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        populateListViewData(rootView);
        return rootView;
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
}
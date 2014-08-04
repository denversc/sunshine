package org.sleepydragon.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        final TextView textView = (TextView) rootView.findViewById(R.id.text);
        final CharSequence forecastText = getActivity().getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
        textView.setText(forecastText);
        return rootView;
    }

}

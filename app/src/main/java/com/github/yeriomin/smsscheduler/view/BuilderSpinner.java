package com.github.yeriomin.smsscheduler.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.yeriomin.smsscheduler.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BuilderSpinner extends Builder {

    protected Map<String, Integer> keys = new HashMap<>();
    protected List<String> values = new ArrayList<>();

    abstract protected boolean shouldBeVisible();
    abstract protected void onAdapterItemSelected(AdapterView<?> parent, View view, int position, long id);
    abstract protected int getSelection();

    @Override
    protected Spinner getView() {
        return (Spinner) view;
    }

    @Override
    public View build() {
        if (shouldBeVisible()) {
            getView().setVisibility(View.VISIBLE);
            getView().setAdapter(getAdapter());
            getView().setSelection(getSelection(), false);
            getView().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onAdapterItemSelected(parent, view, position, id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            getView().setVisibility(View.GONE);
        }
        return getView();
    }

    protected ArrayAdapter getAdapter() {
        return new ArrayAdapter<>(
            activity,
            R.layout.spinner_item,
            new ArrayList<>(values)
        );
    }
}

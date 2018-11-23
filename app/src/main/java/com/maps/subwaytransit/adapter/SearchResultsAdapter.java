package com.maps.subwaytransit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maps.subwaytransit.R;
import com.maps.subwaytransit.model.SearchResults;

import java.util.ArrayList;

public class SearchResultsAdapter extends ArrayAdapter<SearchResults> {
    private Context context;

    private ArrayList<SearchResults> resultsArrayList = new ArrayList<>();

    public SearchResultsAdapter(Context context, ArrayList<SearchResults> resultsArrayList) {
        super(context, 0, resultsArrayList);
        this.context = context;
        this.resultsArrayList = resultsArrayList;
    }

    @NonNull
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_search_result, parent, false);

        TextView placeName = (TextView) view.findViewById(R.id.place_name);
        TextView placeAddress = (TextView) view.findViewById(R.id.place_address);

        placeName.setText(resultsArrayList.get(position).getTitle());
        placeAddress.setText(resultsArrayList.get(position).getAddress());

        return view;
    }

    @Override
    public int getCount() {
        return resultsArrayList.size();
    }
}

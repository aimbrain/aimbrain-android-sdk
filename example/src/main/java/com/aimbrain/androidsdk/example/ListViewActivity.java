package com.aimbrain.androidsdk.example;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.aimbrain.androidsdk.R;
import com.aimbrain.androidsdk.library.ABListActivity;

public class ListViewActivity extends ABListActivity {
    String[] items;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        items = new String[50];
        for (int i = 0; i < 50; i ++ ) {
            items[i] = "Item " + String.valueOf(i);
        }
        adapter = new ArrayAdapter<>(this, R.layout.item_layout, items);

        setListAdapter(adapter);
    }


}

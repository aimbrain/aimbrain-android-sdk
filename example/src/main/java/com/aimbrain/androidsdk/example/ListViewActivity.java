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

        items = new String[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6",
                "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12", "Item 13", "Item 14", "Item 15"};
        adapter = new ArrayAdapter<>(this, R.layout.item_layout, items);

        setListAdapter(adapter);
    }


}

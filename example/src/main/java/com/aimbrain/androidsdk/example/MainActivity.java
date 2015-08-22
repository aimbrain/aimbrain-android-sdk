package com.aimbrain.androidsdk.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aimbrain.androidsdk.R;
import com.aimbrain.androidsdk.library.ABActivity;
import com.aimbrain.androidsdk.library.AuthAsyncResponse;
import com.aimbrain.androidsdk.library.AuthAsyncResponseHandler;
import com.aimbrain.androidsdk.library.AuthLibrary;
import com.aimbrain.androidsdk.library.EventStore;

public class MainActivity extends ABActivity {
    private static final String API_KEY = "demoapi"; // Returns random score for API testing
    private static final String USER_ID = "demouser"; // Returns random score for API testing
    private TextView mText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById(R.id.main_text);
        mText.setText("Try interacting with the app...");
    }

    // Send all gathered events to the AimBrain server
    public void sendEvents(View view) {

        Log.d("Events", EventStore.getEvents());
//        // Create AimBrain AuthLibrary object
//        AuthLibrary abo = new AuthLibrary(API_KEY);
//
//        // Do asynchronous call to AimBrain's API for authentication
//        abo.getAuthAsync(USER_ID, new AuthAsyncResponseHandler() {
//            @Override
//            public void onSuccess(AuthAsyncResponse response) {
//                String msg = "Got reply from AimBrain's servers...\n";
//
//                msg += "\nStatus code: " + String.valueOf(response.statusCode) + "\n";
//                msg += "\nReply body: " + response.body + "\n";
//
//                msg += "\nScore: " + String.valueOf(response.score) + "\n";
//                msg += "ID: " + response.id + "\n";
//                msg += "Method: " + response.method + "\n";
//                msg += "Nonce: " + String.valueOf(response.nonce) + "\n";
//
//                Log.d(this.toString(), msg);
//                mText.setText(msg);
//            }
//
//            @Override
//            public void onFailure(int statusCode, String message, Throwable e) {
//                String msg = "Failed to get reply from AimBrain's servers: " + message +
//                        " (" + String.valueOf(statusCode) + "), exception: " + e.getMessage();
//                Log.d(this.toString(), msg);
//                mText.setText(msg);
//            }
//
//        });
    }

    public void startListView(View view) {
        Intent intent = new Intent(this, ListViewActivity.class);
        startActivity(intent);
    }
}

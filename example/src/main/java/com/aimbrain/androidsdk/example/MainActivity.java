package com.aimbrain.androidsdk.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.aimbrain.androidsdk.library.AuthAsyncResponse;
import com.aimbrain.androidsdk.library.AuthAsyncResponseHandler;
import com.aimbrain.androidsdk.library.AuthLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity {
    private static final String API_KEY = "demoapi"; // Returns random score for API testing
    private static final String USER_ID = "demouser"; // Returns random score for API testing
    private static final Integer MAX_EVENTS_LIST_SIZE = 1000; // To avoid running out of memory

    private Context mContext;
    private TextView mText;
    private Integer mCompleteTouchEvents;
    private List<MotionEvent> mEventsList;


    public MainActivity() {
        mContext = this;
        mCompleteTouchEvents = 0;
        mEventsList = Collections.synchronizedList(new ArrayList<MotionEvent>());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mText = new TextView(mContext);
        setContentView(mText);

        mText.setText("Generate 3 complete touch events by swiping, tapping, etc to test demo of AimBrain's authentication API.");
    }

    @Override
    // Catch events before all View elements
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        // Remove oldest events if we are over capacity
        while (mEventsList.size() > MAX_EVENTS_LIST_SIZE - 1) {
            mEventsList.remove(0);
        }

        // Add a copy of newest event
        mEventsList.add(MotionEvent.obtain(event));

        // Count how many complete (down -> move -> up) events were observed
        if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                mCompleteTouchEvents++;
        }

        // Try authenticating the user every 3 complete touch events
        // with all observed events while the app was running
        if (mCompleteTouchEvents >= 3) {
            mCompleteTouchEvents = 0;
            String msg = "Requesting authentication from AimBrain's servers...";
            Log.d(this.toString(), msg);
            mText.setText(msg);

            authExample(API_KEY, USER_ID, mEventsList);
        }

        // Pass events down the stack
        return super.dispatchTouchEvent(event);
    }

    public void authExample(String apikey, String userid, List<MotionEvent> events) {
        // Create AimBrain AuthLibrary object
        AuthLibrary abo = new AuthLibrary(apikey, "homescreen_context");

        // Do asynchronous call to AimBrain's API for authentication
        abo.getAuthAsync(userid, events, new AuthAsyncResponseHandler() {
            @Override
            public void onSuccess(AuthAsyncResponse response) {
                String msg = "Got reply from AimBrain's servers...\n";

                msg += "\nStatus code: " + String.valueOf(response.statusCode) + "\n";
                msg += "\nReply body: " + response.body + "\n";

                msg += "\nScore: " + String.valueOf(response.score) + "\n";
                msg += "ID: " + response.id + "\n";
                msg += "Method: " + response.method + "\n";
                msg += "Nonce: " + String.valueOf(response.nonce) + "\n";

                Log.d(this.toString(), msg);
                mText.setText(msg);
            }

            @Override
            public void onFailure(int statusCode, String message, Throwable e) {
                String msg = "Failed to get reply from AimBrain's servers: " + message +
                        " (" + String.valueOf(statusCode) + "), exception: " + e.getMessage();
                Log.d(this.toString(), msg);
                mText.setText(msg);
            }

        });
    }
}

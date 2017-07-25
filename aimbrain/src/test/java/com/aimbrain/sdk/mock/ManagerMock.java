package com.aimbrain.sdk.mock;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.privacy.TouchTrackingGuard;
import com.aimbrain.sdk.server.Server;

import java.util.ArrayList;


public class ManagerMock extends Manager {

    public ManagerMock(Server server) {
        super();
        this.server = server;
    }

    public ManagerMock() {
    }

    public ArrayList<PrivacyGuard> getPrivacyGuards() {
        return privacyGuards;
    }

    public ArrayList<TouchTrackingGuard> getTouchTrackingGuards() {
        return touchTrackingGuards;
    }
}

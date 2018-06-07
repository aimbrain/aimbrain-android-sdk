package com.aimbrain.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.test.mock.MockContext;
import android.view.View;

import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.mock.ManagerMock;
import com.aimbrain.sdk.models.FaceTokenType;
import com.aimbrain.sdk.models.SerializedRequest;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.models.VoiceTokenType;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.privacy.TouchTrackingGuard;
import com.aimbrain.sdk.server.AMBNResponseErrorListener;
import com.aimbrain.sdk.server.FaceActions;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.FaceCapturesCallback;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.aimbrain.sdk.server.FaceCompareCallback;
import com.aimbrain.sdk.server.FaceTokenCallback;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.Server;
import com.aimbrain.sdk.server.SessionCallback;
import com.aimbrain.sdk.server.VoiceActions;
import com.aimbrain.sdk.server.VoiceCaptureEnrollCallback;
import com.aimbrain.sdk.server.VoiceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.VoiceCapturesCallback;
import com.aimbrain.sdk.server.VoiceTokenCallback;

import org.junit.Test;
import org.mockito.Mock;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ManagerUnitTest {


    @Mock
    Server server = mock(Server.class);
    @Mock
    SessionCallback sessionCallback = mock(SessionCallback.class);
    @Mock
    SessionModel sesionModel = mock(SessionModel.class);
    @Mock
    AMBNResponseErrorListener errorListener = mock(AMBNResponseErrorListener.class);
    @Mock
    MockContext context = new MockContext();
    @Mock
    SerializedRequest serializedRequest = mock(SerializedRequest.class);
    @Mock
    ScoreCallback scoreCallback = mock(ScoreCallback.class);

    public String userId = "test123";


    @Test
    public void testCreateSession() throws ConnectException, InternalException {
        ManagerMock manager = new ManagerMock(server);
        manager.createSession(userId, context, sessionCallback);
        verify(server, times(1)).createSession(any(String.class), any(byte[].class), any(Context.class), any(SessionCallback.class), any(AMBNResponseErrorListener.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSessionWithoutServer() throws ConnectException, InternalException {
        ManagerMock manager = new ManagerMock(null);
        manager.createSession(userId, context, sessionCallback);
    }

    @Test
    public void testCreateSessionWithErrorListener() throws ConnectException, InternalException {
        ManagerMock manager = new ManagerMock(server);
        manager.createSession(userId, context, sessionCallback, errorListener);
        verify(server, times(1)).createSession(any(String.class), any(byte[].class), any(Context.class), any(SessionCallback.class), any(AMBNResponseErrorListener.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSessionWithErrorListenerWithoutServer() throws ConnectException, InternalException {
        ManagerMock manager = new ManagerMock(null);
        manager.createSession(userId, context, sessionCallback, errorListener);
    }

    @Test
    public void testGetSerializedCreateSession() throws InternalException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedCreateSession(userId, null, context);
        verify(server, times(1)).getSerializedCreateSession(any(String.class), any(byte[].class), any(Context.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedCreateSessionWithoutServer() throws ConnectException, InternalException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedCreateSession(userId, null, context);
    }

    @Test(expected = IllegalStateException.class)
    public void testSubmitCollectedData() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.submitCollectedData(scoreCallback);
    }

    @Test
    public void testGetCurrentScore() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getCurrentScore(scoreCallback);
        verify(server, times(1)).getCurrentScore(any(byte[].class), any(ScoreCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCurrentScoreWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getCurrentScore(scoreCallback);
    }

    @Test
    public void testGetSerializedGetCurrentScore() throws InternalException, SessionException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedGetCurrentScore(null);
        verify(server, times(1)).getSerializedGetCurrentScore(any(byte[].class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedGetCurrentScoreWithoutServer() throws ConnectException, InternalException, SessionException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedGetCurrentScore(null);
    }

    @Test
    public void testSendProvidedFaceCapturesToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedFaceCapturesToEnroll(getMockBitmapList(), mock(FaceCapturesEnrollCallback.class));
        verify(server, times(1)).sendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceCapturesCallback.class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedFaceCapturesToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedFaceCapturesToEnroll(getMockBitmapList(), mock(FaceCapturesEnrollCallback.class));
    }

    @Test
    public void testGetSerializedSendProvidedFaceCapturesToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedFaceCapturesToEnroll(getMockBitmapList(), null);
        verify(server, times(1)).getSerializedSendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedFaceCapturesToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedFaceCapturesToEnroll(getMockBitmapList(), null);
    }

    @Test
    public void testSendProvidedFaceCapturesToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedFaceCapturesToAuthenticate(getMockBitmapList(), mock(FaceCapturesAuthenticateCallback.class));
        verify(server, times(1)).sendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceCapturesCallback.class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedFaceCapturesToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedFaceCapturesToAuthenticate(getMockBitmapList(), mock(FaceCapturesAuthenticateCallback.class));
    }

    @Test
    public void testGetSerializedSendProvidedFaceCapturesToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedFaceCapturesToAuthenticate(getMockBitmapList(), null);
        verify(server, times(1)).getSerializedSendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedFaceCapturesToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedFaceCapturesToAuthenticate(getMockBitmapList(), null);
    }

    @Test
    public void testSendProvidedFaceVideoToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedFaceCapturesToEnroll(new byte[10], mock(FaceCapturesEnrollCallback.class));
        verify(server, times(1)).sendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceCapturesCallback.class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedFaceVideoToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedFaceCapturesToEnroll(new byte[10], mock(FaceCapturesEnrollCallback.class));
    }

    @Test
    public void testGetSerializedSendProvidedFaceVideoToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedFaceCapturesToEnroll(new byte[10], null);
        verify(server, times(1)).getSerializedSendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedFaceVideoToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedFaceCapturesToEnroll(new byte[10], null);
    }

    @Test
    public void testGetSerializedSendProvidedFaceVideosToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedFaceCapturesToAuthenticate(new byte[10], null);
        verify(server, times(1)).getSerializedSendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedFaceVideoToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedFaceCapturesToAuthenticate(new byte[10], null);
    }

    @Test
    public void testSendProvidedFaceVideoToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedFaceCapturesToAuthenticate(new byte[10], mock(FaceCapturesAuthenticateCallback.class));
        verify(server, times(1)).sendProvidedFaceCaptures(any(StringListDataModel.class), any(byte[].class), any(FaceCapturesCallback.class), any(FaceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedFaceVideoToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedFaceCapturesToAuthenticate(new byte[10], mock(FaceCapturesAuthenticateCallback.class));
    }

    @Test
    public void testSendProvidedVoiceCapturesToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedVoiceCapturesToEnroll(new byte[10], mock(VoiceCaptureEnrollCallback.class));
        verify(server, times(1)).sendProvidedVoiceCaptures(any(StringListDataModel.class), any(byte[].class), any(VoiceCaptureEnrollCallback.class), any(VoiceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedVoiceCapturesToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedVoiceCapturesToEnroll(new byte[10], mock(VoiceCaptureEnrollCallback.class));
    }

    @Test
    public void testGetSerializedSendProvidedVoiceCapturesToEnroll() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedVoiceCapturesToEnroll(new byte[10], null);
        verify(server, times(1)).getSerializedSendProvidedVoiceCaptures(any(StringListDataModel.class), any(byte[].class), any(VoiceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedVoiceCapturesToEnrollWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedVoiceCapturesToEnroll(new byte[10], null);
    }

    @Test
    public void testGetSerializedSendProvidedVoiceCapturesToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedSendProvidedVoiceCapturesToAuthenticate(new byte[10], null);
        verify(server, times(1)).getSerializedSendProvidedVoiceCaptures(any(StringListDataModel.class), any(byte[].class), any(VoiceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedSendProvidedVoiceCapturesToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedSendProvidedVoiceCapturesToAuthenticate(new byte[10], null);
    }

    @Test
    public void testSendProvidedVoiceCapturesToAuthenticate() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.sendProvidedVoiceCapturesToAuthenticate(new byte[10], mock(VoiceCapturesAuthenticateCallback.class));
        verify(server, times(1)).sendProvidedVoiceCaptures(any(StringListDataModel.class), any(byte[].class), any(VoiceCapturesCallback.class), any(VoiceActions.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendProvidedVoiceCapturesToAuthenticateWithoutServer() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.sendProvidedVoiceCapturesToAuthenticate(new byte[10], mock(VoiceCapturesAuthenticateCallback.class));
    }

    @Test
    public void testGetFaceToken() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getFaceToken(null, null);
        verify(server, times(1)).getFaceToken(any(FaceTokenType.class), any(byte[].class), any(FaceTokenCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetFaceTokenWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getFaceToken(null, null);
    }

    @Test
    public void testGetSerializedFaceToken() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedFaceToken(null, null);
        verify(server, times(1)).getSerializedFaceToken(any(FaceTokenType.class), any(byte[].class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedFaceTokenWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedFaceToken(null, null);
    }

    @Test
    public void testGetVoiceToken() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getVoiceToken(null, null);
        verify(server, times(1)).getVoiceToken(any(VoiceTokenType.class), any(byte[].class), any(VoiceTokenCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetVoiceTokenWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getVoiceToken(null, null);
    }

    @Test
    public void testGetSerializedVoiceToken() throws SessionException, InternalException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedVoiceToken(null, null);
        verify(server, times(1)).getSerializedVoiceToken(any(VoiceTokenType.class), any(byte[].class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedVoiceTokenWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedVoiceToken(null, null);
    }

    @Test
    public void testEncodePhotos() {
        ManagerMock manager = new ManagerMock(server);
        StringListDataModel stringListDataModel = manager.encodePhotos(getMockBitmapList());
        assertTrue("String data modet can't be null", stringListDataModel != null);
        assertEquals(stringListDataModel.getData().size(), 3);
    }

    @Test
    public void testEncodeVideo() {
        ManagerMock manager = new ManagerMock(server);
        StringListDataModel stringListDataModel = manager.encodeVideo(new byte[10]);
        assertTrue("String data modet can't be null", stringListDataModel != null);
        assertFalse(stringListDataModel.getData().isEmpty());
    }

    @Test
    public void testAddPrivacyGuard() {
        ManagerMock manager = new ManagerMock(server);
        PrivacyGuard privacyGuard = mock(PrivacyGuard.class);
        manager.addPrivacyGuard(privacyGuard);
        assertTrue(manager.getPrivacyGuards() != null);
        assertEquals(1, manager.getPrivacyGuards().size());
        assertEquals(manager.getPrivacyGuards().get(0), privacyGuard);
    }

    @Test
    public void testRemovePrivacyGuard() {
        ManagerMock manager = new ManagerMock(server);
        PrivacyGuard privacyGuard = mock(PrivacyGuard.class);
        manager.addPrivacyGuard(privacyGuard);
        assertTrue(manager.getPrivacyGuards() != null);
        assertEquals(1, manager.getPrivacyGuards().size());
        assertEquals(manager.getPrivacyGuards().get(0), privacyGuard);
        manager.removePrivacyGuard(privacyGuard);
        assertEquals(0, manager.getPrivacyGuards().size());
    }

    @Test
    public void testAddTouchTrackingGuard() {
        ManagerMock manager = new ManagerMock(server);
        TouchTrackingGuard touchTrackingGuard = mock(TouchTrackingGuard.class);
        manager.addTouchTrackingGuard(touchTrackingGuard);
        assertTrue(manager.getTouchTrackingGuards() != null);
        assertEquals(1, manager.getTouchTrackingGuards().size());
        assertEquals(manager.getTouchTrackingGuards().get(0), touchTrackingGuard);
    }

    @Test
    public void testIsTouchTrackingIgnored() {
        ManagerMock manager = new ManagerMock(server);
        Set<View> views = new HashSet<>();
        View view = mock(View.class);
        views.add(view);
        TouchTrackingGuard touchTrackingGuard = new TouchTrackingGuard(views);
        manager.addTouchTrackingGuard(touchTrackingGuard);
        assertTrue(manager.isTouchTrackingIgnored(view));
        assertFalse(manager.isTouchTrackingIgnored(mock(View.class)));
    }

    @Test
    public void testConfigureWithSessionId() {
        ManagerMock manager = new ManagerMock();
        String sessionId = "session123";
        manager.configure(sessionId);
        assertTrue(manager.server != null);
        assertFalse(manager.server.isConfiguredForApiCalls());
        assertEquals(sessionId, manager.server.getSession().getSessionId());
    }

    @Test
    public void testIsConfiguredForApiCalls() {
        Server server = mock(Server.class);
        when(server.isConfiguredForApiCalls()).thenReturn(true).thenReturn(false);
        ManagerMock manager = new ManagerMock(server);
        assertTrue(manager.isConfiguredForApiCalls());
        assertFalse(manager.isConfiguredForApiCalls());
    }

    @Test
    public void testCompareFacesPhotos() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.compareFacesPhotos(getMockBitmapList(), getMockBitmapList(), mock(FaceCompareCallback.class));
        verify(server, times(1)).compareFaces(any(StringListDataModel.class), any(StringListDataModel.class), any(byte[].class), any(FaceCompareCallback.class));
    }

    @Test
    public void testCompareFacesPhotosWithMetadata() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(server);
        manager.compareFacesPhotos(getMockBitmapList(), getMockBitmapList(), new byte[10], mock(FaceCompareCallback.class));
        verify(server, times(1)).compareFaces(any(StringListDataModel.class), any(StringListDataModel.class), any(byte[].class), any(FaceCompareCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testCompareFacesPhotosWithoutServer() throws InternalException, SessionException, ConnectException {
        ManagerMock manager = new ManagerMock(null);
        manager.compareFacesPhotos(getMockBitmapList(), getMockBitmapList(), mock(FaceCompareCallback.class));
        verify(server, times(1)).compareFaces(any(StringListDataModel.class), any(StringListDataModel.class), any(byte[].class), any(FaceCompareCallback.class));
    }

    @Test
    public void testGetSerializedCompareFacesPhotos() throws InternalException, SessionException {
        ManagerMock manager = new ManagerMock(server);
        manager.getSerializedCompareFacesPhotos(getMockBitmapList(), getMockBitmapList(), new byte[10]);
        verify(server, times(1)).getSerializedCompareFaces(any(StringListDataModel.class), any(StringListDataModel.class), any(byte[].class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSerializedCompareFacesPhotosWithoutServer() throws InternalException, SessionException {
        ManagerMock manager = new ManagerMock(null);
        manager.getSerializedCompareFacesPhotos(getMockBitmapList(), getMockBitmapList(), new byte[10]);
        verify(server, times(1)).getSerializedCompareFaces(any(StringListDataModel.class), any(StringListDataModel.class), any(byte[].class));
    }

    private List<Bitmap> getMockBitmapList() {
        List<Bitmap> bitmaps = new ArrayList<>();
        Bitmap mockBitmap = mock(Bitmap.class);
        bitmaps.add(mockBitmap);
        bitmaps.add(mockBitmap);
        bitmaps.add(mockBitmap);
        return bitmaps;
    }
}
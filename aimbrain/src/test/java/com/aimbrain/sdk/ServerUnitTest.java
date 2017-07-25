package com.aimbrain.sdk;

import android.content.Context;
import android.os.Build;
import android.test.mock.MockContext;
import android.view.Display;
import android.view.WindowManager;

import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.helper.Base64Helper;
import com.aimbrain.sdk.mock.ServerMock;
import com.aimbrain.sdk.models.BehaviouralDataModel;
import com.aimbrain.sdk.models.FaceCompareModel;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.models.SerializedRequest;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.StringListDataModel;
import com.aimbrain.sdk.models.VoiceTokenModel;
import com.aimbrain.sdk.models.VoiceTokenType;
import com.aimbrain.sdk.server.FaceActions;
import com.aimbrain.sdk.server.FaceCompareCallback;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.SessionCallback;
import com.aimbrain.sdk.server.VoiceActions;
import com.aimbrain.sdk.server.VoiceTokenCallback;
import com.aimbrain.sdk.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ServerUnitTest {

    @Mock
    private SessionModel sessionModel = mock(SessionModel.class);

    @Mock
    private WindowManager windowManager = mock(WindowManager.class);

    @Mock
    private Display display = mock(Display.class);

    @Mock
    private Base64Helper base64 = mock(Base64Helper.class);

    @Mock
    private MockContext mockContext = mock(MockContext.class);

    @Mock
    private StringListDataModel stringListDataModel = mock(StringListDataModel.class);

    private String userId = "user123";

    private byte[] metadata = "metadata_test".getBytes();


    @Before
    public void setUp() throws Exception {
        when(base64.encodeToString(any(byte[].class), any(int.class))).thenReturn("test data");
        when(base64.decode(any(String.class), any(int.class))).thenReturn("metadata_test".getBytes());
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);
        when(windowManager.getDefaultDisplay()).thenReturn(display);
        when(sessionModel.getSessionId()).thenReturn("test session id");
        String photosString = "[" +
                "\"photo1\"," +
                "\"photo2\"," +
                "\"photo3\"]";
        when(stringListDataModel.toJSON()).thenReturn(new JSONArray(photosString));

        setFinalStaticFieldValue(Build.VERSION.class.getField("RELEASE"), "live_version");
        setFinalStaticFieldValue(android.os.Build.class.getField("MODEL"), "one_plus");

    }

    private void setFinalStaticFieldValue(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    @Test
    public void testIsConfiguredForApiCalls() {
        ServerMock server = new ServerMock(sessionModel);
        server.setApiAllowed(true);
        assertTrue(server.isConfiguredForApiCalls());
    }

    @Test
    public void testIsNotConfiguredForApiCalls() {
        ServerMock server = new ServerMock(sessionModel);
        assertFalse(server.isConfiguredForApiCalls());
    }

    @Test
    public void testGetSerializedCreateSession() throws InternalException, JSONException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedCreateSession(userId, metadata, mockContext);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "userId");
        JsonUtil.assertJsonHasKey(jsonObject, "device");
        JsonUtil.assertJsonHasKey(jsonObject, "system");
        JsonUtil.assertJsonHasKey(jsonObject, "screenWidth");
        JsonUtil.assertJsonHasKey(jsonObject, "screenHeight");
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedCreateSessionWithoutMetadata() throws InternalException, JSONException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedCreateSession(userId, null, mockContext);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "userId");
        JsonUtil.assertJsonHasKey(jsonObject, "device");
        JsonUtil.assertJsonHasKey(jsonObject, "system");
        JsonUtil.assertJsonHasKey(jsonObject, "screenWidth");
        JsonUtil.assertJsonHasKey(jsonObject, "screenHeight");
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedSubmitDataWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedSubmitData(mock(BehaviouralDataModel.class));
    }

    @Test
    public void testGetSerializedSubmitData() throws SessionException, InternalException, JSONException {
        ServerMock server = new ServerMock(sessionModel);
        BehaviouralDataModel behaviouralDataModel = mock(BehaviouralDataModel.class);
        String behaviouralModelString = "{\n" +
                "  \"touches\": [],\n" +
                "  \"accelerations\": [],\n" +
                "  \"textEvents\": []\n" +
                "}";
        when(behaviouralDataModel.toJSON()).thenReturn(new JSONObject(behaviouralModelString));
        SerializedRequest serializedRequest = server.getSerializedSubmitData(behaviouralDataModel);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "session");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedGetCurrentScoreWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedGetCurrentScore(metadata);
    }

    @Test
    public void testGetSerializedGetCurrentScoreWithoutMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        SerializedRequest serializedRequest = server.getSerializedGetCurrentScore(null);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedGetCurrentScoreWithMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedGetCurrentScore(metadata);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedSendProvidedFaceCapturesWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedSendProvidedFaceCaptures(stringListDataModel, null, FaceActions.FACE_AUTH);
    }

    @Test
    public void testGetSerializedSendProvidedFaceCapturesWithoutMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        SerializedRequest serializedRequest = server.getSerializedSendProvidedFaceCaptures(stringListDataModel, null, FaceActions.FACE_AUTH);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedSendProvidedFaceCapturesWithMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedSendProvidedFaceCaptures(stringListDataModel, metadata, FaceActions.FACE_AUTH);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedCompareFacesWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedCompareFaces(stringListDataModel, stringListDataModel, null);
    }

    @Test
    public void testGetSerializedCompareFacesWithoutMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedCompareFaces(stringListDataModel, stringListDataModel, null);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "faces1");
        JsonUtil.assertJsonHasKey(jsonObject, "faces2");
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedCompareFacesWithMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedCompareFaces(stringListDataModel, stringListDataModel, metadata);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "faces1");
        JsonUtil.assertJsonHasKey(jsonObject, "faces2");
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedVoiceTokenWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedVoiceToken(VoiceTokenType.ENROLL1, null);
    }

    @Test
    public void testGetSerializedVoiceTokenWithoutMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        SerializedRequest serializedRequest = server.getSerializedVoiceToken(VoiceTokenType.ENROLL1, null);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "tokentype");
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedVoiceTokenWithMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedVoiceToken(VoiceTokenType.ENROLL1, metadata);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "tokentype");
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test(expected = SessionException.class)
    public void testGetSerializedSendProvidedVoiceCapturesWithoutServer() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(null);
        server.getSerializedSendProvidedVoiceCaptures(stringListDataModel, null, VoiceActions.VOICE_AUTH);
    }

    @Test
    public void testGetSerializedSendProvidedVoiceCapturesWithoutMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        SerializedRequest serializedRequest = server.getSerializedSendProvidedVoiceCaptures(stringListDataModel, null, VoiceActions.VOICE_AUTH);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "voices");
        JsonUtil.assertJsonDoesNotHaveKey(jsonObject, "metadata");
    }

    @Test
    public void testGetSerializedSendProvidedVoiceCapturesWithMetadata() throws InternalException, JSONException, SessionException {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        SerializedRequest serializedRequest = server.getSerializedSendProvidedVoiceCaptures(stringListDataModel, metadata, VoiceActions.VOICE_AUTH);
        String stringJson = serializedRequest.getRequestJSON();
        JSONObject jsonObject = new JSONObject(stringJson);
        JsonUtil.assertJsonHasKey(jsonObject, "voices");
        JsonUtil.assertJsonHasKey(jsonObject, "metadata");
    }

    @Test
    public void testCalculateSignature() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);
        URL url = new URL("https://api.aimbrain.com:443/v1/sessions");
        String jsonObject = "{\"screenHeight\":904,\"device\":\"E2105\"}";
        String test_data = server.calculateSignature("POST", url.getPath(), jsonObject, "secret");
        assertNotNull(test_data);
    }


    @Test
    public void testGetHeadersMap() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setApiKey("key");
        server.setSecret("secret");
        server.setBase64Helper(base64);
        JSONObject json = mock(JSONObject.class);
        URL url = new URL("https://api.aimbrain.com:443/v1/sessions");
        Map<String, String> headers = server.getHeadersMap(json, url);
        assertNotNull(headers);
        assertEquals(2, headers.size());
        assertEquals("key", headers.get("X-aimbrain-apikey"));
        assertTrue(headers.containsKey("X-aimbrain-signature"));
    }

    @Test
    public void testParseCreateSessionResponse() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        when(base64.decode(any(String.class), any(int.class))).thenReturn("metadata_test".getBytes());

        JSONObject json = mock(JSONObject.class);
        when(json.get("session")).thenReturn("test_session");
        when(json.getInt("face")).thenReturn(1);
        when(json.getInt("voice")).thenReturn(2);
        when(json.getInt("behaviour")).thenReturn(3);
        when(json.has("metadata")).thenReturn(true);
        when(json.getString("metadata")).thenReturn("metadata_test");

        SessionCallback sessionCallback = mock(SessionCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SessionModel sessionModel = (SessionModel) invocation.getArguments()[0];
                assertNotNull(sessionModel);
                assertEquals(1, sessionModel.getFaceStatus());
                assertEquals(2, sessionModel.getVoiceStatus());
                assertEquals(3, sessionModel.getBehaviourStatus());
                assertEquals("metadata_test", sessionModel.getMetadataString());
                assertEquals("test_session", sessionModel.getSessionId());
                return null;
            }
        }).when(sessionCallback).onSessionCreated(any(SessionModel.class));

        server.parseCreateSessionResponse(json, sessionCallback);
        verify(sessionCallback, times(1)).onSessionCreated(any(SessionModel.class));
    }

    @Test
    public void testParseCreateSessionResponseWithoutMetadata() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.get("session")).thenReturn("test_session");
        when(json.getInt("face")).thenReturn(1);
        when(json.getInt("voice")).thenReturn(2);
        when(json.getInt("behaviour")).thenReturn(3);
        when(json.has("metadata")).thenReturn(false);

        SessionCallback sessionCallback = mock(SessionCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SessionModel sessionModel = (SessionModel) invocation.getArguments()[0];
                assertNotNull(sessionModel);
                assertEquals(1, sessionModel.getFaceStatus());
                assertEquals(2, sessionModel.getVoiceStatus());
                assertEquals(3, sessionModel.getBehaviourStatus());
                assertNull(sessionModel.getMetadataString());
                assertEquals("test_session", sessionModel.getSessionId());
                return null;
            }
        }).when(sessionCallback).onSessionCreated(any(SessionModel.class));

        server.parseCreateSessionResponse(json, sessionCallback);
        verify(sessionCallback, times(1)).onSessionCreated(any(SessionModel.class));
    }

    @Test
    public void testParseSendAllDataFromQueueResponse() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getDouble("score")).thenReturn(2.0);
        when(json.getInt("status")).thenReturn(1);
        when(json.has("metadata")).thenReturn(true);
        when(json.getString("metadata")).thenReturn("metadata_test");

        final ScoreCallback scoreCallback = mock(ScoreCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ScoreModel scoreModel = (ScoreModel) invocation.getArguments()[0];
                assertNotNull(scoreModel);
                assertEquals(2.0, scoreModel.getScore(), 0.0);
                assertEquals(1, scoreModel.getStatus());
                assertEquals("metadata_test", scoreModel.getMetadataString());
                return null;
            }
        }).when(scoreCallback).success(any(ScoreModel.class));

        server.parseScoreStatusResponse(json, scoreCallback, "session_id");
        verify(scoreCallback, times(1)).success(any(ScoreModel.class));
    }

    @Test
    public void testParseSendAllDataFromQueueResponseWithoutMetadata() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getDouble("score")).thenReturn(2.0);
        when(json.getInt("status")).thenReturn(1);
        when(json.has("metadata")).thenReturn(false);

        final ScoreCallback scoreCallback = mock(ScoreCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ScoreModel scoreModel = (ScoreModel) invocation.getArguments()[0];
                assertNotNull(scoreModel);
                assertEquals(2.0, scoreModel.getScore(), 0.0);
                assertEquals(1, scoreModel.getStatus());
                assertNull(scoreModel.getMetadataString());
                return null;
            }
        }).when(scoreCallback).success(any(ScoreModel.class));

        server.parseScoreStatusResponse(json, scoreCallback, "session_id");
        verify(scoreCallback, times(1)).success(any(ScoreModel.class));
    }

    @Test
    public void testParseCompareFacesRequest() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getDouble("score")).thenReturn(2.0);
        when(json.getDouble("liveliness1")).thenReturn(3.0);
        when(json.getDouble("liveliness2")).thenReturn(4.0);
        when(json.has("metadata")).thenReturn(true);
        when(json.getString("metadata")).thenReturn("metadata_test");

        final FaceCompareCallback callback = mock(FaceCompareCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                FaceCompareModel model = (FaceCompareModel) invocation.getArguments()[0];
                assertNotNull(model);
                assertEquals(2.0, model.getSimilarity(), 0.0);
                assertEquals(3.0, model.getFirstLiveliness(), 0.0);
                assertEquals(4.0, model.getSecondLiveliness(), 0.0);
                assertEquals("metadata_test", model.getMetadataString());
                return null;
            }
        }).when(callback).success(any(FaceCompareModel.class));

        server.parseCompareFacesRequest(json, callback);
        verify(callback, times(1)).success(any(FaceCompareModel.class));
    }

    @Test
    public void testParseCompareFacesRequestWithoutMetadata() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getDouble("score")).thenReturn(2.0);
        when(json.getDouble("liveliness1")).thenReturn(3.0);
        when(json.getDouble("liveliness2")).thenReturn(4.0);
        when(json.has("metadata")).thenReturn(false);

        final FaceCompareCallback callback = mock(FaceCompareCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                FaceCompareModel model = (FaceCompareModel) invocation.getArguments()[0];
                assertNotNull(model);
                assertEquals(2.0, model.getSimilarity(), 0.0);
                assertEquals(3.0, model.getFirstLiveliness(), 0.0);
                assertEquals(4.0, model.getSecondLiveliness(), 0.0);
                assertNull(model.getMetadataString());
                return null;
            }
        }).when(callback).success(any(FaceCompareModel.class));

        server.parseCompareFacesRequest(json, callback);
        verify(callback, times(1)).success(any(FaceCompareModel.class));
    }

    @Test
    public void testParseVoiceTokenResponse() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getString("token")).thenReturn("token_test");
        when(json.has("metadata")).thenReturn(true);
        when(json.getString("metadata")).thenReturn("metadata_test");

        final VoiceTokenCallback callback = mock(VoiceTokenCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                VoiceTokenModel model = (VoiceTokenModel) invocation.getArguments()[0];
                assertNotNull(model);
                assertEquals("token_test", model.getToken());
                assertEquals("metadata_test", model.getMetadataString());
                return null;
            }
        }).when(callback).success(any(VoiceTokenModel.class));

        server.parseVoiceTokenResponse(json, callback);
        verify(callback, times(1)).success(any(VoiceTokenModel.class));
    }

    @Test
    public void testParseVoiceTokenResponseWithoutMetadata() throws Exception {
        ServerMock server = new ServerMock(sessionModel);
        server.setBase64Helper(base64);

        JSONObject json = mock(JSONObject.class);
        when(json.getString("token")).thenReturn("token_test");
        when(json.has("metadata")).thenReturn(false);

        final VoiceTokenCallback callback = mock(VoiceTokenCallback.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                VoiceTokenModel model = (VoiceTokenModel) invocation.getArguments()[0];
                assertNotNull(model);
                assertEquals("token_test", model.getToken());
                assertNull(model.getMetadataString());
                return null;
            }
        }).when(callback).success(any(VoiceTokenModel.class));

        server.parseVoiceTokenResponse(json, callback);
        verify(callback, times(1)).success(any(VoiceTokenModel.class));
    }
}

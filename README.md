# AimBrain SDK integration

For full documentation and references please see: [https://docs.aimbrain.com](https://docs.aimbrain.com)

## Requirements

* Android 4.0.x (API 14) or later

## Permissions

SDK requires the following permissions:

`android.permission.INTERNET`
`android.permission.ACCESS_NETWORK_STATE`

Those permissions are included in SDK’s manifest (there is no need to include it into the application’s manifest)

## Gradle sync

1. Download and extract module project.
2. Add module to your project by selecting File -> New -> Import module.
3. Select the directory where you’ve just extracted the source code and confirm.
4. On you project name right click -> Open module settings -> Dependencies tab -> select (+) icon -> Module dependency -> select **aimbrain**.
5. Choose Select Project with Gradle Files.

## Integration (setting the application class)

In order to integrate aimbrain SDK it is necessary to set up application name in project’s `AndroidManifest.xml`.
If no `Application` class extensions are needed, use `com.aimbrain.sdk.AMBNApplication.AMBNApplication`, otherwise use your extension's name.

```xml
<application android:name=“com.aimbrain.sdk.AMBNApplication.AMBNApplication”>
  ```

## Logging configuration
SDK has configurable logging level. To change logging level change `Logger` class field to :

```java
import com.aimbrain.sdk.util.Logger;
...
Logger.LEVEL = Logger.VERBOSE;
```

Available log levels (in increasing verbosity order) `Logger.ERROR`, `Logger.WARN`, `Logger.INFO`, `Logger.DEBUG`, `Logger.VERBOSE`.

## Configuration
In order to communicate with the server, the application identifier and secret need to be passed with each request. Relevant configuration parameters should be passed to the SDK with use of `Manager`’s `configure` method.

```java
Manager.getInstance().configure("AIMBRAIN_API_KEY", "AIMBRAIN_API_SECRET");
```

## Request serialisation

AimBrain SDK can be used in two modes, by sending data to AimBrain API directly or by serialising requests and submitting the data via server side integration.

To serialise requests `Manager` must have configured session. For serialisation a configuration with session id value can be used.

```java
Manager.getInstance().configure("SESSION ID");
```

Methods with prefix ```getSerialized...``` can be called to retrieve serialized request data once session is set .  

All serialisation calls return ```SerializedRequest``` object. The request data can be accessed via ```SerializedRequest``` method ```getRequestJSON()```.

Please refer to server side integration documentation for serialised data processing details.  

#Request metadata

For some integration scenarios additional data may be required to be sent to server.

Such integration-defined information should be submitted by calling function overload with parameter ```metadata```.

Integration-defined information is returned from server in response field ```metadata```.

# Sessions

## Starting a new session

In order to communicate with the server, a session must be established. Assuming the appropriate configuration parameters were passed to the instance of the `Manager` class, a session can be established as shown in example.

```java
Manager.getInstance().createSession("userId", new SessionCallback() {
  @Override
  public void onSessionCreated(SessionModel session) {
    // Implement method called after the session has been created
  }
});
```

SessionModel returned contains the following parameters:
* sessionId - string containing session id for use with other API requests to maintain session semantics.
* faceStatus - status of the Facial Module for given user (see below).
* behaviourStatus - status of the Facial Module for given user and device pair (see below).


Values possible for faceStatus field:
* 0 - User not enrolled - facial authentication not available, enrollment required.
* 1 - User enrolled - facial authentication available
* 2 - Building template - enrollment done, AimBrain is building user template and no further action is required.

Values possible for behaviourStatus field:
* 0 - User not enrolled - behavioural authentication not available, enrollment required. 
* 1 - User enrolled - behavioural authentication available.

The SessionModel object returned on successful session creation is stored within inner object of the Manager - there is no need to store this object in a separate variable. The created session will be used for communicating with server until the creation of another session.

## Serialising a new session call

To get serialised new session request use

```java
SerializedRequest request = Manager.getInstance().getSerializedCreateSession(userId, metadata, context)
```

# Behavioural module

## Starting data collection

Data collection must be started manually, using `startCollectingData` method of the `Manager` class instance. `Window` object that is currently displayed on top needs to be passed as parameter.
Example of starting data collection on `Activity` creation has been provided.

```java
public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Manager.getInstance().startCollectingData(getWindow());
  }
}
  ```

If data needs to be collected since the application creation, extend `AMBNApplication` class as shown in the example.

```java
public class MyApplication extends AMBNApplication {
  @Override
  public void onCreate() {
    super.onCreate();
    Manager.getInstance().startCollectingData(null);
  }
}
  ```

Remember to set up appropriate application name in project’s `AndroidManifest.xml`.

```xml
<application android:name=“com.my.package.MyApplication”>
  ```

Calling `startCollectingData` with `null` as a parameter results in starting data collection on the next activity start event.

## Adding Window to manager

The process of collecting the data requires the `Manager` instance object to be notified about the changing of the top `Window` object. Window changes associated with the changing of the top Activity are handled automatically, however you have to manually notify `Manager` instance about any other window creation. For example, when a `Dialog` object is created, the `Manager` instance object needs to be notified in the manner shown in the example code.

```java
// Called in Activity’s method
final AlertDialog.Builder builder = new AlertDialog.Builder(this);
AlertDialog dialog = builder.show();

// Notify Manager about changing top Window object
Manager.getInstance().windowChanged(dialog.getWindow());

  ```

## Connecting to server

Data collection is started when the application is created, however it is the programmer’s responsibility to take care of sending it to the server. The SDK has the following methods for this task.

### Sending data

After creating a session, we can send data gathered for analysis to the server. To do so, call `submitCollectedData` method of the Manager’s class instance.

```java
Manager.getInstance().submitCollectedData(new ScoreCallback() {
  @Override
  public void success(ScoreModel scoreModel) {
    // Implement method called after receiving
    // a response from the server with the current score
  }
});
  ```

Server responses for data submission contain the current behavioural score. The `scoreModel` object contains the status (1 for learning, 0 for learned) and the current score as a double.

### Scheduling

Object of the `Manager` class allows for easy scheduling of data submission with the use of `scheduleDataSubmission` method. The method may be called with the following parameters:
`delay` - delay before the first submission in milliseconds
`period` - period between data submissions in milliseconds
`scoreCallback` - callback for receiving responses with the current score from the server (optional)

```java
Manager.getInstance().scheduleDataSubmission(0, 10000, new ScoreCallback() {
  @Override
  public void success(ScoreModel scoreModel) {
    // Implement method called after successful data submission
  }
});
  ```

### Serialising behavioural data submission request

To get serialised data submit request use

```java
SerializedRequest request = Manager.getInstance().getSerializedSubmitCollectedData(metadata)
```

### Getting the current score

There is an option to get the current score from the server without sending any data. To do so use the `getCurrentScore` method of the `Manager` class instance.

```java
Manager.getInstance().getCurrentScore(new ScoreCallback() {
  @Override
  public void success(ScoreModel scoreModel) {
    // Implement method called after receiving
    // response from the server with the current score
  }
});
  ```

### Serialising current score request

To get serialised data score request use

```java
SerializedRequest request = Manager.getInstance().getSerializedGetCurrentScore(metadata)
```

## Privacy guards

In order to disable collecting data from specific views it is necessary to create a `PrivacyGuard` object with a `Set` of these views. Then this `PrivacyGuard` object has to be added to the `Manager` instance.

```java
EditText editTextToIgnore = (EditText)findViewById(R.id.editText);
HashSet<View> setWithIgnoredViews = new HashSet<>();
setWithIgnoredViews.add(editTextToIgnore);
PrivacyGuard privacyGuard = new PrivacyGuard(setWithIgnoredViews);
Manager.getInstance().addPrivacyGuard(privacyGuard);
  ```

For ignoring all views in the application a `true`  value has to be passed as the first parameter in the `PrivacyGuard` constructor.

```java
PrivacyGuard privacyGuard = new PrivacyGuard(true);
Manager.getInstance().addPrivacyGuard(privacyGuard);
  ```

### Sensitive view guards

In some cases it is preferable to only obfuscate the element that is being interacted with while still getting most of the behavioural data (for example in a pin code with a custom keyboard). In this case, views should be added to the SensitiveViewGuard. All of the child views will also be considered as sensitive.

```java
// All of the elements on the Window will be considered as sensitive
SensitiveViewGuard.addView(getWindow().getDecorView());
  ```
When the view is marked as sensitive, it's id is salted (with the salt stored localy) and hashed before sending and the global touch x and y coordinates are set to 0.

## Mapping view identifiers

Collected data contains the touched view's path consisting of view identifiers, starting from the touched view up to the root view. All views with no identifiers defined are ignored while building this path.
SDK allows for defining and customising view identifiers. If no identifier has been defined for a view (for example, in the layout xml file), the view identifier may be set as shown in the example.
The use of `ViewIdMapper` class does not change actual identifiers - it only adds mappings used by the SDK.

```java
ViewIdMapper.getInstance().putViewId(view, "myView");
```

# Facial module

## Taking pictures of the user's face

In order to take picture of the user's face the `PhotoFaceCaptureActivity` has to be started. The resulting images can be obtained in `onActivtyResult` if the `resultCode` is `RESULT_OK`. The images are stored in the static field `PhotoFaceCaptureActivity.images`. When starting activity, three string extras may be passed: upperText - text displayed above face placeholder, lowerText - text displayed below face placeholder, recordingHint - text displayed below face placeholder, that replaces lowerText after clicking camera button. Each of the extras may be omitted - no text will be displayed in destined place. Remember to add `PhotoFaceCaptureActivity` to your application's manifest file.

```xml
<activity android:name="com.aimbrain.sdk.faceCapture.PhotoFaceCaptureActivity"/>
```

```java
//...
Intent intent = new Intent(this, PhotoFaceCaptureActivity.class);
intent.putExtra("upperText", upperText);
intent.putExtra("lowerText", lowerText);
intent.putExtra("recordingHint", recordingHint);
startActivityForResult(intent, photoRequestCode);
//...

protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if(requestCode == photoRequestCode && resultCode == RESULT_OK){
    this.images = PhotoFaceCaptureActivity.images;
  }
}
```

## Recording video of user's face

In order to record video containing user's face  `VideoFaceCaptureActivity` has to be started. The result can be obtained in `onActivtyResult` if result code is `RESULT_OK` using static field: `VideoFaceCaptureActivity.video`. When starting activity, the following extras can be used to customize activity:
* `VideoFaceCaptureActivity.EXTRA_UPPER_TEXT` - text displayed above face placeholder.
* `VideoFaceCaptureActivity.EXTRA_LOWER_TEXT` - text displayed below face placeholder.
* `VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS` - recorded video duration.
* `VideoFaceCaptureActivity.EXTRA_RECORDING_HINT` - text displayed below face placeholder, that replaces lowerText after clicking camera button.

These extras are optional and may be omitted - no text will be displayed in destined place.
Remember to add `VideoFaceCaptureActivity` to your application's manifest file.

```xml
<activity android:name="com.aimbrain.sdk.faceCapture.VideoFaceCaptureActivity"/>
```

Starting capture activity:
```java
//...
Intent intent = new Intent(this, VideoFaceCaptureActivity.class);
intent.putExtra(VideoFaceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
intent.putExtra(VideoFaceCaptureActivity.EXTRA_LOWER_TEXT, lowerText);
intent.putExtra(VideoFaceCaptureActivity.EXTRA_DURATION_MILLIS, 2000);
intent.putExtra(VideoFaceCaptureActivity.EXTRA_RECORDING_HINT, recordingHint);
startActivityForResult(intent, videoRequestCode);
//...
```

Using recorded result:
```java
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if(requestCode == videoRequestCode && resultCode == RESULT_OK){
    this.video = VideoFaceCaptureActivity.video;
  }
}
```

## Authenticating with the facial module

In order to authenticate with the facial module use the `sendProvidedPhotosToAuthenticate` method of the `Manager` class.
An array of cropped images of the face in Bitmap format or video containing face is passed as the parameter.

```java
Manager.getInstance().sendProvidedFaceCapturesToAuthenticate(PhotosFaceCaptureActivity.images, new FaceCapturesAuthenticateCallback() {

  @Override
  public void success(FaceAuthenticateModel faceAuthenticateModel) {

  }

  @Override
  public void failure(VolleyError volleyError) {

  }
});
```

```java
Manager.getInstance().sendProvidedFaceCapturesToAuthenticate(VideoFaceCaptureActivity.video, new FaceCapturesAuthenticateCallback() {

  @Override
  public void success(FaceAuthenticateModel faceAuthenticateModel) {

  }

  @Override
  public void failure(VolleyError volleyError) {

  }
});
```

### Serialising authentication with the facial module

To get serialised authentication request use

```java
SerializedRequest request = Manager.getInstance().getSerializedSendProvidedFaceCapturesToAuthenticate(VideoFaceCaptureActivity.video, metadata)
```

## Enrolling with the facial module

Enrolling with the facial module is done by calling the `sendProvidedFaceCapturesToEnroll` method of the `Manager` class.
An array of cropped images of the face in Bitmap format or video containing face is passed as the parameter.

```java
Manager.getInstance().sendProvidedFaceCapturesToEnroll(PhotoFaceCaptureActivity.images, new FaceCapturesEnrollCallback() {
  @Override
  public void success(FaceEnrollModel faceEnrollModel) {

  }

  @Override
  public void failure(VolleyError volleyError) {

  }
});
```

```java
Manager.getInstance().sendProvidedFaceCapturesToEnroll(VideoFaceCaptureActivity.video, new FaceCapturesEnrollCallback() {
  @Override
  public void success(FaceEnrollModel faceEnrollModel) {

  }

  @Override
  public void failure(VolleyError volleyError) {

  }
});
```
### Serialising enrolling with the facial module request

To get serialised enroll request use

```java
SerializedRequest request = Manager.getInstance().getSerializedSendProvidedFaceCapturesToEnroll(VideoFaceCaptureActivity.video, metadata)
```

## Comparing faces

Two batches of images of faces can be compared using the `compareFacesPhotos` method of the `Manager` class.
The method accepts an array of images of the first face and an array of images of the second face as parameters.

```java
Manager.getInstance().compareFacesPhotos(facialImages1, facialImages2, new FaceCompareCallback() {
  @Override
  public void success(FaceCompareModel faceCompareModel) {

  }

  @Override
  public void failure(VolleyError volleyError) {

  }
});
```

### Serialising face compare request

To get serialised face comparison request use

```java
SerializedRequest request = Manager.getInstance().getSerializedCompareFacesPhotos(facialImages1, facialImages2, metadata)
```

# Voice module

## Retrieving voice token

In order to enroll or authenticate via voice module you have to record user reading text.

The text is retrieved with `getVoiceToken` method. The parameter type of this method specifies use case for retrieved text. See enum `VoiceTokenType` for possible values.

Text to be presented to the user is returned by the result method `getToken()`.

```java
Manager.getInstance().getVoiceToken(VoiceTokenType.AUTH, new VoiceTokenCallback() {
  @Override
  public void success(VoiceTokenModel tokenModel) {
    String token = tokenModel.getToken();
    // use token
  }
});
```

## Serialising voice token call

To get serialised voice token request use
```java
SerializedRequest request = Manager.getInstance().getSerializedVoiceToken(VoiceTokenType.AUTH, metadata)
```

## Recording user voice

In order to record audio containing user's voice  `VoiceCaptureActivity` has to be started.
The result can be obtained in `onActivtyResult` if result code is `RESULT_OK` using static field `VoiceCaptureActivity.audio`.

When starting activity, the following extras can be used to customize activity:
* `VoiceCaptureActivity.EXTRA_UPPER_TEXT` - text displayed above presented token.
* `VoiceCaptureActivity.EXTRA_RECORDING_HINT` - text for user to read.

Top hint is optional and may be omitted - no text will be displayed in destined place.

Remember to add `VoiceCaptureActivity` to your application's manifest file.

```xml
<activity android:name="com.aimbrain.sdk.voiceCapture.VoiceCaptureActivity"/>
```

Starting record activity:
```java
//...
Intent intent = new Intent(this, VoiceCaptureActivity.class);
intent.putExtra(VoiceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
intent.putExtra(VoiceCaptureActivity.EXTRA_RECORDING_HINT, recordingToken);
startActivityForResult(intent, voiceRequestCode);
//...
```

Using recorded audio:
```java
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if(requestCode == voiceRequestCode && resultCode == RESULT_OK){
    this.voice = VoiceCaptureActivity.audio;
  }
}

```

## Authenticating with the voice module

In order to authenticate with the voice module use the `sendProvidedVoiceCapturesToAuthenticate` method of the `Manager` class.
A byte array containing voice audio is passed as the parameter. The recorded audio must be token retrieved by `getVoiceToken` using type `VoiceTokenType.AUTH`.

```java
Manager.getInstance().sendProvidedVoiceCapturesToAuthenticate(VoiceCaptureActivity.audio, new VoiceCapturesAuthenticateCallback() {
  @Override
  public void success(VoiceAuthenticateModel voiceAuthenticateModel) {
    // authentication succeeded
  }

  @Override
  public void failure(VolleyError volleyError) {
    // authentication failed
  }
});
```

### Serialising authentication with the voice module

To get serialised authentication request use

```java
SerializedRequest request = Manager.getInstance().getSerializedSendProvidedVoiceCapturesToAuthenticate(VoiceCaptureActivity.audio, metadata)
```

## Enrolling with the voice module

Enrolling with the voice module is done by calling the `sendProvidedVoiceCapturesToEnroll` method of the `Manager` class. A byte array containing voice audio is passed as the parameter.

```java
Manager.getInstance().sendProvidedVoiceCapturesToEnroll(VoiceCaptureActivity.audio, new VoiceCaptureEnrollCallback() {
  @Override
  public void success(VoiceEnrollModel voiceEnrollModel) {
    // enroll call succesfull
  }

  @Override
  public void failure(VolleyError volleyError) {
    // enroll call failed
  }
});
```

To complete enroll you have to send recoded audios for token types `VoiceTokenType.ENROLL1`, `VoiceTokenType.ENROLL2`, `VoiceTokenType.ENROLL3`, `VoiceTokenType.ENROLL4`, `VoiceTokenType.ENROLL5`.

### Serialising enrolling with the voice module request

To get serialised enroll request use

```java
SerializedRequest request = Manager.getInstance().getSerializedSendProvidedVoiceCapturesToEnroll(VoiceCaptureActivity.audio, metadata)
```

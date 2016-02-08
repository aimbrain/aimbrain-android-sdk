#AimBrain SDK integration


## Permissions
SDK requires the following permissions:

`android.permission.INTERNET`

`android.permission.ACCESS_NETWORK_STATE`

Those permissions are included in SDK’s manifest (there is no need to include it into the application’s manifest)

## Graddle sync
1. Download and extract module project.
2. Add module to your project by selecting File -> New -> Import module.
3. Select the directory where you’ve just extracted the source code and confirm.
4. On you project name right click -> Open module settings -> Dependencies tab -> select (+) icon -> Module dependency -> select **aimbrain**.
5. Choose Select Project with Gradle Files.

## Integration (Setting application class)
In order to integrate aimbrain SDK it is necessary to set up application name in project’s `AndroidManifest.xml`.
If no `Application` class extensions are needed, use `com.aimbrain.sdk.AMBNApplication.AMBNApplication`, otherwise use your extension's name.


```xml
<application android:name=“com.aimbrain.sdk.AMBNApplication.AMBNApplication”>
  ```


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

Calling `startCollectingData` with `null` as parameter results in starting data collection on the next activity start event.

## Configuration
In order to communicate with the server, the application identifier and secret need to be passed with each request. Relevant configuration parameters should be passed to the SDK with use of `Manager`’s `configure` method.

```java
Manager.getInstance().configure("applicationID", "secret");
  ```

## Adding Window to manager
The process of collecting the data requires the `Manager` instance object to be notified about the changing of the top `Window` object. Window changes associated with the changing of the top Activity are handled automatically, however you have to manually notify `Manager` instance about any other window creation. For example, when a `Dialog` object is created, the `Manager` instance object needs to be notified in the manner shown in the example code.

```java
//called in the Activity’s method
final AlertDialog.Builder builder = new AlertDialog.Builder(this);
AlertDialog dialog = builder.show();

//notify Manager about the changing top Window object
Manager.getInstance().windowChanged(dialog.getWindow());

  ```

## Connecting to server
Data collection is started when the application is created, however it is the programmer’s responsibility to take care of sending it to the server. The SDK has the following methods for this task.

### Starting new session
In order to communicate with the application server, it is required to establisha session. Assuming the appropriate configuration parameters were passed to the instance of the `Manager` class, a session can be established as shown in example.

```java
Manager.getInstance().createSession("userId", new SessionCallback() {
   @Override
   public void onSessionCreated(String session) {
       // implement method called after session has been created
   }
});
  ```

The session string returned on successful session creation is stored within inner object of the Manager - there is no need to store this string in a separate variable. The created session will be used for communicating with server until the creation of another session.

### Sending data
After creating a session, we can send data gathered for analysis to the server. To do so, call `submitCollectedData` method of the Manager’s class instance.

```java
Manager.getInstance().submitCollectedData(new ScoreCallback() {
   @Override
   public void success(ScoreModel scoreModel) {
      // implement method called after receiving
      // response from server with current score
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
       //implement method called after successful data submission
   }
});
  ```

### Getting the current score
There is an option to get the current score from the server without sending any data. To do so use the `getCurrentScore` method of the `Manager` class instance.

```java
Manager.getInstance().getCurrentScore(new ScoreCallback() {
   @Override
   public void success(ScoreModel scoreModel) {
       // implement method called after receiving
       // response from server with current score
   }
});
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

## Mapping view identifiers
Collected data contains the touched view's path consisting of view identifiers, starting from the touched view up to the root view. All views with no identifiers defined are ignored while building this path.
SDK allows for defining and customizing view identifiers. If no identifier has been defined for a view (for example, in the layout xml file), the view identifier may be set as shown in the example.
The use of `ViewIdMapper` class does not change actual identifiers - it only adds mappings used by the SDK.

```java
ViewIdMapper.getInstance().putViewId(view, "myView");
  ```

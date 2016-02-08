#Aimbrain SDK integration


## Permissions
SDK requires the following permissions:

`android.permission.INTERNET`

`android.permission.ACCESS_NETWORK_STATE`

Those permissions are included in SDK’s manifest (there is no need to include it into application’s manifest)

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
Data collection must be started manually, using `startCollectingData` method of `Manager` class instance. `Window` object that is currently displayed on top needs to be passed as parameter.
Example of starting data collection on `Activity` creation has been provided.

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Manager.getInstance().startCollectingData(getWindow());
    }
}
  ```
If data needs to be collected since creating application, extend `AMBNApplication` class as shown in the example.

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

Calling `startCollectingData` with `null` as parameter results with starting data collection after on next activity start event.

## Configuration
In order to be able to communicate with server, application identifier and secret need to be passed in each request. Relevant configuration parameters should be passed to the SDK with use of `Manager`’s `configure` method.

```java
Manager.getInstance().configure("applicationID", "secret");
  ```

## Adding Window to manager
The process of collecting data requires `Manager` instance object to be notified about changing top `Window` object. Window changes associated with changing top Activity are handled automatically, however there is need to manually notify `Manager` instance about any other window created. As an example, when `Dialog` object is created, `Manager` instance object needs to be notified in the manner shown in example code.

```java
//called in Activity’s method
final AlertDialog.Builder builder = new AlertDialog.Builder(this);
AlertDialog dialog = builder.show();

//notify Manager about changing top Window object
Manager.getInstance().windowChanged(dialog.getWindow());

  ```

## Connecting to server
Data collection is started when application is created, however it is programmer’s responsibility to take care of sending them to the server. SDK shares convenient methods that allow accomplish this task.

### Starting new session
In order to communicate with application server, there is need to establish session. Assuming appropriate configuration parameters were passed to the instance of `Manager` class, session might be established as shown in example.

```java
Manager.getInstance().createSession("userId", new SessionCallback() {
   @Override
   public void onSessionCreated(String session) {
       // implement method called after session has been created
   }
});
  ```

Session string returned on successful session creation is stored within inner object of Manager - there is no need to store this string in separate variable. Created session will be used for communication with server until creating another session.

### Sending data
After creating session, we can send data gathered for analysis to the server. To do so, call `submitCollectedData` method on Manager’s class instance.

```java
Manager.getInstance().submitCollectedData(new ScoreCallback() {
   @Override
   public void success(ScoreModel scoreModel) {
      // implement method called after receiving
      // response from server with current score
   }
});
  ```

Server responses for data submission with current behavioural score. The `scoreModel` object contains status (1 for learning, 0 for learned) and current score as double.

### Scheduling
Object of `Manager` class allows for easy scheduling of data submission with use of `scheduleDataSubmission` method. The method may be called with the following parameters:
`delay` - delay before first submission in milliseconds
`period` - period between next data submissions in milliseconds
`scoreCallback` - callback for receiving responses with current score from server (optional)

```java
Manager.getInstance().scheduleDataSubmission(0, 10000, new ScoreCallback() {
   @Override
   public void success(ScoreModel scoreModel) {
       //implement method called after successful data submission
   }
});
  ```

### Getting current score
There is an option to get current score from the server without sending any data. To do so use `getCurrentScore` method from `Manager` class instance.

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
In order to disable collecting data from concrete views it is necessary to create a `PrivacyGuard` object with a `Set` of these views. Then this `PrivacyGuard` object has to be added to the `Manager` instance.

```java
EditText editTextToIgnore = (EditText)findViewById(R.id.editText);
HashSet<View> setWithIgnoredViews = new HashSet<>();
setWithIgnoredViews.add(editTextToIgnore);
PrivacyGuard privacyGuard = new PrivacyGuard(setWithIgnoredViews);
Manager.getInstance().addPrivacyGuard(privacyGuard);
  ```

For ignoring all views in application `true` parameter has to be passed into the `PrivacyGuard` constructor.

```java
PrivacyGuard privacyGuard = new PrivacyGuard(true);
Manager.getInstance().addPrivacyGuard(privacyGuard);
  ```

## Mapping view identifiers
Collected data contains touched view path consisted of view identifiers, starting from touched view up to the root view. All views with no identifier defined are ignored while building this path.
SDK allows for defining and customizing view identifiers. If no identifier has been defined for view (for example in layout xml file), view identifier may be set as shown in example.
The use of `ViewIdMapper` class does not change actual identifiers - it only adds mappings used by SDK.

```java
ViewIdMapper.getInstance().putViewId(view, "myView");
  ```

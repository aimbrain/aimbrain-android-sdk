# AimBrain Android SDK


## Wrapper Classes

The following classes have wrap the corresponding classes with interaction event collection.
For widgets it is important to use them in both layouts and Java code.

`ABButton` - wrapper for the Button widget.

`ABEditText` - wrapper for the EditText widget.

`ABGridLayout` - wrapper for the GridLayout widget.

`ABLinearLayout` - wrapper for the LinearLayout widget.

`ABListView` - wrapper for the ListView widget.

`ABRelativeLayout` - wrapper for the RelativeLayout widget.

`ABTextView` - wrapper for the TextView widget.

The Activity wrappers support out of context event capture.

`ABActivity` - wrapper for the Activity class.

`ABAppCompatActivity` - wrapper for the AppCompatActivity class.

`ABListActivity` - wrapper for the ListActivity class.


## EventStore

The EventStore provides methods for manually adding events to the EventStore, in case the elements used are not yet supported by wrappers. Explicit use not recommended.

```java
public class EventStore {

  // Remove accumulated events. Use when restarting session.
  public static void resetEvents() {...}

  // Add a MotionEvent to the event store.
  public static void addEvent(MotionEvent event, String context) {...}

  // Add a KeyEvent to the event store.
  public static void addEvent(int keyCode, KeyEvent event, String context) {...}

  // Manually add a text change event to the event store. Used when soft keyboards don't fire KeyEvent.
  public static void addEvent(long time, int keyCode, String context) {...}

  // Get accumulated events.
  public static synchronized JSONArray getEvents() {...}
}
```

## AuthLibrary Object

```java
AuthLibrary abo = new AuthLibrary(apikey);
abo.getAuthAsync(userid, handler);
```

`apikey` - a static string which can be obtained by emailing founders@aimbrain.com. Acts as a single "namespace" for multiple `userid` identifiers.

`userid` - a static string for single entity, usually a user.

`handler` - an `AuthAsyncResponseHandler` object with two methods, `onSuccess(AuthAsyncResponse response)` and `onFailure(int statusCode, String message, Throwable exception)`. The methods are to be overridden with ones that implement required functionality.


## AuthAsyncResponseHandler Object

```java
AuthAsyncResponseHandler handler = new AuthAsyncResponseHandler() {
 @Override
 public void onSuccess(AuthAsyncResponse response) {
  // Method that gets called on successful server response
 }

 @Override
 public void onFailure(int statusCode, String message, Throwable e) {
  // Method that gets called on failure to receive, parse or
  // request timeout
 }};
```

## AuthAsyncResponse Object
```java
public class AuthAsyncResponse {
 public final Integer statusCode;
 public final String body;
 public final Double score;
 public final String method;
 public final String id;
 public final Long nonce;

 ...
}
```

An object which gets populated after receiving a successful reply.


## Manual Motion Event Collection API AuthLibrary Object

```java
AuthLibrary abo = new AuthLibrary(apikey, context);
abo.getAuthAsync(userid, events, handler);
```

`apikey` - a static string which can be obtained by emailing founders@aimbrain.com. Acts as a single "namespace" for multiple `userid` identifiers.

`context` - a static string for identifying different app contexts. For example, login screen and main app screen should have different context indicated by the parameter.

`userid` - a static string for single entity, usually a user.

`events` - an ArrayList of MotionEvent objects. A copy of MotionEvent object can be done using `obtain`, e.g.: `mEventsList.add(MotionEvent.obtain(event));`

`handler` - an `AuthAsyncResponseHandler` object with two methods, `onSuccess(AuthAsyncResponse response)` and `onFailure(int statusCode, String message, Throwable exception)`. The methods are to be overridden with ones that implement required functionality.

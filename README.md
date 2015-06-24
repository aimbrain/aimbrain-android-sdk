# AimBrain Android SDK


## AuthLibrary Object
```java
AuthLibrary abo = new AuthLibrary(apikey);
abo.getAuthAsync(userid, events, handler);
```

`apikey` - a static string which can be obtained by emailing founders@aimbrain.com. Acts as a single "namespace" for multiple `userid` identifiers.

`userid` - a static string for single entity, usually a user.

`events` - an ArrayList of MotionEvent objects. A copy of MotionEvent object can be done using `obtain`, e.g.: `mEventsList.add(MotionEvent.obtain(event));`

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

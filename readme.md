# AsyncTask
####  Java 1.8 library that facilitates asynchronous programming.

```xml
<groupId>es.zaroz</groupId>
<artifactId>AsyncTask</artifactId>
<version>1.0</version>
```

##  Examples

```java
VoidTask.run(() -> {
    System.out.println("Hello!");
});
```

This creates a new `VoidTask` - a task that returns nothing - and runs it without waiting for the result.

There are two ways of waiting for a Task to finish.

1. Using the `await` method which blocks the current thread until the task is completed
```java
Task task = VoidTask.run(() -> {
    System.out.println("Hello!");
});
task.await();
```
2. Using the `then` and `error` callbacks
```java
VoidTask.run(() -> {
    System.out.println("Hello!");
}).then(() -> {
    System.out.println("Hello task completed!");
}).error(exception -> {
    System.out.println("Hello task failed :(");
    exception.printStackTrace();
});
```

---
```java
ValueTask.run(() -> {
    return "Todd";
});
```
This creates a new `ValueTask` that returns a String (`Todd`) and runs it without waiting for the result.

You can wait for the result of a `ValueTask` the same way that you would wait for the result of a `VoidTask`, but `ValueTask` returns the result in the callback

```java
ValueTask.run(() -> {
    return "Todd";
}).then((t, result) -> {
    System.out.println("My cat is named: " + result);
});
```
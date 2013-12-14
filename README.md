Nirai - A job queue for Android
===============================

Nirai is a job queue written for Android to schedule jobs that run on the background.

It is best suitable for those network requests that aren't urgent and shouldn't make impact on the user experience. Nirai implements a backoff retry so it doesn't overload your services in case of server errors and listens to changes on network statuses to run enqueued jobs when there's a network connection available.

Usage
-----

### Configuring

Add this to your manifest file:

```xml
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
        <uses-permission  android:name="android.permission.ACCESS_NETWORK_STATE" />

        <service android:name="nirai.JobService" android:exported="false" />

        <receiver
            android:name="nirai.JobServiceTrigger"
            android:enabled="false" >
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
```

### Implement a job runner


```java
import nirai.JobRunner;
import nirai.exception.NetworkOfflineException;

public class SimpleJobRunner implements JobRunner {

    @Override
    public void run(final Context context, Map<String, Object> args) throws NetworkOfflineException {
        ThreadUtils.toastOnUiThread(context.getApplicationContext(), "simple job run! ;)", Toast.LENGTH_SHORT);
    }
}
```

When there's no network and you need to try the job again, simply throw a NetworkOfflineException.

```java
import nirai.JobRunner;
import nirai.exception.NetworkOfflineException;

public class NetworkJobRunner implements JobRunner {

    @Override
    public void run(Context context, Map<String, Object> args) throws NetworkOfflineException {

        try {
            /** some code that hits the network **/
            httpclient.execute(new HttpGet("http://www.google.com/"));
        } catch (ClientProtocolException e) {
            // can't recover, log and ignore it
            ThreadUtils.toastOnUiThread(context.getApplicationContext(), "network job ended with an unrecoverable error", Toast.LENGTH_SHORT);
        } catch (IOException e) {
            // try again
            ThreadUtils.toastOnUiThread(context.getApplicationContext(), "network job enqueued for later", Toast.LENGTH_SHORT);
            throw new NetworkOfflineException(); // try again later
        }
    }
}
```

### Posting jobs

```java
Job job = new Job(SimpleJobRunner.class);
JobService.post(context, job);
```

Adding arguments to each job is also possible by passing a map object to the Job constructor.

```java
HashMap<String, Object> args = new HashMap<String, Object>();
args.put("id", 1000);
Job job = new Job(SimpleJobRunner.class, args);
```

Download
--------

Gradle:
```
dependencies {
    compile 'jp.joao:nirai:0.1.0'
}
```

Maven:
```
<dependency>
	<groupId>jp.joao</groupId>
	<artifactId>nirai</artifactId>
	<version>0.1.0</version>
</dependency>
```

License
-------

    Copyright 2013 João Orui
    Copyright 2013 Yuki Fujisaki

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



Special thanks
--------------

Special thanks to Yuki Fujisaki for providing a base start for this project.

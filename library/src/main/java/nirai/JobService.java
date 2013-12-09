package nirai;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import nirai.exception.NetworkOfflineException;
import nirai.util.NetworkUtils;

public class JobService extends IntentService {

    public static final String TAG = "JobService";

    static final String ACTION_RETRY = "nirai.JobService.RETRY";
    static final String ACTION_POST = "nirai.JobService.POST";

    private static final String EXTRA_BACKOFF = "backoff";
    private static final String EXTRA_JOB = "job";


    private JobQueue mRetryQueue;
    private long mLastBackoffDelay;

    public JobService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRetryQueue = new JobQueue(this);
    }

    @Override
    public void onDestroy() {
        mRetryQueue.close();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLastBackoffDelay = 1000;
        if (isRetryTriggerIntent(intent)) {
            disableRetryTrigger();
            boolean result = retryJobs();
            if (!result) {
                enableRetryTrigger();
            }
        } else {
            Job job = Job.fromJson(intent.getStringExtra(EXTRA_JOB));
            if (job == null)
                return;

            if (!mRetryQueue.isEmpty()) {
                // There are jobs waiting in the queue.
                // Enqueue this job.
                mRetryQueue.enqueue(job);
                enableRetryTrigger();
            } else {
                // Process immediately.
                try {
                    processJob(job);
                } catch (NetworkOfflineException e) {
                    Log.i(TAG, "offering job to retry queue: " + job.getRunnerClass());
                    mRetryQueue.enqueue(job);
                    enableRetryTrigger();
                }
            }
        }
    }

    /**
     * Retry worker loop.
     *
     * @return true if all the job is done, false otherwise
     */
    private boolean retryJobs() {
        Log.i(TAG, "start retrying jobs");
        Job job = mRetryQueue.dequeue();
        while (job != null) {
            try {
                processJob(job);
            } catch (NetworkOfflineException e) {
                // enqueue into head of the list again
                mRetryQueue.enqueueFirst(job);
                return false;
            }
            job = mRetryQueue.dequeue();
        }
        return true;
    }

    private void processJob(Job job) throws NetworkOfflineException {
        JobRunner worker;
        try {
            Class clazz = Class.forName(job.getRunnerClass());
            worker = (JobRunner) clazz.newInstance();
        } catch (InstantiationException e) {
            Log.e(TAG, "cannot instantiate report worker: " + job.getRunnerClass(), e);
            return;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "cannot instantiate report worker: " + job.getRunnerClass(), e);
            return;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "cannot instantiate report worker: " + job.getRunnerClass(), e);
            return;
        }
        Log.i(TAG, "processing job: " + job.getRunnerClass());
        worker.run(this, job.getArguments());
    }

    private void disableRetryTrigger() {
        setConnectionReceiverBroadcastEnabled(false);
        Log.i(TAG, "disabling retry trigger");
    }

    private void enableRetryTrigger() {
        // activate broadcast
        setConnectionReceiverBroadcastEnabled(true);
        Log.i(TAG, "enabling retry trigger");

        if (NetworkUtils.isNetworkAvailable(this)) {
            Log.i(TAG, "with alarm, retry after: " + mLastBackoffDelay);

            // set alarm as well, with exponential back-off
            long backoffTimeMs = mLastBackoffDelay; // get back-off time from shared preferences
            long nextAttempt = SystemClock.elapsedRealtime() + backoffTimeMs;

            Intent retryIntent = new Intent(this, JobService.class);
            retryIntent.setAction(ACTION_RETRY);

            long nextBackoffTime = backoffTimeMs * 2;
            if (nextBackoffTime > 3600000)
                nextBackoffTime = 3600000;

            retryIntent.putExtra(EXTRA_BACKOFF, nextBackoffTime);

            PendingIntent retryPendingIntent = PendingIntent.getService(this, 0, retryIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME, nextAttempt, retryPendingIntent);
        }
    }

    private void setConnectionReceiverBroadcastEnabled(boolean isEnabled) {
        ComponentName receiver = new ComponentName(this, JobServiceTrigger.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                isEnabled
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private boolean isRetryTriggerIntent(Intent intent) {
        if (ACTION_RETRY.equals(intent.getAction())) {
            mLastBackoffDelay = intent.getLongExtra(EXTRA_BACKOFF, 1000);
            return true;
        }
        return false;
    }

    public static void post(Context context, Job job) {
        Intent i = new Intent(context, JobService.class);
        i.setAction(ACTION_POST);
        i.putExtra(EXTRA_JOB, job.toJson());
        context.startService(i);
    }

    public static void processRetryQueue(Context context) {
        Intent i = new Intent(context, JobService.class);
        i.setAction(ACTION_RETRY);
        context.startService(i);
    }

}

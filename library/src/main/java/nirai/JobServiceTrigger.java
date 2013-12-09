package nirai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class JobServiceTrigger extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                startService(context);
            }
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startService(context);
        }
    }

    private void startService(Context context) {
        Intent i = new Intent(context, JobService.class);
        i.setAction(JobService.ACTION_RETRY);
        context.startService(i);
    }

}

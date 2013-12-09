package jp.joao.nirai.sample;

import android.content.Context;
import android.widget.Toast;

import java.util.Map;

import nirai.JobRunner;
import nirai.exception.NetworkOfflineException;

public class SimpleJobRunner implements JobRunner {

    @Override
    public void run(final Context context, Map<String, Object> args) throws NetworkOfflineException {
        ThreadUtils.toastOnUiThread(context.getApplicationContext(), "simple job run! ;)", Toast.LENGTH_SHORT);
    }
}

package nirai;

import android.content.Context;

import java.util.Map;

import nirai.exception.NetworkOfflineException;

public interface JobRunner {

    public void run(Context context, Map<String, Object> args)throws NetworkOfflineException;

}

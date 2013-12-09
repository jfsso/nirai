package nirai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class Job {
    private static final Gson sGSON = new GsonBuilder().create();

    protected String mRunnerClass;
    protected Map<String, Object> mArguments;

    public Job() {
    }

    public Job(Class<? extends JobRunner> runnerClass) {
        this(runnerClass, new HashMap<String, Object>());
    }

    public Job(Class<? extends JobRunner> runnerClass, Map<String, Object> arguments) {
        mRunnerClass = runnerClass.getName();
        mArguments = arguments;
    }

    public String toJson() {
        return sGSON.toJson(this, Job.class);
    }

    public String getRunnerClass() {
        return mRunnerClass;
    }

    public Map<String, Object> getArguments() {
        return mArguments;
    }

    public Object getArgument(String key) {
        return mArguments.get(key);
    }

    public void putArgument(String key, Object value) {
        mArguments.put(key, value);
    }

    public static Job fromJson(String json) {
        return sGSON.fromJson(json, Job.class);
    }

    @Override
    public int hashCode() {
        int result = mRunnerClass != null ? mRunnerClass.hashCode() : 0;
        result = 31 * result + (mArguments != null ? mArguments.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (mArguments != null ? !mArguments.equals(job.mArguments) : job.mArguments != null)
            return false;
        if (mRunnerClass != null ? !mRunnerClass.equals(job.mRunnerClass) : job.mRunnerClass != null)
            return false;

        return true;
    }
}

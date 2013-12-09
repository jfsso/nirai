package nirai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class JobQueue {

    private JobStore mDb;

    private static final String TABLE_JOBS = "jobs";

    private static final String _ID = "_id";
    private static final String JOB = "job";
    private static final String CREATED_AT = "created_at";

    private static final String TAG = "JobQueue";

    public JobQueue(Context context) {
        mDb = new JobStore(context);
    }

    public void enqueueFirst(Job job) {
        if (job == null)
            return;

        SQLiteDatabase db = mDb.getWritableDatabase();
        Cursor res = null;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            if (!isEmpty(db)) {
                res = db.query(TABLE_JOBS, new String[]{
                        "min(" + _ID + ")"
                }, null, null, null, null, null);
                if (res != null && res.moveToFirst()) {
                    int head = res.getInt(0);
                    if (head >= 2)
                        values.put(_ID, head - 1);
                }
            }
            enqueue(db, values, job);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.w(TAG, "an error occured in database", e);
        } finally {
            if (res != null)
                res.close();
            db.endTransaction();
        }
    }

    public void enqueue(Job job) {
        if (job == null)
            return;

        SQLiteDatabase db = mDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            enqueue(db, values, job);
        } catch (SQLiteException e) {
            Log.w(TAG, "an error occured in database", e);
        }
    }

    private void enqueue(SQLiteDatabase db, ContentValues values, Job job) {
        values.put(JOB, job.toJson());
        values.put(CREATED_AT, System.currentTimeMillis());
        db.insert(TABLE_JOBS, null, values);
    }

    public Job dequeue() {
        SQLiteDatabase db = mDb.getWritableDatabase();
        Cursor res = null;
        db.beginTransaction();
        try {
            Job job = null;
            res = db.query(TABLE_JOBS, new String[]{
                    _ID, JOB
            }, null, null, null, null, _ID, "1");
            if (res != null && res.moveToFirst()) {
                long id = res.getLong(0);
                job = Job.fromJson(res.getString(1));
                db.delete(TABLE_JOBS, _ID + "=?", new String[]{
                        Long.toString(id)
                });
            }
            db.setTransactionSuccessful();
            return job;
        } catch (SQLiteException e) {
            Log.w(TAG, "an error occured in database", e);
            return null;
        } finally {
            if (res != null)
                res.close();
            db.endTransaction();
        }
    }

    private boolean isEmpty(SQLiteDatabase db) {
        Cursor res = null;
        try {
            res = db.query(TABLE_JOBS, new String[]{
                    "COUNT(*)"
            }, null, null, null, null, null);
            if (res != null && res.moveToFirst()) {
                long count = res.getLong(0);
                return count == 0;
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "an error occured in database", e);
        } finally {
            if (res != null)
                res.close();
        }
        return false;
    }

    public boolean isEmpty() {
        SQLiteDatabase db = mDb.getWritableDatabase();
        return isEmpty(db);
    }

    public void close() {
        mDb.close();
    }

    private static class JobStore extends SQLiteOpenHelper {

        public JobStore(Context context) {
            super(context, "nirai_job_queue", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_JOBS + " ("
                    + _ID + " integer primary key, "
                    + JOB + " text not null, "
                    + CREATED_AT + " integer not null)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
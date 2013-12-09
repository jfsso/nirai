package jp.joao.nirai.sample;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import nirai.JobRunner;
import nirai.exception.NetworkOfflineException;

public class NetworkJobRunner implements JobRunner {

    @Override
    public void run(Context context, Map<String, Object> args) throws NetworkOfflineException {

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet("http://www.google.com/"));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String responseString = out.toString();

                ThreadUtils.toastOnUiThread(context.getApplicationContext(), "request status: " + statusLine.getStatusCode(), Toast.LENGTH_SHORT);
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            // can't recover, log and ignore it
            ThreadUtils.toastOnUiThread(context.getApplicationContext(), "network job ended with an unrecoverable error", Toast.LENGTH_SHORT);
            Log.wtf("NetworkJobRunner", e);
        } catch (IOException e) {
            // try again
            ThreadUtils.toastOnUiThread(context.getApplicationContext(), "network job enqueued for later", Toast.LENGTH_SHORT);
            throw new NetworkOfflineException();
        }
    }
}

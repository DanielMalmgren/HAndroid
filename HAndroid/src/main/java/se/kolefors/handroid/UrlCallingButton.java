package se.kolefors.handroid;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by damal08 on 2014-01-09.
 */
public class UrlCallingButton extends Button implements View.OnClickListener {

    private String url;
    private int status;
    private TextView tv;

    public UrlCallingButton(Context context, String url, int status, TextView tv) {
        super(context);
        this.url = url;
        this.status = status;
        this.tv = tv;
        this.setOnClickListener(this);
    }

    public UrlCallingButton(Context context, String url) {
        super(context);
        this.url = url;
        this.status = 0;
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (status > 0)
            tv.setShadowLayer(10, 0, 0, Color.YELLOW);
        else
            tv.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        DownloadTask dxt = new DownloadTask();
        dxt.execute(url);
    }

    private void downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.getInputStream();
    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

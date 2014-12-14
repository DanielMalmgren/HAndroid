package se.kolefors.handroid;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Daniel Malmgren on 2014-01-06.
 */
abstract class PopulateFragment {

    public void setStartTag(String startTag) {
        this.startTag = startTag;
    }

    public void setItemTag(String itemTag) {
        this.itemTag = itemTag;
    }

    public void setApiFunctions(String apiFunctions) {
        this.apiFunctions = apiFunctions;
    }

    public void setApiIdSpecifier(String apiIdSpecifier) {
        this.apiIdSpecifier = apiIdSpecifier;
    }

    private View rootView;
    private String startTag;
    private String itemTag;
    private String apiFunctions;
    private String apiIdSpecifier;
    private List items;

    public PopulateFragment() {}

    public PopulateFragment(View rootView) {
        this.rootView = rootView;
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        Log.d("parse", "Parsing XML.");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    public void downloadAndCompleteRedraw(String url) throws XmlPullParserException, IOException {
        DownloadXmlTask dxt = new DownloadXmlTask();
        dxt.setComplete_redraw(true);
        Log.i("downloadAndCompleteRedraw", "Fetching XML from " + url+"do="+this.apiFunctions+"/get&output=xml");
        dxt.execute(url+"do="+this.apiFunctions+"/get&output=xml");
    }

    public void downloadAndUpdateStatuses(String url) throws XmlPullParserException, IOException {
        DownloadXmlTask dxt = new DownloadXmlTask();
        Log.i("downloadAndUpdateStatuses", "Fetching XML from " + url+"do="+this.apiFunctions+"/get&output=xml");
        dxt.execute(url+"do="+this.apiFunctions+"/get&output=xml");
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        return conn.getInputStream();
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List> {
        private DownloadXmlTask() {
            this.complete_redraw = false;
        }

        public void setComplete_redraw(boolean complete_redraw) {
            this.complete_redraw = complete_redraw;
        }

        private boolean complete_redraw;

        @Override
        protected List doInBackground(String... urls) {
            Log.d("DownloadXmlTask.doInBackground", "Fetching from " + urls[0]+".");
            try {
                return parse(downloadUrl(urls[0]));
            } catch (IOException e) {
                //e.printStackTrace();
                Log.d("DownloadXmlTask","got IOException.");
                return new ArrayList();
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                Log.d("DownloadXmlTask","got XmlPullParserException.");
                return new ArrayList();
            }
        }

        @Override
        protected void onPostExecute(List result) {
            if(complete_redraw)
                drawObjects(result);
            else
                updateObjects(result);
        }
    }

    abstract void drawObjects(List items);

    abstract void updateObjects(List items);

    public void updateOnOffObjects(List items) {
        this.items = items;
        Iterator it = items.iterator();
        while(it.hasNext())
        {
            Item currDevice = (Item)it.next();

            TextView tv = (TextView) rootView.findViewById(1000+currDevice.id);

            if(currDevice.type != null && currDevice.type.equals("absdimmer")) {
                tv.setShadowLayer(currDevice.status/10, 0, 0, Color.YELLOW);
                SeekBar seekBar = (SeekBar)rootView.findViewById(2000+currDevice.id);
                seekBar.setProgress(currDevice.status);
            } else {
                if (currDevice.status > 0)
                    tv.setShadowLayer(10, 0, 0, Color.YELLOW);
                else
                    tv.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
            }
        }
    }

    public void drawOnOffObjects(List items) {
        Log.d("drawOnOffObjects", "Doing a complete redraw.");
        this.items = items;
        Iterator it = items.iterator();
        TableLayout layout = (TableLayout) rootView.findViewById(R.id.fragment_layout);
        layout.removeAllViewsInLayout();
        TableLayout.LayoutParams lp;
        TableRow.LayoutParams rp;
        if(!it.hasNext()) {
            TextView tv = new TextView(ApplicationContextProvider.getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            tv.setTextColor(Color.BLACK);
            tv.setText(ApplicationContextProvider.getContext().getResources().getString(R.string.no_data_error));
            lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            //lp.setMargins(0,0,0,20);
            layout.addView(tv, lp);
        }
        while(it.hasNext())
        {
            Item currDevice = (Item)it.next();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ApplicationContextProvider.getContext());
            String url = settings.getString("ha_url", null)+"/api.php?do="+this.apiFunctions +"/toggle&"+this.apiIdSpecifier+"="+currDevice.id;
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);
            if(!username.equals("") && !password.equals(""))
                url = url + "&requireslogin=1&login_username="+username+"&login_password="+password;

            TableRow row = new TableRow(ApplicationContextProvider.getContext());
            layout.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1));

            TextView tv = new TextView(ApplicationContextProvider.getContext());
            tv.setId(1000+currDevice.id);
            tv.setText(currDevice.name);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            rp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1);
            rp.gravity = Gravity.BOTTOM;
            row.addView(tv, rp);
            Log.d("drawOnOffObjects", "Skriver namn "+currDevice.name);
            if(currDevice.type != null && currDevice.type.equals("absdimmer")) {
                tv.setShadowLayer(currDevice.status/10, 0, 0, Color.YELLOW);

                SeekBar seekBar = new SeekBar(ApplicationContextProvider.getContext());
                seekBar.setId(2000+currDevice.id);
                rp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                row.addView(seekBar, rp);
                TableRow.LayoutParams params = (TableRow.LayoutParams)seekBar.getLayoutParams();
                params.span = 2;
                seekBar.setLayoutParams(params);
                seekBar.setProgress(currDevice.status);
                seekBar.setOnSeekBarChangeListener(new seekBarListener(url+"&status=", tv));
            } else {
                if (currDevice.status > 0)
                    tv.setShadowLayer(10, 0, 0, Color.YELLOW);

                rp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                Button buttonOn = new UrlCallingButton(ApplicationContextProvider.getContext(), url+"&status=1", 1, tv);
                buttonOn.setText(ApplicationContextProvider.getContext().getResources().getString(R.string.on));
                row.addView(buttonOn, rp);

                Button buttonOff = new UrlCallingButton(ApplicationContextProvider.getContext(), url+"&status=0", 0, tv);
                buttonOff.setText(ApplicationContextProvider.getContext().getResources().getString(R.string.off));
                row.addView(buttonOff, rp);
            }
        }
    }

    private class seekBarListener implements SeekBar.OnSeekBarChangeListener {
        private String url;
        private int progress;
        private TextView tv;
        private seekBarListener(String url, TextView tv){
            this.url = url;
            this.tv = tv;
        }
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.progress = progress;
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i("onStopTrackingTouch", "Dimmer set to " + progress +".");
            tv.setShadowLayer(progress/10, 0, 0, Color.YELLOW);
            if (progress == 1) progress = 2;
            DownloadTask dxt = new DownloadTask();
            dxt.execute(url+progress);
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

    public void drawSingleButtonObjects(List items) {
        Log.d("drawSingleButtonObjects", "Doing a complete redraw.");
        this.items = items;
        Iterator it = items.iterator();
        TableLayout layout = (TableLayout) rootView.findViewById(R.id.fragment_layout);
        layout.removeAllViewsInLayout();
        TableLayout.LayoutParams lp;
        if(!it.hasNext()) {
            TextView tv = new TextView(ApplicationContextProvider.getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            tv.setTextColor(Color.BLACK);
            tv.setText(ApplicationContextProvider.getContext().getResources().getString(R.string.no_data_error));
            lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            layout.addView(tv, lp);
        }
        while(it.hasNext())
        {
            Item currItem  = (Item)it.next();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ApplicationContextProvider.getContext());
            String url = settings.getString("ha_url", null)+"/api.php?do="+this.apiFunctions +"/set&"+this.apiIdSpecifier+"="+currItem.id;
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);

            if(!username.equals("") && !password.equals(""))
                url = url + "&requireslogin=1&login_username="+username+"&login_password="+password;

            lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT);

            Button button = new UrlCallingButton(ApplicationContextProvider.getContext(), url);
            button.setText(currItem.name);
            if (currItem.status > 0)
                button.setShadowLayer(10, 0, 0, Color.YELLOW);
            button.setGravity(Gravity.CENTER_HORIZONTAL);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            layout.addView(button, lp);
        }
    }

    public void drawNoButtonObjects(List items) {
        Log.d("drawNoButtonObjects", "Doing a complete redraw.");
        this.items = items;
        Iterator it = items.iterator();
        TableLayout layout = (TableLayout) rootView.findViewById(R.id.fragment_layout);
        layout.removeAllViewsInLayout();
        TableLayout.LayoutParams lp;
        if(!it.hasNext()) {
            TextView tv = new TextView(ApplicationContextProvider.getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            tv.setTextColor(Color.BLACK);
            tv.setText(ApplicationContextProvider.getContext().getResources().getString(R.string.no_data_error));
            lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            layout.addView(tv, lp);
        }
        while(it.hasNext())
        {
            Item currItem  = (Item)it.next();
            TextView tv = new TextView(ApplicationContextProvider.getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            tv.setTextColor(Color.BLACK);
            tv.setText(currItem.name+": "+currItem.value+" "+currItem.unit);
            layout.addView(tv);
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, null, this.startTag);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(itemTag)) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Item readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, this.itemTag);
        String description = null;
        String type = null;
        int id = -1;
        int status = -1;
        float value = -1;
        String unit = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            String name = parser.getName();
            if (name.equals("name")) {
                description = readStringTag(parser, "name");
            } else if (name.equals("id")) {
                id = readIntTag(parser, "id");
            } else if (name.equals("status")) {
                status = readIntTag(parser, "status");
            } else if (name.equals("value")) {
                value = readFloatTag(parser, "value");
            } else if (name.equals("unit")) {
                unit = readStringTag(parser, "unit");
            } else if (name.equals("type")) {
                type = readStringTag(parser, "type");
            }else {
                skip(parser);
            }
        }
        return new Item(description, id, type, status, value, unit);
    }

    private int readIntTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        int tagContent = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, null, tag);
        return tagContent;
    }

    private float readFloatTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        float tagContent = Float.parseFloat(readText(parser));
        parser.require(XmlPullParser.END_TAG, null, tag);
        return tagContent;
    }

    private String readStringTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String tagContent = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return tagContent;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static class Item {
        public final String name;
        public final int id;
        public final String type;
        public final int status;
        public final float value;
        public final String unit;

        private Item(String name, int id, String type, int status, float value, String unit) {
            this.name = name;
            this.id = id;
            this.type = type;
            this.status = status;
            this.value = value;
            this.unit = unit;
        }
    }

    private static int getPixels(int dipValue){
        Resources r = ApplicationContextProvider.getContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                r.getDisplayMetrics());
        return px;
    }
}

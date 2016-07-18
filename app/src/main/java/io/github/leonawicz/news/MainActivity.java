package io.github.leonawicz.news;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.github.leonawicz.news.feed.FeedArticle;
import io.github.leonawicz.news.feed.FeedArticleAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "HttpExample";
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        list = (ListView) findViewById(R.id.list);
        Spinner spinner = (Spinner) findViewById(R.id.sections_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.content_sections, R.layout.spinner);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http").authority("content.guardianapis.com").appendPath("search")
                        .appendQueryParameter("from-date", currentDate)
                        .appendQueryParameter("to-date", currentDate)
                        .appendQueryParameter("order-by", "newest")
                        .appendQueryParameter("section", adapterView.getSelectedItem().toString().toLowerCase())
                        .appendQueryParameter("show-fields", "thumbnail")
                        .appendQueryParameter("show-tags", "contributor")
                        .appendQueryParameter("api-key", "test");

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    new DownloadJsonTask().execute(builder.build().toString());
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_net, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private class DownloadJsonTask extends AsyncTask<String, Void, String> {

        private ArrayList<FeedArticle> feedArticles = new ArrayList<>();

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Cannot download JSON data. Check your url.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            FeedArticle feedArticle;
            try {
                Log.v("result", result);
                JSONObject jsonRoot = new JSONObject(result).optJSONObject("response");
                JSONArray jsonArray = jsonRoot.optJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    feedArticle = new FeedArticle(
                            jsonObj.optString("webTitle"),
                            jsonObj.optJSONArray("tags").getJSONObject(0).optString("webTitle"),
                            jsonObj.optString("webPublicationDate"),
                            jsonObj.optString("webUrl"),
                            jsonObj.optJSONObject("fields").optString("thumbnail"));
                    feedArticles.add(feedArticle);
                }

                FeedArticleAdapter adapter = new FeedArticleAdapter(MainActivity.this, feedArticles);
                list.setAdapter(adapter);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        FeedArticle article = feedArticles.get(position);
                        openWebPage(article.getUrl());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String downloadUrl(String urlString) throws IOException {
        InputStream inStream = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            inStream = conn.getInputStream();
            return readStream(inStream);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    public String readStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.append(line);
        }
        return lines.toString();
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
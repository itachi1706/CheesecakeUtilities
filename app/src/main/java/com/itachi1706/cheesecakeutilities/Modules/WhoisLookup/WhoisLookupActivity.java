package com.itachi1706.cheesecakeutilities.Modules.WhoisLookup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.itachi1706.appupdater.Util.UpdaterHelper.HTTP_QUERY_TIMEOUT;

public class WhoisLookupActivity extends BaseActivity {

    private Button submitBtn;
    private EditText input;
    private TextView raw;

    @Override
    public String getHelpDescription() {
        return "This utility queries and retrieves WHOIS data if it is available";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whois_lookup);

        submitBtn = findViewById(R.id.whois_submit);
        input = findViewById(R.id.whois_entry);
        raw = findViewById(R.id.whois_entry);

        submitBtn.setOnClickListener(v -> clickBtn());
    }

    private void clickBtn() {
        String inputText = input.getText().toString();
        if (inputText.isEmpty()) {
            // TODO: Error Line in edittext
            Toast.makeText(this, "No input", Toast.LENGTH_LONG).show();
            return;
        }

        // Query
        new WhoisQuery().execute(inputText);
    }

    private void processResult(String result) {
        Gson gson = new Gson();
        WhoisObject whois = gson.fromJson(result, WhoisObject.class);
        raw.setText(whois.getRaw());
    }


    class WhoisQuery extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String url = "http://api.itachi1706.com/api/whois.php?domain=" + strings[0];
            String tmp;
            try {
                URL urlConn = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
                conn.setConnectTimeout(HTTP_QUERY_TIMEOUT);
                conn.setReadTimeout(HTTP_QUERY_TIMEOUT);
                InputStream in = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                {
                    str.append(line);
                }
                in.close();
                tmp = str.toString();

                runOnUiThread(() -> {
                    processResult(tmp);
                });;
            } catch (IOException e) {
                Log.e("WhoisQuery", "Exception: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}

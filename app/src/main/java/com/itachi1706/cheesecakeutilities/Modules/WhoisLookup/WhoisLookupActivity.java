package com.itachi1706.cheesecakeutilities.Modules.WhoisLookup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.DeprecationHelper;
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
    private TextInputEditText input;
    private TextView raw, general, availability;
    private TextInputLayout inputLayout;
    private LinearLayout availLayout;

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
        raw = findViewById(R.id.whois_raw_output);
        general = findViewById(R.id.whois_search);
        inputLayout = findViewById(R.id.whois_entry_til);
        availability = findViewById(R.id.whois_availability);
        availLayout = findViewById(R.id.whois_avail_layout);

        submitBtn.setOnClickListener(v -> clickBtn());
    }

    private void clickBtn() {
        inputLayout.setErrorEnabled(false);
        String inputText = input.getText().toString();
        if (inputText.isEmpty()) {
            inputLayout.setError("No Input Detected");
            inputLayout.setErrorEnabled(true);
            return;
        }

        // Query
        raw.setText(""); // Clear stuff
        general.setText("");
        availLayout.setVisibility(View.GONE);
        new WhoisQuery().execute(inputText);
    }

    private void processResult(String result) {
        Gson gson = new Gson();
        WhoisObject whois = gson.fromJson(result, WhoisObject.class);
        if (whois.getMsg() != 0) {
            // Error found
            if (whois.getError() != null) inputError(whois.getError());
            return;
        }
        if (!whois.getValiddomain()) {
            inputError("Invalid Domain Entered");
            return;
        }
        general.setText("Found " + whois.getDomain() + " from " + whois.getWhoisserver());
        availability.setText((whois.getAvailable()) ? "AVAILABLE" : "UNAVAILABLE");
        availability.setTextColor((whois.getAvailable()) ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
        availLayout.setVisibility(View.VISIBLE);
        raw.setText(whois.getRaw().replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<"));
    }

    private void inputError(String error) {
        inputLayout.setError(error);
        inputLayout.setErrorEnabled(true);
    }


    class WhoisQuery extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String url = "http://api.itachi1706.com/api/whois.php?domain=" + strings[0];
            Log.i("WhoisQuery", "Querying: " + url);
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

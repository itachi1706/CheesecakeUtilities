package com.itachi1706.cheesecakeutilities.modules.whoislookup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.lang.ref.WeakReference;

public class WhoisLookupActivity extends BaseModuleActivity {

    private Button submitBtn;
    private TextInputEditText input;
    private TextView raw, general, availability;
    private TextInputLayout inputLayout;
    private LinearLayout availLayout;
    private ProgressBar inProgress;

    @Override
    public String getHelpDescription() {
        return "This utility queries and retrieves WHOIS data for domains and provide information such as domain availability and information";
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
        inProgress = findViewById(R.id.whois_pb);

        handler = new WhoisHandler(this);

        submitBtn.setOnClickListener(this::clickBtn);
    }

    private void clickBtn(View v) {
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
        inProgress.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm != null && imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        new WhoisQuery(handler).execute(inputText);
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
        String genText = "Found " + whois.getDomain() + " from " + whois.getWhoisserver();
        if (whois.getSubwhois() != null) genText += " with additional data from " + whois.getSubwhois();
        general.setText(genText);
        availability.setText((whois.getAvailable()) ? "AVAILABLE" : "UNAVAILABLE");
        availability.setTextColor((whois.getAvailable()) ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
        availLayout.setVisibility(View.VISIBLE);
        inProgress.setVisibility(View.INVISIBLE);
        raw.setText(whois.getRaw().replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<"));
    }

    private void inputError(String error) {
        inputLayout.setError(error);
        inputLayout.setErrorEnabled(true);
        inProgress.setVisibility(View.INVISIBLE);
    }

    private WhoisHandler handler;

    static class WhoisHandler extends Handler {

        private final WeakReference<WhoisLookupActivity> mActivity;

        WhoisHandler(WhoisLookupActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WhoisLookupActivity activity = mActivity.get();
            if (activity == null) return; // Don't do anything
            switch (msg.what) {
                case WhoisQuery.WHOIS_RESULT:
                    activity.processResult((String) msg.obj);
                    break;
                default: super.handleMessage(msg); break;
            }
        }
    }
}

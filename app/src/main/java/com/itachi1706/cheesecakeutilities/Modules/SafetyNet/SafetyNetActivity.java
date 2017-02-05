package com.itachi1706.cheesecakeutilities.Modules.SafetyNet;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.scottyab.safetynet.SafetyNetHelper;
import com.scottyab.safetynet.SafetyNetResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.common.GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE;

public class SafetyNetActivity extends BaseActivity {

    private static final String API_KEY = BuildConfig.GOOGLE_VERIFICATION_API_KEY;

    private View loading;
    private static final String TAG = "SafetyNet";
    final SafetyNetHelper safetyNetHelper = new SafetyNetHelper(API_KEY);
    private TextView resultsTV;
    private TextView nonceTV;
    private TextView timestampTV;
    private View resultsContainer;
    private View sucessResultsContainer;
    private TextView packagenameTV;

    @Override
    public String getHelpDescription() {
        return "Does a basic validation check of SafetyNet. Obtained from the SafetyNet Sample project";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_net);

        resultsTV = (TextView)findViewById(R.id.results);
        nonceTV = (TextView)findViewById(R.id.nonce);
        timestampTV = (TextView)findViewById(R.id.timestamp);
        packagenameTV = (TextView)findViewById(R.id.packagename);
        resultsContainer = findViewById(R.id.resultsContainer);
        sucessResultsContainer = findViewById(R.id.sucessResultsContainer);
        loading = findViewById(R.id.loading);
        Button runTestBtn = (Button) findViewById(R.id.runTestButton);
        runTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });


    }

    private void runTest() {
        showLoading(true);
        Log.d(TAG, "Package: " + this.getPackageName());

        Log.d(TAG, "SafetyNet start request");
        safetyNetHelper.requestTest(this, new SafetyNetHelper.SafetyNetWrapperCallback() {
            @Override
            public void error(int errorCode, String errorMessage) {
                showLoading(false);
                handleError(errorCode, errorMessage);
            }

            @Override
            public void success(boolean ctsProfileMatch) {
                Log.d(TAG, "SafetyNet req success: ctsProfileMatch:" + ctsProfileMatch);
                showLoading(false);
                updateUIWithSucessfulResult(safetyNetHelper.getLastResponse());

            }
        });
    }

    private void handleError(int errorCode, String errorMsg) {
        Log.e(TAG, errorMsg);

        StringBuilder b=new StringBuilder();

        switch(errorCode){
            default:
            case SafetyNetHelper.SAFTYNET_API_REQUEST_UNSUCCESSFUL:
                b.append("SafetyNet request: fail\n");
                break;
            case SafetyNetHelper.RESPONSE_ERROR_VALIDATING_SIGNATURE:
                b.append("SafetyNet request: success\n");
                b.append("Response signature validation: error\n");
                break;
            case SafetyNetHelper.RESPONSE_FAILED_SIGNATURE_VALIDATION:
                b.append("SafetyNet request: success\n");
                b.append("Response signature validation: fail\n");
                break;
            case SafetyNetHelper.RESPONSE_VALIDATION_FAILED:
                b.append("SafetyNet request: success\n");
                b.append("Response validation: fail\n");
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                b.append("SafetyNet request: fail\n");
                b.append("\n*GooglePlayServices outdated*\n");
                try {
                    int v = getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
                    b.append("You are running version:\n").append(v).append("\nSafetyNet requires minimum:\n").append(GOOGLE_PLAY_SERVICES_VERSION_CODE).append("\n");
                } catch (Exception NameNotFoundException) {
                    b.append("Could not find Google Play Services on this device.\nThis Utility only supports Google Play devices");
                }
                break;
        }
        if (errorMsg.contains(API_KEY)) errorMsg = errorMsg.replace(API_KEY, "<API_KEY>");
        resultsTV.setText(b.toString() + "\nError Msg:\n" + errorMsg);

        sucessResultsContainer.setVisibility(View.GONE);
        revealResults(ContextCompat.getColor(this, R.color.problem));
    }

    private void showLoading(boolean show) {
        loading.setVisibility(show ? View.VISIBLE : View.GONE);
        if(show) {
            resultsContainer.setBackgroundColor(Color.TRANSPARENT);
            resultsContainer.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void revealResults(Integer colorTo){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            doPropertyAnimatorReveal(colorTo);
            resultsContainer.setVisibility(View.VISIBLE);
        }else{
            resultsContainer.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doPropertyAnimatorReveal(Integer colorTo) {
        Integer colorFrom = Color.TRANSPARENT;
        Drawable background = resultsContainer.getBackground();
        if (background instanceof ColorDrawable){
            colorFrom = ((ColorDrawable) background).getColor();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                resultsContainer.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private void updateUIWithSucessfulResult(SafetyNetResponse safetyNetResponse) {
        resultsTV.setText("SafetyNet request: success \nResponse validation: success\nCTS profile match: "
                + (safetyNetResponse.isCtsProfileMatch() ? "true" : "false"));

        sucessResultsContainer.setVisibility(View.VISIBLE);

        nonceTV.setText(safetyNetResponse.getNonce());

        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Date timeOfResponse = new Date(safetyNetResponse.getTimestampMs());
        timestampTV.setText(sim.format(timeOfResponse));
        packagenameTV.setText(safetyNetResponse.getApkPackageName());

        revealResults(ContextCompat.getColor(this, safetyNetResponse.isCtsProfileMatch() ? R.color.pass : R.color.fail));
    }

}

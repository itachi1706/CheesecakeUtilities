package com.itachi1706.cheesecakeutilities.Modules.SafetyNet;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;
import com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet.SafetyNetHelper;
import com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet.SafetyNetResponse;
import com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.common.GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE;

public class SafetyNetActivity extends BaseModuleActivity {

    private static final String API_KEY = BuildConfig.GOOGLE_VERIFICATION_API_KEY;

    private View loading;
    private static final String TAG = "SafetyNet";
    private SafetyNetHelper safetyNetHelper;

    private TextView resultsTV;
    private TextView nonceTV;
    private TextView timestampTV;
    private View resultsContainer;
    private View successResultsContainer;
    private TextView packageNameTV;
    private TextView resultNoteTV;
    private TextView welcomeTV;

    @Override
    public String getHelpDescription() {
        return "Does a basic validation check of SafetyNet. Obtained from the SafetyNet Sample project";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_net);

        safetyNetHelper = new SafetyNetHelper(API_KEY);
        LogHelper.d(TAG, "AndroidAPIKEY: " + Utils.getSigningKeyFingerprint(this) + ";" + getPackageName());

        resultsTV = findViewById(R.id.results);
        nonceTV = findViewById(R.id.nonce);
        timestampTV = findViewById(R.id.timestamp);
        packageNameTV = findViewById(R.id.packagename);
        resultsContainer = findViewById(R.id.resultsContainer);
        successResultsContainer = findViewById(R.id.sucessResultsContainer);
        resultNoteTV = findViewById(R.id.resultsNote);
        welcomeTV = findViewById(R.id.welcomeTV);
        loading = findViewById(R.id.loading);
        Button runTestBtn = findViewById(R.id.runTestButton);
        runTestBtn.setOnClickListener(v -> runTest());

        if (ConnectionResult.SUCCESS != GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
            handleError(0, "Google Play Services is not available on this device.\n\nThis SafetyNet test will not work");
        }
    }

    private void runTest() {
        showLoading(true);
        LogHelper.d(TAG, "Package: " + this.getPackageName());

        LogHelper.d(TAG, "SafetyNet start request");
        Trace safetyNetTrace = FirebasePerformance.getInstance().newTrace("safetynet_validation");
        safetyNetTrace.start();
        safetyNetHelper.requestTest(this, new SafetyNetHelper.SafetyNetWrapperCallback() {
            @Override
            public void error(int errorCode, String errorMessage) {
                showLoading(false);
                safetyNetTrace.stop();
                handleError(errorCode, errorMessage);
            }

            @Override
            public void success(boolean ctsProfileMatch, boolean basicIntegrity) {
                LogHelper.d(TAG, "SafetyNet req success: ctsProfileMatch:" + ctsProfileMatch + " and basicIntegrity, " + basicIntegrity);
                showLoading(false);
                updateUIWithSuccessfulResult(safetyNetHelper.getLastResponse());
                safetyNetTrace.stop();
            }
        });
    }

    private void handleError(int errorCode, String errorMsg) {
        LogHelper.e(TAG, errorMsg);

        StringBuilder b = new StringBuilder();

        switch(errorCode){
            default:
            case SafetyNetHelper.SAFETY_NET_API_REQUEST_UNSUCCESSFUL:
                b.append("SafetyNet request failed\n");
                b.append("(This could be a networking issue.)\n");
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
        resultsTV.setText(b.toString());
        resultNoteTV.setText(getString(R.string.safetynet_error, errorMsg));

        successResultsContainer.setVisibility(View.VISIBLE);
        welcomeTV.setVisibility(View.GONE);
        revealResults(ContextCompat.getColor(this, R.color.problem));
    }

    private void showLoading(boolean show) {
        loading.setVisibility(show ? View.VISIBLE : View.GONE);
        if(show) {
            resultsContainer.setBackgroundColor(Color.TRANSPARENT);
            resultsContainer.setVisibility(View.GONE);
            welcomeTV.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void revealResults(Integer colorTo){
        doPropertyAnimatorReveal(colorTo);
        resultsContainer.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doPropertyAnimatorReveal(Integer colorTo) {
        Integer colorFrom = Color.TRANSPARENT;
        Drawable background = resultsContainer.getBackground();
        if (background instanceof ColorDrawable) {
            colorFrom = ((ColorDrawable) background).getColor();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(animator -> resultsContainer.setBackgroundColor((Integer) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private void updateUIWithSuccessfulResult(SafetyNetResponse safetyNetResponse) {
        resultsTV.setText(getString(R.string.safetynet_results, safetyNetResponse.isCtsProfileMatch(), safetyNetResponse.isBasicIntegrity()));
        resultNoteTV.setText(R.string.safetynet_results_note);

        successResultsContainer.setVisibility(View.VISIBLE);

        nonceTV.setText(safetyNetResponse.getNonce());

        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Date timeOfResponse = new Date(safetyNetResponse.getTimestampMs());
        timestampTV.setText(sim.format(timeOfResponse));
        packageNameTV.setText(safetyNetResponse.getApkPackageName());

        revealResults(ContextCompat.getColor(this, safetyNetResponse.isCtsProfileMatch() ? R.color.pass : R.color.fail));
    }

}

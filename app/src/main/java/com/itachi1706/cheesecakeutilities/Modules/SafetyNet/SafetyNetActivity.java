package com.itachi1706.cheesecakeutilities.Modules.SafetyNet;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.itachi1706.cheesecakeutilities.BuildConfig;
import com.itachi1706.cheesecakeutilities.R;
import com.scottyab.safetynet.SafetyNetHelper;
import com.scottyab.safetynet.SafetyNetResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SafetyNetActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.GOOGLE_VERIFICATION_API_KEY;

    private View loading;
    private AlertDialog infoDialog;
    private static final String TAG = "SafetyNetHelperSAMPLE";
    final SafetyNetHelper safetyNetHelper = new SafetyNetHelper(API_KEY);
    private TextView resultsTV;
    private TextView nonceTV;
    private TextView timestampTV;
    private View resultsContainer;
    private boolean hasAnimated =false;
    private View sucessResultsContainer;
    private TextView packagenameTV;
    private Button runTestBtn;

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
        runTestBtn = (Button) findViewById(R.id.runTestButton);
        runTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });


    }

    private void runTest() {
        showLoading(true);

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
                    String vName = getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName.split(" ")[0];
                    b.append("You are running version:\n" + vName + " " + v + "\nSafetyNet requires minimum:\n7.3.27 7327000\n");
                } catch (Exception NameNotFoundException) {
                    b.append("Could not find GooglePlayServices on this device.\nPackage com.google.android.gms missing.");
                }
                break;
        }
        resultsTV.setText(b.toString() + "\nError Msg:\n" + errorMsg);

        sucessResultsContainer.setVisibility(View.GONE);
        revealResults(getResources().getColor(R.color.problem));
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
        resultsTV.setText("SafetyNet request: success \nResponse validation: success\nCTS profile match: "+ (safetyNetResponse.isCtsProfileMatch() ? "true" : "false"));

        sucessResultsContainer.setVisibility(View.VISIBLE);

        nonceTV.setText(safetyNetResponse.getNonce());

        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Date timeOfResponse = new Date(safetyNetResponse.getTimestampMs());
        timestampTV.setText(sim.format(timeOfResponse));
        packagenameTV.setText(safetyNetResponse.getApkPackageName());

        revealResults(getResources().getColor(safetyNetResponse.isCtsProfileMatch() ? R.color.pass : R.color.fail));
    }

}

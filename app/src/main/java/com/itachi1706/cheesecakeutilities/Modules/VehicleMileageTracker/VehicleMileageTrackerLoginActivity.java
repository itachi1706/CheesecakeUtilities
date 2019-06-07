package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker in CheesecakeUtilities
 */
@Deprecated
public class VehicleMileageTrackerLoginActivity extends BaseModuleActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar progress;
    private static final String TAG = "VehicleMileageLogin";

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private SharedPreferences sp;

    private static final int RC_SIGN_IN = 9001;

    @Override
    public String getHelpDescription() {
        return "An utility to track vehicle mileage\n\nNote: This is the login screen where you have " +
                "to login with a Google Account to continue as your mileage records will be saved based on your Google Account";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mileage_tracker_login);
        // Set up the login form.
        progress = findViewById(R.id.sign_in_progress);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        sp = PrefHelper.getDefaultSharedPreferences(this);
        SignInButton mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setSize(SignInButton.SIZE_WIDE);
        mEmailSignInButton.setOnClickListener(v -> {
            //Attempts to sign in with Google
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        mAuth = FirebaseAuth.getInstance();
        if (getIntent().hasExtra("logout") && getIntent().getBooleanExtra("logout", false)) {
            mAuth.signOut();
            if (sp.contains("firebase_uid")) sp.edit().remove("firebase_uid").apply();
        }

        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        if (firebaseRemoteConfig.getBoolean("veh_mileage_debug"))
            findViewById(R.id.test_account).setVisibility(View.VISIBLE);

        findViewById(R.id.test_account).setOnClickListener(v -> mAuth.signInWithEmailAndPassword("test@test.com", "test123").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                LogHelper.d(TAG, "signInTestEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                // If sign in fails, display a message to the user.
                LogHelper.w(TAG, "signInTestEmail:failure", task.getException());
                Toast.makeText(VehicleMileageTrackerLoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            LogHelper.d(TAG, "Sign In Result:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                firebaseAuthWithGoogle(acct);
            } else {
                // Signed out, show unauthenticated UI.
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        LogHelper.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progress.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        LogHelper.d(TAG, "signInWithGoogle:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        LogHelper.w(TAG, "signInWithGoogle:failure", task.getException());
                        Toast.makeText(VehicleMileageTrackerLoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                    progress.setVisibility(View.GONE);
                });
    }

    private void updateUI(FirebaseUser user) {
        progress.setVisibility(View.GONE);
        if (user != null) {
            // There's a user
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            sp.edit().putString("firebase_uid", user.getUid()).apply();
            Intent i = new Intent(this, VehicleMileageMainActivity.class);
            if (getIntent().hasExtra("globalcheck")) i.putExtra("globalcheck", getIntent().getBooleanExtra("globalcheck", false));
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Currently Logged Out", Toast.LENGTH_SHORT).show();
            sp.edit().remove("firebase_uid").apply();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        LogHelper.d(TAG, "onConnectionFailed:" + connectionResult);
        if (mAuth.getCurrentUser() == null)
            new AlertDialog.Builder(this).setTitle("Unable to connect to Google Servers")
                    .setMessage("We are unable to connect to Google Servers to sign you in, therefore this utility cannot be used")
                    .setCancelable(false).setPositiveButton(android.R.string.ok, (dialog, which) -> finish()).show();
    }
}


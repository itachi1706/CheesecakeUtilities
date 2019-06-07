package com.itachi1706.cheesecakeutilities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.itachi1706.cheesecakeutilities.Util.LogHelper

class FirebaseLoginActivity : BaseModuleActivity(), GoogleApiClient.OnConnectionFailedListener {

    companion object {
        private const val TAG: String = "FirebaseLoginActivity"
        private const val RC_SIGN_IN: Int = 9001
        private const val FB_UID: String = "firebase_uid"

        const val CONTINUE_INTENT: String = "forwardTo"
        const val HELP_EXTRA: String = "helpMessage"
    }

    private var message : String = "Firebase Login Activity"

    private lateinit var progress: ProgressBar
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var sp: SharedPreferences

    private var continueIntent: Intent? = null

    override val helpDescription: String
        get() = message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_login)

        // Setup login form
        progress = findViewById(R.id.sign_in_progress)
        progress.isIndeterminate = true
        progress.visibility = View.GONE
        val mEmailSignInButton = findViewById<SignInButton>(R.id.email_sign_in_button)
        mEmailSignInButton.setSize(SignInButton.SIZE_WIDE)
        mEmailSignInButton.setOnClickListener {
            // Attempts to sign in with Google
            Log.d(TAG, "Signing in with Google")
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this)

        // Setup Google Signin
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        if (intent.hasExtra("logout") && intent.getBooleanExtra("logout",  false)) {
            mAuth.signOut()
            if (sp.contains(FB_UID)) sp.edit().remove(FB_UID).apply()
        }
        if (intent.hasExtra(CONTINUE_INTENT)) continueIntent = intent.getParcelableExtra(CONTINUE_INTENT)
        if (intent.hasExtra(HELP_EXTRA)) message = intent.getStringExtra(HELP_EXTRA)

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
        val testAccount = findViewById<Button>(R.id.test_account)
        if (firebaseRemoteConfig.getBoolean("firebase_login_debug"))
            testAccount.visibility = View.VISIBLE

        testAccount.setOnClickListener{
            mAuth.signInWithEmailAndPassword("test@test.com", "test123").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in successful, update UI with the signed-in user's information
                LogHelper.d(TAG, "signInTestEmail:success")
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user
                LogHelper.w(TAG, "signInTestEmail:failure", task.exception!!)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        } }
    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            LogHelper.d(TAG, "Sign In Result: " + result.isSuccess)
            if (result.isSuccess) {
                // Signed in successfully, show authenticated UI
                val acct = result.signInAccount
                firebaseAuthWithGoogle(acct!!)
            } else {
                // Signed out, show unauthenticated UI
                updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        LogHelper.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
        progress.visibility = View.VISIBLE

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                LogHelper.d(TAG, "signInWithGoogle:success")
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user
                LogHelper.w(TAG, "signInWithGoogle:failure", task.exception!!)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
            progress.visibility = View.GONE
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        progress.visibility = View.GONE
        if (user != null) {
            // There's a user
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
            sp.edit().putString(FB_UID, user.uid).apply()
            if (continueIntent != null) {
                if (intent.hasExtra("globalcheck")) continueIntent!!.putExtra("globalcheck", intent.getBooleanExtra("globalcheck", false))
                startActivity(continueIntent!!)
                finish()
            } else Log.e(TAG, "No continue intent found")
        } else {
            Toast.makeText(this, "Currently Logged Out", Toast.LENGTH_SHORT).show()
            sp.edit().remove(FB_UID).apply()
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In will not be available.
        LogHelper.d(TAG, "onConnectionFailed: $connectionResult")
        if (mAuth.currentUser == null)
            AlertDialog.Builder(this).setTitle("Unable to connect to Google Servers")
                    .setMessage("We are unable to connect to Google Servers to sign you in, therefore login cannot be conducted")
                    .setCancelable(false).setPositiveButton(android.R.string.ok) { _, _ -> finish()}.show()
    }
}

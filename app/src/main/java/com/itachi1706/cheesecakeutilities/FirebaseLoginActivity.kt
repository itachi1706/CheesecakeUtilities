package com.itachi1706.cheesecakeutilities

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.itachi1706.cheesecakeutilities.Util.LogHelper

class FirebaseLoginActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val RC_SIGN_IN: Int = 9001
    private val TAG: String = "FirebaseLoginActivity"
    private val FB_UID: String = "firebase_uid"

    val message : String = "Firebase Login Activity"

    lateinit var progress: ProgressBar
    lateinit var mGoogleApiClient: GoogleApiClient
    val mAuth = FirebaseAuth.getInstance()
    val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

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
        mEmailSignInButton.setOnClickListener { v ->
            // Attempts to sign in with Google
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        if (intent.hasExtra("logout") && intent.getBooleanExtra("logout", false)) {
            mAuth.signOut()
            if (sp.contains(FB_UID)) sp.edit().remove(FB_UID).apply()
        }

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
        val testAccount = findViewById<Button>(R.id.test_account)
        if (firebaseRemoteConfig.getBoolean("firebase_login_debug"))
            testAccount.visibility = View.VISIBLE

        testAccount.setOnClickListener{ v -> mAuth.signInWithEmailAndPassword("test@test.com", "test123").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in successful, update UI with the signed-in user's information
                LogHelper.d(TAG, "signInTestEmail:success")
                val user = mAuth.currentUser
                // TODO: updateUI(user)
            } else {
                // If sign in fails, display a message to the user
                LogHelper.w(TAG, "signInTestEmail:failure", task.exception!!)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                // TODO: updateUI(null)
            }
        } }

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

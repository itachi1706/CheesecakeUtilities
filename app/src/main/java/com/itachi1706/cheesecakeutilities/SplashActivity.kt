package com.itachi1706.cheesecakeutilities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Do any long running stuff

        // Launch the main activity
        startActivity(Intent(this, MainMenuActivity::class.java))
        finish()
    }
}

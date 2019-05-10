package com.itachi1706.cheesecakeutilities.Modules.APKMirrorDownloader.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.preference.PreferenceFragment

import com.itachi1706.cheesecakeutilities.R
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

@Deprecated("Switched to using my own preference menu")
class PreferencesFragment : PreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        findPreference("apkmirror_github").setOnPreferenceClickListener {
            val githubIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/itachi1706/APKMirror"))
            startActivity(githubIntent)
            true
        }

        findPreference("apkmirror_libs").setOnPreferenceClickListener {
            val notices = Notices()
            notices.addNotice(Notice("jsoup: Java HTML Parser", "https://github.com/jhy/jsoup/", "Jonathan Hedley (jonathan@hedley.net)", MITLicense()))
            notices.addNotice(Notice("AndroidX Jetpack Support Libraries", "https://developer.android.com/jetpack/androidx", "Copyright (c) 2011 The Android Open Source Project", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("AdvancedWebView", "https://github.com/delight-im/Android-AdvancedWebView", "Copyright (c) delight.im (https://www.delight.im/)", MITLicense()))
            notices.addNotice(Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", MITLicense()))
            notices.addNotice(Notice("LicensesDialog", "http://psdev.de", "Copyright 2013 Philip Schiffer <admin@psdev.de>", ApacheSoftwareLicense20()))
            LicensesDialog.Builder(activity).setNotices(notices).setTitle(getString(R.string.libraries)).build().show()
            true
        }
    }

}


package com.itachi1706.cheesecakeutilities.modules.barcodeTools

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeHistoryFragment
import kotlinx.android.synthetic.main.activity_viewpager_frag.*

class BarcodeHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager_frag)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tab_layout.tabMode = TabLayout.MODE_FIXED
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL
        view_pager.adapter = BarcodeHistoryTabAdapter(this)

        val tabs = arrayOf("Scanned", "Generated")
        TabLayoutMediator(tab_layout, view_pager) { tab, position -> tab.text = tabs[position] }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) { finish(); true }
        else super.onOptionsItemSelected(item)
    }

    class BarcodeHistoryTabAdapter(val activity: FragmentActivity): FragmentStateAdapter(activity) {
        override fun getItemCount(): Int { return 2 }
        override fun createFragment(position: Int): Fragment {
            return BarcodeHistoryFragment.newInstance(getBarcodeString(activity.applicationContext, position), if (position == 0) BarcodeHelper.SP_BARCODE_SCANNED else BarcodeHelper.SP_BARCODE_GENERATED)
        }
    }

    companion object {
        @JvmStatic
        fun getBarcodeString(context: Context, pos: Int): String {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            return if (pos == 0) sp.getString(BarcodeHelper.SP_BARCODE_SCANNED, "")!!
            else sp.getString(BarcodeHelper.SP_BARCODE_GENERATED, "")!!
        }
    }
}

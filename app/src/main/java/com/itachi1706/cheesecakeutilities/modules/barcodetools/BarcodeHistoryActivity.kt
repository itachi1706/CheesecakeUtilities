package com.itachi1706.cheesecakeutilities.modules.barcodetools

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.itachi1706.cheesecakeutilities.databinding.ActivityViewpagerFragBinding
import com.itachi1706.cheesecakeutilities.modules.barcodetools.fragments.BarcodeHistoryFragment
import com.itachi1706.helperlib.helpers.PrefHelper

class BarcodeHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewpagerFragBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewpagerFragBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tabLayout.tabMode = TabLayout.MODE_FIXED
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        binding.viewPager.adapter = BarcodeHistoryTabAdapter(this)

        val tabs = arrayOf("Scanned", "Generated")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> tab.text = tabs[position] }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            val sp = PrefHelper.getSharedPreferences(context, "BarcodeHistory")
            return if (pos == 0) sp.getString(BarcodeHelper.SP_BARCODE_SCANNED, "")!!
            else sp.getString(BarcodeHelper.SP_BARCODE_GENERATED, "")!!
        }
    }
}

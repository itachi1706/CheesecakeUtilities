package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.databinding.ActivityLinearRecyclerviewBinding
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.objects.DualLineString
import com.itachi1706.cheesecakeutilities.recyclerAdapters.DualLineStringRecyclerAdapter
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper

class ScoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinearRecyclerviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinearRecyclerviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    private fun updateList() {
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "loadScores") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                val scoreList: ArrayList<DualLineString> = ArrayList()
                dataSnapshot.children.forEach {
                    val gpa = it.getValue(GpaScoring::class.java) ?: return@forEach
                    scoreList.add(DualLineString(gpa.name, gpa.description, gpa))
                }
                val adapter = DualLineStringRecyclerAdapter(scoreList, false)

                adapter.setOnClickListener { view ->
                    val viewHolder = view.tag as DualLineStringRecyclerAdapter.StringViewHolder
                    val pos = viewHolder.adapterPosition
                    LogHelper.d(TAG, "Score Item Clicked: $pos")
                    val score = scoreList[pos].extra as GpaScoring

                    val sb = StringBuilder()
                    sb.append("Short Name: ${score.shortname}\nDescription: ${score.description}\n\nGrade Tiers:\n")
                    if (score.gradetier.isEmpty()) sb.append("No Tiers Found")
                    else { score.gradetier.forEach {
                        if (score.type == "count") sb.append("${it.name}: ${it.value.toInt()} (${it.desc})\n")
                        else sb.append("${it.name}: ${it.value} (${it.desc})\n")
                    } }

                    if (!score.passtier.isNullOrEmpty()) {
                        sb.append("\nPass Tiers:\n")
                        score.passtier.forEach { sb.append("${it.name}: ${if (it.value > 0) "Pass" else "Fail"} (${it.desc})\n") }
                    }
                    AlertDialog.Builder(view.context).setTitle(score.name).setMessage(sb.toString()).setPositiveButton(
                        com.itachi1706.appupdater.R.string.dialog_action_positive_close, null).show()
                }
                binding.recyclerView.adapter = adapter
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "GpaCalcScoreActivity"
    }
}

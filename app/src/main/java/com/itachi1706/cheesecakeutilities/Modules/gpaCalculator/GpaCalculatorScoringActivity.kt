package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import com.itachi1706.cheesecakeutilities.objects.DualLineString
import kotlinx.android.synthetic.main.activity_linear_recyclerview.*

class GpaCalculatorScoringActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear_recyclerview)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recycler_view.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.itemAnimator = DefaultItemAnimator()
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    lateinit var adapter: DualLineStringRecyclerAdapter

    private fun updateList() {
        val scoreList: ArrayList<DualLineString> = ArrayList()
        adapter = DualLineStringRecyclerAdapter(scoreList, false)
        GpaCalculatorFirebaseUtils.getGpaDatabase().child(GpaCalculatorFirebaseUtils.FB_REC_SCORING).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                LogHelper.w(TAG, "loadScores:cancelled", databaseError.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                dataSnapshot.children.forEach {
                    val gpa = it.getValue(GpaScoring::class.java) ?: return@forEach
                    scoreList.add(DualLineString(gpa.name, gpa.description, gpa))
                }
                adapter.update(scoreList)
                recycler_view.adapter = adapter
            }
        })

        adapter.setOnClickListener { view ->
            val viewHolder = view.tag as DualLineStringRecyclerAdapter.StringViewHolder
            val pos = viewHolder.adapterPosition
            LogHelper.d(TAG, "DEBUG: Pos Clicked: $pos")
            val score = scoreList[pos].extra as GpaScoring

            val sb = StringBuilder()
            sb.append("Description: ${score.description}\n\nGrade Tiers:\n")
            if (score.gradetier.isEmpty()) sb.append("No Tiers Found")
            else {
                score.gradetier.forEach {
                    sb.append("${it.name}: ${it.value} (${it.desc})\n")
                }
            }

            if (!score.passtier.isNullOrEmpty()) {
                sb.append("\nPass Tiers:\n")
                score.passtier.forEach {
                    sb.append("${it.name}: ${it.value} (${it.desc})\n")
                }
            }
            AlertDialog.Builder(view.context).setTitle(score.name)
                    .setMessage(sb.toString()).setPositiveButton(R.string.dialog_action_positive_close, null).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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

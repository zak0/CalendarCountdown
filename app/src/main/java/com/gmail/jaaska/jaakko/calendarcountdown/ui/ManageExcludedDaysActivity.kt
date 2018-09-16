package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import kotlinx.android.synthetic.main.activity_manage_excluded_days.*
import kotlinx.android.synthetic.main.listitem_excluded_days.view.*

class ManageExcludedDaysActivity : AppCompatActivity() {

    private lateinit var settings: CountdownSettings
    private var adapter: ExcludedDaysRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_excluded_days)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // CountdownSettings should be in the extras, if not, then a crash is justified...
        settings = intent.getSerializableExtra(CountdownSettings.extraName) as CountdownSettings

        adapter = ExcludedDaysRecyclerViewAdapter()
        recyclerViewExcludedDays.layoutManager = LinearLayoutManager(this)
        recyclerViewExcludedDays.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))
        recyclerViewExcludedDays.adapter = adapter

        floatingActionButtonAddExcludedDays.setOnClickListener {
            AddExcludedDaysDialog(this@ManageExcludedDaysActivity, settings) {
                adapter?.notifyDataSetChanged()
                refreshTotalExcludedDaysCount()
            }.show()
        }

        refreshTotalExcludedDaysCount()
    }

    private fun refreshTotalExcludedDaysCount() {
        textViewCount.text = settings.getExcludedDaysCount().toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Pressing back will finish this activity and pass current CountdownSettings as params
     */
    override fun onBackPressed() {
        val data = Intent()
        data.putExtra(CountdownSettings.extraName, settings)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private inner class ExcludedDaysRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_excluded_days, parent, false)
            return ExcludedDaysViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val item = settings.excludedDays[position]
            val holder = viewHolder as ExcludedDaysViewHolder

            val fromString = DateUtil.databaseDateToUiDate(item.fromDate)
            val toString = DateUtil.databaseDateToUiDate(item.toDate)

            holder.itemView.context.apply {
                holder.itemView.textViewExclDateFrom.text = fromString
                holder.itemView.textViewExclDateTo.text = toString
                holder.itemView.textViewExclDaysCount.text = item.daysCount.toString()
            }

            holder.itemView.buttonDelete.setOnClickListener {
                // Confirm deletion with a dialog first.
                AlertDialog.Builder(this@ManageExcludedDaysActivity)
                        .setTitle(R.string.excluded_days_manager_confirm_delete_title)
                        .setMessage(getString(R.string.excluded_days_manager_confirm_delete_message,
                                item.daysCount,
                                fromString,
                                toString))
                        .setPositiveButton(R.string.common_yes) { _, _ ->
                            settings.excludedDays.remove(item)
                            adapter?.notifyDataSetChanged()
                            refreshTotalExcludedDaysCount()}
                        .setNegativeButton(R.string.common_no) { _, _ -> }
                        .show()
            }
        }

        override fun getItemCount(): Int {
            return settings.excludedDays.size
        }
    }

    private inner class ExcludedDaysViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)

    companion object {
        const val REQUEST_CODE_MANAGE_EXCLUDED_DAYS = 100
    }
}
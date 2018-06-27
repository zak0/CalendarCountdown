package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import kotlinx.android.synthetic.main.activity_manage_excluded_days.*
import kotlinx.android.synthetic.main.listitem_excluded_days.view.*
import java.util.*

class ManageExcludedDaysActivity : AppCompatActivity() {

    private lateinit var settings: CountdownSettings
    private var adapter: ExcludedDaysRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_excluded_days)

        // CountdownSettings should be in the extras, if not, then a crash is justified...
        settings = intent.getSerializableExtra(CountdownSettings.extraName) as CountdownSettings

        adapter = ExcludedDaysRecyclerViewAdapter()
        recyclerViewExcludedDays.layoutManager = LinearLayoutManager(this)
        recyclerViewExcludedDays.adapter = adapter

        floatingActionButtonAddExcludedDays.setOnClickListener {
            AddExcludedDaysDialog(this@ManageExcludedDaysActivity,
                    settings, recyclerViewExcludedDays).show()
        }
    }

    override fun onStop() {
        // Save the excluded days to DB
        DatabaseHelper(this, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION).apply {
            openDb()
            saveCountdownToDB(settings)
            closeDb()
        }
        super.onStop()
    }

    private inner class ExcludedDaysRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_excluded_days, parent, false)
            return ExcludedDaysViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val item = settings.excludedDays[position]
            val holder = viewHolder as ExcludedDaysViewHolder

            val from = Date(item.fromDate)
            val to = Date(item.toDate)
            val fromString = DateUtil.formatDate(from)
            val toString = DateUtil.formatDate(to)

            holder.itemView.context.apply {
                holder.itemView.textViewExclDateFrom.text = getString(R.string.setup_excluded_days_from, fromString)
                holder.itemView.textViewExclDateTo.text = getString(R.string.setup_excluded_days_to, toString)
                holder.itemView.textViewExclDaysCount.text = getString(R.string.setup_excluded_days_count, item.daysCount)
            }

            holder.itemView.setOnLongClickListener {
                // TODO: Notify user somehow (refresh the RecyclerView or atleast throw a Toast??)
                settings.excludedDays.remove(item)
                true
            }
        }

        override fun getItemCount(): Int {
            return settings.excludedDays.size
        }
    }

    private inner class ExcludedDaysViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}
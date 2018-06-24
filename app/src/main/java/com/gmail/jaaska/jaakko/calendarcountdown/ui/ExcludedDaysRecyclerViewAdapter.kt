package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.gmail.jaaska.jaakko.calendarcountdown.data.ExcludedDays
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import kotlinx.android.synthetic.main.listitem_excluded_days.view.*

import java.util.Date

/**
 * Created by jaakko on 24.6.2016.
 */
class ExcludedDaysRecyclerViewAdapter(private val items: MutableList<ExcludedDays>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_excluded_days, parent, false)
        return ExcludedDaysViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder = viewHolder as ExcludedDaysViewHolder
        holder.items = this.items
        holder.item = item

        val from = Date(item.fromDate)
        val to = Date(item.toDate)
        val fromString = DateUtil.formatDate(from)
        val toString = DateUtil.formatDate(to)

        holder.itemView.context.apply {
            holder.fromDate.text = getString(R.string.setup_excluded_days_from, fromString)
            holder.toDate.text = getString(R.string.setup_excluded_days_to, toString)
            holder.daysCount.text = getString(R.string.setup_excluded_days_count, item.daysCount)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private class ExcludedDaysViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        val fromDate: TextView = rootView.textViewExclDateFrom
        val toDate: TextView = rootView.textViewExclDateTo
        val daysCount: TextView = rootView.textViewExclDaysCount

        var items: MutableList<ExcludedDays>? = null
        var item: ExcludedDays? = null

        init {
            itemView.setOnLongClickListener {
                // TODO: Notify user somehow (refresh the RecyclerView or atleast throw a Toast??)
                items?.remove(item)
                true
            }
        }
    }
}

package zak0.github.calendarcountdown.ui

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import zak0.github.calendarcountdown.R
import zak0.github.calendarcountdown.data.CountdownSettings
import zak0.github.calendarcountdown.util.DateUtil
import kotlinx.android.synthetic.main.listitem_countdown.view.*

/**
 * Created by jaakko on 16.5.2016.
 */
class CountdownsRecyclerViewAdapter(private val items: List<CountdownSettings>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_countdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val vh = holder as ViewHolder

        vh.textViewEndDate.text = DateUtil.databaseDateToUiDate(item.endDate)
        vh.textViewDaysCount.text = "${item.daysToEndDate}"
        vh.textViewLabel.text = item.label
        vh.item = item
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount() - items count: " + Integer.toString(items.size))
        return items.size
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textViewDaysCount: TextView = itemView.textViewCountdownDaysToGo
        var textViewLabel: TextView = itemView.textViewCountdownLabel
        var textViewEndDate: TextView = itemView.textViewCountdownEndDate

        var item: CountdownSettings? = null

        init {
            itemView.setOnClickListener { v ->
                val setupIntent = Intent(v.context, SetupActivity::class.java)
                setupIntent.putExtra(CountdownSettings.extraName, item)
                v.context.startActivity(setupIntent)
            }

        }
    }

    companion object {
        private val TAG = CountdownsRecyclerViewAdapter::class.java.simpleName
    }
}

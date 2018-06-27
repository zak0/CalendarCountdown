package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import kotlinx.android.synthetic.main.activity_manage_excluded_days.*

class ManageExcludedDaysActivity : AppCompatActivity() {

    private lateinit var settings: CountdownSettings
    private var adapter: ExcludedDaysRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_excluded_days)

        // CountdownSettings should be in the extras, if not, then a crash is justified...
        settings = intent.getSerializableExtra(CountdownSettings.extraName) as CountdownSettings

        adapter = ExcludedDaysRecyclerViewAdapter(settings.excludedDays)
        recyclerViewExcludedDays.layoutManager = LinearLayoutManager(this)
        recyclerViewExcludedDays.adapter = adapter

        floatingActionButtonAddExcludedDays.setOnClickListener {
            AddExcludedDaysDialog(this@ManageExcludedDaysActivity,
                    settings, recyclerViewExcludedDays).show()
        }
    }
}
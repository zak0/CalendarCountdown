package com.gmail.jaaska.jaakko.calendarcountdown.storage

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.data.ExcludedDays
import com.gmail.jaaska.jaakko.calendarcountdown.data.GeneralSettings

import java.util.ArrayList

/**
 * Handler for all SQLite activity.
 * Reads and writes into the database.
 *
 *
 * Created by jaakko on 24.6.2018.
 */
class DatabaseHelper(context: Context,
                     name: String,
                     version: Int)
    : SQLiteOpenHelper(context, name, null, version) {

    // Member variables
    private var db: SQLiteDatabase? = null

    fun openDb() {
        Log.d(TAG, "openDb() - called")
        db = this.writableDatabase
    }

    fun closeDb() {
        Log.d(TAG, "closeDb() - called")
        db?.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        initSchema(db)
        Log.d(TAG, "onCreate() - done")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add general settings table
        if (oldVersion == 1 && newVersion > 1) {
            // create generalsettings table
            val sql = "CREATE TABLE '" + TBLGENERALSETTINGS + "' (" +
                    "'" + COLGSSORTBY + "' INTEGER)"
            db.execSQL(sql)

            // init general settings table with "default" values
            // note: this table only has one row
            this.db = db
            saveGeneralSettings()
        }
    }

    /**
     * Constructs empty tables. This is called when DB is first created.
     */
    private fun initSchema(db: SQLiteDatabase) {

        // create countdown table
        var sql = "CREATE TABLE '" + TBLCOUNTDOWN + "' (" +
                "`" + COLCOUNTDOWNID + "` INTEGER," +
                "`" + COLCDENDDATE + "` INTEGER," +
                "`" + COLCDEXCLUDEWEEKENDS + "` INTEGER," +
                "`" + COLCDLABEL + "` TEXT," +
                "`" + COLCDWIDGET + "` INTEGER," +
                "PRIMARY KEY(" + COLCOUNTDOWNID + ")" +
                ")"
        db.execSQL(sql)

        // create excludeddays table
        sql = "CREATE TABLE `" + TBLEXCLUDEDDAYS + "` (" +
                "`" + COLEXCLUDEDDAYSID + "` INTEGER," +
                "`" + COLCOUNTDOWNID + "` INTEGER," +
                "`" + COLEDFROMDATE + "` INTEGER," +
                "`" + COLEDTODATE + "` INTEGER," +
                "PRIMARY KEY(" + COLEXCLUDEDDAYSID + ")" +
                ")"
        db.execSQL(sql)

        // create generalsettings table
        sql = "CREATE TABLE '" + TBLGENERALSETTINGS + "' (" +
                "'" + COLGSSORTBY + "' INTEGER)"
        db.execSQL(sql)

        // init general settings table with "default" values
        // note: this table only has one row
        this.db = db
        saveGeneralSettings()

        Log.d(TAG, "initSchema() - done")
    }

    /**
     * Loads the Countdown that is set to be used on a widget.
     */
    fun loadSettingsForWidget(): CountdownSettings? {
        var ret: CountdownSettings? = null

        val sql = "select * from $TBLCOUNTDOWN where $COLCDWIDGET=1"

        db?.also { db ->
            val cur = db.rawQuery(sql, null)
            cur.moveToFirst()
            if (!cur.isAfterLast) {
                ret = cursorToCountdown(cur)
                ret?.also { loadExcludedDaysForCountdown(it) }
            }
        }

        return ret
    }

    fun saveGeneralSettings() {
        val gs = GeneralSettings

        // GeneralSettings table only has one row.
        // Empty the table before saving to ensure there stays only one...
        var sql = "delete from $TBLGENERALSETTINGS"
        db?.execSQL(sql)

        // ...Then insert the settings into the table.
        sql = "insert into " + TBLGENERALSETTINGS + " (" +
                COLGSSORTBY + ") values (" +
                gs.sortOrder + ")"
        db?.execSQL(sql)

    }

    fun loadGeneralSettings() {
        val gs = GeneralSettings

        // GeneralSettings table only has a one row
        val sql = "select * from $TBLGENERALSETTINGS"

        db?.also { db ->
            val cur = db.rawQuery(sql, null)
            cur.moveToFirst()
            if (!cur.isAfterLast) {
                gs.sortOrder = cur.getInt(0)
            }
            cur.close()
        }
    }

    /**
     * Reads and returns settings from database.
     */
    fun loadSettings(): List<CountdownSettings> {
        val ret = ArrayList<CountdownSettings>()

        val sql = "select * from $TBLCOUNTDOWN"

        db?.also { db ->
            val cur = db.rawQuery(sql, null)
            cur.moveToFirst()

            var countdown: CountdownSettings
            while (!cur.isAfterLast) {
                countdown = cursorToCountdown(cur)
                ret.add(countdown)

                // Load excluded date ranges for countdown
                loadExcludedDaysForCountdown(countdown)

                cur.moveToNext()
            }
        }

        Log.d(TAG, "loadSettings() - loaded " + Integer.toString(ret.size) + " countdowns from DB")

        return ret
    }

    /**
     * Loads and sets excluded date ranges for given countdown.
     */
    private fun loadExcludedDaysForCountdown(countdown: CountdownSettings) {
        val sql = "select * from " + TBLEXCLUDEDDAYS +
                " where " + COLCOUNTDOWNID + "=" + Integer.toString(countdown.dbId)

        db?.also { db ->
            val cur = db.rawQuery(sql, null)
            cur.moveToFirst()
            var exclDays: ExcludedDays
            while (!cur.isAfterLast) {
                exclDays = cursorToExcludedDays(cur)
                exclDays.setSettings(countdown) // this is null before setting
                countdown.addExcludedDays(exclDays)
                cur.moveToNext()
            }
        }
    }

    /**
     * Saves a given countdown into the database.
     */
    fun saveCountdownToDB(settings: CountdownSettings) {
        val list = ArrayList<CountdownSettings>()
        list.add(settings)
        saveToDB(list)
    }

    /**
     * Saves all settings given as parameter into DB.
     */
    private fun saveToDB(list: List<CountdownSettings>) {
        var sql: String

        // Iterate through all the settings.
        for (settings in list) {
            db?.also { db ->
                // Check if entry for current CountDownSettings already exists in DB
                sql = "select * from " + TBLCOUNTDOWN + " where " + COLCOUNTDOWNID + "=" + Integer.toString(settings.dbId)
                val cur = db.rawQuery(sql, null)
                if (cur.count > 0) {
                    // It id already exist --> update existing
                    Log.d(TAG, "saveToDB() - updating countdownid " + Integer.toString(settings.dbId))
                    sql = "update " + TBLCOUNTDOWN + " set " + COLCDENDDATE + "=" + java.lang.Long.toString(settings.endDate) + "," +
                            COLCDEXCLUDEWEEKENDS + "=" + (if (settings.isExcludeWeekends) "1" else "0") + "," +
                            COLCDLABEL + "='" + settings.label + "'," +
                            COLCDWIDGET + "=" + (if (settings.isUseOnWidget) "1" else "0") + " where " + COLCOUNTDOWNID + "=" + Integer.toString(settings.dbId)
                    db.execSQL(sql)
                } else {
                    // It did not exist --> insert a new entry
                    Log.d(TAG, "saveToDB() - inserting a new countdown entry")
                    sql = "insert into " + TBLCOUNTDOWN + "(" + COLCDENDDATE + "," + COLCDEXCLUDEWEEKENDS + "," + COLCDLABEL + "," + COLCDWIDGET + ") " +
                            "values(" + java.lang.Long.toString(settings.endDate) + "," + (if (settings.isExcludeWeekends) "1" else "0") + "," +
                            "'" + settings.label + "'," + (if (settings.isUseOnWidget) "1" else "0") + ")"
                    db.execSQL(sql)

                    // Update settings with corresponding rowID.
                    // (Used when inserting excluded ranges)
                    val rowIdCur = db.rawQuery("select last_insert_rowid();", null)
                    rowIdCur.moveToFirst()
                    settings.dbId = rowIdCur.getInt(0)
                    rowIdCur.close()
                }

                cur.close()

                // Save excluded date ranges.
                saveExcludedDaysOfCountdown(settings)
            }
        }
    }

    private fun saveExcludedDaysOfCountdown(countdown: CountdownSettings) {

        // First clear all the existing excluded ranges for the countdown.
        var sql = "delete from " + TBLEXCLUDEDDAYS +
                " where " + COLCOUNTDOWNID + "=" + Integer.toString(countdown.dbId)
        db?.execSQL(sql)

        db?.also { db ->
            for (excludedDays in countdown.excludedDays) {
                // Check if entry already exists.
                sql = "select * from " + TBLEXCLUDEDDAYS + " where " + COLEXCLUDEDDAYSID + "=" + Integer.toString(excludedDays.dbId)
                val cur = db.rawQuery(sql, null)

                if (cur.count > 0) {
                    // Entry did already exist --> update it.
                    Log.d(TAG, "saveExcludedDaysOfCountdown() - updating an excludeddays entry")
                    sql = "update " + TBLEXCLUDEDDAYS + " set " +
                            COLEDFROMDATE + "=" + java.lang.Long.toString(excludedDays.fromDate) + "," +
                            COLEDTODATE + "=" + java.lang.Long.toString(excludedDays.toDate) +
                            " where " + COLEXCLUDEDDAYSID + "=" + Integer.toString(excludedDays.dbId)
                    db.execSQL(sql)

                } else {
                    // Did not already exist in the database.
                    // --> insert a new one.

                    Log.d(TAG, "saveExcludedDaysOfCountdown() - inserting a new excludeddays entry")
                    sql = ("insert into " + TBLEXCLUDEDDAYS + "(" +
                            COLCOUNTDOWNID + "," +
                            COLEDFROMDATE + "," +
                            COLEDTODATE + ") values ("
                            + Integer.toString(countdown.dbId) + "," +
                            java.lang.Long.toString(excludedDays.fromDate) + "," +
                            java.lang.Long.toString(excludedDays.toDate) + ")")

                    db.execSQL(sql)

                    // Update excludedDays with corresponding rowID.
                    val exclRowIdCur = db.rawQuery("select last_insert_rowid();", null)
                    exclRowIdCur.moveToFirst()
                    excludedDays.dbId = exclRowIdCur.getInt(0)
                    exclRowIdCur.close()
                }

                cur.close()
            }
        }
    }

    private fun cursorToCountdown(cur: Cursor): CountdownSettings {
        val ret = CountdownSettings()

        ret.dbId = cur.getInt(0)
        ret.endDate = cur.getLong(1)
        ret.isExcludeWeekends = cur.getInt(2) == 1
        ret.label = cur.getString(3)
        ret.isUseOnWidget = cur.getInt(4) == 1

        return ret
    }

    private fun cursorToExcludedDays(cur: Cursor): ExcludedDays {
        val ret = ExcludedDays()

        ret.dbId = cur.getInt(0)
        ret.fromDate = cur.getLong(2)
        ret.toDate = cur.getLong(3)

        return ret
    }

    /**
     * Daletes given countdown from the database.
     */
    fun deleteCountdown(settings: CountdownSettings) {
        // First delete excluded date ranges.
        var sql = "delete from " + TBLEXCLUDEDDAYS +
                " where " + COLCOUNTDOWNID + "=" + Integer.toString(settings.dbId)
        db?.execSQL(sql)

        // Then delete the countdown itself.
        sql = "delete from " + TBLCOUNTDOWN +
                " where " + COLCOUNTDOWNID + "=" + Integer.toString(settings.dbId)
        db?.execSQL(sql)
    }

    companion object {

        private val TAG = DatabaseHelper::class.java.simpleName

        const val DB_NAME = "calendarcountdown.db"
        const val DB_VERSION = 2

        private const val TBLCOUNTDOWN = "countdown"
        private const val TBLEXCLUDEDDAYS = "excludeddays"
        private const val TBLGENERALSETTINGS = "generalsettings"

        private const val COLCOUNTDOWNID = "countdownid"
        private const val COLCDENDDATE = "cdenddate"
        private const val COLCDEXCLUDEWEEKENDS = "cdexcludeweekends"
        private const val COLCDLABEL = "cdlabel"
        private const val COLCDWIDGET = "cdwidget"

        private const val COLEXCLUDEDDAYSID = "excludeddaysid"
        private const val COLEDFROMDATE = "edfromdate"
        private const val COLEDTODATE = "edtodate"

        private const val COLGSSORTBY = "gssortby"
    }
}

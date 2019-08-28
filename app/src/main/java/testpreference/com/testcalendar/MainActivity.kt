package testpreference.com.testcalendar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var calendarIdSpinner: Spinner? = null
    private var calendarIdTable: Hashtable<String, String>? = null
    private var newEventButton: Button? = null
    private var deleteEvent: Button? = null
    private var EditEvent: Button? = null
    private var cancelEvent: Button? = null
    private var eventExist: Button? = null
    private var addingInLoop: Button?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarIdSpinner = findViewById(R.id.calendarid_spinner) as Spinner?
        newEventButton = findViewById(R.id.newevent_button) as Button?
        deleteEvent = findViewById(R.id.deleteEvent) as Button?
        EditEvent = findViewById(R.id.editEvent) as Button?
        cancelEvent = findViewById(R.id.cancelEvent) as Button?
        eventExist = findViewById(R.id.eventExist) as Button?
        addingInLoop = findViewById(R.id.addingInLoop) as Button?
        newEventButton?.setOnClickListener {
            if (CalendarHelper.haveCalendarReadWritePermissions(this@MainActivity)) {
                addNewEvent()
            } else {
                CalendarHelper.requestCalendarReadWritePermission(this@MainActivity)
            }
        }
        addingInLoop?.setOnClickListener {
            for (i in 0..3){
                val oneHour = (1000 * 60 * 60).toLong()
                val tenMinutes = (1000 * 60 * 5).toLong()

                val oneHourFromNow = Date().time + oneHour
                val tenMinutesFromNow = Date().time + tenMinutes

                val calendar = Calendar.getInstance()

                calendar.add(Calendar.DAY_OF_YEAR, 1+i)
                val tomorrow = calendar.time


                CalendarHelper.MakeNewCalendarEntry(6590+i, this, "Test", "Add event", "Somewhere", tomorrow.time + tenMinutes,tomorrow.time + oneHour, false, true, CalendarHelper.getCalendarId(this), 3)

            }
        }
        deleteEvent?.setOnClickListener {
            CalendarHelper.deleteEvent(this@MainActivity, 6589)
        }
        eventExist?.setOnClickListener {
            CalendarHelper.checkEventsInCal(this@MainActivity, 6589)
        }

        EditEvent?.setOnClickListener {
            val oneHour = (1000 * 60 * 60).toLong()
            val tenMinutes = (1000 * 60 * 10).toLong()

            val calendar = Calendar.getInstance()

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = calendar.time

            CalendarHelper.editEvent(this@MainActivity, 6589,tomorrow.time + tenMinutes,tomorrow.time + oneHour)
        }
        cancelEvent?.setOnClickListener {
            CalendarHelper.cancelEvent(this@MainActivity, 6589)
        }

        calendarIdSpinner?.onItemSelectedListener = CustomOnItemSelectedListener()

    }

    override fun onResume() {
        super.onResume()
        if (calendarIdTable != null) {
            calendarIdTable!!.clear()
        }
        if (CalendarHelper.haveCalendarReadWritePermissions(this)) {
            //Load calendars
            calendarIdTable = CalendarHelper.listCalendarId(this)

            updateCalendarIdSpinner()

        }

    }

    private fun updateCalendarIdSpinner() {
        if (calendarIdTable == null) {
            return
        }

        val list = ArrayList<String>()

        val e = calendarIdTable!!.keys()
        while (e.hasMoreElements()) {
            val key = e.nextElement() as String
            list.add(key)
        }

        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        calendarIdSpinner!!.adapter = dataAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == CalendarHelper.CALENDARHELPER_PERMISSION_REQUEST_CODE) {
            if (CalendarHelper.haveCalendarReadWritePermissions(this)) {
                Toast.makeText(this, "Have Calendar Read/Write Permission.",
                        Toast.LENGTH_LONG).show()
                updateCalendarIdSpinner()
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addNewEvent() {
        if (calendarIdTable == null) {
            Toast.makeText(this, "No calendars found. Please ensure at least one google account has been added.",
                    Toast.LENGTH_LONG).show()
            //Load calendars
            calendarIdTable = CalendarHelper.listCalendarId(this)

            updateCalendarIdSpinner()

            return
        }

        val oneHour = (1000 * 60 * 60).toLong()
        val tenMinutes = (1000 * 60 * 5).toLong()

        val oneHourFromNow = Date().time + oneHour
        val tenMinutesFromNow = Date().time + tenMinutes

        CalendarHelper.MakeNewCalendarEntry(6589, this, "Test", "Add event", "Somewhere", tenMinutesFromNow, tenMinutesFromNow + oneHour, false, true, CalendarHelper.getCalendarId(this), 3)

    }

    companion object {

        private val TAG = "MainActivity"
    }


}

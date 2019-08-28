package testpreference.com.testcalendar

/**
 * Created by cyong on 11/05/16.
 */

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Reminders
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast

import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Hashtable
import java.util.TimeZone

object CalendarHelper {

    //Remember to initialize this activityObj first, by calling initActivityObj(this) from
    //your activity
    private val TAG = "CalendarHelper"
    val CALENDARHELPER_PERMISSION_REQUEST_CODE = 99

    fun MakeNewCalendarEntry(eventid: Int, caller: Activity, title: String, description: String, location: String, startTime: Long, endTime: Long, allDay: Boolean, hasAlarm: Boolean, calendarId: Int, selectedReminderValue: Int) {
        if (!checkEventsInCal(caller,eventid)){
            val cr = caller.contentResolver
            val values = ContentValues()
            values.put(Events.DTSTART, startTime)
            values.put(Events.DTEND, endTime)
            values.put(Events.TITLE, title)
            values.put(Events._ID, eventid)
            values.put(Events.DESCRIPTION, description)
            values.put(Events.CALENDAR_ID, calendarId)
            values.put(Events.STATUS, Events.STATUS_CONFIRMED)


            if (allDay) {
                values.put(Events.ALL_DAY, true)
            }

            if (hasAlarm) {
                values.put(Events.HAS_ALARM, true)
            }

            //Get current timezone
            values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            Log.i(TAG, "Timezone retrieved=>" + TimeZone.getDefault().id)
            if (ActivityCompat.checkSelfPermission(caller, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            val uri = cr.insert(Events.CONTENT_URI, values)
            // get the event ID that is the last element in the Uri
            if (uri != null) {
                val eventID = java.lang.Long.parseLong(uri.lastPathSegment)
                if (hasAlarm) {
                    val reminders = ContentValues()
                    reminders.put(Reminders.EVENT_ID, eventID)
                    reminders.put(Reminders.METHOD, Reminders.METHOD_ALARM)
                    reminders.put(Reminders.MINUTES, selectedReminderValue)

                    val uri2 = cr.insert(Reminders.CONTENT_URI, reminders)
                }
            }
        }
    }

    fun checkEventsInCal(context: Context, eventid: Int) : Boolean {
        val cr = context.contentResolver
        val cursor = cr.query(Uri.parse("content://com.android.calendar/events"), arrayOf("_id"), "_id=?", arrayOf("" + eventid + ""), null)
        if (cursor!!.moveToFirst()) {
            Toast.makeText(context, "Event Exists!", Toast.LENGTH_SHORT).show()
            return true
        } else {
            Toast.makeText(context, "Event Doesn't Exist!", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun requestCalendarReadWritePermission(caller: Activity) {
        val permissionList = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_CALENDAR)

        }

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CALENDAR)

        }

        if (permissionList.size > 0) {
            val permissionArray = arrayOfNulls<String>(permissionList.size)

            for (i in permissionList.indices) {
                permissionArray[i] = permissionList[i]
            }

            ActivityCompat.requestPermissions(caller,
                    permissionArray,
                    CALENDARHELPER_PERMISSION_REQUEST_CODE)
        }

    }

    fun deleteEvent(c: Context, eventId: Int) {
        val CALENDAR_URI = Uri.parse("content://com.android.calendar/events")
        val uri = ContentUris.withAppendedId(CALENDAR_URI, eventId.toLong())
        c.contentResolver.delete(uri, null, null)
    }

    fun editEvent(context: Context, eventId: Int, startTime: Long, endTime: Long) {
        val cr = context.contentResolver
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.toLong())
        val event = ContentValues()
        event.put(CalendarContract.Events.TITLE, "Date Changed")
        event.put(CalendarContract.Events.DESCRIPTION, "New event updated")
        event.put(Events.DTSTART, startTime)
        event.put(Events.DTEND, endTime)
        event.put(Events.STATUS, Events.STATUS_CONFIRMED)
        cr.update(eventUri, event, null, null)
    }

    fun cancelEvent(context: Context, eventId: Int) {
        val cr = context.contentResolver
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.toLong())
        val event = ContentValues()
        event.put(CalendarContract.Events.TITLE, "Event Cancelled")
        event.put(CalendarContract.Events.DESCRIPTION, "Event updated")
        event.put(Events.STATUS, Events.STATUS_CANCELED)
        cr.update(eventUri, event, null, null)
    }

    fun listCalendarId(c: Context): Hashtable<String, String>? {

        if (haveCalendarReadWritePermissions(c as Activity)) {

            val projection = arrayOf("_id", "calendar_displayName")
            val calendars: Uri
            calendars = Uri.parse("content://com.android.calendar/calendars")

            val contentResolver = c.getContentResolver()
            val managedCursor = contentResolver.query(calendars, projection, null, null, null)

            if (managedCursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var cont = 0
                val nameCol = managedCursor.getColumnIndex(projection[1])
                val idCol = managedCursor.getColumnIndex(projection[0])
                val calendarIdTable = Hashtable<String, String>()

                do {
                    calName = managedCursor.getString(nameCol)
                    calID = managedCursor.getString(idCol)
                    Log.v(TAG, "CalendarName:$calName ,id:$calID")
                    calendarIdTable[calName] = calID
                    cont++
                } while (managedCursor.moveToNext())
                managedCursor.close()

                return calendarIdTable
            }

        }

        return null
    }

    fun getCalendarId(context: Context): Int {

        var cursor: Cursor? = null
        val contentResolver = context.contentResolver
        val calendars = CalendarContract.Calendars.CONTENT_URI

        val EVENT_PROJECTION = arrayOf(CalendarContract.Calendars._ID, // 0
                CalendarContract.Calendars.ACCOUNT_NAME, // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2
                CalendarContract.Calendars.OWNER_ACCOUNT, // 3
                CalendarContract.Calendars.IS_PRIMARY                     // 4
        )

        val PROJECTION_ID_INDEX = 0
        val PROJECTION_ACCOUNT_NAME_INDEX = 1
        val PROJECTION_DISPLAY_NAME_INDEX = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        val PROJECTION_VISIBLE = 4
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return -1
        }
        cursor = contentResolver.query(calendars, EVENT_PROJECTION, null, null, null)

        if (cursor!!.moveToFirst()) {
            var calName: String
            var calId: Long = 0
            var visible: String

            do {
                calName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX)
                calId = cursor.getLong(PROJECTION_ID_INDEX)
                visible = cursor.getString(PROJECTION_VISIBLE)
                if (visible == "1") {
                    return calId.toInt()
                }
                Log.e("Calendar Id : ", "$calId : $calName : $visible")
            } while (cursor.moveToNext())

            return calId.toInt()
        }
        return 1
    }

    fun haveCalendarReadWritePermissions(caller: Activity): Boolean {
        var permissionCheck = ContextCompat.checkSelfPermission(caller,
                Manifest.permission.READ_CALENDAR)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionCheck = ContextCompat.checkSelfPermission(caller,
                    Manifest.permission.WRITE_CALENDAR)

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

}
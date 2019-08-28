package testpreference.com.testcalendar;

/**
 * Created by cyong on 11/05/16.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

public class CalendarHelper {

    //Remember to initialize this activityObj first, by calling initActivityObj(this) from
//your activity
    private static final String TAG = "CalendarHelper";
    public static final int CALENDARHELPER_PERMISSION_REQUEST_CODE = 99;


    public static void MakeNewCalendarEntry(int eventid,Activity caller, String title, String description, String location, long startTime, long endTime, boolean allDay, boolean hasAlarm, int calendarId, int selectedReminderValue) {

        ContentResolver cr = caller.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startTime);
        values.put(Events.DTEND, endTime);
        values.put(Events.TITLE, title);
        values.put(Events._ID, eventid);
        values.put(Events.DESCRIPTION, description);
        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.STATUS, Events.STATUS_CONFIRMED);


        if (allDay) {
            values.put(Events.ALL_DAY, true);
        }

        if (hasAlarm) {
            values.put(Events.HAS_ALARM, true);
        }

        //Get current timezone
        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Log.i(TAG, "Timezone retrieved=>" + TimeZone.getDefault().getID());
        if (ActivityCompat.checkSelfPermission(caller, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Uri uri = cr.insert(Events.CONTENT_URI, values);
        // get the event ID that is the last element in the Uri
        if (uri != null) {
            long eventID = Long.parseLong(uri.getLastPathSegment());
            System.out.println("event id " + eventID + "}");
            if (hasAlarm) {
                ContentValues reminders = new ContentValues();
                reminders.put(Reminders.EVENT_ID, eventID);
                reminders.put(Reminders.METHOD, Reminders.METHOD_ALARM);
                reminders.put(Reminders.MINUTES, selectedReminderValue);

                Uri uri2 = cr.insert(Reminders.CONTENT_URI, reminders);
            }
        }


    }

    public static void checkEventsInCal(Context context, int eventid) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/events"), new String[]{"_id"}, "_id=?", new String[]{"" + eventid + ""}, null);
        if (cursor.moveToFirst()) {
            Toast.makeText(context, "Event Exists!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Event Doesn't Exist!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void requestCalendarReadWritePermission(Activity caller) {
        List<String> permissionList = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_CALENDAR);

        }

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CALENDAR);

        }

        if (permissionList.size() > 0) {
            String[] permissionArray = new String[permissionList.size()];

            for (int i = 0; i < permissionList.size(); i++) {
                permissionArray[i] = permissionList.get(i);
            }

            ActivityCompat.requestPermissions(caller,
                    permissionArray,
                    CALENDARHELPER_PERMISSION_REQUEST_CODE);
        }

    }

    public static void deleteEvent(Context c, int eventId) {
        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
        Uri uri = ContentUris.withAppendedId(CALENDAR_URI, eventId);
        c.getContentResolver().delete(uri, null, null);
    }

    public static void editEvent(Context context, int eventId) {
        final long oneHour = 1000 * 60 * 60;
        final long tenMinutes = 1000 * 60 * 10;


        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();


        ContentResolver cr = context.getContentResolver();
        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.TITLE, "Date Changed");
        event.put(CalendarContract.Events.DESCRIPTION, "New event updated");
        event.put(Events.DTSTART, tomorrow.getTime() + tenMinutes);
        event.put(Events.DTEND, tomorrow.getTime() + oneHour);
        event.put(Events.STATUS, Events.STATUS_CONFIRMED);
        cr.update(eventUri, event, null, null);
    }

    public static void cancelEvent(Context context, int eventId) {
        ContentResolver cr = context.getContentResolver();
        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.TITLE, "Event Cancelled");
        event.put(CalendarContract.Events.DESCRIPTION, "Event updated");
        event.put(Events.STATUS, Events.STATUS_CANCELED);
        cr.update(eventUri, event, null, null);
    }

    public static Hashtable listCalendarId(Context c) {

        if (haveCalendarReadWritePermissions((Activity) c)) {

            String projection[] = {"_id", "calendar_displayName"};
            Uri calendars;
            calendars = Uri.parse("content://com.android.calendar/calendars");

            ContentResolver contentResolver = c.getContentResolver();
            Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

            if (managedCursor.moveToFirst()) {
                String calName;
                String calID;
                int cont = 0;
                int nameCol = managedCursor.getColumnIndex(projection[1]);
                int idCol = managedCursor.getColumnIndex(projection[0]);
                Hashtable<String, String> calendarIdTable = new Hashtable<>();

                do {
                    calName = managedCursor.getString(nameCol);
                    calID = managedCursor.getString(idCol);
                    Log.v(TAG, "CalendarName:" + calName + " ,id:" + calID);
                    calendarIdTable.put(calName, calID);
                    cont++;
                } while (managedCursor.moveToNext());
                managedCursor.close();

                return calendarIdTable;
            }

        }

        return null;
    }

    public static int getCalendarId(Context context) {

        Cursor cursor = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT,                 // 3
                CalendarContract.Calendars.IS_PRIMARY                     // 4
        };

        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
        int PROJECTION_VISIBLE = 4;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return -1;
        }
        cursor = contentResolver.query(calendars, EVENT_PROJECTION, null, null, null);

        if (cursor.moveToFirst()) {
            String calName;
            long calId = 0;
            String visible;

            do {
                calName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                calId = cursor.getLong(PROJECTION_ID_INDEX);
                visible = cursor.getString(PROJECTION_VISIBLE);
                if(visible.equals("1")){
                    return (int)calId;
                }
                Log.e("Calendar Id : ", "" + calId + " : " + calName + " : " + visible);
            } while (cursor.moveToNext());

            return (int)calId;
        }
        return 1;
    }

    public static boolean haveCalendarReadWritePermissions(Activity caller) {
        int permissionCheck = ContextCompat.checkSelfPermission(caller,
                Manifest.permission.READ_CALENDAR);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionCheck = ContextCompat.checkSelfPermission(caller,
                    Manifest.permission.WRITE_CALENDAR);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }

        return false;
    }

}
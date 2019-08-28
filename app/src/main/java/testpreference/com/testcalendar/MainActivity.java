package testpreference.com.testcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Spinner calendarIdSpinner;
    private Hashtable<String, String> calendarIdTable;
    private Button newEventButton;
    private Button deleteEvent;
    private Button EditEvent;
    private Button cancelEvent;
    private Button eventExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarIdSpinner = (Spinner) findViewById(R.id.calendarid_spinner);
        newEventButton = (Button) findViewById(R.id.newevent_button);
        deleteEvent = (Button) findViewById(R.id.deleteEvent);
        EditEvent = (Button) findViewById(R.id.editEvent);
        cancelEvent = (Button) findViewById(R.id.cancelEvent);
        eventExist = (Button) findViewById(R.id.eventExist);
        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CalendarHelper.haveCalendarReadWritePermissions(MainActivity.this)) {
                    addNewEvent();
                } else {
                    CalendarHelper.requestCalendarReadWritePermission(MainActivity.this);
                }
            }
        });

        deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarHelper.deleteEvent(MainActivity.this, 6589);
            }
        });
        eventExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarHelper.checkEventsInCal(MainActivity.this, 6589);

            }
        });

        EditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarHelper.editEvent(MainActivity.this, 6589);
            }
        });
        cancelEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarHelper.cancelEvent(MainActivity.this, 6589);
            }
        });

        calendarIdSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        System.out.println("primary calendar id is " + CalendarHelper.getCalendarId(this) + "");


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (calendarIdTable != null) {
            calendarIdTable.clear();
        }
        if (CalendarHelper.haveCalendarReadWritePermissions(this)) {
            //Load calendars
            calendarIdTable = CalendarHelper.listCalendarId(this);

            updateCalendarIdSpinner();

        }

    }

    private void updateCalendarIdSpinner() {
        if (calendarIdTable == null) {
            return;
        }

        List<String> list = new ArrayList<String>();

        Enumeration e = calendarIdTable.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            list.add(key);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarIdSpinner.setAdapter(dataAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == CalendarHelper.CALENDARHELPER_PERMISSION_REQUEST_CODE) {
            if (CalendarHelper.haveCalendarReadWritePermissions(this)) {
                Toast.makeText(this, (String) "Have Calendar Read/Write Permission.",
                        Toast.LENGTH_LONG).show();
                updateCalendarIdSpinner();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void addNewEvent() {
        if (calendarIdTable == null) {
            Toast.makeText(this, (String) "No calendars found. Please ensure at least one google account has been added.",
                    Toast.LENGTH_LONG).show();
            //Load calendars
            calendarIdTable = CalendarHelper.listCalendarId(this);

            updateCalendarIdSpinner();

            return;
        }

        final long oneHour = 1000 * 60 * 60;
        final long tenMinutes = 1000 * 60 * 5;

        long oneHourFromNow = (new Date()).getTime() + oneHour;
        long tenMinutesFromNow = (new Date()).getTime() + tenMinutes;

        CalendarHelper.MakeNewCalendarEntry(6589,this, "Test", "Add event", "Somewhere", tenMinutesFromNow, tenMinutesFromNow + oneHour, false, true, CalendarHelper.getCalendarId(this), 3);

    }


}

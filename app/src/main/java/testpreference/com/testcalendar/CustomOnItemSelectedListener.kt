package testpreference.com.testcalendar

/**
 * Created by cyong on 25/05/16.
 */
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast

class CustomOnItemSelectedListener : OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        /*Toast.makeText(parent.getContext(),
                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_SHORT).show();*/
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onNohingSelected() called.")
    }

    companion object {

        private val TAG = "OnItemSelectedListener"
    }

}
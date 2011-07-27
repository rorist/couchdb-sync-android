package info.lamatricexiste.budiez;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class ContactsList extends ListActivity {

    private final static String NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        // Get data

        // Format data
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        // HashMap<String, String> map = new HashMap<String, String>();

        // Show data
        ListAdapter adapter = new SimpleAdapter(ContactsList.this, list, android.R.id.list,
                new String[] { NAME }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
    }
}

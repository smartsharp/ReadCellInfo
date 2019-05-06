package com.vargo.readcellinfo;

import android.content.Context;
import android.database.Cursor;
import android.preference.Preference;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RecordsActivity extends AppCompatActivity {

    private ListView lvDemo1;
    private MyCursorAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        lvDemo1 = (ListView)findViewById(R.id.lvDemo1);
        mAdapter = new MyCursorAdapter(this, VendorDatabase.getCellsCursor());
        lvDemo1.setAdapter(mAdapter);
    }

    public class MyCursorAdapter extends CursorAdapter {

        /**
         * @param context
         * @param c
         */
        public MyCursorAdapter(Context context, Cursor c) {
            super(context, c, false);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO Auto-generated method stub
            return LayoutInflater.from(context).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stub
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);

            String location = cursor.getString(cursor.getColumnIndex(VendorDatabaseUtil.TABLE_CELL_ITEM_LOCATION));
            String cells = cursor.getString(cursor.getColumnIndex(VendorDatabaseUtil.TABLE_CELL_ITEM_CELLS));
            String neighbors = cursor.getString(cursor.getColumnIndex(VendorDatabaseUtil.TABLE_CELL_ITEM_NEIGHBORS));
            StringBuilder sb = new StringBuilder();
            sb.append("Location:\n").append(location).append("\n");
            sb.append("Cells:\n").append(cells).append("\n");
            sb.append("Neighbors:\n").append(neighbors).append("\n");
            text1.setText(sb.toString());
        }

    }

}

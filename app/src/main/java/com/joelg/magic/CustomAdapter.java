package com.joelg.magic;

/**
 * Created by Joel on 4/5/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Joel on 3/24/2015.
 */
public class CustomAdapter extends ArrayAdapter<User> {
    Context context;
    int layoutResourceId;
    public ArrayList<User> users;

    public CustomAdapter(Context context, int layoutResourceId, ArrayList<User> users) {
        super(context, layoutResourceId, users);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TextView name_text;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            name_text = (TextView) row.findViewById(R.id.name_text);
            row.setTag(name_text);
        }
        else name_text = (TextView) row.getTag();

        final User user = users.get(position);
        final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkbox);
        name_text.setTextSize(32);
        name_text.setText(user.name);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) MainActivity.selectedNumbers.add(user.number);
                else MainActivity.selectedNumbers.remove(user.number);
            }
        });

        return row;
    }
}
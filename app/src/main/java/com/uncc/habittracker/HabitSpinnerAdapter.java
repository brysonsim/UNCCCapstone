package com.uncc.habittracker;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.uncc.habittracker.data.model.UserHabitDoc;

import java.util.ArrayList;

public class HabitSpinnerAdapter extends ArrayAdapter<UserHabitDoc> {
    private final ArrayList<UserHabitDoc> values;

    public HabitSpinnerAdapter(Context context, int textViewResourceId,
                       ArrayList<UserHabitDoc> values) {
        super(context, textViewResourceId, values);
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public UserHabitDoc getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView)super.getView(position, convertView, parent);
        label.setText(values.get(position).getNameOverride());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView)super.getDropDownView(position, convertView, parent);
        label.setText(values.get(position).getNameOverride());
        return label;
    }
}
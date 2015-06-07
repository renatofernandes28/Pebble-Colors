package com.example.renatofernandes.colors.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.renatofernandes.colors.R;
import com.example.renatofernandes.colors.objects.RGBColor;

import java.util.List;

/**
 * Created by renatofernandes on 15-06-06.
 */
public class RGBColorAdapter extends ArrayAdapter<RGBColor> {

    //Use ViewHolder to speed up population of listView
    private static class ViewHolder {
        TextView colorValue;
    }

    public RGBColorAdapter(Context context, int resource, List<RGBColor> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RGBColor rgbColor = getItem(position);

        ViewHolder holder;
        //If view is not created yet, we create it
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.rgb_color_item, parent, false);
            holder.colorValue = (TextView) convertView.findViewById(R.id.color_value);
            convertView.setTag(holder);
        }
        //Otherwise recycle
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Bold selected commands
        if(rgbColor.isSelected()){
            holder.colorValue.setTypeface(null, Typeface.BOLD);
        }
        else{
            holder.colorValue.setTypeface(null, Typeface.NORMAL);
        }

        //Populate string to show current components
        holder.colorValue.setText(getContext().getString(R.string.command_string,
                rgbColor.isAbsolute() ? "Absolute" : "Relative",
                rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue()));

        return convertView;
    }
}


package net.multiform_music.rss.ifeedgood.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.multiform_music.rss.ifeedgood.R;

import java.util.ArrayList;

/**
 * Created by michel.dio on 22/09/2017.
 *
 */

public class CustomSpinnerConfigFiltreCategoryAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Context activity;
    private ArrayList<String> asr;

    public CustomSpinnerConfigFiltreCategoryAdapter(Context context, ArrayList<String> asr) {
        this.asr=asr;
        activity = context;
    }

    public int getCount()
    {
        return asr.size();
    }

    public Object getItem(int i)
    {
        return asr.get(i);
    }

    public long getItemId(int i)
    {
        return (long)i;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView txt = new TextView(activity);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(15);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        txt.setText(asr.get(position));
        txt.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.spinnerCategoryBackgroundMenu));


        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {

        TextView txt = new TextView(activity);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(16);
        txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txt.setText(asr.get(i));
        txt.setTextColor(Color.parseColor("#ffffff" + ""));
        txt.setBackgroundColor(Color.parseColor("#586666" + ""));

        return  txt;
    }

    public ArrayList<String> getAsr() {
        return asr;
    }

    public void setAsr(ArrayList<String> asr) {
        this.asr = asr;
    }
}

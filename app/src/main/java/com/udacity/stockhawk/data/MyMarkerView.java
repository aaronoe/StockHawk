package com.udacity.stockhawk.data;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * Created by aaron on 17.03.17.
 */

public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private DateFormat mDataFormat;
    private Date mDate;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
        this.mDataFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
        this.mDate = new Date();
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        long currentTimestamp = (long) e.getX();

        tvContent.setText("" + e.getY() + "$ on: " + getTimedate(currentTimestamp));

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }


    private String getTimedate(long timestamp){

        try{
            mDate.setTime(timestamp);
            return mDataFormat.format(mDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }


}
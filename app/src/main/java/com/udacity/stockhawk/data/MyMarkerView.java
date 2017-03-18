package com.udacity.stockhawk.data;

import android.content.Context;
import android.graphics.Canvas;
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

import timber.log.Timber;

/**
 *
 * Created by aaron on 17.03.17.
 */

public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private DateFormat mDataFormat;
    private Date mDate;
    private long referenceTimestamp;
    private int graphWidth;

    public MyMarkerView(Context context, int layoutResource,
                        long referenceTime, int graphWidth) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
        this.mDataFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
        this.mDate = new Date();
        this.referenceTimestamp = referenceTime;
        this.graphWidth = graphWidth;
    }

    // callbacks every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        long currentTimestamp = (long) e.getX();

        tvContent.setText("" + e.getY() + "$ on: " + getTimedate(currentTimestamp + referenceTimestamp));

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


    @Override
    public void draw(Canvas canvas, float posx, float posy) {
        // take offsets into consideration
        posx += getOffset().getX();
        posy += getOffset().getY();

        int min_offset = 0;

        Timber.d("Posx: " + posx + " - tvcontent: " + tvContent.getWidth() + " - graph: " + graphWidth);

        // AVOID OFFSCREEN
        if (posx + tvContent.getWidth() > graphWidth - 100) {
            posx = posx - (tvContent.getWidth() / 2);
        }
        if (posx < min_offset) {
            posx = min_offset;
        }

        if (posy - tvContent.getHeight() < 0) {
            posy = 0;
        }

        // translate to the correct position and draw
        canvas.translate(posx, posy);
        draw(canvas);
        canvas.translate(-posx, -posy);
    }

}
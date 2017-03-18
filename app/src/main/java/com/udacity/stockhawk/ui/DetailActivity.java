package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.HourAxisValueFormatter;
import com.udacity.stockhawk.data.MyMarkerView;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.udacity.stockhawk.R.id.change;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.chart) LineChart mLineChart;
    @BindView(R.id.symbol) TextView symbol;
    @BindView(R.id.price) TextView price;
    @BindView(change) TextView changeTextView;

    String historicalData;
    String stockSymbol;
    DecimalFormat dollarFormat;
    DecimalFormat dollarFormatWithPlus;
    DecimalFormat percentageFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");


        // get stock symbol
        Intent intentThatStartedActivity = getIntent();


        if (intentThatStartedActivity.hasExtra(getString(R.string.stock_intent_key))) {
            stockSymbol = intentThatStartedActivity.getStringExtra(getString(R.string.stock_intent_key));
            populateViews();
            extractData(stockSymbol, 40);
        }
    }


    private void populateViews(){

        Uri queryUri = Uri.withAppendedPath(Contract.Quote.URI, stockSymbol);
        Cursor result = getContentResolver().query(queryUri, null, null, null, null);
        if (result != null) {

            if (result.moveToFirst()) {
                historicalData = result.getString(Contract.Quote.POSITION_HISTORY);
                symbol.setText(result.getString(Contract.Quote.POSITION_SYMBOL));
                price.setText(dollarFormat.format(result.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = result.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = result.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    changeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    changeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
                }
                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                if (PrefUtils.getDisplayMode(this)
                        .equals(this.getString(R.string.pref_display_mode_absolute_key))) {
                    changeTextView.setText(change);
                } else {
                    changeTextView.setText(percentage);
                }
            }
            result.close();
        }
    }


    public void extractData(String symbol, int numberOfWeeks) {

        List<Entry> entries = new ArrayList<Entry>();


        String[] values = historicalData.split("\n");

        long referenceTimestamp = Long.parseLong(values[numberOfWeeks].split(",")[0]);


        for (int i = values.length < numberOfWeeks ? values.length : numberOfWeeks; i >= 0; i--) {

            long timestamp = Long.parseLong(values[i].split(",")[0]);
            float price = Float.parseFloat(values[i].split(",")[1].trim());

            long diffTimestamp = timestamp - referenceTimestamp;
            Timber.d(""+diffTimestamp);

            entries.add(new Entry(diffTimestamp, price));
        }

        IAxisValueFormatter xAxisformatter = new HourAxisValueFormatter(referenceTimestamp);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(xAxisformatter);

        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12);

        xAxis.setLabelRotationAngle(45);
        LineDataSet dataSet = new LineDataSet(entries, "Last 5 weeks for: " + symbol);
        dataSet.setColor(R.color.colorAccent);

        dataSet.setValueTextColor(R.color.colorPrimary);
        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);
        mLineChart.setExtraBottomOffset(5);

        MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(),
                R.layout.custom_marker_view);
        mLineChart.setMarker(myMarkerView);

        mLineChart.setDescription(null);

    }


}

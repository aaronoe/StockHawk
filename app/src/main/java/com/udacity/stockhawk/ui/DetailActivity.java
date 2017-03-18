package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.udacity.stockhawk.R.id.chart;

public class DetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindView(chart) LineChart mLineChart;
    @BindView(R.id.symbol) TextView symbol;
    @BindView(R.id.price) TextView price;
    @BindView(R.id.change) TextView changeTextView;
    @BindView(R.id.chart_selection_spinner) Spinner spinner;

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

        spinner.setOnItemSelectedListener(this);
        String[] categories = getResources().getStringArray(R.array.graph_choices);
        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

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
            //extractData(stockSymbol, 1000);
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

        List<Entry> entries = new ArrayList<>();

        String[] values = historicalData.split("\n");
        int valuesLength = values.length;

        if (numberOfWeeks > valuesLength) {
            Toast.makeText(this,
                    getString(R.string.error_less_data_available, valuesLength),
                    Toast.LENGTH_SHORT).show();
            numberOfWeeks = valuesLength - 1;
        }

        long referenceTimestamp = Long.parseLong(values[numberOfWeeks].split(",")[0]);


        for (int i = values.length < numberOfWeeks ? values.length : numberOfWeeks; i >= 0; i--) {

            long timestamp = Long.parseLong(values[i].split(",")[0]);
            float price = Float.parseFloat(values[i].split(",")[1].trim());

            long diffTimestamp = timestamp - referenceTimestamp;
            Timber.d(""+diffTimestamp);

            entries.add(new Entry(diffTimestamp, price));
        }

        IAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTimestamp);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);

        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12);

        xAxis.setLabelRotationAngle(60);
        LineDataSet dataSet = new LineDataSet(entries, "Last 5 weeks for: " + symbol);
        dataSet.setColor(Color.GRAY);
        dataSet.setDrawValues(false);

        mLineChart.getXAxis().setTextColor(Color.GRAY);
        mLineChart.getAxisLeft().setTextColor(Color.GRAY);
        mLineChart.getAxisRight().setTextColor(Color.GRAY);
        mLineChart.getLegend().setTextColor(Color.GRAY);


        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);
        mLineChart.setExtraBottomOffset(5);

        MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(),
                R.layout.custom_marker_view, referenceTimestamp, mLineChart.getWidth(), mLineChart.getHeight());
        mLineChart.setMarker(myMarkerView);

        mLineChart.setDescription(null);

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        mLineChart.clear();

        if (item.equals(getString(R.string.choice_one_month))) {
            extractData(stockSymbol, 4);
        } else if (item.equals(getString(R.string.choice_three_months))) {
            extractData(stockSymbol, 12);
        } else if (item.equals(getString(R.string.choice_six_months))) {
            extractData(stockSymbol, 24);
        } else if (item.equals(getString(R.string.choice_one_year))) {
            extractData(stockSymbol, 48);
        } else if (item.equals(getString(R.string.choice_two_years))) {
            extractData(stockSymbol, 96);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

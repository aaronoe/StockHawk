package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.HourAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.chart) LineChart mLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);


        Intent intentThatStartedActivity = getIntent();
        String stockSymbol;

        if (intentThatStartedActivity.hasExtra(getString(R.string.stock_intent_key))) {
            stockSymbol = intentThatStartedActivity.getStringExtra(getString(R.string.stock_intent_key));
            Toast.makeText(this, stockSymbol, Toast.LENGTH_SHORT).show();
            extractData(stockSymbol);
        }
    }


    public void extractData(String symbol) {

        Uri queryUri = Uri.withAppendedPath(Contract.Quote.URI, symbol);
        Cursor result = getContentResolver().query(queryUri, null, null, null, null);

        if (result != null) {

            if (result.moveToFirst()) {
                String historicalData = result.getString(Contract.Quote.POSITION_HISTORY);

                LongSparseArray hValues = new LongSparseArray();
                List<Entry> entries = new ArrayList<Entry>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                String[] values = historicalData.split("\n");

                long referenceTimestamp = Long.parseLong(values[29].split(",")[0]);

                //int k = 0;
                for (int i = values.length < 10 ? values.length : 10; i >= 0; i--) {
                    long timestamp = Long.parseLong(values[i].split(",")[0]);
                    float price = Float.parseFloat(values[i].split(",")[1].trim());

                    long diffTimestamp = timestamp - referenceTimestamp;
                    Timber.d(""+diffTimestamp);

                    //String formattedDate = sdf.format(new Date(timestamp));

                    entries.add(new Entry(diffTimestamp, price));
                    //k++;

                    hValues.put(timestamp, price);
                }

                IAxisValueFormatter xAxisformatter = new HourAxisValueFormatter(referenceTimestamp);
                XAxis xAxis = mLineChart.getXAxis();
                xAxis.setValueFormatter(xAxisformatter);
                xAxis.setLabelRotationAngle(0.5f);

                LineDataSet dataSet = new LineDataSet(entries, "Label");
                dataSet.setColor(R.color.colorAccent);

                dataSet.setValueTextColor(R.color.colorPrimary);
                LineData lineData = new LineData(dataSet);
                mLineChart.setData(lineData);


                for (int i = 0; i < hValues.size(); i++) {
                    Log.e(DetailActivity.class.getSimpleName(),
                            "i: " + i + ", key: " +  hValues.keyAt(i) + ", value: " + hValues.valueAt(i));
                }
            }

            result.close();
        }

    }


}

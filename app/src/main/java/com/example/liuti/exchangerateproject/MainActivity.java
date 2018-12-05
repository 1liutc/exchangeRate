package com.example.liuti.exchangerateproject;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.DataPointInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "ExchangeRate";
    static final String CurrencyToCheck = "CurrencyToCheck";
    static final String BaseCurrency = "BaseCurrency";
    private Bundle selection;

    private void initializeDefaultSelectionPreference() {
        SharedPreferences pref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        if (pref.contains(BaseCurrency)) {
            return;
        }
        StringBuilder str = new StringBuilder();
        str.append(R.id.CNY).append(",");
        str.append(R.id.EUR).append(",");
        str.append(R.id.GBP).append(",");
        str.append(R.id.JPY).append(",");
        pref.edit()
                .putString(CurrencyToCheck, str.toString())
                .putInt(BaseCurrency, R.id.base_USD)
                .apply();
    }

    private void readDefaultSelectionPreference() {
        SharedPreferences pref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

        ArrayList<Integer> currencyToCheck = new ArrayList<Integer>();
        String savedString = pref.getString(CurrencyToCheck, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        while (st.hasMoreTokens()) {
            currencyToCheck.add(Integer.parseInt(st.nextToken()));
        }

        selection = new Bundle();
        selection.putIntegerArrayList(CurrencyToCheck,currencyToCheck);
        selection.putInt(BaseCurrency, pref.getInt(BaseCurrency, View.NO_ID));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDefaultSelectionPreference();
        readDefaultSelectionPreference();

        final Button startSelectBaseCurrency = findViewById(R.id.startSelectCurrency);
        startSelectBaseCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, MainActivity.this.toString());
                Log.d(TAG, this.toString());
                Intent t = new Intent(MainActivity.this, SelectCurrency.class);
                t.putExtras(selection);
                Toast.makeText(MainActivity.this,selection.toString(),Toast.LENGTH_LONG).show();
                startActivityForResult(t,0,null);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Math.random()*40 - 20, Math.random()*400 - 200),
                new DataPoint(Math.random()*40 - 20, Math.random()*400 - 200),
                new DataPoint(Math.random()*40 - 20, Math.random()*400 - 200),
                new DataPoint(Math.random()*40 - 20, Math.random()*400 - 200),
        });

        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // set manual X bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-100);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-10);
        graph.getViewport().setMaxX(10);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.addSeries(series2);

        Toast.makeText(MainActivity.this, Integer.toString(resultCode), Toast.LENGTH_LONG).show();
    }
}

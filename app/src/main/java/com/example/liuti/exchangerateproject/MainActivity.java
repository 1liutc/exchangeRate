package com.example.liuti.exchangerateproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The only activity of exchangeRate.
 */
public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject>{
    /**
     * Default logging tag for messages from the main activity.
     */
    static final String TAG = "ExchangeRate";
    /**
     * Name tag to store and retrieve preferences.
     */
    @SuppressWarnings("CheckStyle")
    static final String Selected_Currencies = "Selected_Currencies", Base_Currency = "Base_Currency";

    /**
     * The array of currencies' abbreviations and their descriptions.<p>
     */
    private String[] currencyArr;
    /**
     * {@code selectedCurrencies} Stores the choice of currencies that we want to check rates.<p>
     * {@code tSelectedCurrencies} Temporarily stores the selected currencies.
     */
    private boolean[] selectedCurrencies, tSelectedCurrencies;
    /**
     * {@code selectedBaseCurrency} Stores the choice of base currency.<p>
     * {@code tSelectedCurrencies} Temporarily stores the selected base currency.
     */
    private int selectedBaseCurrency, tSelectedBaseCurrency;


    /**
     * Request queue for network requests.
     */
    private static RequestQueue requestQueue;

    /**
     * Put default selections into preference files.
     */
    @SuppressWarnings({"magicnumber", "CheckStyle"})
    private void initializeDefaultSelectionPreference() {
        SharedPreferences pref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
//        if (pref.contains(BaseCurrency)) {
//            return;
//        }
        StringBuilder str = new StringBuilder();
        str.append(1).append(",");
        str.append(2).append(",");
        str.append(3).append(",");
        str.append(4).append(",");
        pref.edit()
                .clear()
                .putString(Selected_Currencies, str.toString())
                .putInt(Base_Currency, 5)
                .apply();
    }

    /**
     * Read default selectin from preference files.
     */
    private void readDefaultSelectionPreference() {
        SharedPreferences pref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        String savedString = pref.getString(Selected_Currencies, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");

        selectedBaseCurrency = pref.getInt(Base_Currency, 0);
        selectedCurrencies = new boolean[currencyArr.length];
        tSelectedCurrencies = new boolean[currencyArr.length];
        while (st.hasMoreTokens()) {
            selectedCurrencies[Integer.parseInt(st.nextToken())] = true;
        }

        Toast.makeText(MainActivity.this, Arrays.toString(selectedCurrencies),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencyArr = getResources().getStringArray(R.array.currencies);
        initializeDefaultSelectionPreference();
        readDefaultSelectionPreference();

        requestQueue = Volley.newRequestQueue(this);

        //For building the single selection alert dialog of selectBaseCurrency only.
        @SuppressWarnings("CheckStyle")
        final AlertDialog.Builder singleSel_selectBaseCurrency
                = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.select_currency)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        selectedBaseCurrency = tSelectedBaseCurrency;
                        Toast.makeText(MainActivity.this, Integer.toString(selectedBaseCurrency), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                });
        @SuppressWarnings("CheckStyle")
        final DialogInterface.OnClickListener onSingleSelListener_selectBaseCurrency
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                tSelectedBaseCurrency = id;
                Toast.makeText(MainActivity.this, Integer.toString(tSelectedBaseCurrency), Toast.LENGTH_LONG).show();
            }
        };
        final Button selectBaseCurrency = findViewById(R.id.selectBaseCurrency);
        selectBaseCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                singleSel_selectBaseCurrency
                        .setSingleChoiceItems(R.array.currencies,
                                MainActivity.this.selectedBaseCurrency,
                                onSingleSelListener_selectBaseCurrency)
                        .create()
                        .show();
                Toast.makeText(MainActivity.this, Arrays.toString(selectedCurrencies), Toast.LENGTH_LONG).show();
            }
        });

        //For building the multi selection alert dialog of selectCurrencyToCheck only.
        @SuppressWarnings("CheckStyle")
        final AlertDialog.Builder multiSel_selectCurrencyToCheck
                = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.select_currency)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        System.arraycopy(tSelectedCurrencies, 0,
                                selectedCurrencies, 0, tSelectedCurrencies.length);
                        Toast.makeText(MainActivity.this, Arrays.toString(selectedCurrencies), Toast.LENGTH_LONG).show();
                        startAPICall();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                });
        @SuppressWarnings("CheckStyle")
        final DialogInterface.OnMultiChoiceClickListener onMultiSelListener_selectCurrencyToCheck
                = new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(final DialogInterface dialog, final int i, final boolean checked) {
                MainActivity.this.tSelectedCurrencies[i] = checked;
            }
        };
        final Button selectCurrencyToCheck = findViewById(R.id.selectCurrencyToCheck);
        selectCurrencyToCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                System.arraycopy(selectedCurrencies, 0,
                        tSelectedCurrencies, 0, selectedCurrencies.length);
                multiSel_selectCurrencyToCheck
                        .setMultiChoiceItems(R.array.currencies,
                                MainActivity.this.tSelectedCurrencies,
                                onMultiSelListener_selectCurrencyToCheck)
                        .create().
                        show();
            }
        });
    }

    /**
     * TODO: To make MainActivity implement onClickListener. Used for drawing the graph.
     */
    @SuppressWarnings("CheckStyle")
    public void onResponse(final JSONObject response) {

        Log.d(TAG, response.toString());
        try {
            JSONObject rates = response.getJSONObject("rates");
            String[] datesArr;
            {
                Iterator<String> itr = rates.keys();
                ArrayList<String> datesAL = new ArrayList<String>();
                while (itr.hasNext()) {
                    datesAL.add(itr.next());
                }
                datesArr = datesAL.toArray(new String[0]);
            }
            Arrays.sort(datesArr);

//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < datesArr.length; i++) {
//
//                sb.append(datesArr[i]);
//                sb.append(':');
//                sb.append(rates.getJSONObject(datesArr[i]).getDouble("USD"));
//                sb.append('\n');
//                if (i == datesArr.length / 2) {
//                    Log.d(TAG, sb.toString());
//                    sb.delete(0, sb.length());
//                }
//            }
//            Log.d(TAG, sb.toString());
        } catch (Exception e) {
//            Log.wtf(TAG, e.getMessage());
        }

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200),
                new DataPoint(Math.random() * 40 - 20, Math.random() * 400 - 200)
        });

        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(final Series series, final DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Series1: On Data Point clicked: " + dataPoint, Toast.LENGTH_SHORT).show();
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

        graph.removeAllSeries();
        graph.addSeries(series2);

        Toast.makeText(MainActivity.this, "0v0", Toast.LENGTH_LONG).show();
    }

    /**
     * Make an API call.
     */
    void startAPICall() {

        final int start = 0, countryCodeLen = 3;
        StringBuffer sb = new StringBuffer();
        sb.append("?start_at=");
        sb.append("2017-12-01");
        sb.append("&end_at=");
        sb.append("2018-12-01");
        sb.append("&base=");
        sb.append(currencyArr[selectedBaseCurrency], start, start + countryCodeLen);
        sb.append("&symbols=");
        for (int i = 0; i < selectedCurrencies.length; i++) {
            if (selectedCurrencies[i]) {
                sb.append(currencyArr[i], start, start + countryCodeLen);
                sb.append(',');
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            //noinspection CheckStyle
            sb.delete(sb.length() - 9, sb.length());
        }
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://api.exchangeratesapi.io/history"
                            + sb.toString(),
                    null,
                    MainActivity.this,
                    new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.error_message) + error.getLocalizedMessage(),
                            Toast.LENGTH_LONG);
                }
            });
            Toast.makeText(MainActivity.this,
                    Boolean.toString(jsonObjectRequest == null),
                    Toast.LENGTH_LONG).show();
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}

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
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.GridLabelRenderer;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The only activity of exchangeRate.
 */
public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject> {
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
     * {@code seleCurrencies} Stores the choice of currencies that we want to check rates.<p>
     * {@code tSeleCurrencies} Temporarily stores the selected currencies.
     */
    private boolean[] seleCurrencies, tSeleCurrencies;
    /**
     * An ArrayList that stores the index of selected currencies.
     */
    private ArrayList<Integer> selCurrencyLi;
    /**
     * {@code selectedBaseCurrency} Stores the choice of base currency.<p>
     * {@code tSeleCurrencies} Temporarily stores the selected base currency.
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
        seleCurrencies = new boolean[currencyArr.length];
        tSeleCurrencies = new boolean[currencyArr.length];
        while (st.hasMoreTokens()) {
            seleCurrencies[Integer.parseInt(st.nextToken())] = true;
        }

        Toast.makeText(MainActivity.this, Arrays.toString(seleCurrencies),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        currencyArr = getResources().getStringArray(R.array.currencies);
        initializeDefaultSelectionPreference();
        readDefaultSelectionPreference();

        requestQueue = Volley.newRequestQueue(this);

        selCurrencyLi = new ArrayList<Integer>();

        //For building the single selection alert dialog of selectBaseCurrency only.
        @SuppressWarnings("CheckStyle") final AlertDialog.Builder singleSel_selectBaseCurrency
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
        @SuppressWarnings("CheckStyle") final DialogInterface.OnClickListener onSingleSelListener_selectBaseCurrency
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
                Toast.makeText(MainActivity.this, Arrays.toString(seleCurrencies), Toast.LENGTH_LONG).show();
            }
        });

        //For building the multi selection alert dialog of selectCurrencyToCheck only.
        @SuppressWarnings("CheckStyle") final AlertDialog.Builder multiSel_selectCurrencyToCheck
                = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.select_currency)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        System.arraycopy(tSeleCurrencies, 0,
                                seleCurrencies, 0, tSeleCurrencies.length);
                        selCurrencyLi.clear();
                        for (int i = 0; i < seleCurrencies.length; i++) {
                            if (seleCurrencies[i]) {
                                selCurrencyLi.add(i);
                            }
                        }
                        Toast.makeText(MainActivity.this, Arrays.toString(seleCurrencies), Toast.LENGTH_LONG).show();
                        startAPICall();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                });
        @SuppressWarnings("CheckStyle") final DialogInterface.OnMultiChoiceClickListener onMultiSelListener_selectCurrencyToCheck
                = new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(final DialogInterface dialog, final int i, final boolean checked) {
                MainActivity.this.tSeleCurrencies[i] = checked;
            }
        };
        final Button selectCurrencyToCheck = findViewById(R.id.selectCurrencyToCheck);
        selectCurrencyToCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                System.arraycopy(seleCurrencies, 0,
                        tSeleCurrencies, 0, seleCurrencies.length);
                multiSel_selectCurrencyToCheck
                        .setMultiChoiceItems(R.array.currencies,
                                MainActivity.this.tSeleCurrencies,
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
        GraphView graph = findViewById(R.id.graph);
        graph.setVisibility(View.INVISIBLE);
        graph.removeAllSeries();

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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date[] dates = new Date[datesArr.length];
            for (int i = 0; i < datesArr.length; i++) {
                dates[i] = sdf.parse(datesArr[i]);
            }
            for (int i = 0; i < selCurrencyLi.size(); i++) {
                LineGraphSeries<DataPoint> ser = new LineGraphSeries<>();
                String symbol = currencyArr[selCurrencyLi.get(i)];
                for (int j = 0; j < datesArr.length; j++) {
                    DataPoint dp = new DataPoint(
                            dates[j],
                            rates.getJSONObject(datesArr[j]).getDouble(symbol));
                    ser.appendData(dp, true, datesArr.length, true);
                }
                ser.setTitle(symbol);
                graph.addSeries(ser);
            }
        } catch (Exception e) {
        }

        GridLabelRenderer lblRenderer = graph.getGridLabelRenderer();
        lblRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(MainActivity.this, new SimpleDateFormat("M/yy")));
        lblRenderer.setVerticalAxisTitle("ExchangeRate");
        lblRenderer.setHorizontalAxisTitle("Time");
        lblRenderer.setNumHorizontalLabels(5);


        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        Toast.makeText(MainActivity.this, "0v0", Toast.LENGTH_LONG).show();
    }

    /**
     * Make an API call.
     */
    void startAPICall() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        StringBuffer sb = new StringBuffer();
        sb.append("?start_at=");
        sb.append("2017-12-01");
        sb.append("&end_at=");
        sb.append("2018-12-01");
        sb.append("&base=");
        sb.append(currencyArr[selectedBaseCurrency]);
        sb.append("&symbols=");
        for (int i = 0; i < seleCurrencies.length; i++) {
            if (seleCurrencies[i]) {
                sb.append(currencyArr[i]);
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

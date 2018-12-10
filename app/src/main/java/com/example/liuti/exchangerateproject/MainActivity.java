package com.example.liuti.exchangerateproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.GridLabelRenderer;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private Button selectBaseCurrency, selectCurrencyToCheck;

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
     * {@code selectedBase} Stores the choice of base currency.<p>
     * {@code tSeleCurrencies} Temporarily stores the selected base currency.
     */
    private int selectedBase, tselectedBase;

    /**
     * Request queue for network requests.
     */
    private static RequestQueue requestQueue;

    /**
     * Put default selections into preference files.
     */
    @SuppressWarnings({"magicnumber", "CheckStyle"})
    private void initializeDefaultSelectionPreference() {
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
//        if (pref.contains(BaseCurrency)) {
//            return;
//        }
        StringBuilder str = new StringBuilder();
        str.append(5).append(",");
        str.append(8).append(",");
        str.append(9).append(",");
        str.append(17).append(",");
        pref.edit()
                .clear()
                .putString(Selected_Currencies, str.toString())
                .putInt(Base_Currency, 31)
                .apply();
    }

    /**
     * Read default selection from preference files.
     */
    private void readDefaultSelectionPreference() {
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        String savedString = pref.getString(Selected_Currencies, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");

        selectedBase = pref.getInt(Base_Currency, 0);
        seleCurrencies = new boolean[currencyArr.length];
        tSeleCurrencies = new boolean[currencyArr.length];
        selCurrencyLi = new ArrayList<>();
        while (st.hasMoreTokens()) {
            int t = Integer.parseInt(st.nextToken());
            seleCurrencies[t] = true;
            selCurrencyLi.add(t);
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

        //For building the single selection alert dialog of selectBaseCurrency only.
        @SuppressWarnings("CheckStyle") final AlertDialog.Builder singleSel_selectBaseCurrency
                = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.select_currency)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        selectedBase = tselectedBase;
                        Toast.makeText(MainActivity.this, Integer.toString(selectedBase), Toast.LENGTH_LONG).show();
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
                tselectedBase = id;
                Toast.makeText(MainActivity.this, Integer.toString(tselectedBase), Toast.LENGTH_LONG).show();
            }
        };
        selectBaseCurrency = findViewById(R.id.selectBaseCurrency);
        selectBaseCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                singleSel_selectBaseCurrency
                        .setSingleChoiceItems(R.array.currencies,
                                selectedBase,
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
                        selCurrencyLi.clear();
                        for (int i = 0; i < tSeleCurrencies.length; i++) {
                            if (tSeleCurrencies[i]) {
                                selCurrencyLi.add(i);
                            }
                        }
                        if (selCurrencyLi.size() > 8) {
                            //restore original state
                            selCurrencyLi.clear();
                            for (int i = 0; i < seleCurrencies.length; i++) {
                                if (seleCurrencies[i]) {
                                    selCurrencyLi.add(i);
                                }
                            }
                            Toast.makeText(MainActivity.this, "Too many selections (more than 8)", Toast.LENGTH_LONG).show();
                            return;
                        } else if (selCurrencyLi.size() == 0) {
                            for (int i = 0; i < seleCurrencies.length; i++) {
                                if (seleCurrencies[i]) {
                                    selCurrencyLi.add(i);
                                }
                            }
                            Toast.makeText(MainActivity.this, "Please select at least one", Toast.LENGTH_LONG).show();
                            return;
                        }
                        System.arraycopy(tSeleCurrencies, 0,
                                seleCurrencies, 0, tSeleCurrencies.length);
                        Toast.makeText(MainActivity.this, Arrays.toString(seleCurrencies), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                });
        @SuppressWarnings("CheckStyle") final DialogInterface.OnMultiChoiceClickListener onMultiSelListener_selectCurrencyToCheck
                = new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(final DialogInterface dialog, final int i, final boolean checked) {
                tSeleCurrencies[i] = checked;
            }
        };

        selectCurrencyToCheck = findViewById(R.id.selectCurrencyToCheck);
        selectCurrencyToCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                System.arraycopy(seleCurrencies, 0,
                        tSeleCurrencies, 0, seleCurrencies.length);
                multiSel_selectCurrencyToCheck
                        .setMultiChoiceItems(R.array.currencies,
                                tSeleCurrencies,
                                onMultiSelListener_selectCurrencyToCheck)
                        .create().
                        show();
            }
        });

        Button since = findViewById(R.id.time);
        since.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {

                            }
                        }, year, month, day);
                //Toast.makeText(MainActivity.this, picker.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).toString(), Toast.LENGTH_LONG).show();
                picker.getDatePicker().setSpinnersShown(true);
                picker.getDatePicker().setCalendarViewShown(false);
                picker.show();
            }
        });

        Button start = findViewById(R.id.startAPI);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAPICall();
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
            int[] colors = getResources().getIntArray(R.array.rainbow);
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
                ser.setColor(colors[i]);
                graph.addSeries(ser);
            }
        } catch (Exception e) {
        }

        GridLabelRenderer lblRenderer = graph.getGridLabelRenderer();
        DefaultLabelFormatter dlf = new DefaultLabelFormatter() {
            private SimpleDateFormat large = new SimpleDateFormat("M/yy");
            private SimpleDateFormat small = new SimpleDateFormat("M/d");
            private Calendar mCalendar = Calendar.getInstance();
            private long twoMonths = 2 * 30 * 24 * 60 * 60 * 1000;

            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    this.mCalendar.setTimeInMillis((long) value);
                    Log.e(TAG, Double.toString(mViewport.getMaxX(false)));
                    if (mViewport.getMaxX(false)
                            - mViewport.getMinX(false) < twoMonths) {
                        return small.format(this.mCalendar.getTimeInMillis());
                    } else {
                        return large.format(this.mCalendar.getTimeInMillis());
                    }
                } else {
                    return super.formatLabel(value, false);
                }
            }
        };
        dlf.setViewport(graph.getViewport());
        lblRenderer.setLabelFormatter(dlf);
        lblRenderer.setVerticalAxisTitle("ExchangeRate");
        lblRenderer.setHorizontalAxisTitle("Time");
        lblRenderer.setNumHorizontalLabels(5);


        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.getLegendRenderer().setVisible(true);

        graph.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
        sb.append(currencyArr[selectedBase]);
        sb.append("&symbols=");
        for (int i = 0; i < seleCurrencies.length; i++) {
            if (seleCurrencies[i]) {
                sb.append(currencyArr[i]);
                sb.append(',');
            }
        }
        sb.deleteCharAt(sb.length() - 1);
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
        requestQueue.add(jsonObjectRequest);
    }
}

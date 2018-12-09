package com.example.liuti.exchangerateproject;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Select base currency, currencies to show, and start an API call.
 */
public final class SelectCurrency extends AppCompatActivity {

    private Bundle selection;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        selection = getIntent().getExtras();

        Log.e(MainActivity.TAG,selection.toString());
        Log.e(MainActivity.TAG,Integer.toString(selection.getInt(MainActivity.Base_Currency)));
        Toast.makeText(SelectCurrency.this, Integer.toString(selection.getInt(MainActivity.Base_Currency)), Toast.LENGTH_LONG).show();
        int v = R.id.base_USD;
        Log.e(MainActivity.TAG, Integer.toString(R.id.base_USD) + " SelectCurrency");
        for (int id : selection.getIntegerArrayList(MainActivity.Selected_Currencies)) {
            ((CheckBox) findViewById(id)).setChecked(true);
        }

        Button cancelSelection = findViewById(R.id.cancel_currencyToCheck);
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        final Button selectAndCallAPI = findViewById(R.id.select_currencyToCheck);
        selectAndCallAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RadioGroup baseGroup = findViewById(R.id.baseCurrency);
                RadioButton selectedBase = findViewById(baseGroup.getCheckedRadioButtonId());
                //<TO-DO>

                ArrayList<Integer> selectedToCheck = new ArrayList<Integer>();
                LinearLayout currencyToCheckLinear = findViewById(R.id.CurrencyToCheckLinear);
                int count = currencyToCheckLinear.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childv = currencyToCheckLinear.getChildAt(i);
                    if (childv instanceof CheckBox) {
                        CheckBox childc = (CheckBox) childv;
                        if (childc.isChecked()) {
                            selectedToCheck.add(childc.getId());
                            //<TO-DO>
                        }
                    }
                }

                selection.putInt(MainActivity.Base_Currency, baseGroup.getCheckedRadioButtonId());
                selection.putIntegerArrayList(MainActivity.Selected_Currencies, selectedToCheck);
                Intent t = new Intent(SelectCurrency.this, MainActivity.class);
                t.putExtras(selection);
                Toast.makeText(SelectCurrency.this,t.getExtras().toString(),Toast.LENGTH_LONG).show();
                setResult(1, t);
                finish();
            }
        });
    }
}

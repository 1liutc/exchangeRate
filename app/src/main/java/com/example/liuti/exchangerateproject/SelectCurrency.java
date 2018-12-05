package com.example.liuti.exchangerateproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_currency);

        Bundle selection = getIntent().getExtras();

        ((RadioButton) findViewById(selection.getInt(MainActivity.BaseCurrency))).setChecked(true);
        for (int id : selection.getIntegerArrayList(MainActivity.CurrencyToCheck)) {
            ((CheckBox) findViewById(id)).setChecked(true);
        }

        Button cancelSelection = findViewById(R.id.cancelSelection);
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        Button selectAndCallAPI = findViewById(R.id.selectAndCallAPI);
        selectAndCallAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RadioGroup baseGroup = findViewById(R.id.baseCurrency);
                RadioButton selectedBase = findViewById(baseGroup.getCheckedRadioButtonId());

                ArrayList<String> selectedToCheck = new ArrayList<String>();
                LinearLayout currencyToCheckLinear = findViewById(R.id.CurrencyToCheckLinear);
                int count = currencyToCheckLinear.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childv = currencyToCheckLinear.getChildAt(i);
                    if (childv instanceof CheckBox) {
                        CheckBox childc = (CheckBox) childv;
                        if (childc.isChecked()) {
                            selectedToCheck.add(childc.getText().toString());
                        }
                    }
                }

                Intent t = getIntent();

                setResult(1, t);
                finish();
            }
        });
    }
}

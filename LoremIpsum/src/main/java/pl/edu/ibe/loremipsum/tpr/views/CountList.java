/*
 * This file is part of Test Platform.
 *
 * Test Platform is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Test Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Test Platform; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Ten plik jest częścią Platformy Testów.
 *
 * Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 *
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 *
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pl.edu.ibe.loremipsum.tpr.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 11.08.14.
 */
public class CountList extends LinearLayout implements RadioGroup.OnCheckedChangeListener {


    @InjectView(R.id.count_group)
    RadioGroup radioGroup;
    @InjectView(R.id.no_answer_error)
    TextView noAnswerError;
    private int min;
    private int max;
    private LayoutInflater inflater;
    private HashMap<Integer, RadioButton> radioButtons;
    private OnItemSelectedListener onItemSelectedListener;

    public CountList(Context context) {
        super(context);
        init();
    }

    public CountList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(CountList.this, getSelectedNumber());
        }
    }

    private void init() {
        radioButtons = new HashMap<>();
        inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.count_list, this, true);
        ButterKnife.inject(this, this);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        radioGroup.setOnCheckedChangeListener(this);
    }

    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;

        radioGroup.removeAllViews();

        int number = max - min + 1;
        RadioButton radioButton;


        LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(8, 8, 8, 8);


        for (int i = 0; i < number; i++) {
            radioButton = (RadioButton) inflater.inflate(R.layout.count_item, null, false);
            radioButton.setText((i + min) + "");
            radioButton.setId(i + number);
            radioButtons.put((i + min), radioButton);
            radioGroup.addView(radioButton, layoutParams);
        }
    }

    public Integer getSelectedNumber() {
        for (Map.Entry<Integer, RadioButton> integerRadioButtonEntry : radioButtons.entrySet()) {
            if (integerRadioButtonEntry.getValue().isChecked()) {
                return integerRadioButtonEntry.getKey();
            }
        }
        return -1;
    }

    public void showCorrect(int correct) {
        for (Map.Entry<Integer, RadioButton> integerRadioButtonEntry : radioButtons.entrySet()) {
            if (integerRadioButtonEntry.getKey() == correct) {
                integerRadioButtonEntry.getValue().setSelected(true);
            }
            integerRadioButtonEntry.getValue().setEnabled(false);
        }
        radioGroup.setEnabled(false);

    }

    public interface OnItemSelectedListener {
        void onItemSelected(CountList countList, int number);
    }
}

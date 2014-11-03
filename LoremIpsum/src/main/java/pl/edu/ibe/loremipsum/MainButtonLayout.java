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

package pl.edu.ibe.loremipsum;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.testplatform.R;

public class MainButtonLayout extends RelativeLayout {

    @InjectView(R.id.text)
    TextView text;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.additional_text)
    TextView additionalText;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    private String buttonString;
    private int buttonImage;
    private String buttonAdditionalText;

    public MainButtonLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public MainButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MainButtonLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MainButtonLayout, defStyle, 0);

        buttonString = a.getString(R.styleable.MainButtonLayout_buttonString);
        buttonImage = a.getResourceId(R.styleable.MainButtonLayout_buttonImage, -1);
        buttonAdditionalText = a.getString(R.styleable.MainButtonLayout_buttonAdditionalString);
        a.recycle();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_main_button, this, true);
        ButterKnife.inject(this, view);

        text.setText(buttonString);
        image.setImageResource(buttonImage);
        additionalText.setText(buttonAdditionalText);
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText.setText(additionalText);
    }

    public void setProgressBarVisibility(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}

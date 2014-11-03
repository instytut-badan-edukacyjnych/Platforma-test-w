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

package pl.edu.ibe.loremipsum.tablet;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;

/**
 * Klasa bazowa (View) wyświetlania wyników
 *
 *
 */
public class ResultBaseView extends View {
//	private static final String TAG = "ResultBaseView";


    /**
     * parametry wyswietlania grupy odniesienia
     */
    protected Paint m_paintG = null;
    /**
     * parametry wyswietlania procentów
     */
    protected Paint m_paintP = null;

    /**
     * grupa odniesienia
     */
    protected int m_reference = 0;
    /**
     * wyniki do prezentacji
     */
    protected TestResult m_viewResult = null;

    /**
     * opis obszaru matematyki
     */
    protected ResultDesc.ResultDescItem m_mDesc = null;
    /**
     * opis obszaru pisania
     */
    protected ResultDesc.ResultDescItem m_pDesc = null;
    /**
     * opis obszaru czytania
     */
    protected ResultDesc.ResultDescItem m_cDesc = null;


    /**
     * Konstruktor
     *
     * @param a_context - kontekt wyswietlania
     */
    public ResultBaseView(Context a_context) {

        super(a_context);

        m_paintG = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_paintG.setTextAlign(Paint.Align.LEFT);
        m_paintG.setColor(0xFFD14E87);        // czerwony
        m_paintG.setTypeface(Typeface.create("Tahoma", Typeface.NORMAL));
        m_paintG.setTextSize(18.0f);

        m_paintP = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_paintP.setTextAlign(Paint.Align.CENTER);
        m_paintP.setColor(0xFF000000);        // czarny
        m_paintP.setTextSize(14.0f);

    }

}



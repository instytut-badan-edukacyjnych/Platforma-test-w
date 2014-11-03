/************************************
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
 ************************************/
package pl.edu.ibe.loremipsum.arbiter;


import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.BaseTask;


/**
 * Marks task based on item location on speciefed fields
 *
 */
public class PlaceArbiter extends ExternalArbiter {
//	private static final String TAG = "PlaceArbiter";


    private final int APP_BAD_PLACE_SHIFT = 1000;


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.BaseArbiter#Assess(java.util.ArrayList, java.lang.String)
     */
    @Override
    public void Assess(ArrayList<BaseTask.FieldSelect> a_data, String a_answer) {

        m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;
        m_arbiter = APP_ARBITER_COMPUTER;

        if ((a_data == null) || (a_answer == null)) {
            m_mark = APP_MARK_UNDEFINED;

            return;
        }

        int number = GetPlaceNr(a_answer);
        int answer = Check(a_data, a_answer);

        if (answer < APP_BAD_PLACE_SHIFT) {
            if (answer == number) {
                m_mark = APP_MARK_1;
            } else {
                m_mark = APP_MARK_0;
            }
        } else {
            m_mark = APP_MARK_0;
        }

    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.BaseArbiter#Assess(java.util.ArrayList, java.lang.String, int)
     */
    @Override
    public void Assess(ArrayList<BaseTask.FieldSelect> a_data, String a_answer, int a_threshold) {

        m_arbiter = APP_ARBITER_COMPUTER;
        if ((a_data == null) || (a_answer == null)) {
            m_mark = APP_MARK_UNDEFINED;

            return;
        }

        int number = GetPlaceNr(a_answer);
        int answer = Check(a_data, a_answer);

        m_arbiter = APP_ARBITER_COMPUTER;
        if (answer < APP_BAD_PLACE_SHIFT) {
            if (answer == number) {
                m_mark = APP_MARK_2;
            } else if (answer >= a_threshold) {
                m_mark = APP_MARK_1;
            } else {
                m_mark = APP_MARK_0;
            }
        } else {
            answer %= APP_BAD_PLACE_SHIFT;

            if (answer >= a_threshold) {
                m_mark = APP_MARK_1;
            } else {
                m_mark = APP_MARK_0;
            }
        }

    }

    /**
     * Checks objects locations on fields
     *
     * @param a_data   - active task fields
     * @param a_answer - correct answer string
     * @return number of correctly placed objects  + APP_BAD_SELECT_SHIFT * number of wrongly placed objects
     */
    private int Check(ArrayList<BaseTask.FieldSelect> a_data, String a_answer) {

        int correctly = 0;
        int incorrectly = 0;
        m_answer = "";
        for (BaseTask.FieldSelect f : a_data) {
            if (f.m_selected) {
                m_answer += f.m_name;
            }

            if (a_answer.contains(f.m_name)) {
                if (f.m_selected) {
                    ++correctly;
                }
            } else {
                if (f.m_selected) {
                    incorrectly++;
                }
            }
        }

        return correctly + APP_BAD_PLACE_SHIFT * incorrectly;
    }

    /**
     * Defines number of fields
     *
     * @param a_answer - - correct answer string
     * @return number of answer fields
     */
    private int GetPlaceNr(String a_answer) {

        // pola odpowiedzi oddzielane sa spacjami, obliczamy liczbe pól do zaznaczenia
        int number = a_answer.trim().length() + 1;

        return number / 2;
    }

}



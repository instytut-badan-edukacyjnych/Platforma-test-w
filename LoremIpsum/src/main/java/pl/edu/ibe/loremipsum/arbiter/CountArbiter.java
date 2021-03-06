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


import java.util.Vector;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Marks task based on number of selected objects
 *
 *
 */
public class CountArbiter extends ExternalArbiter {
    private static final String TAG = CountArbiter.class.toString();


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.ExternalArbiter#Assess(java.util.Vector, java.lang.String)
     */
    @Override
    public void Assess(Vector<BaseTask.FieldSelect> a_data, String a_answer) {

        m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;

        int number = 0;
        try {
            number = Integer.parseInt(a_answer);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, e);
            return;
        }

        int selectNr = 0;
        m_answer = "";
        for (BaseTask.FieldSelect f : a_data) {
            if (f.m_selected) {
                ++selectNr;

                m_answer += f.m_name;
            }
        }

        m_arbiter = APP_ARBITER_COMPUTER;
        if (number == selectNr) {
            m_mark = APP_MARK_1;
        } else {
            m_mark = APP_MARK_0;
        }
    }
}



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
 * Marks task based on field selection including execution time
 *
 */
public class TimeSelectArbiter extends SelectArbiter {
    private static final String TAG = TimeSelectArbiter.class.toString();

    /**
     * mark threshold in ms
     */
    protected int m_level = 0;
    /**
     * 1 mark threshold in ms in scale 0-1-2
     */
    protected int m_level1 = 0;

    /**
     * mark processed flag
     */
    private boolean m_processed = false;


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.BaseArbiter#SetExtraParameters(int, int)
     */
    @Override
    public void SetExtraParameters(int a_par1, int a_par2) {

        m_level = a_par1;
        m_level1 = a_par2;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.SelectArbiter#Assess(java.util.Vector, java.lang.String, int)
     */
    @Override
    public void Assess(Vector<BaseTask.FieldSelect> a_data, String a_answer) {

        if (m_processed) {
            return;
        }

        m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;
        m_arbiter = APP_ARBITER_COMPUTER;

        m_processed = true;
        long response = System.currentTimeMillis();
        response -= m_startTime;

        m_answer = Long.toString(response);

        LogUtils.d(TAG, "time: " + response);

        if (CheckAnswer(a_data, a_answer)) {
            if (LoremIpsumApp.m_markRange > BaseTask.TASK_MARK_RANGE_0_1) {
                if (response < m_level) {
                    m_mark = APP_MARK_2;
                } else if (response < m_level1) {
                    m_mark = APP_MARK_1;
                } else {
                    m_mark = APP_MARK_0;
                }
            } else {
                if (response < m_level) {
                    m_mark = APP_MARK_1;
                } else {
                    m_mark = APP_MARK_0;
                }
            }
        } else {
            m_mark = APP_MARK_0;
        }

    }

    /**
     * Checks if answer is correct
     * @param a_data   - Information about task active field
     * @param a_answer - correct answer string
     * @return true if answer is correct
     */
    private boolean CheckAnswer(Vector<BaseTask.FieldSelect> a_data, String a_answer) {

        int sel = 0;
        boolean hit = false;
        m_answer += " ";
        for (BaseTask.FieldSelect f : a_data) {
            if (f.m_selected) {
                m_answer += f.m_name;

                ++sel;
                if (a_answer.compareTo(f.m_name) == 0) {
                    hit = true;
                }
            }
        }

        if (hit && sel == 1) {
            return true;
        } else {
            return false;
        }
    }

}



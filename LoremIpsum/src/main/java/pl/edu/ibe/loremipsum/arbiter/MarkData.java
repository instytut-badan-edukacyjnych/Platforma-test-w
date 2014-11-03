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


import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;

/**
 * Mark entity class
 */
public class MarkData {

    /**
     * mark
     */
    public double m_mark;
    /**
     * arbiter
     */
    public int m_arbiter;
    /**
     * previous mark
     */
    public int m_previousMark;

    /**
     * task step
     */
    public int m_step;

    /**
     * task solution attempts
     */
    public int m_attempts;

    /**
     * number of command repeats
     */
    public int m_listenNumber;

    /**
     * Task reload number
     */
    public int m_reloadNumber;

    /**
     * task duration time
     */
    public long m_durationTime;

    /**
     * task solution time
     */
    public long m_solveTime;

    /**
     * task start time
     */
    public long m_startDelay;

    /**
     * task end time
     */
    public long m_completeDelay;

    /**
     * answer
     */
    public String m_answer;
    public String area;


    /**
     * Constructor
     */
    public MarkData() {

        m_mark = BaseArbiter.APP_MARK_NONE;
        m_arbiter = BaseArbiter.APP_ARBITER_UNKNOWN;
        m_previousMark = BaseArbiter.APP_MARK_UNDEFINED;
        m_step = 0;
        m_attempts = 0;
        m_listenNumber = 0;
        m_reloadNumber = 0;
        m_durationTime = -1;
        m_solveTime = -1;
        m_startDelay = -1;
        m_completeDelay = -1;
        m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;
    }

    /**
     * Fill up
     *
     * @param a_mark - data
     */
    public void Add(MarkData a_mark) {

        if (a_mark.m_mark != BaseArbiter.APP_MARK_NONE) {
            m_mark = a_mark.m_mark;
            m_arbiter = a_mark.m_arbiter;
            m_previousMark = a_mark.m_previousMark;
            m_step = a_mark.m_step;
            m_attempts += a_mark.m_attempts;
            m_listenNumber += a_mark.m_listenNumber;
            m_reloadNumber += a_mark.m_reloadNumber;
            if (m_durationTime > 0) {
                m_durationTime += a_mark.m_durationTime;
            } else {
                m_durationTime = a_mark.m_durationTime;
            }
            if (m_solveTime > 0) {
                m_solveTime += a_mark.m_solveTime;
            } else {
                m_solveTime = a_mark.m_solveTime;
            }
            if (m_startDelay < 0) {
                m_startDelay = a_mark.m_startDelay;
            }
            if (m_startDelay < 0) {
                m_completeDelay = a_mark.m_completeDelay;
            }
            m_answer = a_mark.m_answer;
        }
    }

}


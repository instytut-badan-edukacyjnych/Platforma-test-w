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
import java.util.Vector;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.BaseTask;


/**
 * Task mark base class
 */
public class BaseArbiter {
//	private static final String TAG = "BaseArbiter";


    /**
     * package name
     */
    public static final String APP_PACKAGE_NAME = "pl.edu.ibe.loremipsum.arbiter.";


    /**
     * marks flags
     */
    public static final int APP_MARK_0 = 0;
    public static final int APP_MARK_1 = 1;
    public static final int APP_MARK_2 = 2;
    public static final int APP_MARK_NONE = -2;
    /**
     * mark
     */
    protected int m_mark = APP_MARK_NONE;
    public static final int APP_MARK_UNDEFINED = -1;
    /**
     * mark before improvement
     */
    protected int m_previousMark = APP_MARK_UNDEFINED;
    /**
     * arbiter flags
     */
    public static final int APP_ARBITER_HUMAN = 0;
    public static final int APP_ARBITER_COMPUTER = 1;
    public static final int APP_ARBITER_UNKNOWN = -1;
    /**
     * arbiter
     */
    protected int m_arbiter = APP_ARBITER_UNKNOWN;
    /**
     * cumulated value
     */
    public static long m_cumulate = 0;
    /**
     * task start time
     */
    protected long m_startTime = 0;
    /**
     * task solution begin  time
     */
    protected long m_beginTime = 0;
    /**
     * task solution end time
     */
    protected long m_finishTime = 0;
    /**
     * task end time
     */
    protected long m_endTime = 0;
    /**
     * Command end time
     */
    protected long m_completeTime = 0;
    /**
     * task solution attempts
     */
    protected int m_attempts = 0;
    /**
     * command repeats number
     */
    protected int m_listenNumber = 0;
    /**
     * task start number
     */
    protected int m_reloadNumber = 0;
    /**
     * answer
     */
    protected String m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;

    /**
     * Data to string
     *
     * @param a_name - task name
     * @param a_dir  - path
     * @param a_mark - mark
     * @return String  with detailed info about task solution
     */
    public static String ToTxtString(String a_name, String a_dir, MarkData a_mark) {

        StringBuffer buff = new StringBuffer();

        buff.append(">> MARK ");
        buff.append(a_name);
        buff.append("\n");
        buff.append(">@  dir: ");
        buff.append(a_dir);
        buff.append("\n");
        buff.append(">A  mark: ");
        buff.append(a_mark.m_mark);
        buff.append("\n");
        buff.append(">B  arbiter: ");
        buff.append(a_mark.m_arbiter);
        buff.append("\n");
        buff.append(">C  previous: ");
        buff.append(a_mark.m_previousMark);
        buff.append("\n");
        buff.append(">D  step: ");
        buff.append(a_mark.m_step);
        buff.append("\n");
        buff.append(">E  attempts: ");
        buff.append(a_mark.m_attempts);
        buff.append("\n");
        buff.append(">F  commands: ");
        buff.append(a_mark.m_listenNumber);
        buff.append("\n");
        buff.append(">G  reload: ");
        buff.append(a_mark.m_reloadNumber);
        buff.append("\n");
        buff.append(">H  duration [ms]: ");
        buff.append(a_mark.m_durationTime);
        buff.append("\n");
        buff.append(">I  solve [ms]: ");
        buff.append(a_mark.m_solveTime);
        buff.append("\n");
        buff.append(">J  delay [ms]: ");
        buff.append(a_mark.m_startDelay);
        buff.append("\n");
        buff.append(">K  delay2 [ms]: ");
        buff.append(a_mark.m_completeDelay);
        buff.append("\n");

        return buff.toString();
    }

    /**
     * Additional arbiter params
     *
     * @param a_par1 - parametr 1
     * @param a_par2 - parametr 2
     */
    public void SetExtraParameters(int a_par1, int a_par2) {

        // empty
    }

    /**
     * Called at task start
     */
    public void StartTask() {

        m_startTime = System.currentTimeMillis();
    }

    /**
     * Called at task end
     */
    public void EndTask() {

        m_endTime = System.currentTimeMillis();
    }

    /**
     * Called at touch screen
     */
    public void TouchScreen() {

        if (m_beginTime != 0) {
            m_finishTime = System.currentTimeMillis();
        } else {
            m_beginTime = System.currentTimeMillis();
        }
    }

    /**
     * Called at finish command
     */
    public void FinishCommand() {

        m_completeTime = System.currentTimeMillis();
    }

    /**
     * Called at task solution attempt
     */
    public void Attempt() {

        ++m_attempts;
    }

    /**
     * Called at command repeat
     */
    public void RepeatCommand() {

        ++m_listenNumber;
    }

    /**
     * called at task restart
     */
    public void ReloadTask() {

        ++m_reloadNumber;
    }

    /**
     * Task mark
     *
     * @param a_mark    - mark
     * @param a_arbiter - arbiter
     */
    public void Mark(int a_mark, int a_arbiter) {

        m_mark = a_mark;
        m_arbiter = a_arbiter;
    }

    /**
     * force mark
     *
     * @param a_mark - mark
     */
    public void ForceMark(int a_mark) {

        if (m_previousMark == APP_MARK_UNDEFINED) {
            m_previousMark = m_mark;
        }
        m_mark = a_mark;
        m_arbiter = APP_ARBITER_HUMAN;
    }

    /**
     * Reads task mark
     *
     * @return task mark
     */
    public MarkData GetMark() {

        MarkData mark = new MarkData();

        mark.m_mark = m_mark;
        mark.m_arbiter = m_arbiter;
        mark.m_previousMark = m_previousMark;
        mark.m_attempts = m_attempts;
        mark.m_listenNumber = m_listenNumber;
        mark.m_reloadNumber = m_reloadNumber;
        if (m_endTime != 0 && m_startTime != 0) {
            mark.m_durationTime = m_endTime - m_startTime;
        } else {
            mark.m_durationTime = -1;
        }
        if (m_finishTime != 0 && m_beginTime != 0) {
            mark.m_solveTime = m_finishTime - m_beginTime;
        } else {
            mark.m_solveTime = -1;
        }
        if (m_beginTime != 0 && m_startTime != 0) {
            mark.m_startDelay = m_beginTime - m_startTime;
        } else {
            mark.m_startDelay = -1;
        }
        if (m_startTime != 0 && m_completeTime != 0) {
            mark.m_completeDelay = m_completeTime - m_startTime;
        } else {
            mark.m_completeDelay = -1;
        }
        mark.m_answer = m_answer;

        return mark;
    }

    /**
     * Taks mark
     *
     * @param a_data   - information about active fields in task
     * @param a_answer - proper answer string
     */
    public void Assess(Vector<BaseTask.FieldSelect> a_data, String a_answer) {

        // empty
    }

    /**
     * Task mark
     *
     * @param a_data      -  information about active fields in task
     * @param a_answer    -proper answer string
     * @param a_threshold - mark threshold
     */
    public void Assess(Vector<BaseTask.FieldSelect> a_data, String a_answer, int a_threshold) {

        // empty
    }

    /**
     * Task mark
     *
     * @param a_data   -  information about active fields in task
     * @param a_answer - proper answer string
     */
    public void Assess(ArrayList<BaseTask.FieldSelect> a_data, String a_answer) {

        // empty
    }

    /**
     * Task mark
     *
     * @param a_data      -  information about active fields in task
     * @param a_answer    - proper answer string
     * @param a_threshold - mark threshold
     */
    public void Assess(ArrayList<BaseTask.FieldSelect> a_data, String a_answer, int a_threshold) {

        // empty
    }

    /**
     * Task mark
     *
     * @param a_data - information about active fields in task
     * @param a_hour - proper answer hour
     * @param a_min  - proper answer minutes
     */
    public void Assess(ArrayList<BaseTask.FieldSelect> a_data, int a_hour, int a_min) {

        // empty
    }



}



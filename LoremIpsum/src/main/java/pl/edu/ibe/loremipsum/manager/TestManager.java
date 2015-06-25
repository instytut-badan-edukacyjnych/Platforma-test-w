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


package pl.edu.ibe.loremipsum.manager;

import java.util.Vector;

import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.resultfixer.data.PupilData;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Created by adam on 20.03.14.
 */
public class TestManager {
    private static final String TAG = TestManager.class.toString();
    /**
     * Test start flag
     */
    @Deprecated
    public static CurrentTaskSuiteService.TestMode m_runTestFlag = CurrentTaskSuiteService.TestMode.NORMAL;
    /**
     * running task
     */
    public static TaskInfo m_runTaskInfo = null;
    /**
     * Number of tasks in test
     */
    public static int m_taskNumber = 0;


    /**
     * previous task
     */
    public static TaskInfo m_prevTaskInfo = null;
    /**
     * Temporary keeped result files
     */
    public static Vector<String> m_resultFiles = null;
    /**
     * Test results
     */
    public static TestResult m_result = new TestResult();

    /**
     * Prepares test
     *
     * @param testMode - @link{TestMode} enum
     * @param examined - examined
     * @return true if success
     */
    public static boolean prepareTest(CurrentTaskSuiteService.TestMode testMode, PupilData examined) {

        LoremIpsumApp.PrepareResult(examined);

        m_runTestFlag = testMode;
        LoremIpsumApp.m_taskOrder.clear();

        m_runTaskInfo = null;
        m_taskNumber = 0;
        m_prevTaskInfo = null;
        LoremIpsumApp.m_finishFlag = false;

        if (m_runTestFlag == CurrentTaskSuiteService.TestMode.NORMAL) {
            if ((LoremIpsumApp.m_areas.size() > 0) && (examined != null)) {
                m_result.Assign(examined, LoremIpsumApp.m_testManager.m_name);
                m_result.Start(true);

                LoremIpsumApp.m_testManager.Restart("");
                return true;
            }
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.TUTORIAL) {
            if ((LoremIpsumApp.m_manual.size() > 0) && (examined != null)) {
                m_result.Assign(examined, "");
                m_result.Start(false);

                LoremIpsumApp.m_manualManager.Restart("");
                return true;
            }
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.DEMO) {
            if (examined != null) {
                m_result.Assign(examined, "");
                m_result.Start(false);

                LoremIpsumApp.demoManager.Restart("");
                return true;
            }
        }
        return false;
    }

    /**
     * Gets next task
     *
     * @return task info or null if test ended
     */

    public static TaskInfo GetNextTask(OnModeChanged onModeChanged) {
        LogUtils.v(TAG, "Getting next task");
        if (m_runTestFlag == CurrentTaskSuiteService.TestMode.NORMAL) {
            if (LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableCatAlgoritm) {
                return LoremIpsumApp.m_manualManager.GetNextTask();
            }

            TaskInfo nextTask = LoremIpsumApp.m_testManager.GetNextTask();

            // LogUtils.d(TAG, "m_taskNumber = " + m_taskNumber + "  nextTask = " + nextTask.m_name);
            if (nextTask != null && m_taskNumber < TaskSuiteConfig.maxTasksNumerPerTest) {
                m_taskNumber++;
                m_prevTaskInfo = m_runTaskInfo;
                m_runTaskInfo = nextTask;
            } else {
                LoremIpsumApp.m_finishFlag = true;

                nextTask = LoremIpsumApp.m_finishTask;

                m_prevTaskInfo = null;
                m_result.Enable(false);
                m_runTaskInfo = nextTask;
            }

            return nextTask;
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.TUTORIAL) {
            TaskInfo nextTask = LoremIpsumApp.m_manualManager.GetNextTask();
            if (nextTask != null)
                return nextTask;

            LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite()
                    .getCurrentTestRunData().setTestMode(CurrentTaskSuiteService.TestMode.NORMAL);
            onModeChanged.modeChanged(CurrentTaskSuiteService.TestMode.NORMAL);
            LoremIpsumApp.m_testManager.Restart("");
            m_result.Enable(true);
            return GetNextTask(onModeChanged);
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.DEMO) {
            return LoremIpsumApp.demoManager.GetNextTask();
        }
        throw new RuntimeException("It shouldn't happen. Flag: " + m_runTestFlag);
    }

    /**
     * Get previous task
     *
     * @return task info
     */
    public static TaskInfo GetPrevTask() {
        if (m_runTestFlag == CurrentTaskSuiteService.TestMode.NORMAL) {
            if (LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableCatAlgoritm) {
                return LoremIpsumApp.m_manualManager.GetPrevTask();
            }
            if (m_prevTaskInfo != null) {
                m_result.Del(m_runTaskInfo);
                m_result.Del(m_prevTaskInfo);

                m_resultFiles = null;

                --m_taskNumber;
                m_runTaskInfo = m_prevTaskInfo;
                m_prevTaskInfo = null;

                return m_runTaskInfo;
            }
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.TUTORIAL) {
            return LoremIpsumApp.m_manualManager.GetPrevTask();
        } else if (m_runTestFlag == CurrentTaskSuiteService.TestMode.DEMO) {
            return LoremIpsumApp.m_manualManager.GetPrevTask();
        }

        return null;
    }

    public static boolean isFinished() {
        return LoremIpsumApp.m_finishFlag;
    }

    public interface OnModeChanged {
        void modeChanged(CurrentTaskSuiteService.TestMode testMode);
    }
}

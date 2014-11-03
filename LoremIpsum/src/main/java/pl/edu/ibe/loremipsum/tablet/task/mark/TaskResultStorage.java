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

package pl.edu.ibe.loremipsum.tablet.task.mark;

import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.manager.BaseManager;
import pl.edu.ibe.loremipsum.manager.TestManager;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;

/**
 * Created by adam on 08.04.14.
 */
public class TaskResultStorage {
    /**
     * Dopisuje informację o wyniku rozwiązywania zadania
     *
     * @param a_mark - ocena zadania
     */
    public static void StoreTaskResult(MarkData a_mark) {
        if (TestManager.m_runTaskInfo != null && !LoremIpsumApp.m_finishFlag) {
            LoremIpsumApp.m_testManager.Mark(TestManager.m_runTaskInfo, a_mark.m_mark);

            TestResult.ResultTaskItem result = new TestResult.ResultTaskItem();

            result.m_taskName = TestManager.m_runTaskInfo.m_name;
            result.m_area = TestManager.m_runTaskInfo.m_area;
            result.m_nr = TestManager.m_taskNumber;
            result.m_mark = a_mark.m_mark;
            result.m_answer = a_mark.m_answer;
            result.m_taskDuration = (int) a_mark.m_solveTime;
            BaseManager.ManagerTestInfo info = LoremIpsumApp.m_testManager.GetTestInfo(TestManager.m_runTaskInfo.m_area);
            if (info != null) {
                result.m_theta = info.m_vector.m_theta;
                result.m_se = info.m_vector.m_se;
            } else {
                result.m_theta = LoremIpsumApp.APP_UNDEFINED_VALUE;
                result.m_se = LoremIpsumApp.APP_UNDEFINED_VALUE;
            }
            result.m_files = TestManager.m_resultFiles;

            TestManager.m_resultFiles = null;

            TestManager.m_result.Add(result);


            if (LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR1 || LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR2) {
                ServiceProvider services = LoremIpsumApp.obtain().getServiceProvider();
                services.results().storeTprResults(a_mark).subscribe((resultId) -> {
                    //do nothing
                });
            }
        }
    }
}

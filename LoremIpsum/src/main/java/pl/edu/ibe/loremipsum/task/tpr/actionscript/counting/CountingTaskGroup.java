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

package pl.edu.ibe.loremipsum.task.tpr.actionscript.counting;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.arbiter.tpr.actionscript.CountingArbiter;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.tpr.AutoTutorialEndTask;
import pl.edu.ibe.loremipsum.task.tpr.TPR2TaskGroup;
import pl.edu.ibe.loremipsum.task.tpr.TutorialEndTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Created by adam on 14.08.14.
 */
public class CountingTaskGroup extends TPR2TaskGroup implements CountingTask.CountingTaskInterface {
    private static final String TAG = CountingTaskGroup.class.toString();


    private ArrayList<SummaryTaskResults> countingTaskResultses;
    private SummaryTaskResults currentSummaryTaskResult;
    private ArrayList<Integer> groupsCount;
    private int counter;

    private boolean newCycle;
    private boolean endCycle;
    private long startTime;
    private String area;

    private boolean tutorialLoop;
    private boolean tutorialWasShown = false;
    private boolean tutorialWasSuccesfull;
    private long overallTime;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public CountingTaskGroup(Context context) {
        super(context);
        countingTaskResultses = new ArrayList<>();
        currentSummaryTaskResult = new SummaryTaskResults();
        groupsCount = new ArrayList<>();

    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        overallTime = System.currentTimeMillis();
        area = a_info.m_area;
        return super.Create(a_info, a_handler, a_dir);
    }

    @Override
    public boolean NextTask() throws IOException {
        boolean returnValue = super.NextTask();
        if (endCycle && !tutorialLoop && countingTaskResultses.size() > 0) {

            countingTaskResultses.get(countingTaskResultses.size() - 1).overallTime = System.currentTimeMillis() - startTime;
        }
        tutorialLoop = false;
        if (m_task instanceof CountingSummaryTask) {
            ((CountingSummaryTask) m_task).setView(view);
            ((CountingSummaryTask) m_task).setData(currentSummaryTaskResult);
            currentSummaryTaskResult.isTutorial = ((CountingSummaryTask) m_task).showCorrect;
            countingTaskResultses.add(currentSummaryTaskResult);
            currentSummaryTaskResult = new SummaryTaskResults();
            newCycle = true;
            endCycle = true;
            if (isAfterTutorial) {
                groupsCount.add(counter);
                counter = 0;
            }
        }
        if (m_task instanceof CountingTask) {
            ((CountingTask) m_task).setCountingTaskInterface(this);
            if (newCycle) {
                startTime = System.currentTimeMillis();
                newCycle = false;
            }
            if (isAfterTutorial) {
                counter++;
            }
        }
        if (m_task instanceof TutorialEndTask) {
            currentSummaryTaskResult = new SummaryTaskResults();
            newCycle = true;
        }
        if (m_task instanceof AutoTutorialEndTask) {
            ((AutoTutorialEndTask) m_task).setTaskGroupController(this);
            currentSummaryTaskResult = new SummaryTaskResults();
            newCycle = true;
        }

        return returnValue;
    }

    @Override
    public boolean SelectTask(int a_index) throws IOException {
        boolean returnValue = super.SelectTask(a_index);
        if (m_task instanceof CountingSummaryTask) {
            ((CountingSummaryTask) m_task).setView(view);
            ((CountingSummaryTask) m_task).setData(currentSummaryTaskResult);
            countingTaskResultses.add(currentSummaryTaskResult);
            currentSummaryTaskResult = new SummaryTaskResults();
        }
        if (m_task instanceof CountingTask) {
            ((CountingTask) m_task).setCountingTaskInterface(this);
        }
        if (m_task instanceof TutorialEndTask) {
            currentSummaryTaskResult = new SummaryTaskResults();
        }
        return returnValue;
    }


    @Override
    public void setTaskStatistics(int correctBallsCount, int wrongBallsCount, long startTime, long endTime) {
        CountingTaskResults countingTaskResults = new CountingTaskResults();
        countingTaskResults.correctBallsCount = correctBallsCount;
        countingTaskResults.wrongBallsCount = wrongBallsCount;
        countingTaskResults.time = endTime - startTime;

        currentSummaryTaskResult.resultses.add(countingTaskResults);

    }

    @Override
    public MarkData GetMark() {
        MarkData mark;
        double result = 0;
        if (m_arbiter instanceof CountingArbiter) {
            if (m_task instanceof CountingSummaryTask) {
                ((CountingSummaryTask) m_task).requestStatisticUpdate();
            }
            if (countingTaskResultses.size() > 0) {
                countingTaskResultses.get(countingTaskResultses.size() - 1).overallTime = System.currentTimeMillis() - startTime;
            }
            int testCount = 0;

            for (SummaryTaskResults countingTaskResultse : countingTaskResultses) {
                if (!countingTaskResultse.isTutorial) {
                    testCount++;
                }
            }


            result = ((CountingArbiter) m_arbiter).setSummary(countingTaskResultses, testCount == 6, groupsCount, tutorialWasSuccesfull, isAbstractTask, System.currentTimeMillis() - overallTime);
        }
        //super calls arbiter.
        mark = super.GetMark();
        mark.m_mark = result;

        mark.area = area;
        return mark;
    }

    @Override
    public boolean tutorialIsSuccesful() {
//        public static var REQUIRE_PERCENT_TO_PASS_TASK1:Number 	= 0.80;

        if (tutorialWasShown) {
            LogUtils.d(TAG, "tutorial already shown");
            return true;
        }

        double taskNumber = 0;
        double wrongCount = 0;

        for (SummaryTaskResults countingTaskResultse : countingTaskResultses) {
            if (countingTaskResultse.isTutorial) {
                for (CountingTaskResults resultse : countingTaskResultse.resultses) {
                    if (resultse.answer != resultse.correctBallsCount) {
                        wrongCount++;
                    }
                    taskNumber++;
                }
            }
        }
        tutorialWasShown = true;
        LogUtils.d(TAG, "Tutorial result = " + (taskNumber - wrongCount) / taskNumber + " repeat tutorial? " + (((taskNumber - wrongCount) / taskNumber > 0.80) ? false : true));
        tutorialWasSuccesfull = (taskNumber - wrongCount) / taskNumber > 0.80 ? true : false;
        return tutorialWasSuccesfull;
    }

    @Override
    public void clearTutorialData() {
//        countingTaskResultses = new ArrayList<>();
        tutorialLoop = true;
    }

    @Override
    public AutoTutorialEndTask.Mode getMode() {
        return AutoTutorialEndTask.Mode.COUNTING;
    }

    public static class SummaryTaskResults {
        public long overallTime;
        public ArrayList<CountingTaskResults> resultses;
        public boolean isTutorial;

        public SummaryTaskResults() {
            resultses = new ArrayList<>();
        }
    }

    public static class CountingTaskResults {
        public int correctBallsCount;
        public int wrongBallsCount;
        public long time;
        public int answer;
    }
}

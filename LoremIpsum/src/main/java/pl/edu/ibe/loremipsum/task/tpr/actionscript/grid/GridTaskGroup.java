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

package pl.edu.ibe.loremipsum.task.tpr.actionscript.grid;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.arbiter.tpr.actionscript.GridArbiter;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.tpr.AutoTutorialEndTask;
import pl.edu.ibe.loremipsum.task.tpr.TPR2TaskGroup;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 20.08.14.
 */
public class GridTaskGroup extends TPR2TaskGroup {
    private static final String TAG = GridTaskGroup.class.getCanonicalName();
    private int taskId = 1;
    private boolean wasChecked;
    private long startTime;
    /**
     * Constructor.
     *
     * @param context Android context.
     */
    private ArrayList<GridTask.GridTaskResult> gridTaskResults;
    private GridTask.GridTaskResult gridTaskResult;
    private boolean tutorialWasSuccesful;
    private String area;

    public GridTaskGroup(Context context) {
        super(context);
        gridTaskResults = new ArrayList<>();
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        area = a_info.m_area;
        return super.Create(a_info, a_handler, a_dir);
    }

    @Override
    public boolean NextTask() throws IOException {
        if (m_arbiter instanceof GridArbiter && m_task instanceof GridTask) {
            gridTaskResult = ((GridTask) m_task).getData();
            gridTaskResult.taskEnd = System.currentTimeMillis();
            gridTaskResult.time = gridTaskResult.taskEnd - gridTaskResult.taskStart;
            gridTaskResults.add(gridTaskResult);
            LogUtils.e(TAG, "ADD DATA (Next task)!" + gridTaskResults.size() + "  " + gridTaskResult.isTutorial + " result = " + gridTaskResult);
        }

        boolean returnValue = super.NextTask();
        if (m_task instanceof AutoTutorialEndTask) {
            ((AutoTutorialEndTask) m_task).setTaskGroupController(this);
            if (BuildConfig.ENABLE_FAKE_RANDOM) {
                taskId++;
            }
        }
        if (BuildConfig.ENABLE_FAKE_RANDOM) {
            if (m_task instanceof GridTask) {
                ((GridTask) m_task).setFakeRandomMode(taskId, isAbstractTask);
            }
        }
        return returnValue;
    }


    @Override
    public boolean SelectTask(int a_index) throws IOException {
        boolean returnValue = super.SelectTask(a_index);
        return returnValue;
    }


    @Override
    public MarkData GetMark() {
        MarkData markData;
        int count = 0;
        for (GridTask.GridTaskResult taskResult : gridTaskResults) {
            count += taskResult.results.size();
        }
//demo1:8,
//demo2:8,
// proper: 42
        double mark = ((GridArbiter) m_arbiter).setData(gridTaskResults, isAbstractTask, tutorialWasSuccesful, System.currentTimeMillis() - startTime, count == 58 || count == 50);

        //Super calls arbiter;
        markData = super.GetMark();
        markData.m_mark = mark;
        markData.area = area;

        return markData;
    }


    @Override
    public boolean tutorialIsSuccesful() {
        if (!wasChecked) {
            tutorialWasSuccesful = GridArbiter.isTutorialSuccesfull(gridTaskResults);
            wasChecked = true;
            if (tutorialWasSuccesful) {
                taskId++;
            }
            return tutorialWasSuccesful;
        } else {
            return true;
        }
    }


    @Override
    public void clearTutorialData() {

    }

    @Override
    public AutoTutorialEndTask.Mode getMode() {
        return AutoTutorialEndTask.Mode.GRID;
    }
}

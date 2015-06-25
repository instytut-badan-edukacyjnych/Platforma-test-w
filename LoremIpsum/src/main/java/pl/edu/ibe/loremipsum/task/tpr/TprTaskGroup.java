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


package pl.edu.ibe.loremipsum.task.tpr;


import android.content.Context;

import java.io.IOException;

import pl.edu.ibe.loremipsum.arbiter.BaseArbiter;
import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskGroup;


/**
 * Klasa bazowa zadania grup zadań. Stanowi opakowanie do kilku zadań
 * składających się na jedno zadanie.
 */
public abstract class TprTaskGroup extends TaskGroup implements TaskGroupController {
    private static final String TAG = TprTaskGroup.class.toString();

    protected boolean isAfterTutorial;


    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public TprTaskGroup(Context context) {
        super(context);
    }


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#NextTask()
     */
    @Override
    public boolean NextTask() throws IOException {
        boolean returnValue = super.NextTask();
        if (m_task instanceof TutorialEndTask) {
            ((TutorialEndTask) m_task).setTaskGroupController(this);
            isAfterTutorial = true;
        }
        if (m_task instanceof AutoTutorialEndTask) {
            isAfterTutorial = true;
        }


        return returnValue;
    }


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SelectTask(int)
     */
    @Override
    public boolean SelectTask(int a_index) throws IOException {
        boolean returnValue = super.SelectTask(a_index);
        if (m_task instanceof TutorialEndTask) {
            ((TutorialEndTask) m_task).setTaskGroupController(this);
        }

        return returnValue;
    }


    @Override
    public void rollbackToPoint(int taskNumber) {
        isAfterTutorial = false;
        m_taskIndex = taskNumber;
        ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 50);
    }

    @Override
    public void goToNextTask() {
        ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 50);
    }


    @Override
    public void StartTask() {
        super.StartTask();
        if (m_arbiter != null) {
            m_arbiter.StartTask();
        }
    }

    @Override
    public void EndTask() {
        super.EndTask();
        if (m_arbiter != null) {
            m_arbiter.EndTask();
        }
    }

    /**
     * Osługa wywoływana po zakończeniu odtwarzania dźwieku
     */
    @Override
    public void SoundFinish() {
        super.SoundFinish();
        ArbiterCommandFinish();
    }

    @Override
    public void SetMark(int a_mark) {
        super.SetMark(a_mark);
        if (m_arbiter != null) {
            if (m_property.expandMarksFlag) {
                m_arbiter.ForceMark(a_mark);
            } else {
                m_arbiter.Mark(a_mark, BaseArbiter.APP_ARBITER_HUMAN);
            }
        }
    }


    @Override
    public MarkData GetMark() {
        super.GetMark();
        if (m_arbiter != null) {
            return m_arbiter.GetMark();
        } else {
            return new MarkData();
        }
    }

    @Override
    protected void ScreenTouched() {
        super.ScreenTouched();

        if (m_arbiter != null) {
            m_arbiter.TouchScreen();
        }
    }

    @Override
    public void ArbiterRepeatCommand() {
        if (m_arbiter != null) {
            m_arbiter.RepeatCommand();
        }
        super.ArbiterRepeatCommand();
    }

    @Override
    protected void ArbiterAttempt() {
        if (m_arbiter != null) {
            m_arbiter.Attempt();
        }
        super.ArbiterAttempt();
    }

    @Override
    public void ArbiterCommandFinish() {

        if (m_arbiter != null) {
            m_arbiter.FinishCommand();
        }
        super.ArbiterCommandFinish();
    }


}



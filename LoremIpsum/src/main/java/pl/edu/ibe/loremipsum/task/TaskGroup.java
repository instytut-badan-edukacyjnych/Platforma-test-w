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


package pl.edu.ibe.loremipsum.task;


import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Vector;

import pl.edu.ibe.loremipsum.arbiter.BaseArbiter;
import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa bazowa zadania grup zadań. Stanowi opakowanie do kilku zadań
 * składających się na jedno zadanie.
 */
public class TaskGroup extends BaseTask {
    private static final String TAG = TaskGroup.class.toString();

    /**
     * lista zadan
     */
    protected static Vector<TaskGroupInfo> m_tasks = null;
    /**
     * aktualnie wykonywane zadanie
     */
    protected BaseTask m_task = null;
    /**
     * indeks aktualnie wykonywanego zadania
     */
    protected int m_taskIndex;
    /**
     * indeks petli zadan po powtórzeniu polecenia
     */
    protected int m_loopIndex = -1;
    protected BaseTask m_loopTask = null;
    /**
     * flaga uruchamiania alternatywnej wersji zadań
     */
    protected boolean m_alternativeFlag = false;

    /**
     * ocena zadania grupowego
     */
    protected MarkData m_groupMark = null;
    /**
     * flaga odczytsania oceny zadanai grupowego
     */
    protected boolean m_markUpdate = false;

    protected LinearLayout view;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public TaskGroup(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean valid = super.Create(a_info, a_handler, a_dir);

        /* pojemnik na zadania */
        m_tasks = new Vector<TaskGroupInfo>(10);

        if (valid) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                /* pomijamy pierwszy element bo to nagłowek zadania grupowego */
                for (int index = 1; index < list.getLength(); ++index) {
                    NamedNodeMap map = list.item(index).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_DETAILS_TASK_CLASS);
                        if (node != null) {
                            TaskGroupInfo info = new TaskGroupInfo(a_info);
                            info.m_class = node.getNodeValue();
                            info.m_groupIndex = index;

                            LogUtils.d(TAG, "Create: " + info.m_class);

                            m_tasks.add(info);
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        if (m_tasks.size() > 0) {
            m_taskIndex = 0;

            m_task = BaseTask.CreateInstance(getContext(), this, m_tasks.get(m_taskIndex), m_tasks.get(m_taskIndex).m_class, m_actHandler, m_resultDir);
            UpdateInfo(m_task, m_tasks.get(m_taskIndex));

            LogUtils.d(TAG, "Create.CreateInstance: " + m_taskIndex + " " + m_task.m_taskInfo.m_class);
        }

        m_groupMark = new MarkData();

        BaseArbiter.m_cumulate = 0;

        return valid;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SizeChanged(int, int, int, int)
     */
    @Override
    public void SizeChanged(int w, int h, int oldw, int oldh) {
        if (m_task != null) {
            m_task.SizeChanged(w, h, oldw, oldh);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        if (view != null) {
            view.setVisibility(View.GONE);
        }


        LogUtils.d(TAG, "Destory");

        if (m_task != null) {
            m_task.Destory();
        }

        if (m_loopTask != null) {
            m_loopTask.Destory();
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Draw(android.graphics.Canvas)
     */
    @Override
    public void Draw(Canvas canvas) {
        if (m_task != null) {
            m_task.Draw(canvas);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#TouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean TouchEvent(MotionEvent event) {
        if (m_task != null)
            return m_task.TouchEvent(event);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#GetButtonVisibility()
     */
    @Override
    public int GetButtonVisibility() {
        return m_task.GetButtonVisibility();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#PlayCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayCommand(MediaPlayer a_player) throws IOException {
        m_task.PlayCommand(a_player);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#GetMarkRange()
     */
    @Override
    public int GetMarkRange() {
        return m_task.GetMarkRange();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#PlayExtraCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayExtraCommand(MediaPlayer a_player) throws IOException {
        m_task.PlayExtraCommand(a_player);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#RepeatCommand(android.media.MediaPlayer)
     */
    @Override
    public void RepeatCommand(MediaPlayer a_player) throws IOException {
        m_alternativeFlag = !m_alternativeFlag;

        if (m_loopIndex < 0) {
            for (int index = 0; index < m_taskIndex; ++index) {
                TaskGroupInfo info = m_tasks.get(index);
                if (info.m_loop) {
                    m_loopIndex = index;
                    break;
                }
            }
        }

        if (m_loopIndex >= 0) {
            BaseArbiter.m_cumulate = 0;

            m_actHandler.sendMessage(m_actHandler.obtainMessage(BaseTask.TASK_MESS_SELECT_TASK, m_loopIndex, 0));
        } else {
            m_task.RepeatCommand(a_player);
        }

    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#StartTask()
     */
    @Override
    public void StartTask() {
        m_markUpdate = false;

        LoremIpsumApp.m_markRange = m_task.GetMarkRange();

        m_task.StartTask();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#EndTask()
     */
    @Override
    public void EndTask() {
        m_task.EndTask();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#NextTask()
     */
    @Override
    public boolean NextTask() throws IOException {
        m_taskIndex++;

        LogUtils.d(TAG, "NextTask: " + m_taskIndex);

        if (m_tasks.size() > m_taskIndex) {
            TaskGroupInfo info = m_tasks.get(m_taskIndex);
            if (info.m_loaded) {
                if (info.m_alternative && m_alternativeFlag) {
                    if (m_tasks.size() > m_taskIndex) {
                        m_taskIndex++;
                    }
                }
            }

            BaseTask task = null;
            if (m_taskIndex != m_loopIndex) {
                task = BaseTask.CreateInstance(getContext(), this, m_tasks.get(m_taskIndex), m_tasks.get(m_taskIndex).m_class, m_actHandler, m_resultDir);

                info = m_tasks.get(m_taskIndex);
                UpdateInfo(task, info);
            } else {
                task = m_loopTask;
            }

            LogUtils.d(TAG, "NextTask.CreateInstance: " + m_taskIndex + " " + task.m_taskInfo.m_class);

            if (info.m_alternative) {
                m_taskIndex++;
            }

            if ((m_task != m_loopTask) && (m_task != null)) {
                LogUtils.d(TAG, "NextTask.Destroy: " + m_task.m_taskInfo.m_class);
                m_task.Destory();
            }
            m_task = null;
            m_task = task;

            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SelectTask(int)
     */
    @Override
    public boolean SelectTask(int a_index) throws IOException {
        LogUtils.d(TAG, "SelectTask: " + a_index);

        if (m_tasks.size() > a_index) {
            m_taskIndex = a_index;

            BaseTask task = null;
            if (a_index != m_loopIndex) {
                task = BaseTask.CreateInstance(getContext(), this, m_tasks.get(m_taskIndex), m_tasks.get(m_taskIndex).m_class, m_actHandler, m_resultDir);
                TaskGroupInfo info = m_tasks.get(m_taskIndex);
                UpdateInfo(task, info);
                LogUtils.d(TAG, "SelectTask.CreateInstance: " + m_taskIndex + " " + task.m_taskInfo.m_class);
            } else {
                task = m_loopTask;
            }

            if ((m_task != m_loopTask) && (m_task != null)) {
                LogUtils.d(TAG, "SelectTask.Destroy: " + m_task.m_taskInfo.m_class);
                m_task.Destory();
            }
            m_task = null;
            m_task = task;

            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#IsSoundNextTask()
     */
    @Override
    public boolean IsSoundNextTask() {

        return m_task.IsSoundNextTask();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SoundFinish()
     */
    @Override
    public void SoundFinish() {

        m_task.SoundFinish();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SetMark(int)
     */
    @Override
    public void SetMark(int a_mark) {

        m_task.SetMark(a_mark);
        MarkData mark = m_task.GetMark();
        m_markUpdate = true;

        m_groupMark.Add(mark);

    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#GetMark()
     */
    @Override
    public MarkData GetMark() {

        if (!m_markUpdate) {
            MarkData mark = m_task.GetMark();
            m_groupMark.Add(mark);
        }

        return m_groupMark;
    }


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ArbiterRepeatCommand()
     */
    @Override
    public void ArbiterRepeatCommand() {

        m_task.ReloadTask();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ArbiterAttempt()
     */
    @Override
    protected void ArbiterAttempt() {

        m_task.ArbiterRepeatCommand();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ArbiterCommandFinish()
     */
    @Override
    public void ArbiterCommandFinish() {

        m_task.ArbiterCommandFinish();
    }

    /**
     * Aktualizauje informace o zadaniu
     *
     * @param a_task - zadanie
     * @param a_info - informacja o zadaniu w grupie
     */
    protected void UpdateInfo(BaseTask a_task, TaskGroupInfo a_info) {

        if (!a_info.m_loaded) {
            a_info.m_loaded = true;

            a_info.m_loop = a_task.m_property.loop;
            if (a_info.m_loop) {
                m_loopIndex = m_taskIndex;
                m_loopTask = a_task;
            }

            a_info.m_alternative = a_task.m_property.alternative;
        }
    }

    public void setView(LinearLayout view) {
        this.view = view;
    }


    /**
     * Informacja grupowa o zadaniu
     */
    protected class TaskGroupInfo extends TaskInfo {

        public boolean m_loaded;
        public boolean m_alternative;
        public boolean m_loop;

        public TaskGroupInfo(TaskInfo a_info) {

            super(a_info);

            m_loaded = false;
            m_alternative = false;
            m_loop = false;
        }
    }

}



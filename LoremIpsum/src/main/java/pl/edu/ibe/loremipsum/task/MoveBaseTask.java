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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Klasa bazwoa obsługi zadania polegajacego na przesunieciu określonej liczby
 * elementów na pole docelowe.
 *
 *
 */
public class MoveBaseTask extends LookTask {
    private static final String TAG = MoveBaseTask.class.toString();

    protected int m_width;
    protected int m_height;
    protected ArrayList<FieldSelect> m_items = null;
    protected ArrayList<TargetField> m_targetList = null;
    protected FieldSelect m_move = null;
    protected int m_xOffset;
    protected int m_yOffset;
    protected String m_answer = null;
    protected int m_horBase;
    protected ArrayList<PlaceFiled> m_pullList = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public MoveBaseTask(Context context) {
        super(context);
    }


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_move = null;
        m_items = new ArrayList<FieldSelect>(12);
        m_pullList = new ArrayList<PlaceFiled>(12);
        m_targetList = new ArrayList<TargetField>(3);
        m_horBase = 0;

        return valid;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        for (FieldSelect f : m_items) {
            if (f.m_mask != null) {
                f.m_mask.recycle();
                f.m_mask = null;
            }
        }

        for (TargetField f : m_targetList) {
            if (f.m_mask != null) {
                f.m_mask.recycle();
                f.m_mask = null;
            }
        }

        super.Destory();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Draw(android.graphics.Canvas)
     */
    @Override
    public void Draw(Canvas canvas) {

        super.Draw(canvas);

        for (FieldSelect f : m_items) {
            if (f.m_mask == null)
                LogUtils.e(TAG, "m_mask is null");
            else
                canvas.drawBitmap(f.m_mask, null, f.m_srce, m_paint);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#TouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean TouchEvent(MotionEvent event) {

        ScreenTouched();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int last = event.getPointerCount() - 1;
                if (last >= 0) {
                    Float xPos = new Float(event.getX(last));
                    Float yPos = new Float(event.getY(last));
                    int x = xPos.intValue();
                    int y = yPos.intValue();

                    for (FieldSelect f : m_items) {
                        if ((x > f.m_srce.left) && (x < f.m_srce.right) && (y > f.m_srce.top) && (y < f.m_srce.bottom)) {
                            if (f.m_mask == null)
                                LogUtils.e(TAG, "m_mask is null");
                            else if ((f.m_mask.getPixel(x - f.m_srce.left, y - f.m_srce.top) & COLOR_ALPHA_MASK) != 0) {
                                m_xOffset = f.m_srce.left - x;
                                m_yOffset = f.m_srce.top - y;
                                m_width = f.m_srce.right - f.m_srce.left;
                                m_height = f.m_srce.bottom - f.m_srce.top;
                                m_move = f;

                                if (m_items.remove(f)) {
                                    m_items.add(f);
                                }

                                if (m_property.pull) {
                                    // zwolnienie pola
                                    for (PlaceFiled p : m_pullList) {
                                        if ((m_move.m_srce.left + m_width / 2) == p.m_xPlace) {
                                            if (((m_move.m_srce.top + m_height / 2) == p.m_yPlace) || m_property.baseLine) {
                                                p.m_occupied = false;
                                                LogUtils.d(TAG, "pull free " + m_move.m_srce.left + " " + m_move.m_srce.top);
                                                break;
                                            }
                                        }
                                    }
                                }

                                m_actHandler.sendEmptyMessage(TASK_MESS_HAPTIC_FEEDBACK);
                                break;
                            }
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (m_move != null) {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();

                        m_move.m_srce.left = x + m_xOffset;
                        m_move.m_srce.right = m_move.m_srce.left + m_width;
                        m_move.m_srce.top = y + m_yOffset;
                        m_move.m_srce.bottom = m_move.m_srce.top + m_height;

                        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {

                if ((m_move != null)) {
                    int x = (m_move.m_srce.left + m_move.m_srce.right) / 2;
                    int y = (m_move.m_srce.top + m_move.m_srce.bottom) / 2;
                    boolean hit = false;

                    for (TargetField t : m_targetList) {
                        if (x > 0 && x < t.m_mask.getWidth() && y > 0 && y < t.m_mask.getHeight()) {
                            if ((t.m_mask.getPixel(x, y) & COLOR_ALPHA_MASK) != 0) {
                                m_move.m_selected = true;
                                hit = true;

                                if (m_property.pull) {
                                    PlaceFiled near = null;
                                    int dist = -1;
                                    for (PlaceFiled p : m_pullList) {
                                        if (!p.m_occupied) {
                                            int d = (x - p.m_xPlace) * (x - p.m_xPlace) +
                                                    (y - p.m_yPlace) * (y - p.m_yPlace);
                                            if ((dist > d) || (dist < 0)) {
                                                dist = d;
                                                near = p;
                                            }
                                        }
                                    }

                                    if (near != null) {
                                        LogUtils.d(TAG, "occupied");

                                        near.m_occupied = true;

                                        m_move.m_srce.left = near.m_xPlace - m_width / 2;
                                        m_move.m_srce.right = m_move.m_srce.left + m_width;

                                        if (m_property.baseLine) {
                                            m_move.m_srce.top = m_horBase - m_height;
                                            m_move.m_srce.bottom = m_move.m_srce.top + m_height;
                                        } else {
                                            m_move.m_srce.top = near.m_yPlace - m_height / 2;
                                            m_move.m_srce.bottom = m_move.m_srce.top + m_height;
                                        }

                                        LogUtils.d(TAG, " pull " + m_move.m_srce.left + " " + m_move.m_srce.top);
                                    } else {
                                        hit = false;
                                    }
                                }

                                break;
                            }
                        }
                    }

                    if (!hit) {
                        m_move.m_selected = false;
                        m_move.m_srce = new Rect(m_move.m_dest);
                    }
                }

                m_move = null;

                ArbiterAssess(m_items, m_answer);
                ArbiterAttempt();

                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                m_actHandler.sendEmptyMessage(TASK_MESS_MARK);
            }
            break;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ReloadTask()
     */
    @Override
    public void ReloadTask() {

        super.ReloadTask();

        for (FieldSelect f : m_items) {
            f.m_srce = new Rect(f.m_dest);
        }

        for (PlaceFiled p : m_pullList) {
            p.m_occupied = false;
        }
    }


    /**
     * Opis pola gdzie może być połozony element
     *
     *
     */
    protected class PlaceFiled {

        public int m_xPlace;
        public int m_yPlace;
        public boolean m_occupied;
    }


    /**
     * Opis pola
     *
     *
     */
    protected class TargetField {

        public Bitmap m_mask;
        public String m_answer;
    }

}



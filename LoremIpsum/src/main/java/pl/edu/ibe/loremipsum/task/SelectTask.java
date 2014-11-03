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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Vector;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsługi zadania polegającym na wyborze jednego z wydzielonych
 * pól zaznaczanych identycznym markerem
 *
 *
 */
public class SelectTask extends AnimateTask {
    private static final String TAG = SelectTask.class.toString();


    protected Bitmap m_marker = null;
    protected int m_width;
    protected int m_height;
    protected Vector<FieldSelect> m_pos = null;
    protected String m_answer = null;
    protected int m_threshold;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public SelectTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.AnimateTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_pos = new Vector<FieldSelect>(4);


        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_DETAILS_TASK_MARKER);
                        if (node != null) {
                            String fileName = node.getNodeValue();

                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                            valid = bitmapMaker.isValid();
                            m_width = bitmapMaker.getWidth();
                            m_height = bitmapMaker.getHeight() * VIEW_SIZE_CORECTION_FACTOR_MUL / VIEW_SIZE_CORECTION_FACTOR_DIV;
                            m_marker = bitmapMaker.getBitmap();
                        } else {
                            m_marker = null;
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_MARKER);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_ANSWER);
                        if (node != null) {
                            m_answer = node.getNodeValue();
                        } else {
                            m_answer = null;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_THRESHOLD);
                        if (node != null) {
                            try {
                                m_threshold = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }

                        } else {
                            m_threshold = 0;
                        }
                    }
                }
            } else {
                valid = false;
            }

            list = m_document.getElementsByTagName(XML_DETAILS_FIELD);
            if (list != null) {
                if (list.getLength() > 0) {
                    for (int index = 0; index < list.getLength(); ++index) {
                        NamedNodeMap map = list.item(index).getAttributes();
                        if (map != null) {
                            Rect rec = new Rect();
                            String name = null;

                            Node node = map.getNamedItem(XML_DETAILS_FIELD_NAME);
                            if (node != null) {
                                name = node.getNodeValue();
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_NAME);
                            }
                            node = null;

                            node = map.getNamedItem(XML_DETAILS_FIELD_X);
                            if (node != null) {
                                try {
                                    rec.left = Integer.parseInt(node.getNodeValue());
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_X);
                            }
                            node = null;

                            node = map.getNamedItem(XML_DETAILS_FIELD_Y);
                            if (node != null) {
                                try {
                                    rec.top = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_Y);
                            }

                            if (valid) {
                                rec.right = rec.left + m_width;
                                rec.bottom = rec.top + m_height;

                                FieldSelect sel = new FieldSelect();
                                sel.m_name = name;
                                sel.m_srce = rec;
                                sel.m_selected = false;

                                m_pos.add(sel);
                            }
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        return valid;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.AnimateTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        for (FieldSelect f : m_pos) {
            if (f.m_mask != null) {
                f.m_mask.recycle();
                f.m_mask = null;
            }
        }

        if (m_marker != null) {
            m_marker.recycle();
            m_marker = null;
        }

        super.Destory();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.AnimateTask#Draw(android.graphics.Canvas)
     */
    @Override
    public void Draw(Canvas canvas) {

        super.Draw(canvas);

        if ((m_marker != null)) {
            for (FieldSelect f : m_pos) {
                if (f.m_selected) {
                    canvas.drawBitmap(m_marker, null, f.m_srce, m_paint);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#TouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean TouchEvent(MotionEvent event) {

        ScreenTouched();

        boolean retValue = false;

        if (m_marker != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();

                        for (FieldSelect f : m_pos) {
                            if ((x > f.m_srce.left) && (x < f.m_srce.right) && (y > f.m_srce.top) && (y < f.m_srce.bottom)) {
                                if (f.m_selected) {
                                    f.m_selected = false;
                                } else {
                                    for (FieldSelect n : m_pos) {
                                        n.m_selected = false;
                                    }
                                    f.m_selected = true;
                                }

                                retValue = true;

                                ArbiterAssess(m_pos, m_answer);
                                ArbiterAttempt();

                                m_actHandler.sendEmptyMessage(TASK_MESS_HAPTIC_FEEDBACK);
                                m_actHandler.sendEmptyMessage(TASK_MESS_MARK);

                                break;
                            }
                        }
                    }
                }
                break;

            }
        }

        return retValue;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ReloadTask()
     */
    @Override
    public void ReloadTask() {

        super.ReloadTask();

        for (FieldSelect f : m_pos) {
            f.m_selected = false;
        }
    }

}



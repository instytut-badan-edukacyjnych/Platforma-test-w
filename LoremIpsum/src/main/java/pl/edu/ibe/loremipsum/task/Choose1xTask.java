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
 * Klasa obsługi zadania zadania polegającymna wybraniu jednego elementu.
 * Każdy z elementów zaznaczony jet na własnej masce.
 *
 *
 */
public class Choose1xTask extends AnimateTask {
    private static final String TAG = Choose1xTask.class.toString();


    protected Vector<FieldSelect> m_field = null;
    protected int m_number = 0;
    protected String m_answer = null;
    protected int m_threshold = 0;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public Choose1xTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.AnimateTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_field = new Vector<FieldSelect>(6);
        int level = 0;
        int level1 = 0;

        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    Node rootNode = list.item(a_info.m_groupIndex);
                    NamedNodeMap map = rootNode.getAttributes();
                    if (map != null) {

                        try {
                            // odpowiedź
                            m_answer = GetString(map, XML_DETAILS_TASK_ANSWER);

                            // próg pomiedzy oceną 1 i 2
                            m_threshold = GetInteger(map, XML_DETAILS_TASK_THRESHOLD, 0);

                            // próg przy pomiarze czasu
                            level = GetInteger(map, XML_DETAILS_TASK_LEVEL, 0);

                            // drugi próg przy pomiarze czasu
                            level = GetInteger(map, XML_DETAILS_TASK_LEVEL_1, 0);
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER, e);
                        }
                    }

                    NodeList childList = rootNode.getChildNodes();
                    if (childList != null) {
                        for (int index = 0; index < childList.getLength(); ++index) {
                            Node childNode = childList.item(index);
                            if (childNode.getNodeName().compareTo(XML_DETAILS_FIELD) == 0) {
                                NamedNodeMap childMap = childNode.getAttributes();
                                if (childMap != null) {
                                    Bitmap bitmap = null;
                                    String name = null;
                                    int width = 0;
                                    ;
                                    int height = 0;

                                    try {
                                        // nazwa pola
                                        name = GetString(childMap, XML_DETAILS_FIELD_NAME);

                                        // obrazek pola
                                        String fileName = GetString(childMap, XML_DETAILS_FIELD_MASK);
                                        VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                                        LogUtils.d(TAG, "." + XML_DETAILS_FIELD_MASK + ": " + fileName);

                                        BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                                        valid = bitmapMaker.isValid();
                                        width = bitmapMaker.getWidth();
                                        height = bitmapMaker.getHeight();
                                        bitmap = bitmapMaker.getBitmap();
                                        map = null;

                                        if (valid) {
                                            Rect srce = new Rect(0, 0, width, height);
                                            Rect dest = new Rect(srce);
                                            dest.bottom = (srce.bottom * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;

                                            FieldSelect sel = new FieldSelect();
                                            sel.m_name = name;
                                            sel.m_srce = new Rect(0, 0, width / 2, height / 2);
                                            sel.m_dest = dest;
                                            sel.m_mask = bitmap;
                                            sel.m_selected = false;

                                            m_field.add(sel);
                                        }
                                    } catch (XMLFileException | IOException e) {
                                        LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER, e);
                                    }
                                }
                            }
                        }
                    } else {
                        valid = false;
                    }
                }
            } else {
                valid = false;
            }

        }

        if (valid) {
            if (m_arbiter != null) {
                m_arbiter.SetExtraParameters(level, level1);
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

        for (FieldSelect f : m_field) {
            if (f.m_mask != null) {
                f.m_mask.recycle();
                f.m_mask = null;
            }
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

        for (FieldSelect f : m_field) {
            if (f.m_selected && f.m_mask != null) {
                canvas.drawBitmap(f.m_mask, f.m_srce, f.m_dest, m_paint);
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

        if (m_field.size() > 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = (yPos.intValue() * VIEW_SIZE_CORECTION_FACTOR_DIV) / VIEW_SIZE_CORECTION_FACTOR_MUL;

                        for (FieldSelect f : m_field) {
                            if (f.m_mask == null)
                                LogUtils.e(TAG, "m_mask is null");
                            else if ((f.m_mask.getPixel(x / 2, y / 2) & COLOR_ALPHA_MASK) != 0) {
                                if (f.m_selected) {
                                    f.m_selected = false;
                                } else {
                                    for (FieldSelect n : m_field) {
                                        n.m_selected = false;
                                    }
                                    f.m_selected = true;
                                }

                                if (GetMarkRange() == TASK_MARK_RANGE_0_1) {
                                    ArbiterAssess(m_field, m_answer);
                                } else {
                                    ArbiterAssess(m_field, m_answer, m_threshold);
                                }
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

        return true;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#ReloadTask()
     */
    @Override
    public void ReloadTask() {

        super.ReloadTask();

        for (FieldSelect f : m_field) {
            f.m_selected = false;
        }
    }

}

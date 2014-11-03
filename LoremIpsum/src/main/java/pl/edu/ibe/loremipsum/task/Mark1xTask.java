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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
 * Klasa obsługi zadania pojegającym na zaznaczaniu elementu obrazem
 * ze wspólnego markera/maski
 *
 *
 */
public class Mark1xTask extends AnimateTask {
    private static final String TAG = Mark1xTask.class.toString();


    protected int m_sizeX;
    protected int m_sizeY;

    protected Bitmap m_marker = null;
    protected Vector<FieldSelect> m_pos = null;
    protected String m_answer = null;
    protected int m_threshold = 0;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public Mark1xTask(Context context) {
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
        int level = 0;
        int level1 = 0;

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

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                Bitmap temp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                Matrix mat = new Matrix();
                                mat.setScale(0.5f, 0.5f);
                                m_marker = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), mat, true);
                                // INFO_<else>
//								m_marker = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                // INFO_RESIZE <end>

                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                m_marker = null;
                                valid = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                            BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                            valid = bitmapMaker.isValid();
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
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_LEVEL);
                        if (node != null) {
                            try {
                                level = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }

                        } else {
                            level = 0;
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_LEVEL_1);
                        if (node != null) {
                            try {
                                level1 = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }

                        } else {
                            level1 = 0;
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_SX);
                        if (node != null) {
                            try {
                                m_sizeX = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_SX);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_SY);
                        if (node != null) {
                            try {
                                m_sizeY = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_SY);
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
                                    rec.top = Integer.parseInt(node.getNodeValue());
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_Y);
                            }

                            if (valid) {
                                rec.right = rec.left + m_sizeX;
                                rec.bottom = rec.top + m_sizeY;

                                FieldSelect sel = new FieldSelect();
                                sel.m_name = name;
                                sel.m_srce = new Rect(rec.left / 2, rec.top / 2, rec.right / 2, rec.bottom / 2);
                                sel.m_dest = new Rect(rec.left,
                                        (rec.top * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV,
                                        rec.right,
                                        (rec.bottom * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV);

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
        LogUtils.d(TAG, ".Destory");

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

    @Override
    /**
     *
     */ public void Draw(Canvas canvas) {

        super.Draw(canvas);

        if ((m_marker != null)) {
            for (FieldSelect f : m_pos) {
                if (f.m_selected) {
                    canvas.drawBitmap(m_marker, f.m_srce, f.m_dest, m_paint);
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
                            if (((x / 2) > f.m_srce.left) && ((x / 2) < f.m_srce.right) && ((y / 2) > f.m_srce.top) && ((y / 2) < f.m_srce.bottom)) {
                                if (f.m_selected) {
                                    f.m_selected = false;
                                } else {
                                    for (FieldSelect n : m_pos) {
                                        n.m_selected = false;
                                    }
                                    f.m_selected = true;
                                }

                                retValue = true;

                                if (GetMarkRange() == TASK_MARK_RANGE_0_1) {
                                    ArbiterAssess(m_pos, m_answer);
                                } else {
                                    ArbiterAssess(m_pos, m_answer, m_threshold);
                                }
                                ArbiterAttempt();

                                m_actHandler.sendEmptyMessage(TASK_MESS_HAPTIC_FEEDBACK);
                                m_actHandler.sendEmptyMessage(TASK_MESS_MARK);

                                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);

                                if (m_property.stepAnswer) {
                                    m_actHandler.sendEmptyMessage(TASK_MESS_NEXT_TASK);
                                }

                                break;
                            }
                        }
                    }
                }
                break;

                case MotionEvent.ACTION_UP: {
                    if (m_property.deselectUp) {
                        for (FieldSelect f : m_pos) {
                            f.m_selected = false;
                        }

                        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
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



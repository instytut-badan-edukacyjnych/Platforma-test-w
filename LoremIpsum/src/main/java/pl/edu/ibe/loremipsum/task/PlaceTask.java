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
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * Klasa obsługi zadania polegajacego na przesunieciu określonej liczby
 * elementów na okreslone pola.
 *
 *
 */
public class PlaceTask extends LookTask {
    private static final String TAG = PlaceTask.class.toString();


    protected int m_width;
    protected int m_height;
    protected ArrayList<FieldSelect> m_items = null;
    protected FieldSelect m_move = null;
    protected int m_xOffset;
    protected int m_yOffset;
    protected String m_answer = null;
    protected int m_horBase;
    protected ArrayList<FieldSelect> m_pullList = null;
    protected Bitmap m_target = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public PlaceTask(Context context) {
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
        m_pullList = new ArrayList<FieldSelect>(12);
        m_horBase = 0;

        if (m_document != null) {
            int width = 0;
            int height = 0;

            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_DETAILS_TASK_ANSWER);
                        if (node != null) {
                            m_answer = node.getNodeValue();
                        } else {
                            m_answer = null;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_PLACE);
                        if (node != null) {
                            String fileName = node.getNodeValue();

                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            // TODO w ogóle tu się dzieje... czemu m_target jest dwa razy przypisywany? czy INFO_RESIZE ma jakiś wpływ na aplikację? był jakiś poprzedni system budowania, który uwzględniał takie komentarze?
                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                Bitmap temp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                Matrix mat = new Matrix();
                                mat.setScale(0.5f, 0.5f);
                                m_target = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), mat, true);
                                // INFO_RESIZE <else>
                                m_target = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                // INFO_RESIZE <end>

                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }

                        } else {
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_PLACE);
                        }
                        node = null;

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
                            Bitmap bmp = null;
                            int px = 0;
                            int py = 0;

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
                            node = null;

                            if (m_property.pull) {
                                // pola moga nie wystepowac pomimo ustawienia flagi
                                node = map.getNamedItem(XML_DETAILS_FIELD_PX);
                                if (node != null) {
                                    try {
                                        px = Integer.parseInt(node.getNodeValue());
                                    } catch (NumberFormatException e) {
                                        valid = false;
                                        LogUtils.e(TAG, e);
                                    }
                                }
                                node = null;

                                node = map.getNamedItem(XML_DETAILS_FIELD_PY);
                                if (node != null) {
                                    try {
                                        py = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                    } catch (NumberFormatException e) {
                                        valid = false;
                                        LogUtils.e(TAG, e);
                                    }
                                }
                            }

                            node = map.getNamedItem(XML_DETAILS_FIELD_MASK);
                            if (node != null) {
                                String fileName = node.getNodeValue();

                                VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                                if (bmpFile.exists()) {
                                    // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                    // jest mała więc nie pomniejszamy
                                    bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                    height = (bmp.getHeight() * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                    width = bmp.getWidth();
                                    LogUtils.d(TAG, "BitmapFactory: " + fileName);
                                } else {
                                    // nie ma obrazka
                                    bmp = null;
                                    valid = false;
                                    LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                                }
                            } else {
                                bmp = null;
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_MASK);
                            }

                            if (valid) {
                                boolean mirror = false;
                                if (rec.left < 0) {
                                    mirror = true;
                                    rec.right = -rec.left;
                                    rec.left = rec.right - width;
                                    px = -px;
                                } else {
                                    rec.right = rec.left + width;
                                }
                                rec.bottom = rec.top + height;

                                FieldSelect sel = new FieldSelect();
                                sel.m_name = name;
                                sel.m_srce = rec;
                                sel.m_dest = new Rect(rec);
                                sel.m_mask = bmp;
                                sel.m_xPlace = px;
                                sel.m_yPlace = py;
                                sel.m_selected = false;
                                sel.m_mirror = mirror;

                                m_items.add(sel);

                                if (px != 0 && py != 0) {
                                    FieldSelect f = new FieldSelect();
                                    f.m_xPlace = px;
                                    f.m_yPlace = py;
                                    f.m_selected = false;
                                    f.m_mirror = mirror;
                                    m_pullList.add(f);
                                }
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
                                    for (FieldSelect p : m_pullList) {
                                        if ((!p.m_mirror && m_move.m_srce.left == p.m_xPlace) ||
                                                (p.m_mirror && m_move.m_srce.right == p.m_xPlace)) {
                                            if (m_move.m_srce.top == p.m_yPlace) {
                                                p.m_selected = false;
                                                LogUtils.d(TAG, "pull free" + m_move.m_srce.left + " " + m_move.m_srce.top);
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

                    if (x > 0 && x < m_target.getWidth() &&
                            y > 0 && y < m_target.getHeight()) {
                        if ((m_target.getPixel(x, y) & COLOR_ALPHA_MASK) != 0) {
                            m_move.m_selected = true;
                            hit = true;

                            if (m_property.pull) {
                                FieldSelect near = null;
                                int dist = -1;
                                for (FieldSelect p : m_pullList) {
                                    if (!p.m_selected) {
                                        int d = (m_move.m_srce.top - p.m_yPlace) * (m_move.m_srce.top - p.m_yPlace);
                                        if (!p.m_mirror) {
                                            d += (m_move.m_srce.left - p.m_xPlace) * (m_move.m_srce.left - p.m_xPlace);
                                        } else {
                                            d += (m_move.m_srce.right - p.m_xPlace) * (m_move.m_srce.right - p.m_xPlace);
                                        }
                                        if ((dist > d) || (dist < 0)) {
                                            dist = d;
                                            near = p;
                                        }
                                    }
                                }

                                if (near != null) {
                                    LogUtils.d(TAG, "occupied");

                                    near.m_selected = true;

                                    if (!near.m_mirror) {
                                        m_move.m_srce.left = near.m_xPlace;
                                        m_move.m_srce.right = m_move.m_srce.left + m_width;
                                    } else {
                                        m_move.m_srce.right = near.m_xPlace;
                                        m_move.m_srce.left = m_move.m_srce.right - m_width;
                                    }

                                    m_move.m_srce.top = near.m_yPlace;
                                    m_move.m_srce.bottom = m_move.m_srce.top + m_height;

                                    LogUtils.d(TAG, "pull " + m_move.m_srce.left + " " + m_move.m_srce.top);
                                } else {
                                    hit = false;
                                }
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

        for (FieldSelect p : m_pullList) {
            p.m_selected = false;
        }
    }

}



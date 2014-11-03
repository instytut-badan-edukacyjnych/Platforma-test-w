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
 * Klasa obsługi zadania polegajacym na przesuwaniu wydzielonych obszarów
 * ustawionych w ciagu. Ciąg w zalezności od wielkości i liczby obszarów
 * może składac się z kilku wierszy.
 *
 *
 */
public class ShuffleTask extends LookTask {
    private static final String TAG = ShuffleTask.class.toString();


    protected ArrayList<FieldSelect> m_cards = null;
    protected ArrayList<FieldSelect> m_place = null;
    protected FieldSelect m_move = null;
    protected int m_xOffset;
    protected int m_yOffset;
    protected int m_width;
    protected int m_height;
    protected String m_answer = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public ShuffleTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_cards = new ArrayList<FieldSelect>(6);
        m_place = new ArrayList<FieldSelect>(6);


        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_DETAILS_TASK_ANSWER);
                        if (node != null) {
                            m_answer = node.getNodeValue();
                        } else {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_SX);
                        if (node != null) {
                            try {
                                m_width = Integer.parseInt(node.getNodeValue());
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
                                m_height = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
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
                            Bitmap bmp = null;
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
                                    rec.top = (rec.top * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_Y);
                            }
                            node = null;

                            node = map.getNamedItem(XML_DETAILS_FIELD_MASK);
                            if (node != null) {
                                String fileName = node.getNodeValue();

                                VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                                if (bmpFile.exists()) {
                                    // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                    // jest mała więc nie pomniejszamy
                                    bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
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
                                rec.right = rec.left + m_width;
                                rec.bottom = rec.top + m_height;

                                FieldSelect sel = new FieldSelect();
                                sel.m_name = name;
                                sel.m_srce = rec;
                                sel.m_dest = new Rect(rec);
                                sel.m_mask = bmp;
                                sel.m_selected = false;

                                m_cards.add(sel);
                                m_place.add(sel);
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
        for (FieldSelect f : m_cards) {
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

        for (FieldSelect f : m_cards) {
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

                    for (FieldSelect f : m_cards) {
                        if ((x > f.m_srce.left) && (x < f.m_srce.right) && (y > f.m_srce.top) && (y < f.m_srce.bottom)) {
                            m_xOffset = f.m_srce.left - x;
                            m_yOffset = f.m_srce.top - y;
                            m_move = f;

                            if (m_cards.remove(f)) {
                                m_cards.add(f);
                            }

                            m_actHandler.sendEmptyMessage(TASK_MESS_HAPTIC_FEEDBACK);
                            break;
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

                if (m_move != null) {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();
                        x += m_xOffset + m_width / 2;
                        y += m_yOffset + m_height / 2;

                        // wyjmujemy z listy
                        if (m_move.m_mask == null)
                            LogUtils.e(TAG, "m_mask is null");
                        Bitmap bmp = m_move.m_mask;
                        String nam = m_move.m_name;
                        for (int index = 0; index < m_place.size(); ++index) {
                            if (m_move.m_name.compareTo(m_place.get(index).m_name) == 0) {
                                ++index;
                                for (; index < m_place.size(); ++index) {
                                    if (m_place.get(index).m_mask == null)
                                        LogUtils.e(TAG, "m_mask is null");
                                    m_place.get(index - 1).m_mask = m_place.get(index).m_mask;
                                    m_place.get(index - 1).m_name = m_place.get(index).m_name;
                                }
                                break;
                            }
                        }

                        // szukamy gdzie włożyć
                        m_place.get(m_place.size() - 1).m_mask = bmp;
                        m_place.get(m_place.size() - 1).m_name = nam;
                        for (int index = m_place.size() - 1; index >= 0; --index) {
                            if ((m_place.get(index).m_dest.left < x) && (m_place.get(index).m_dest.top < y)) {
                                for (; index < m_place.size(); ++index) {
                                    if (m_place.get(index).m_mask == null)
                                        LogUtils.e(TAG, "m_mask is null");
                                    Bitmap tmpBmp = m_place.get(index).m_mask;
                                    String tmpNam = m_place.get(index).m_name;
                                    m_place.get(index).m_mask = bmp;
                                    m_place.get(index).m_name = nam;
                                    bmp = tmpBmp;
                                    nam = tmpNam;
                                }

                                break;
                            }
                        }

                        m_cards.clear();
                        for (FieldSelect f : m_place) {
                            f.m_srce = new Rect(f.m_dest);
                            m_cards.add(f);
                        }

                        ArbiterAssess(m_cards, m_answer);
                        ArbiterAttempt();

                        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                        m_actHandler.sendEmptyMessage(TASK_MESS_MARK);
                    }
                }
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

        for (FieldSelect f : m_cards) {

            f.m_srce = new Rect(f.m_dest);
        }
    }

}



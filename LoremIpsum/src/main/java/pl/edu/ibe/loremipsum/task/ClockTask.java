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
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsługi zadania pojegającym na ustawieniu wskazówek zegara
 * w okreslonym położeniu
 *
 *
 */
public class ClockTask extends LookTask {
    private static final String TAG = ClockTask.class.toString();


    protected ArrayList<FieldSelect> m_hand = null;
    protected int m_xCenter = 0;
    protected int m_yCenter = 0;
    protected FieldSelect m_move = null;
    protected int m_width = 0;
    protected int m_height = 0;
    protected boolean m_pullFlag = false;
    protected int m_answerHour = 0;
    protected int m_answerMin = 0;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public ClockTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_hand = new ArrayList<FieldSelect>(2);
        m_pullFlag = m_property.pull;


        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            // odpowiedź
                            String ans = GetString(map, XML_DETAILS_TASK_ANSWER);

                            int pos = ans.indexOf(':');
                            if (pos > 0) {
                                String hour = ans.substring(0, pos);
                                String min = ans.substring(pos + 1);
                                try {
                                    m_answerHour = Integer.parseInt(hour);
                                    if (m_answerHour >= 12) {
                                        m_answerHour -= 12;
                                    }
                                    m_answerHour *= 30;    // zmiana na kąt w stopniach
                                    // zmiana na zakres -180 +180
                                    if (m_answerHour > 180) {
                                        m_answerHour -= 360;
                                    }

                                    m_answerMin = Integer.parseInt(min);
                                    m_answerMin *= 6;        // zmiana na kąt w stopniach
                                    // zmiana na zakres -180 +180
                                    if (m_answerMin > 180) {
                                        m_answerMin -= 360;
                                    }

                                    LogUtils.d(TAG, "hour: " + m_answerHour);
                                    LogUtils.d(TAG, "min: " + m_answerMin);

                                } catch (NumberFormatException e) {
                                    LogUtils.e(TAG, e);
                                }
                            }

                            // współrzedne srodka
                            m_xCenter = GetInteger(map, XML_DETAILS_TASK_SX);
                            m_yCenter = GetInteger(map, XML_DETAILS_TASK_SY);
                            m_yCenter = (m_yCenter * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                        } catch (XMLFileException e) {
                            m_answerHour = -1;
                            m_answerMin = -1;

                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER, e);
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
                            String name = null;
                            Bitmap bmp = null;
                            int xPlace = 0;
                            int yPlace = 0;
                            int align = 0;
                            int init = 0;

                            try {
                                // nazwa pola
                                name = GetString(map, XML_DETAILS_FIELD_NAME);

                                // położenie pola
                                xPlace = GetInteger(map, XML_DETAILS_FIELD_X);
                                yPlace = GetInteger(map, XML_DETAILS_FIELD_Y);
                                yPlace = (yPlace * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;

                                // wyrównanie
                                align = GetInteger(map, XML_DETAILS_FIELD_PX);

                                // położenie poczatkowe
                                init = GetInteger(map, XML_DETAILS_FIELD_PY);

                                // obrazek
                                String fileName = GetString(map, XML_DETAILS_FIELD_MASK);
                                VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                                LogUtils.d(TAG, "." + XML_DETAILS_FIELD_MASK + ": " + fileName);

                                if (bmpFile.exists()) {
                                    // INFO_RESIZE nie pomniejszamy plików bo sa małe
                                    bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                    m_height = (bmp.getHeight() * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                    m_width = bmp.getWidth();

                                } else {
                                    // nie ma obrazka
                                    bmp = null;
                                    valid = false;

                                    LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                                }

                                if (valid) {
                                    FieldSelect sel = new FieldSelect();
                                    sel.m_name = name;
                                    sel.m_xPlace = xPlace;
                                    sel.m_yPlace = yPlace;
                                    sel.m_mask = bmp;
                                    sel.m_srce = new Rect(-xPlace,
                                            -yPlace,
                                            m_width - xPlace,
                                            m_height - yPlace);
                                    sel.m_dest = new Rect(init, align, 0, 0);

                                    m_hand.add(sel);
                                }
                            } catch (XMLFileException e) {
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER, e);
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

        for (FieldSelect f : m_hand) {
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

        for (FieldSelect f : m_hand) {
            canvas.save();
            canvas.translate(m_xCenter, m_yCenter);
            canvas.rotate(f.m_dest.left);
            if (f.m_mask == null)
                LogUtils.e(TAG, "m_mask is null");
            else
                canvas.drawBitmap(f.m_mask, null, f.m_srce, m_paint);
            canvas.restore();
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
                    xPos = xPos - m_xCenter;
                    Float yPos = new Float(event.getY(last));
                    yPos = m_yCenter - yPos;
                    double angle = Math.atan2(xPos, yPos) * 180.0 / Math.PI;
                    double r = Math.sqrt(xPos * xPos + yPos * yPos);

                    for (FieldSelect f : m_hand) {
                        double x = -r * Math.sin((angle - f.m_dest.left) * Math.PI / 180.0);
                        double y = -r * Math.cos((angle - f.m_dest.left) * Math.PI / 180.0);

                        if ((x > f.m_srce.left) && (x < f.m_srce.right) && (y > f.m_srce.top) && (y < f.m_srce.bottom)) {
                            m_move = f;

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

                        x = x - m_xCenter;
                        y = m_yCenter - y;
                        Double angle = Math.atan2(x, y) * 180.0 / Math.PI;

                        if (m_pullFlag) {
                            angle += m_move.m_dest.top / 2;
                            angle /= m_move.m_dest.top;
                            m_move.m_dest.left = angle.intValue() * m_move.m_dest.top;
                        }

                        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {

                m_move = null;

                ArbiterAssess(m_hand, m_answerHour, m_answerMin);
                ArbiterAttempt();

                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                m_actHandler.sendEmptyMessage(TASK_MESS_MARK);
            }
            break;
        }

        return true;
    }

}



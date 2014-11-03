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
import android.os.Message;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsłgi zadania generujacego ezadana liczbę elementów w losowym połozeniu.
 * Zadanie pomocnicze.
 *
 *
 */
public class BurstTask extends LookTask {
    protected static final int BURST_HOR_RANGE = 1000;
    protected static final int BURST_HOR_OFFSET = 50;
    protected static final int BURST_VER_RANGE = 600;
    protected static final int BURST_VER_OFFSET = 50;
    private static final String TAG = BurstTask.class.toString();
    /**
     * wyswietlany obrazek
     */
    protected Bitmap m_item = null;
    /**
     * szerokośc obrazka
     */
    protected int m_width = 0;
    /**
     * wysokośc obrazka
     */
    protected int m_height = 0;
    /**
     * odstep pomiedzy wyswietlaniem kolejnych obrazkw w ms
     */
    protected int m_interval = 0;
    /**
     * lliczba powtórzeń wyswuiietlenia obrazka
     */
    protected int m_repeat = 0;
    protected int m_index = 0;
    protected Vector<FieldSelect> m_pos = null;
    Random m_rand = null;
    private Handler m_burstHandler = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public BurstTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_rand = new Random();

        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            // element do wyświetlenia
                            String fileName = GetString(map, XML_DETAILS_TASK_ITEM);
                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            LogUtils.d(TAG, "." + XML_DETAILS_TASK_ITEM + ": " + fileName);

                            BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                            valid = bitmapMaker.isValid();
                            m_width = bitmapMaker.getWidth();
                            m_height = bitmapMaker.getHeight();
                            m_item = bitmapMaker.getBitmap();

                            // liczba elementow do wyświetlenia
                            m_repeat = GetInteger(map, XML_DETAILS_TASK_NUMBER);

                            // odestep pomiędzy obrazkami
                            m_interval = GetInteger(map, XML_DETAILS_TASK_TIME);
                        } catch (XMLFileException e) {
                            m_item = null;
                            valid = false;

                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        if (valid) {
            if (m_repeat > 0) {
                m_pos = new Vector<FieldSelect>(m_repeat);
            }

            m_burstHandler = new BurstHandler();
            m_burstHandler.sendEmptyMessage(0);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        if (m_item != null) {
            m_item.recycle();
            m_item = null;
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

        if ((m_item != null)) {
            for (FieldSelect f : m_pos) {
                canvas.drawBitmap(m_item, null, f.m_srce, m_paint);
            }
        }
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
        m_index = 0;

        m_burstHandler.sendEmptyMessage(0);
    }


    /**
     * Klasa obsługi zdarzeń związanych z generowanie elementów na ekranie
     *
     *
     */
    private class BurstHandler extends Handler {

        /**
         * Konstruktor
         */
        public BurstHandler() {

            super();
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message a_msg) {

            super.handleMessage(a_msg);

            if (m_index < m_repeat) {
                FieldSelect field = new FieldSelect();
                field.m_srce = new Rect(m_rand.nextInt(BURST_HOR_RANGE) + BURST_HOR_OFFSET - m_width / 2,
                        m_rand.nextInt(BURST_VER_RANGE) + BURST_VER_OFFSET - m_height / 2,
                        m_width, m_height);
                field.m_srce.right += field.m_srce.left;
                field.m_srce.bottom += field.m_srce.top;
                m_pos.add(field);

                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);

                ++m_index;

                m_burstHandler.sendEmptyMessageDelayed(0, m_interval);
            }
        }
    }

    ;

}


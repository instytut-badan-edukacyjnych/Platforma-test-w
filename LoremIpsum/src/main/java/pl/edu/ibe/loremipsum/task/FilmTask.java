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
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Vector;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsługi zadania wyświetlającego sekwencję obrazków w określonych
 * odstępach czasu.
 * Zadanie pomocnicze.
 *
 *
 */
public class FilmTask extends LookTask {
    private static final String TAG = FilmTask.class.toString();

    /**
     * bitmapa wyswietlana co jakis czas na ekranie
     */
    protected int m_timeOn = 0;
    protected int m_timeOff = 0;
    protected Vector<FieldSelect> m_film = null;

    private Handler m_animateHandler = null;
    private int m_filmIndex = 0;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public FilmTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        m_film = new Vector<FieldSelect>(4);

        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            // czas właczenia
                            m_timeOn = GetInteger(map, XML_DETAILS_TASK_TIME_ON);

                            // czas wyłaczenia
                            m_timeOff = GetInteger(map, XML_DETAILS_TASK_TIME_OFF);
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER);
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
                            Bitmap bitmap = null;

                            try {
                                // nazwa pola
                                name = GetString(map, XML_DETAILS_FIELD_NAME);

                                // obrazek
                                String fileName = GetString(map, XML_DETAILS_FIELD_MASK);
                                VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                                LogUtils.d(TAG, "." + XML_DETAILS_FIELD_MASK + ": " + fileName);

                                if (bmpFile.exists()) {
                                    // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                    Bitmap temp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                    Matrix mat = new Matrix();
                                    mat.setScale(0.5f, 0.5f);
                                    bitmap = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), mat, true);
                                    // INFO_RESIZE <else>
//									bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                    // INFO_RESIZE <end>

                                } else {
                                    // nie ma obrazka
                                    bitmap = null;
                                    valid = false;
                                    LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                                }
                                BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                                valid = bitmapMaker.isValid();
                                bitmap = bitmapMaker.getBitmap();

                                if (valid) {
                                    FieldSelect sel = new FieldSelect();
                                    sel.m_name = name;
                                    sel.m_mask = bitmap;

                                    sel.m_selected = false;

                                    m_film.add(sel);
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

        m_filmIndex = 0;

        m_animateHandler = new AnimateHandler();

        return valid;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        for (FieldSelect f : m_film) {
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

        for (FieldSelect f : m_film) {
            if (f.m_selected) {
                if (f.m_mask == null)
                    LogUtils.e(TAG, "m_mask is null");
                else
                    canvas.drawBitmap(f.m_mask, null, new Rect(0, 0, 1130, 752), m_paint);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#PlayCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayCommand(MediaPlayer a_player) throws IOException {

        super.PlayCommand(a_player);

        for (FieldSelect f : m_film) {
            f.m_selected = false;
        }
        m_animateHandler.sendEmptyMessageDelayed(0, m_timeOn);

        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#RepeatCommand(android.media.MediaPlayer)
     */
    @Override
    public void RepeatCommand(MediaPlayer a_player) throws IOException {

        super.RepeatCommand(a_player);

        ArbiterRepeatCommand();

        for (FieldSelect f : m_film) {
            f.m_selected = false;
        }
        m_animateHandler.sendEmptyMessageDelayed(0, m_timeOn);

        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
    }


    /**
     * Obsługa komunikatów sterujacych animacja
     *
     * Statanszek
     */
    private class AnimateHandler extends Handler {

        /**
         * KOnstruktor
         */
        public AnimateHandler() {

            super();
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message a_msg) {

            super.handleMessage(a_msg);

            if (m_filmIndex < m_film.size()) {
                for (FieldSelect f : m_film) {
                    f.m_selected = false;
                }
                m_film.get(m_filmIndex).m_selected = true;

                ++m_filmIndex;
                m_animateHandler.sendEmptyMessageDelayed(1, m_timeOff);

                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
            }
        }
    }

}



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
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsługi zadania wyświetlającego obrazki w określonych odstępach
 * czasu przez czas odtwarzania dźwięku.
 * Brak pliku w opisie nie powoduje uznania zadania za niewłasciwe ponieważ
 * z tek klasy dziedzicza inne klasy zadań.
 * Zadanie pomocnicze.
 *
 * Class handles task based on displaying images in specified time interval during playback of sound
 *
 * Lack of file in description doesn't disqualifies this task because it  inherits from other classes
 *
 *Support task
 *
 *
 */
public class AnimateTask extends LookTask {
    private static final String TAG = AnimateTask.class.toString();


    /**
     * bitmapa wyswietlana co jakis czas na ekranie
     * bitmap displayed in time interval on screen
     */
    protected Bitmap m_film = null;
    /**
     * czas w ms na jaki pojawia się m_film
     * time in ms that film is faded  in.
     */
    protected int m_timeOn = 0;
    /**
     * czas w ms na jaki znika m_film
     * time in ms that film is faded  out.
     */
    protected int m_timeOff = 0;

    /**
     * flaga włączenia pojawianai się na ekranie m_film
     * falg enables m_film
     */
    protected boolean m_filmEnabled = false;
    /**
     * flaga pojawienei się m_fim na ekranie
     * m_film is displayed
     */
    protected boolean m_filmState = false;
    /**
     * uchwyt do obsługi komuniaktów sterujacyc hwyświetlaniem m_film
     * communication handler
     */
    private Handler m_animateHandler = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public AnimateTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);

        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            // plansza animacji
                            String fileName = GetString(map, XML_DETAILS_TASK_FILM);
                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            LogUtils.d(TAG, "." + XML_DETAILS_TASK_FILM + ": " + fileName);

                            BitmapMaker bitmapMaker = makeScaledBy2Bitmap(bmpFile);
                            valid = bitmapMaker.isValid();
                            m_film = bitmapMaker.getBitmap();


                            // czas włączenia animacji
                            m_timeOn = GetInteger(map, XML_DETAILS_TASK_TIME_ON);

                            // czas wyłączenia animacji
                            m_timeOff = GetInteger(map, XML_DETAILS_TASK_TIME_OFF);
                        } catch (XMLFileException e) {
                            // nie ma animacji
                            m_film = null;
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        m_filmEnabled = false;
        m_filmState = false;

        if (m_film != null) {
            m_animateHandler = new AnimateHandler();
        } else {
            m_animateHandler = null;
        }

        return valid;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#Destory()
     */
    @Override
    public void Destory() throws IOException {

        if (m_film != null) {
            m_film.recycle();
            m_film = null;
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

        if (m_filmState) {
            canvas.drawBitmap(m_film, null, new Rect(0, 0, VIEW_TASK_VIEW_WIDTH, VIEW_TASK_VIEW_HEIGHT), m_paint);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#PlayCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayCommand(MediaPlayer a_player) throws IOException {

        super.PlayCommand(a_player);

        if (m_film != null) {
            m_filmState = true;
            m_filmEnabled = true;
            m_animateHandler.sendEmptyMessageDelayed(0, m_timeOn);

            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.LookTask#RepeatCommand(android.media.MediaPlayer)
     */
    @Override
    public void RepeatCommand(MediaPlayer a_player) throws IOException {

        super.RepeatCommand(a_player);

        ArbiterRepeatCommand();

        if (m_film != null) {
            m_filmState = true;
            m_filmEnabled = true;
            m_animateHandler.sendEmptyMessageDelayed(0, m_timeOn);

            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SoundFinish()
     */
    @Override
    public void SoundFinish() {

        ArbiterCommandFinish();

        m_filmEnabled = false;
        m_filmState = false;

        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
    }


    /**
     * Klasa obsługi zdarzeń związanych z animacja
     *
     *
     */
    private class AnimateHandler extends Handler {

        /**
         * Constructor
         */
        public AnimateHandler() {

            super();
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);

            if (m_filmEnabled) {
                if (m_filmState) {
                    m_filmState = false;
                    m_animateHandler.sendEmptyMessageDelayed(0, m_timeOff);
                } else {
                    m_filmState = true;
                    m_animateHandler.sendEmptyMessageDelayed(0, m_timeOn);
                }

                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
            }
        }
    }

}



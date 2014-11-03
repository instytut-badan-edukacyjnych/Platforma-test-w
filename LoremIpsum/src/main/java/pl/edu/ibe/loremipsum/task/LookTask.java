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
import android.media.MediaPlayer;
import android.os.Handler;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa Obsługi zadania wyswietlające obraz podstawowy
 *
 *
 */
public class LookTask extends BaseTask {
    private static final String TAG = LookTask.class.toString();

    protected Bitmap m_main = null;
    protected String m_sound = null;
    protected String m_extra = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public LookTask(Context context) {
        super(context);
    }


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);
        int level = 0;
        int level1 = 0;

        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            String fileName = GetString(map, XML_DETAILS_TASK_MAIN);
                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // ale głownej planszy nie pomniejszamy
                                m_main = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                m_main = null;
                                valid = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }

                            // polecenie
                            m_sound = getString(map, XML_DETAILS_TASK_SOUND, null);
                            if (m_sound != null) {
                                VirtualFile file = a_info.getDirectory().getChildFile(m_sound);
                                if (!file.exists()) {
                                    m_sound = null;
                                    valid = false;

                                    LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                                }
                                file = null;
                            }

                            // dodatkowe polecenie
                            m_extra = getString(map, XML_DETAILS_TASK_EXTRA, "");
                            VirtualFile file = a_info.getDirectory().getChildFile(m_extra);
                            if (!file.exists()) {
                                m_extra = null;
                            }
                            file = null;

                            // próg przy pomiarach czasu
                            level = GetInteger(map, XML_DETAILS_TASK_LEVEL, 0);

                            // drugi prog przy pomiarach czasu
                            level1 = GetInteger(map, XML_DETAILS_TASK_LEVEL_1, 0);
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
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
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Destory()
     */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        if (m_main != null) {
            m_main.recycle();
            m_main = null;
        }

        super.Destory();
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#SizeChanged(int, int, int, int)
     */
    @Override
    public void SizeChanged(int w, int h, int oldw, int oldh) {

        super.SizeChanged(w, h, oldw, oldh);

        if ((m_main != null) && (m_paint != null)) {
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            Canvas can = new Canvas(bmp);
            can.drawBitmap(m_main, null, can.getClipBounds(), m_paint);
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#Draw(android.graphics.Canvas)
     */
    @Override
    public void Draw(Canvas canvas) {

        super.Draw(canvas);

        canvas.drawColor(m_bgColor);

        if (m_main != null) {
            canvas.drawBitmap(m_main, null, new Rect(0, 0, 1130, 752), m_paint);
        }
    }

    private void playSound(String name, MediaPlayer player) throws IOException {
        if (m_sound != null) {
            VirtualFile file = m_taskInfo.getDirectory().getChildFile(name);
            File cacheFile = new File(getTaskTempDir() + "/sound_" + name);
            CopyStream.copyStream(file.getInputStream(), new FileOutputStream(cacheFile)).execute().subscribe();
            LogUtils.v(TAG, "Writing to temp file: " + cacheFile.getAbsolutePath());
            LogUtils.v(TAG, "From " + file);

            try {
                FileInputStream fis = new FileInputStream(cacheFile);
                player.reset();
                player.setDataSource(fis.getFD());
                player.prepare();
                player.start();
            } catch (IOException e) {
                LogUtils.e(TAG, e);
            }
        } else {
            player.stop();
        }
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#PlayCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayCommand(MediaPlayer a_player) throws IOException {
        playSound(m_sound, a_player);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#PlayExtraCommand(android.media.MediaPlayer)
     */
    @Override
    public void PlayExtraCommand(MediaPlayer a_player) throws IOException {
        playSound(m_extra, a_player);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.BaseTask#RepeatCommand(android.media.MediaPlayer)
     */
    @Override
    public void RepeatCommand(MediaPlayer a_player) throws IOException {
        ArbiterRepeatCommand();
        playSound(m_sound, a_player);
    }
}

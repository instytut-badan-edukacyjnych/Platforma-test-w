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
import android.os.Handler;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa obsługi zadania wyświetlającego plansze przez określony czas.
 * Zadanie pomocnicze.
 *
 *
 */
public class TimerTask extends LookTask {
    private static final String TAG = TimerTask.class.toString();

    protected int m_pause;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public TimerTask(Context context) {
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
                        Node node = map.getNamedItem(XML_DETAILS_TASK_TIME);
                        if (node != null) {
                            try {
                                m_pause = Integer.parseInt(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_TIME);
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        m_actHandler.removeMessages(TASK_MESS_NEXT_TASK);
        m_actHandler.sendEmptyMessageDelayed(TASK_MESS_NEXT_TASK, m_pause * 1000);

        return valid;
    }

}

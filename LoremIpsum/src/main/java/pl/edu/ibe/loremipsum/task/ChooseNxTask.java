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
import android.view.MotionEvent;

import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa obsługi zadania pojegajacym na wybraniu kilku elementów.
 * Kazdy z elementów zaznaczony jest na własnej masce.
 *
 *
 */
public class ChooseNxTask extends Choose1xTask {
    private static final String TAG = ChooseNxTask.class.toString();

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public ChooseNxTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.Choose1xTask#TouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean TouchEvent(MotionEvent event) {

        ScreenTouched();

        boolean retValue = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int last = event.getPointerCount() - 1;
                if (last >= 0) {
                    Float xPos = new Float(event.getX(last));
                    Float yPos = new Float(event.getY(last));
                    int x = xPos.intValue();
                    int y = yPos.intValue();

                    for (FieldSelect f : m_field) {
                        if (f.m_mask == null)
                            LogUtils.e(TAG, "m_mask is null");
                        else if ((f.m_mask.getPixel(x / 2, y / 2) & COLOR_ALPHA_MASK) != 0) {
                            f.m_selected = !f.m_selected;
                            retValue = true;

                            if (m_property.mark02Flag) {
                                ArbiterAssess(m_field, m_answer, m_threshold);
                            } else {
                                ArbiterAssess(m_field, m_answer);
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

        return retValue;
    }

}



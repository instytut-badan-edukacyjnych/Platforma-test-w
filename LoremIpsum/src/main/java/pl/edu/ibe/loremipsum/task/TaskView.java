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
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

/**
 * Klasa obsługi wyswietlanai zadania
 */
public final class TaskView extends View {

//	private static final String TAG = "TaskInfo";


    private BaseTask m_task = null;


    /**
     * Konstruktor
     *
     * @param a_context - kontekt wyswietlania
     */
    public TaskView(Context a_context) {

        super(a_context);

        setHapticFeedbackEnabled(true);
        setClickable(true);
    }


    /**
     * Przzyporzadkowuje zadanie do wyswietlenia
     *
     * @param a_task - wyswietlane zadanie
     */
    public void Assign(BaseTask a_task) {

        m_task = a_task;

    }

    /**
     * Zwarca uchwyt do wyswietlanego zdania
     *
     * @return uchwyt do wyswietlanego zadania
     */
    public BaseTask GetTask() {

        return m_task;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        if (m_task != null) {

            m_task.SizeChanged(w, h, oldw, oldh);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (m_task != null) {

            m_task.Draw(canvas);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (m_task != null) {
            if (m_task.TouchEvent(event)) {
                invalidate();
            }
        }

        return true;
    }

    /**
     * Zwalania zaspby przydzielone do zadania
     */
    public void Release() throws IOException {

        if (m_task != null) {
            m_task.Destory();
            m_task = null;

            System.runFinalization();
            System.gc();
        }
    }

}



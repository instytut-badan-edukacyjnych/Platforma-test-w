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

package pl.edu.ibe.loremipsum.tablet;


import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.TaskView;


/**
 * Bazowa klasa przełaczania zadań
 *
 *
 */
public class TaskChangeBase {
    private static final String TAG = "TaskChangeBase";


    /**
     * uchwyt do aktywnośc, z której został owywołane
     */
    protected TaskAct m_parent = null;

    /**
     * uchwyt do pierwszego planu wyświetlania
     */
    protected TaskView m_foreView = null;
    /**
     * uchwyt do drugiego planu wyświetlania
     */
    protected TaskView m_backView = null;

    /**
     * interpolator zwiekszający
     */
    protected Interpolator m_accelerator = new AccelerateInterpolator();
    /**
     * interpolator zmniejszający
     */
    protected Interpolator m_decelerator = new DecelerateInterpolator();


    /**
     * Funkcja zmieniajace pierwszy i drugi plan
     *
     * @param a_parent - aktywnośc wywołujaca
     * @param a_fore   - pierwszy plan
     * @param a_back   - drugi plan
     */
    public void Change(TaskAct a_parent, final TaskView a_fore, final TaskView a_back) {

        m_parent = a_parent;
        m_foreView = a_fore;
        m_backView = a_back;
    }

}



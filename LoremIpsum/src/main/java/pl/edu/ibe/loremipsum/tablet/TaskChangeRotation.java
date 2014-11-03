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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import java.io.IOException;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.TaskView;
import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa przełaczania zadań przez obrot (3D)
 *
 *
 */
public final class TaskChangeRotation extends TaskChangeBase {
    private static final String TAG = "TaskChangeRotation";


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.tablet.TaskChangeBase#Change(pl.edu.ibe.loremipsum.tablet.task.TaskAct, pl.edu.ibe.loremipsum.task.TaskView, pl.edu.ibe.loremipsum.task.TaskView)
     */
    @Override
    public void Change(TaskAct a_parent, final TaskView a_fore, final TaskView a_back) {

        super.Change(a_parent, a_fore, a_back);

        final ObjectAnimator foreToback = ObjectAnimator.ofFloat(m_foreView, "rotationX", 0f, 90f);
        foreToback.setDuration(500);
        foreToback.setInterpolator(m_accelerator);
        final ObjectAnimator backTofore = ObjectAnimator.ofFloat(m_backView, "rotationX", -90f, 0f);
        backTofore.setDuration(800);
        backTofore.setInterpolator(m_decelerator);

        foreToback.addListener(new AnimatorListenerAdapter() {

            /*
             * (non-Javadoc)
             * @see android.animation.AnimatorListenerAdapter#onAnimationEnd(android.animation.Animator)
             */
            @Override
            public void onAnimationEnd(Animator anim) {

                m_foreView.setVisibility(View.GONE);
                m_foreView.setRotationX(0.0f);

                backTofore.start();

                m_backView.setVisibility(View.VISIBLE);
            }
        });

        backTofore.addListener(new AnimatorListenerAdapter() {

            /*
             * (non-Javadoc)
             * @see android.animation.AnimatorListenerAdapter#onAnimationEnd(android.animation.Animator)
             */
            @Override
            public void onAnimationEnd(Animator anim) {
                try {
                    TaskView fake = m_foreView;
                    m_foreView = m_backView;
                    m_backView = fake;

                    m_parent.DeleteTask();
                    m_parent.StartTask();
                } catch (IOException e) {
                    LogUtils.e(TAG, "Exception during onAnimationEnd", e);
                }
            }
        });

        foreToback.start();
    }

}



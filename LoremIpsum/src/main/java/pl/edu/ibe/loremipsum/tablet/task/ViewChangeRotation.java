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

package pl.edu.ibe.loremipsum.tablet.task;

import android.animation.TimeInterpolator;
import android.view.View;

/**
 * Klasa zmiany zadania przez obrot ekranu
 *
 *
 */
public final class ViewChangeRotation extends ViewChangeBase {

    private TaskAct taskAct;

    public ViewChangeRotation(OnAnimationEnd onAnimationEnd) {
        super(onAnimationEnd);
    }


    /*
             * (non-Javadoc)
             * @see pl.edu.ibe.loremipsum.tablet.task.TaskAct.ViewChangeBase#Change()
             */
    @Override
    public void Change(final View fore, final View back, TimeInterpolator m_accelerator, TimeInterpolator m_decelerator) {

//
//        final ObjectAnimator foreToback = ObjectAnimator.ofFloat(fore, "rotationX", 0f, 90f);
//        foreToback.setDuration(500);
//        foreToback.setInterpolator(taskAct.m_accelerator);
//        final ObjectAnimator backTofore = ObjectAnimator.ofFloat(back, "rotationX", -90f, 0f);
//        backTofore.setDuration(500);
//        backTofore.setInterpolator(taskAct.m_decelerator);
//
//        foreToback.addListener(new AnimatorListenerAdapter() {
//
//            /*
//             * (non-Javadoc)
//             * @see android.animation.AnimatorListenerAdapter#onAnimationEnd(android.animation.Animator)
//             */
//            @Override
//            public void onAnimationEnd(Animator anim) {
//                taskAct.m_foreView.setVisibility(View.GONE);
//                taskAct.m_foreView.setRotationX(0.0f);
//                backTofore.start();
//                taskAct.m_backView.setVisibility(View.VISIBLE);
//            }
//        });
//
//        backTofore.addListener(new AnimatorListenerAdapter() {
//
//            /*
//             * (non-Javadoc)
//             * @see android.animation.AnimatorListenerAdapter#onAnimationEnd(android.animation.Animator)
//             */
//            @Override
//            public void onAnimationEnd(Animator anim) {
//                TaskView fake = taskAct.m_foreView;
//                taskAct.m_foreView = taskAct.m_backView;
//                taskAct.m_backView = fake;
//
//                taskAct.m_backView.Release();
//
//                taskAct.DeleteTask();
//                taskAct.StartTask();
//            }
//        });
//
//        foreToback.start();
    }
}

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;

import java.io.IOException;

import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskView;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Klasa zmiany zadania przez zaciemnienie
 *
 *
 */
public class ViewChangeOpaque extends ViewChangeBase {
    private static final String TAG = ViewChangeOpaque.class.toString();

    public ViewChangeOpaque(OnAnimationEnd onAnimationEnd) {
        super(onAnimationEnd);
    }

    /*
                 * (non-Javadoc)
                 * @see pl.edu.ibe.loremipsum.tablet.task.TaskAct.ViewChangeBase#Change()
                 */
    @Override
    public void Change(final View fore, final View back, TimeInterpolator m_accelerator, TimeInterpolator m_decelerator) {
        BaseTask foregroundTask = null;
        BaseTask backgroundTask = null;
        if (fore != null && fore instanceof TaskView)
            foregroundTask = ((TaskView) fore).GetTask();
        if (back != null && back instanceof TaskView)
            backgroundTask = ((TaskView) back).GetTask();

        LogUtils.v(TAG, "ViewChangeOpaque.Change from " + (foregroundTask != null ? foregroundTask.GetName() : "?")
                + " to " + (backgroundTask != null ? backgroundTask.GetName() : "?"));

        final ObjectAnimator foreToback = ObjectAnimator.ofFloat(fore, "alpha", 1.0f, 0.0f);
        foreToback.setDuration(500);
        foreToback.setInterpolator(m_accelerator);
        final ObjectAnimator backTofore = ObjectAnimator.ofFloat(back, "alpha", 0.1f, 1.0f);
        backTofore.setDuration(800);
        backTofore.setInterpolator(m_decelerator);

        back.setAlpha(0.1f);

        foreToback.addListener(new AnimatorListenerAdapter() {
            /*
             * (non-Javadoc)
             * @see android.animation.AnimatorListenerAdapter#onAnimationEnd(android.animation.Animator)
             */
            @Override
            public void onAnimationEnd(Animator anim) {
                LogUtils.w(TAG, "---------->>>> (1) ANIMATION ENDED <<<<----------");
                fore.setVisibility(View.GONE);
                fore.setAlpha(1.0f);

                backTofore.start();

                back.setVisibility(View.VISIBLE);
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
                    LogUtils.w(TAG, "---------->>>> (2) ANIMATION ENDED <<<<----------");
                    onAnimationEnd.animationEnded();
                } catch (IOException e) {
                    LogUtils.e(TAG, "Exception during onAnimationEnd", e);
                }
            }
        });

        foreToback.start();
    }
}

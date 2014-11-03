/************************************
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
 ************************************/

package pl.edu.ibe.loremipsum.arbiter.tpr.cordova;

import java.util.ArrayList;
import java.util.Collections;

import pl.edu.ibe.loremipsum.arbiter.tpr.Tpr1Arbiter;
import pl.edu.ibe.loremipsum.task.tpr.cordova.grid.GridTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 07.08.14.
 */
public class GridArbiter extends Tpr1Arbiter {

    private static final String TAG = GridArbiter.class.getSimpleName();
    ArrayList<Double> results;
    private double summaryUserMistake;

    public void setData(ArrayList<GridTask.GridTaskResult> data) {

        results = new ArrayList<>();
        ArrayList<Long> times = new ArrayList<>();
        ArrayList<Integer> conf = new ArrayList<>();
        long overallTime = 0;
        summaryUserMistake = 0;
        for (GridTask.GridTaskResult gridTaskResult : data) {
            area = gridTaskResult.area;
            results.addAll(gridTaskResult.results);

            for (Double result : gridTaskResult.results) {
                maxCorrectAnswersCount++;
                if (result == 0.0) {
                    userCorrectAnswers++;
                }
                summaryUserMistake += result;
            }


            times.add(gridTaskResult.time);
            conf.add(gridTaskResult.numOfItems);
            overallTime += gridTaskResult.time;
        }

        ArrayList<Integer> sortedConf = new ArrayList<>(conf);
        Collections.sort(sortedConf);


        answerString = makeTaskColumns(7, sortedConf, "", false) + makeTaskColumns(7, new ArrayList<>(conf), "_k", false) + "tc_zad7;" + "w_zad7;" +
                "\n" + makeResultRowTPR1(7, results, times, conf, false, overallTime, results.size() == 28);
        LogUtils.d(TAG, answerString);
    }

    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        if (results == null || results.size() < 28) {
            return -7;
        } else {
            return 1.0 - summaryUserMistake / 280.0;
        }
    }

}

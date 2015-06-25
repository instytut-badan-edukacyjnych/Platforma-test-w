/**
 * *********************************
 * This file is part of Test Platform.
 * <p>
 * Test Platform is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * Test Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Test Platform; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * <p>
 * Ten plik jest częścią Platformy Testów.
 * <p>
 * Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 * <p>
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 * <p>
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 * **********************************
 */


package pl.edu.ibe.loremipsum.arbiter.tpr.cordova;

import java.util.ArrayList;
import java.util.Collections;

import pl.edu.ibe.loremipsum.arbiter.tpr.Tpr1Arbiter;
import pl.edu.ibe.loremipsum.task.tpr.cordova.fourfields.FourFieldsBoardTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Created by adam on 04.08.14.
 */
public class FourFieldsBoardArbiter extends Tpr1Arbiter {
    private static final String TAG = FourFieldsBoardArbiter.class.getSimpleName();
    private ArrayList<FourFieldsBoardTask.CurrentItemHandler> data;
    private static final int NUMBER_OF_TASKS = 49;

    public void setData(ArrayList<FourFieldsBoardTask.CurrentItemHandler> data) {
        this.data = data;
        ArrayList<Integer> conf = new ArrayList<>();
        ArrayList<Double> result = new ArrayList<>();
        ArrayList<Long> times = new ArrayList<>();
        long overallTime = 0;
        //first element is ignored
//        if (data.size() > 0) {
//            data.remove(0);
//        }

        for (int i1 = 0; i1 < data.size(); i1++) {
            FourFieldsBoardTask.CurrentItemHandler currentItemHandler = data.get(i1);
            area = currentItemHandler.area;
            if (currentItemHandler.userAnswer == currentItemHandler.isCorrect) {
                result.add(1d);
                userCorrectAnswers++;
            } else {
                result.add(0d);
            }
            times.add(currentItemHandler.clickTime - currentItemHandler.displayStartTime);
            overallTime += currentItemHandler.endDisplayTime - currentItemHandler.displayStartTime;
        }


        for (int i = 0; i < data.size(); i++) {
            if (data.size() > i) {
                conf.add(10 * data.get(i).configItem + (data.get(i).isCorrect ? 0 : 1));
            } else {
                conf.add(10 * data.get(i).configItem);
            }
        }

        maxCorrectAnswersCount = data.size();

        ArrayList<Integer> sortedConf = new ArrayList<>(conf);
        Collections.sort(sortedConf);


        answerString = makeTaskColumns(6, sortedConf, "", true) + makeTaskColumns(6, new ArrayList<>(conf), "_k", true) + "tc_zad6;" + "w_zad6;" + "\n"
                + makeResultRowTPR1(6, result, times, conf, true, overallTime, data.size() == NUMBER_OF_TASKS);
        LogUtils.d(TAG, answerString);

    }


    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        if (data == null || data.size() != NUMBER_OF_TASKS) {
            return -7;
        }
        return (userCorrectAnswers * 1.0) / (maxCorrectAnswersCount * 1.0);
    }


}

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
import pl.edu.ibe.loremipsum.task.tpr.cordova.counting.CountingTaskGroup;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Created by adam on 28.07.14.
 */
public class CountingArbiter extends Tpr1Arbiter {

    private static final String TAG = CountingArbiter.class.getSimpleName();
    ArrayList<Integer> answers;
    private ArrayList<CountingTaskGroup.SummaryTaskResults> summary;

    public void setSummary(ArrayList<CountingTaskGroup.SummaryTaskResults> summary, boolean taskIsCompleted, ArrayList<Integer> conf, String area) {
        this.summary = summary;
        this.area = area;
        answers = new ArrayList<>();
        ArrayList<Long> times = new ArrayList<>();
        ArrayList<Integer> correctBallsCount = new ArrayList<>();
        long overallTime = 0;
        for (CountingTaskGroup.SummaryTaskResults summaryTaskResults : summary) {
            if (!summaryTaskResults.isTutorial) {
                overallTime += summaryTaskResults.overallTime;
                for (CountingTaskGroup.CountingTaskResults itemResult : summaryTaskResults.resultses) {
                    answers.add(itemResult.answer);
                    correctBallsCount.add(itemResult.correctBallsCount);
                }
                times.add(summaryTaskResults.overallTime);
            }
        }

        ArrayList<Double> result = new ArrayList<>();

        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i) == correctBallsCount.get(i)) {
                result.add(1d);
                userCorrectAnswers++;
            } else {
                result.add(0d);
            }
        }

        maxCorrectAnswersCount = answers.size();

        ArrayList<Integer> sortedConf = new ArrayList<>(conf);
        Collections.sort(sortedConf);


        answerString = makeTaskColumns(1, sortedConf, "", false) + makeTaskColumns(1, new ArrayList<>(conf), "_k", false) + "tc_zad1;" + "w_zad1;" + "\n"
                + makeResultRowTPR1(1, result, times, conf, false, overallTime, answers.size() == 14);
        LogUtils.d(TAG, answerString);
    }


    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        if (answers == null || answers.size() != 14) {
            return -7;
        }
        return (userCorrectAnswers * 1.0) / (maxCorrectAnswersCount * 1.0);
    }

}

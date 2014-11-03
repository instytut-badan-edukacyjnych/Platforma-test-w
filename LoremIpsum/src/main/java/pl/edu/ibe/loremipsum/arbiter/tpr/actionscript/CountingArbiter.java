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

package pl.edu.ibe.loremipsum.arbiter.tpr.actionscript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import pl.edu.ibe.loremipsum.arbiter.tpr.ArbiterUtils;
import pl.edu.ibe.loremipsum.arbiter.tpr.Tpr2Arbiter;
import pl.edu.ibe.loremipsum.task.tpr.actionscript.counting.CountingTaskGroup;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;

/**
 * Created by adam on 28.07.14.
 */
public class CountingArbiter extends Tpr2Arbiter {

    private static final String TAG = CountingArbiter.class.getSimpleName();

    private HashMap<String, Integer> numberOfBoardsCounter;

    private Tuple.Two<String, String>[] targetArray;

    public CountingArbiter() {
        numberOfBoardsCounter = new HashMap<>();
        targetArray = new Tuple.Two[200];
    }

    private boolean isTutorialSuccessfull(ArrayList<CountingTaskGroup.SummaryTaskResults> countingTaskResultses) {
        double taskNumber = 0;
        double wrongCount = 0;

        for (CountingTaskGroup.SummaryTaskResults countingTaskResultse : countingTaskResultses) {
            if (countingTaskResultse.isTutorial) {
                for (CountingTaskGroup.CountingTaskResults resultse : countingTaskResultse.resultses) {
                    if (resultse.answer != resultse.correctBallsCount) {
                        wrongCount++;
                    }
                    taskNumber++;
                }
            }
        }
        LogUtils.d(TAG, "Tutorial result = " + (taskNumber - wrongCount) / taskNumber + " repeat tutorial? " + (((taskNumber - wrongCount) / taskNumber > 0.80) ? false : true));

        return (taskNumber - wrongCount) / taskNumber > 0.80 ? true : false;
    }

    public double setSummary(ArrayList<CountingTaskGroup.SummaryTaskResults> summary, boolean taskIsCompleted, ArrayList<Integer> conf, boolean tutorialWasSuccesfull, boolean isAbstract, long totalTime) {


        targetArray[1] = Tuple.Two.create("Z1_VERSION_" + getTypeLetter(isAbstract), isAbstract ? ABSTRACT : NORMAL);
        targetArray[4] = Tuple.Two.create("Z1_TEST_PASS_" + getTypeLetter(isAbstract), tutorialWasSuccesfull ? "1" : "0");


        CountingTaskGroup.SummaryTaskResults summaryTaskResults;
        int tutorialCount = 0;
        Tuple.Two two;
        ArrayList<CountingTaskGroup.SummaryTaskResults> additionalTutorial = new ArrayList<>();


        double correctnessCount = 0;
        for (int i = 0; i < summary.size(); i++) {
            summaryTaskResults = summary.get(i);
            if (summaryTaskResults.isTutorial) {
                targetArray[5 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), summaryTaskResults.isTutorial, isAbstract, false, true, false, -1, false), calculateCorrectness(summaryTaskResults.resultses) + "");
                targetArray[11 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), summaryTaskResults.isTutorial, isAbstract, false, false, false, -1, false), summaryTaskResults.overallTime + "");
                if (tutorialCount > 3) {
                    additionalTutorial.add(summaryTaskResults);
                }
                tutorialCount++;
            }
        }
        //Jeśli tutorial nie był przechodzony 2 razy to generujemy placeholder;
        if (additionalTutorial.size() > 0) {
            for (int i = 0; i < 3; i++) {
                targetArray[8 + i] = Tuple.Two.create(generateHeader(summary.get(i).resultses.size(), summary.get(i).isTutorial, isAbstract, false, true, false, -1, false), "7");
                targetArray[14 + i] = Tuple.Two.create(generateHeader(summary.get(i).resultses.size(), summary.get(i).isTutorial, isAbstract, false, false, false, -1, false), "7");
            }
        }
        targetArray[4] = Tuple.Two.create("Z1_TEST_PASS_" + getTypeLetter(isAbstract), isTutorialSuccessfull(additionalTutorial) ? "1" : "0");


        //Koniec demo;
//Usuwamy wpisy o demo i sortujemy

        for (Iterator<CountingTaskGroup.SummaryTaskResults> resultIterator = summary.iterator(); resultIterator.hasNext(); ) {
            if (resultIterator.next().isTutorial) {
                resultIterator.remove();
            }
        }

        ArrayList<CountingTaskGroup.SummaryTaskResults> sortedSummary = new ArrayList<>(summary);
        Collections.sort(sortedSummary, (lhs, rhs) -> {
            if (lhs.resultses.size() == rhs.resultses.size()) {
                return 0;
            }
            return lhs.resultses.size() - rhs.resultses.size();
        });

        for (int i = 0; i < sortedSummary.size(); i++) {
            summaryTaskResults = sortedSummary.get(i);
            if (!summaryTaskResults.isTutorial) {
                targetArray[17 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), summaryTaskResults.isTutorial, isAbstract, false, false, true, -1, false), calculateCorrectness(summaryTaskResults.resultses) + "");

            }
        }
        CountingTaskGroup.CountingTaskResults result;
        int summaryOffset = 0;
        for (int i = 0; i < sortedSummary.size(); i++) {
            summaryTaskResults = sortedSummary.get(i);
            for (int j = 0; j < summaryTaskResults.resultses.size(); j++) {
                result = summaryTaskResults.resultses.get(j);
                targetArray[23 + j + summaryOffset] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), false, isAbstract, false, true, false, j + 1, false), result.answer == result.correctBallsCount ? "1" : "0");
            }
            summaryOffset += summaryTaskResults.resultses.size();
            targetArray[50 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), false, isAbstract, false, false, false, -1, false), summaryTaskResults.overallTime + "");
        }
/////KOPIE!

        for (int i = 0; i < summary.size(); i++) {
            summaryTaskResults = summary.get(i);
            if (!summaryTaskResults.isTutorial) {
                targetArray[56 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), summaryTaskResults.isTutorial, isAbstract, true, false, true, -1, false), calculateCorrectness(summaryTaskResults.resultses) + "");

            }
        }
        summaryOffset = 0;
        for (int i = 0; i < summary.size(); i++) {
            summaryTaskResults = summary.get(i);
            for (int j = 0; j < summaryTaskResults.resultses.size(); j++) {
                result = summaryTaskResults.resultses.get(j);
                targetArray[62 + j + summaryOffset] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), false, isAbstract, true, true, false, j + 1, false), result.answer == result.correctBallsCount ? "1" : "0");
            }
            summaryOffset += summaryTaskResults.resultses.size();
            targetArray[89 + i] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), false, isAbstract, true, false, false, -1, false), summaryTaskResults.overallTime + "");
        }

//Czasy jednostkowe
        summaryOffset = 0;
        for (int i = 0; i < summary.size(); i++) {
            summaryTaskResults = summary.get(i);
            for (int j = 0; j < summaryTaskResults.resultses.size(); j++) {
                result = summaryTaskResults.resultses.get(j);
                targetArray[95 + j + summaryOffset] = Tuple.Two.create(generateHeader(summaryTaskResults.resultses.size(), false, isAbstract, true, false, false, j + 1, true), result.time + "");
            }
            summaryOffset += summaryTaskResults.resultses.size();
        }

        double mark = calculateSum(summary);


        double seriesSum = 0;
        double counter = 0;
        for (CountingTaskGroup.SummaryTaskResults taskResults : summary) {
            for (CountingTaskGroup.CountingTaskResults resultse : taskResults.resultses) {
                if (resultse.answer == resultse.correctBallsCount) {
                    counter++;
                }
                seriesSum += counter / taskResults.resultses.size();
                counter = 0;
            }
        }


        targetArray[0] = Tuple.Two.create("Z1_" + getTypeLetter(isAbstract), String.valueOf(seriesSum / 1)); //Task1.as DIVIDER. Nigdzie nie przyjmuje innej wartości niż 1;
        targetArray[2] = Tuple.Two.create("W_Z1_" + getTypeLetter(isAbstract), taskIsCompleted ? "1" : "0");
        targetArray[3] = Tuple.Two.create("TC_Z1_" + getTypeLetter(isAbstract), String.valueOf(totalTime));
//        Answers.ANSWERS_TASK_1_A[3] = Utils.formatTime(TOTAL_TIME_END);


//Generowanie ostatecznych stringów
        ArrayList<Tuple.Two> resultArray = ArbiterUtils.<Tuple.Two>trimArray(targetArray);
        StringBuilder headerString = new StringBuilder();
        StringBuilder valuesString = new StringBuilder();
        for (Tuple.Two<String, String> stringStringTwo : resultArray) {
            if (stringStringTwo == null || stringStringTwo.first == null || stringStringTwo.second == null) {
                headerString.append("null");
                headerString.append(SEPARATOR);
                valuesString.append("null");
                valuesString.append(SEPARATOR);
                continue;
            }
            headerString.append(stringStringTwo.first);
            headerString.append(SEPARATOR);
            valuesString.append(stringStringTwo.second);
            valuesString.append(SEPARATOR);
        }


        answerString = headerString + "\n" + valuesString;
        LogUtils.d(TAG, answerString);
//        W zadaniu #1 (kulki, zwierzęta):
//        - samouczek nie jest wliczany do wyniku
//                - prezentowane jest kilka serii po kilka plansz z kulkami
//        - po każdej serii badany udziela odpowiedzi ile było kulek (zwierząt) na poszczególnych planszach, za każdą pojedynczą odpowiedź na temat planszy dostaje ocenę 0 lub 1 w zależności od poprawności
//        - powyższe oceny są sumowane i dzielone przez liczbę plansz w danej serii („procent” poprawności danej serii)
//                - po wszystkich seriach, w/w wyniki serii są sumowane, dzielone przez ilość serii (uśredniane) i traktowane jako wynik badania
//                - finalny wynik jest z zakresu 0.0 (wszystko źle) .. 1.0 (wszystko dobrze)
        if (!taskIsCompleted) {
            return -7;
        }
        return mark;
    }

    private double calculateSum(ArrayList<CountingTaskGroup.SummaryTaskResults> summary) {
        double sum = 0;
        double partSum = 0;
        for (CountingTaskGroup.SummaryTaskResults summaryTaskResults : summary) {
            if (!summaryTaskResults.isTutorial) {
                for (CountingTaskGroup.CountingTaskResults resultse : summaryTaskResults.resultses) {
                    if (resultse.answer == resultse.correctBallsCount) {
                        partSum++;
                    }
                }
                sum += partSum / summaryTaskResults.resultses.size();
                partSum = 0;
            }
        }
        return sum / summary.size();
    }


    private double calculateCorrectness(ArrayList<CountingTaskGroup.CountingTaskResults> resultses) {
        double correctCount = 0;
        for (CountingTaskGroup.CountingTaskResults resultse : resultses) {
            if (resultse.answer == resultse.correctBallsCount) {
                correctCount++;
            }
        }
        return correctCount / resultses.size();
    }

    public String generateHeader(int elementsCount, boolean isTutorial, boolean isAbstractMode, boolean isCopy, boolean isResultHeader, boolean isCorrectness, int index, boolean isPartialTime) {
        StringBuilder taskIdForCounterUsage = new StringBuilder();

        StringBuilder builder = new StringBuilder();

        taskIdForCounterUsage.append(elementsCount);
        taskIdForCounterUsage.append(isTutorial);
        taskIdForCounterUsage.append(isAbstractMode);
        taskIdForCounterUsage.append(isCopy);
        taskIdForCounterUsage.append(isResultHeader);
        taskIdForCounterUsage.append(isCorrectness);
        taskIdForCounterUsage.append(index);
        taskIdForCounterUsage.append(isPartialTime);

        int boardCounter = 1;
        if (numberOfBoardsCounter.get(taskIdForCounterUsage.toString()) != null) {
            boardCounter = numberOfBoardsCounter.get(taskIdForCounterUsage.toString());
            boardCounter++;
        }
        numberOfBoardsCounter.put(taskIdForCounterUsage.toString(), boardCounter);

        int numberOfBoards = elementsCount;

        if (isTutorial) {
            //Z1_S21_TEST_N || Z1_T21_TEST_N
            //Summmary result Z1_S21_TEST_N
            if (isResultHeader) {
                builder.append("Z1_S");
                builder.append(numberOfBoards);
                builder.append(boardCounter);
                builder.append("_TEST_");
                builder.append(getTypeLetter(isAbstractMode));
            } else {
                //SummaryTime Z1_T21_TEST_N
                builder.append("Z1_T");
                builder.append(numberOfBoards);
                builder.append(boardCounter);
                builder.append("_TEST_");
                builder.append(getTypeLetter(isAbstractMode));
            }
        } else {
            //Z1_S21_N |||	Z1_S21_P1_N |||	Z1_S21_P2_N |||	Z1_T21_N |||	Z1_S21_K_N |||	Z1_T21_K_N |||	Z1_S21_P1T_K_N |||	Z1_S21_P2T_K_N
            if (!isCopy) {
                if (isCorrectness) {
                    //Correctness Z1_S21_N
                    builder.append("Z1_S");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_");
                    builder.append(getTypeLetter(isAbstractMode));
                } else if (isResultHeader) {
                    //BoardsResults Z1_S21_P1_N; Z1_S21_P2_N
                    builder.append("Z1_S");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_P");
                    builder.append(index);
                    builder.append("_");
                    builder.append(getTypeLetter(isAbstractMode));
                } else {
                    //Task time Z1_T21_N
                    builder.append("Z1_T");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_");
                    builder.append(getTypeLetter(isAbstractMode));
                }
            } else {
                if (isCorrectness) {
                    //Correctness repeated Z1_S21_K_N
                    builder.append("Z1_S");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_K_");
                    builder.append(getTypeLetter(isAbstractMode));
                } else if (isResultHeader) {
                    builder.append("Z1_S");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_P");
                    builder.append(index);
                    builder.append("T_K_");
                    builder.append(getTypeLetter(isAbstractMode));
                } else if (isPartialTime) {
                    //Boards time Z1_S21_P1T_K_N; Z1_S21_P2T_K_N
                    builder.append("Z1_S");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_P");
                    builder.append(index);
                    builder.append("T_K_");
                    builder.append(getTypeLetter(isAbstractMode));
                } else {
                    //Task time repeated Z1_T21_K_N
                    builder.append("Z1_T");
                    builder.append(numberOfBoards);
                    builder.append(boardCounter);
                    builder.append("_K_");
                    builder.append(getTypeLetter(isAbstractMode));
                }
            }
        }
        return builder.toString();
    }


    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        return (userCorrectAnswers * 1.0) / (maxCorrectAnswersCount * 1.0);
    }

}

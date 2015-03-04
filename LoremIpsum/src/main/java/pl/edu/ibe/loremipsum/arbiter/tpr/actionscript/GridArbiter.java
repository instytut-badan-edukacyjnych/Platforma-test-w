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

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import pl.edu.ibe.loremipsum.arbiter.tpr.ArbiterUtils;
import pl.edu.ibe.loremipsum.arbiter.tpr.Tpr2Arbiter;
import pl.edu.ibe.loremipsum.task.tpr.actionscript.grid.GridTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;

/**
 * Created by adam on 07.08.14.
 */
public class GridArbiter extends Tpr2Arbiter {

    private static final String TAG = GridArbiter.class.getSimpleName();
    private static final String NOT_IMPORTANT = "7";
    public static double ANSWERS_TASK_7_TOTAL_POINTS = 0;
    public static int ANSWERS_TASK7_IS_TUTORIAL_SUCCESSFUL = 4;
    public ArrayList<GridTaskConfigurationEntry> mapMatrix;

    private Tuple.Two<String, String>[] targetArray;

    public GridArbiter() {
        targetArray = new Tuple.Two[200];
        mapMatrix = new ArrayList<>();
        //From Task7Config.as file

        GridTaskConfigurationEntry entry;

        //{s:0,  	orderk:0,  	order:0}, 		//0
        entry = new GridTaskConfigurationEntry();
        entry.s = 0;
        entry.orderk = 0;
        entry.order = 0;
        mapMatrix.add(entry);
        //    {s:6,  	orderk:2, 	order:15},		//1
        entry = new GridTaskConfigurationEntry();
        entry.s = 6;
        entry.orderk = 2;
        entry.order = 15;
        mapMatrix.add(entry);
        //{s:3,  	orderk:6, 	order:6},		//2
        entry = new GridTaskConfigurationEntry();
        entry.s = 3;
        entry.orderk = 6;
        entry.order = 6;
        mapMatrix.add(entry);
//---------
        //{s:9, 	orderk:9, 	order:27},		//3
        entry = new GridTaskConfigurationEntry();
        entry.s = 9;
        entry.orderk = 9;
        entry.order = 27;
        mapMatrix.add(entry);
//        {s:4, 	orderk:14, 	order:9},		//4
        entry = new GridTaskConfigurationEntry();
        entry.s = 4;
        entry.orderk = 14;
        entry.order = 9;
        mapMatrix.add(entry);
//        {s:1,  	orderk:17, 	order:2},		//5
        entry = new GridTaskConfigurationEntry();
        entry.s = 1;
        entry.orderk = 17;
        entry.order = 2;
        mapMatrix.add(entry);
        //-----
//        {s:5, 	orderk:19, 	order:12},		//6
        entry = new GridTaskConfigurationEntry();
        entry.s = 5;
        entry.orderk = 19;
        entry.order = 12;
        mapMatrix.add(entry);
//        {s:10, 	orderk:22, 	order:32},		//7
        entry = new GridTaskConfigurationEntry();
        entry.s = 10;
        entry.orderk = 22;
        entry.order = 32;
        mapMatrix.add(entry);
//        {s:7, 	orderk:27, 	order:19},		//8
        entry = new GridTaskConfigurationEntry();
        entry.s = 7;
        entry.orderk = 27;
        entry.order = 19;
        mapMatrix.add(entry);
//----
//        {s:2,  	orderk:31, 	order:4},		//9
        entry = new GridTaskConfigurationEntry();
        entry.s = 2;
        entry.orderk = 31;
        entry.order = 4;
        mapMatrix.add(entry);
//        {s:8, 	orderk:33, 	order:23},		//10
        entry = new GridTaskConfigurationEntry();
        entry.s = 8;
        entry.orderk = 33;
        entry.order = 23;
        mapMatrix.add(entry);
//        {s:11, 	orderk:37, 	order:37}		//1
        entry = new GridTaskConfigurationEntry();
        entry.s = 11;
        entry.orderk = 37;
        entry.order = 37;
        mapMatrix.add(entry);


    }

    public static boolean isTutorialSuccesfull(ArrayList<GridTask.GridTaskResult> gridTaskResults) {
        //        public static var REQUIRE_PERCENT_TO_PASS_TASK7:Number 	= 0.75;
        double wrongCount = 0;
        double elementsCount = 0;

        for (GridTask.GridTaskResult taskResult : gridTaskResults) {
            for (Double result : taskResult.results) {
                if (result > 0) {
                    wrongCount++;
                }
                elementsCount++;
            }
        }
        return (elementsCount - wrongCount) / elementsCount > 0.75;
    }


    public double setData(ArrayList<GridTask.GridTaskResult> data, boolean isAbstractTask, boolean tutorialWasSuccesful, long totalTime, boolean isFinished) {
        ArrayList<GridTask.GridTaskResult> additionalTutorialArray = new ArrayList<>();
        ArrayList<GridTask.GridTaskResult> sortedData = new ArrayList<>(data);
        String typeLetter = getTypeLetter(isAbstractTask);

        //sortujemy rosnąco po ilości plansz
        Collections.sort(sortedData, (lhs, rhs) -> {
            if (lhs.results.size() == rhs.results.size()) {
                return 0;
            } else {
                return lhs.results.size() - rhs.results.size();
            }
        });


        targetArray[1] = Tuple.Two.create("Z7_VERSION_" + typeLetter, typeLetter);
        targetArray[2] = Tuple.Two.create("W_Z7_" + typeLetter, isFinished ? "1" : "0");
        targetArray[3] = Tuple.Two.create("TC_Z7_" + typeLetter, totalTime + "");

        int tutorialStartOffsetAnswers = 5;
        int tutorialStartOffsetTimes = 13;
        if (tutorialWasSuccesful) {
            targetArray[4] = Tuple.Two.create("Z7_TEST_PASS_" + typeLetter, "1");

            targetArray[9] = Tuple.Two.create("Z7_S12_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[10] = Tuple.Two.create("Z7_S23_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[11] = Tuple.Two.create("Z7_S24_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[12] = Tuple.Two.create("Z7_S32_TEST_" + typeLetter, NOT_IMPORTANT);

            //czasy
            targetArray[17] = Tuple.Two.create("Z7_T12_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[18] = Tuple.Two.create("Z7_T23_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[19] = Tuple.Two.create("Z7_T24_TEST_" + typeLetter, NOT_IMPORTANT);
            targetArray[20] = Tuple.Two.create("Z7_T32_TEST_" + typeLetter, NOT_IMPORTANT);

        } else {
            targetArray[4] = Tuple.Two.create("Z7_TEST_PASS_" + typeLetter, "0");
        }


        HashMap<Integer, Integer> itemsOccurence = new HashMap<>();
        int itemCounter = 0;

        GridTask.GridTaskResult gridTaskResult;
        for (int i = 0; i < data.size(); i++) {
            gridTaskResult = data.get(i);
            if (gridTaskResult.isTutorial) {

                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);

                targetArray[tutorialStartOffsetAnswers] = Tuple.Two.create("Z7_S" + gridTaskResult.results.size() + itemCounter + "_TEST_" + typeLetter, ArbiterUtils.calculateVectorSum(gridTaskResult.results) + "");
                targetArray[tutorialStartOffsetTimes] = Tuple.Two.create("Z7_T" + gridTaskResult.results.size() + itemCounter + "_TEST_" + typeLetter, gridTaskResult.time + "");

                if (tutorialStartOffsetAnswers > 8) {//keep additional tutorial data
                    additionalTutorialArray.add(gridTaskResult);
                }

                tutorialStartOffsetAnswers++;
                tutorialStartOffsetTimes++;
            }
        }

        //Sprawdzamy ponownie czy tutorial się udał
        if (!tutorialWasSuccesful) {
            targetArray[4] = Tuple.Two.create("Z7_TEST_PASS_" + typeLetter, isTutorialSuccesfull(additionalTutorialArray) ? "1" : "0");

        }
//koniec tutoriala

//usuwamy tutorial, już nie będzie potrzebny

        for (Iterator<GridTask.GridTaskResult> resultIterator = data.iterator(); resultIterator.hasNext(); ) {
            if (resultIterator.next().isTutorial) {
                resultIterator.remove();
            }
        }
        for (Iterator<GridTask.GridTaskResult> resultIterator = sortedData.iterator(); resultIterator.hasNext(); ) {
            if (resultIterator.next().isTutorial) {
                resultIterator.remove();
            }
        }

        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        //Zsumowane odległości z plansz w posortowanej kolejności zaczynamy od komórki 21.
        for (int i = 0; i < sortedData.size(); i++) {
            gridTaskResult = sortedData.get(i);
            if (!gridTaskResult.isTutorial) {

                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);

                //4 plansze tutoriala
                targetArray[21 + i] = Tuple.Two.create("Z7_S" + gridTaskResult.results.size() + itemCounter + "_" + typeLetter, ArbiterUtils.calculateVectorSum(gridTaskResult.results) + "");
            }
        }
//Poszczególne wyniki z plansz w posortowanej kolejności. Zaczynamy od komórki 33.
        int summaryOffset = 0;
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        for (int i = 0; i < sortedData.size(); i++) {
            gridTaskResult = sortedData.get(i);
            if (!gridTaskResult.isTutorial) {
                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);


                for (int j = 0; j < gridTaskResult.results.size(); j++) {
                    targetArray[33 + j + summaryOffset] = Tuple.Two.create("Z7_S" + gridTaskResult.results.size() + itemCounter + "_P" + (j + 1) + "_" + typeLetter, gridTaskResult.results.get(j) + "");
                    LogUtils.d(TAG, "i = " + i + " j = " + j + " summaryOffset = " + summaryOffset + " sum = " + (33 + j + summaryOffset) + " value = " + gridTaskResult.results.get(j));
                }
                summaryOffset += gridTaskResult.results.size();
            }
        }
        //Czasy planszy w posortowanej kolejności. Zaczynamy od komórki 75
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        for (int i = 0; i < sortedData.size(); i++) {
            gridTaskResult = sortedData.get(i);
            if (!gridTaskResult.isTutorial) {

                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);


                targetArray[75 + i] = Tuple.Two.create("Z7_T" + gridTaskResult.results.size() + itemCounter + "_" + typeLetter, gridTaskResult.time + "");
            }
        }

        //Zsumowane odległości z plansz w orginalnej kolejności zaczynamy od komórki 87.
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        for (int i = 0; i < data.size(); i++) {
            gridTaskResult = data.get(i);
            if (!gridTaskResult.isTutorial) {
                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);

                //4 plansze tutoriala
                targetArray[87 + i] = Tuple.Two.create("Z7_S" + gridTaskResult.results.size() + itemCounter + "_K_" + typeLetter, ArbiterUtils.calculateVectorSum(gridTaskResult.results) + "");
            }
        }
        //Poszczególne wyniki z plansz w orginalnej kolejności. Zaczynamy od komórki 99.
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        summaryOffset = 0;
        for (int i = 0; i < data.size(); i++) {
            gridTaskResult = data.get(i);
            if (!gridTaskResult.isTutorial) {
                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);


                for (int j = 0; j < gridTaskResult.results.size(); j++) {
                    targetArray[99 + j + summaryOffset] = Tuple.Two.create("Z7_S" + gridTaskResult.results.size() + itemCounter + "_P" + (j + 1) + "_K_" + typeLetter, gridTaskResult.results.get(j) + "");
//                    Log.d(TAG, "i = " + i + " j = " + j + " summaryOffset = " + summaryOffset + " sum = " + (99 + j + summaryOffset) + " value = " + gridTaskResult.results.get(j));
                }
                summaryOffset += gridTaskResult.results.size();
            }
        }
        //Czasy planszy w orginalnej kolejności. Zaczynamy od komórki 141
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        for (int i = 0; i < data.size(); i++) {
            gridTaskResult = data.get(i);
            if (!gridTaskResult.isTutorial) {
                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);


                targetArray[141 + i] = Tuple.Two.create("Z7_T" + gridTaskResult.results.size() + itemCounter + "_K_" + typeLetter, gridTaskResult.time + "");
            }
        }
        // Wystąpiene wzorca w kolejności orginalnej. Zaczynamy od komórki 153 i 166

//        Z7_PAT21_K_N
        itemsOccurence = new HashMap<>();
        itemCounter = 0;
        for (int i = 0; i < data.size(); i++) {
            gridTaskResult = data.get(i);
            if (!gridTaskResult.isTutorial) {

                if (itemsOccurence.get(gridTaskResult.results.size()) == null) {
                    itemCounter = 0;
                } else {
                    itemCounter = itemsOccurence.get(gridTaskResult.results.size());
                }
                itemCounter++;
                itemsOccurence.put(gridTaskResult.results.size(), itemCounter);


                ArrayList<String> pattern = ArbiterUtils.checkIsPattern(gridTaskResult.results);
                targetArray[153 + i] = Tuple.Two.create("Z7_PAT" + gridTaskResult.results.size() + itemCounter + "_K_" + typeLetter, pattern.get(0));
                targetArray[166 + i] = Tuple.Two.create("Z7_PAT_OFFSET" + gridTaskResult.results.size() + itemCounter + "_K_" + typeLetter, pattern.get(1));
            }
        }

        Double sum = Double.valueOf(0.0);
        double divider = 0;
        for (GridTask.GridTaskResult taskResult : data) {
            for (Double result : taskResult.results) {
                sum += result;
            }
            divider++;
        }
        targetArray[0] = Tuple.Two.create("Z7_" + typeLetter, sum.floatValue() + "");
//        W zadaniu #7 (biedronki, kleksy):
//        - samouczek nie jest wliczany do wyniku
//                - prezentowane jest kilkanaście serii po kilka plansz
//        - po każdej planszy badany udziela odpowiedzi poprzez wskazanie (z zachowaniem kolejności) gdzie pojawiały się biedronki, za każde wskazanie miejsca badany dostaje ocenę równą odległości o jaką się pomylił (idealnie: 0.0, o 1 kratkę obok: 1.0, o 1 kratkę po ukosie: 1.41, itd.)
//        - po każdej serii, wyniki z poszczególnych plansz tej serii są sumowane (suma odległości pomyłek) i tworzą wynik z serii
//                - na koniec, wszystkie wyniki serii są sumowane i dzielone przez liczbę serii (uśredniane, czyli otrzymuje się średnią sumę odległości pomyłek w seriach)
//                - tak otrzymany wynik jest odejmowany od liczby 100, różnica dzielona przez 100, ewentualne wartości ujemne są podciągane do zera i uznawane za wynik badania
//        - finalny wynik jest z zakresu 0.0 (średnia suma odległości pomyłek w seriach wynosi 100 lub więcej) .. 1.0 (wszystko dobrze)

        sum = sum / divider;
        sum = 100 - sum;
        sum = sum / 100;
        sum = Math.max(0, sum);


        /////// OStateczne przygotowanie stringa
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
        if (!isFinished) {
            return -7;
        }
        return sum.floatValue();
    }


    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        return (userCorrectAnswers * 1.0) / (maxCorrectAnswersCount * 1.0) * 100.0;
    }

    public class GridTaskConfigurationEntry {
        public int s;
        public int orderk;
        public int order;

        @Override
        public String toString() {
            return "GridTaskConfigurationEntry{" +
                    "s=" + s +
                    ", orderk=" + orderk +
                    ", order=" + order +
                    '}';
        }
    }
}

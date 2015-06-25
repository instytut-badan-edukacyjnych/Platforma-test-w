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
import java.util.Iterator;

import pl.edu.ibe.loremipsum.arbiter.tpr.ArbiterUtils;
import pl.edu.ibe.loremipsum.arbiter.tpr.Tpr2Arbiter;
import pl.edu.ibe.loremipsum.task.tpr.actionscript.fourfields.FourFieldsBoardTask;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;

/**
 * Created by adam on 04.08.14.
 */
public class FourFieldsBoardArbiter extends Tpr2Arbiter {
    private static final String TAG = FourFieldsBoardArbiter.class.getSimpleName();

    private Tuple.Two<String, String>[] targetArray;


    public FourFieldsBoardArbiter() {
        targetArray = new Tuple.Two[500];
    }

    public double setData(ArrayList<FourFieldsBoardTask.Entry> entries, String area, boolean isAbstract, boolean isFinished, long overallTime, boolean tutorialWasSuccessful) {
        String typeLetter = getTypeLetter(isAbstract);


        targetArray[1] = Tuple.create("Z6_VERSION" + typeLetter, typeLetter);
        targetArray[2] = Tuple.create("W_Z6_" + typeLetter, isFinished ? "1" : "0");
        targetArray[3] = Tuple.create("TC_Z6_" + typeLetter, String.valueOf(overallTime));


        targetArray[5] = Tuple.create("Z6_TEST_PASS_" + typeLetter, tutorialWasSuccessful ? "1" : "0");


        ArrayList<FourFieldsBoardTask.Entry> extractedTutorial1 = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> extractedTutorial2 = new ArrayList<>();

        FourFieldsBoardTask.Entry taskEntry;


        for (int i = 0; i < entries.size(); i++) {
            taskEntry = entries.get(i);
            if (taskEntry.isTutorial) {
                if (i < 12) {
                    extractedTutorial1.add(taskEntry);
                } else {
                    extractedTutorial2.add(taskEntry);
                }
            }
        }


        for (Iterator<FourFieldsBoardTask.Entry> iterator = entries.iterator(); iterator.hasNext(); ) {
            FourFieldsBoardTask.Entry entry = iterator.next();
            if (entry.isTutorial) {
                iterator.remove();

            }
        }


        entries.remove(0);

        double sum = 0;
        for (FourFieldsBoardTask.Entry entry : entries) {
            if (entry.wasAnswerCorrect) {
                sum++;
            }
        }


        targetArray[0] = Tuple.create("Z6_" + typeLetter, String.valueOf(sum));

        targetArray[4] = Tuple.create("TC_Z6_" + typeLetter, String.valueOf((96 - sum)));

        for (Iterator<FourFieldsBoardTask.Entry> resultIterator = entries.iterator(); resultIterator.hasNext(); ) {
            if (resultIterator.next().isTutorial) {
                resultIterator.remove();
            }
        }
//// Extracted tutorial
        resolveTutorial(extractedTutorial1, 1, typeLetter);
        resolveTutorial(extractedTutorial2, 2, typeLetter);

        //Sum of quaters

        ArrayList<FourFieldsBoardTask.Entry> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries, (lhs, rhs) -> {
            if (lhs.location.ordinal() == rhs.location.ordinal()) {
                return 0;
            }
            return lhs.location.ordinal() - rhs.location.ordinal();
        });
        Collections.sort(sortedEntries, (lhs, rhs) -> {
            if (lhs.isCorrect == rhs.isCorrect) {
                return 0;
            }
            if (rhs.isCorrect) {
                return -1;
            }
            return 1;
        });


        ///SUMY GRUP
        int resultSum = 0;
        boolean isCorrect;
        ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groupedByLocationEntriesAndCorrectness = groupByLocationAndCorrectness(sortedEntries);
        for (ArrayList<FourFieldsBoardTask.Entry> groupedByLocationEntry : groupedByLocationEntriesAndCorrectness) {
            for (FourFieldsBoardTask.Entry entry : groupedByLocationEntry) {
                if (entry.wasAnswerCorrect) {
                    resultSum++;
                }
                isCorrect = groupedByLocationEntry.get(0).isCorrect;
                switch (groupedByLocationEntry.get(0).location) {
                    case UL:
                        if (isCorrect) {
                            if (targetArray[18] != null) {
                                resultSum += Double.valueOf(targetArray[18].second);
                            }
                            targetArray[18] = Tuple.create("Z6_C" + 1 + "_T_" + typeLetter, String.valueOf(resultSum));
                        } else {
                            if (targetArray[22] != null) {
                                resultSum += Double.valueOf(targetArray[22].second);
                            }
                            targetArray[22] = Tuple.create("Z6_C" + 1 + "_N_" + typeLetter, String.valueOf(resultSum));
                        }
                        break;
                    case UR:
                        if (isCorrect) {
                            if (targetArray[19] != null) {
                                resultSum += Double.valueOf(targetArray[19].second);
                            }
                            targetArray[19] = Tuple.create("Z6_C" + 2 + "_T_" + typeLetter, String.valueOf(resultSum));
                        } else {
                            if (targetArray[23] != null) {
                                resultSum += Double.valueOf(targetArray[23].second);
                            }
                            targetArray[23] = Tuple.create("Z6_C" + 2 + "_N_" + typeLetter, String.valueOf(resultSum));
                        }
                        break;
                    case LR:
                        if (isCorrect) {
                            if (targetArray[20] != null) {
                                resultSum += Double.valueOf(targetArray[20].second);
                            }
                            targetArray[20] = Tuple.create("Z6_C" + 3 + "_T_" + typeLetter, String.valueOf(resultSum));
                        } else {
                            if (targetArray[24] != null) {
                                resultSum += Double.valueOf(targetArray[24].second);
                            }
                            targetArray[24] = Tuple.create("Z6_C" + 3 + "_N_" + typeLetter, String.valueOf(resultSum));
                        }
                        break;
                    case LL:
                        if (isCorrect) {
                            if (targetArray[21] != null) {
                                resultSum += Double.valueOf(targetArray[21].second);
                            }
                            targetArray[21] = Tuple.create("Z6_C" + 4 + "_T_" + typeLetter, String.valueOf(resultSum));
                        } else {
                            if (targetArray[25] != null) {
                                resultSum += Double.valueOf(targetArray[25].second);
                            }
                            targetArray[25] = Tuple.create("Z6_C" + 4 + "_N_" + typeLetter, String.valueOf(resultSum));
                        }
                        break;
                }
                resultSum = 0;
            }
        }

//Zgrupowane wyniki po lokacji i poprawności

        int firstQuaterYesOccurenceCounter = 0;
        int firstQuaterNoOccurenceCounter = 0;
        int secondQuaterYesOccurenceCounter = 0;
        int secondQuaterNoOccurenceCounter = 0;
        int thirdQuaterYesOccurenceCounter = 0;
        int thirdQuaterNoOccurenceCounter = 0;
        int fourthQuaterYesOccurenceCounter = 0;
        int fourthQuaterNoOccurenceCounter = 0;


        int quater1Occurence = 1;
        int quater2Occurence = 1;
        int quater3Occurence = 1;
        int quater4Occurence = 1;


        for (int i = 0; i < entries.size(); i++) {
            taskEntry = entries.get(i);
            isCorrect = taskEntry.isCorrect;
            switch (taskEntry.location) {
                case UL:
                    if (isCorrect) {
                        if (taskEntry.touchTime != -7) {
                            targetArray[26 + firstQuaterYesOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_T_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[26 + firstQuaterYesOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_T_" + typeLetter, "0");
                        }
                        targetArray[122 + firstQuaterYesOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_TT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        firstQuaterYesOccurenceCounter++;
                    } else {
                        if (taskEntry.touchTime != -7) {
                            targetArray[38 + firstQuaterNoOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_N_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[38 + firstQuaterNoOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_N_" + typeLetter, "0");
                        }
                        targetArray[134 + firstQuaterNoOccurenceCounter] = Tuple.create("Z6_C1_" + quater1Occurence + "_NT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        firstQuaterNoOccurenceCounter++;
                    }
                    quater1Occurence++;
                    break;
                case UR:
                    if (isCorrect) {
                        if (taskEntry.touchTime != -7) {
                            targetArray[50 + secondQuaterYesOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_T_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[50 + secondQuaterYesOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_T_" + typeLetter, "0");
                        }
                        targetArray[146 + secondQuaterYesOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_TT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        secondQuaterYesOccurenceCounter++;
                    } else {
                        if (taskEntry.touchTime != -7) {
                            targetArray[62 + secondQuaterNoOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_N_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[62 + secondQuaterNoOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_N_" + typeLetter, "0");
                        }
                        targetArray[158 + secondQuaterNoOccurenceCounter] = Tuple.create("Z6_C2_" + quater2Occurence + "_NT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        secondQuaterNoOccurenceCounter++;
                    }
                    quater2Occurence++;
                    break;
                case LR:
                    if (isCorrect) {
                        if (taskEntry.touchTime != -7) {
                            targetArray[74 + thirdQuaterYesOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_T_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[74 + thirdQuaterYesOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_T_" + typeLetter, "0");
                        }
                        targetArray[170 + thirdQuaterYesOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_TT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        thirdQuaterYesOccurenceCounter++;
                    } else {
                        if (taskEntry.touchTime != -7) {
                            targetArray[86 + thirdQuaterNoOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_N_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[86 + thirdQuaterNoOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_N_" + typeLetter, "0");
                        }
                        targetArray[182 + thirdQuaterNoOccurenceCounter] = Tuple.create("Z6_C3_" + quater3Occurence + "_NT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        thirdQuaterNoOccurenceCounter++;
                    }
                    quater3Occurence++;
                    break;
                case LL:
                    if (isCorrect) {
                        if (taskEntry.touchTime != -7) {
                            targetArray[98 + fourthQuaterYesOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_T_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[98 + fourthQuaterYesOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_T_" + typeLetter, "0");
                        }
                        targetArray[194 + fourthQuaterYesOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_TT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        fourthQuaterYesOccurenceCounter++;
                    } else {
                        if (taskEntry.touchTime != -7) {
                            targetArray[110 + fourthQuaterNoOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_N_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");
                        } else {
                            targetArray[110 + fourthQuaterNoOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_N_" + typeLetter, "0");
                        }
                        targetArray[206 + fourthQuaterNoOccurenceCounter] = Tuple.create("Z6_C4_" + quater4Occurence + "_NT_" + typeLetter, String.valueOf(taskEntry.touchTime));
                        fourthQuaterNoOccurenceCounter++;
                    }
                    quater4Occurence++;
                    break;
            }
        }
//KOPIE
        firstQuaterYesOccurenceCounter = 0;
        firstQuaterNoOccurenceCounter = 0;
        secondQuaterYesOccurenceCounter = 0;
        secondQuaterNoOccurenceCounter = 0;
        thirdQuaterYesOccurenceCounter = 0;
        thirdQuaterNoOccurenceCounter = 0;
        fourthQuaterYesOccurenceCounter = 0;
        fourthQuaterNoOccurenceCounter = 0;

        quater1Occurence = 0;
        quater2Occurence = 0;
        quater3Occurence = 0;
        quater4Occurence = 0;

        int quaterIndex = 0;
        int currentOccurence = 0;
        for (int i = 0; i < entries.size(); i++) {
            taskEntry = entries.get(i);

            switch (taskEntry.location) {
                case UL:
                    quaterIndex = 1;
                    quater1Occurence++;
                    if (taskEntry.isCorrect) {
                        firstQuaterYesOccurenceCounter++;
                        currentOccurence = firstQuaterYesOccurenceCounter;
                    } else {
                        firstQuaterNoOccurenceCounter++;
                        currentOccurence = firstQuaterNoOccurenceCounter;
                    }
                    break;
                case UR:
                    quaterIndex = 2;
                    quater2Occurence++;
                    if (taskEntry.isCorrect) {
                        secondQuaterYesOccurenceCounter++;
                        currentOccurence = secondQuaterYesOccurenceCounter;
                    } else {
                        secondQuaterNoOccurenceCounter++;
                        currentOccurence = secondQuaterNoOccurenceCounter;
                    }
                    break;
                case LR:
                    quaterIndex = 3;
                    quater3Occurence++;
                    if (taskEntry.isCorrect) {
                        thirdQuaterYesOccurenceCounter++;
                        currentOccurence = thirdQuaterYesOccurenceCounter;
                    } else {
                        thirdQuaterNoOccurenceCounter++;
                        currentOccurence = thirdQuaterNoOccurenceCounter;
                    }
                    break;
                case LL:
                    quaterIndex = 4;
                    quater4Occurence++;
                    if (taskEntry.isCorrect) {
                        fourthQuaterYesOccurenceCounter++;
                        currentOccurence = firstQuaterYesOccurenceCounter;
                    } else {
                        fourthQuaterNoOccurenceCounter++;
                        currentOccurence = firstQuaterNoOccurenceCounter;
                    }
                    break;
            }


            if (taskEntry.touchTime != -7) {
//                Z6_C1_24_T_K_N
                targetArray[218 + i] = Tuple.create("Z6_C" + quaterIndex + "_" + currentOccurence + "_" + (taskEntry.isCorrect ? "T" : "N") + "_K_" + typeLetter, taskEntry.wasAnswerCorrect ? "1" : "0");

            } else {
//                Z6_C1_24_T_K_N
                targetArray[218 + i] = Tuple.create("Z6_C" + quaterIndex + "_" + currentOccurence + "_" + (taskEntry.isCorrect ? "T" : "N") + "_K_" + typeLetter, "99");
            }
//            Z6_C1_1_NT_K_N
            targetArray[314 + i] = Tuple.create("Z6_C" + quaterIndex + "_" + currentOccurence + "_" + (taskEntry.isCorrect ? "T" : "N") + "T_K_" + typeLetter, String.valueOf(taskEntry.touchTime));


        }


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

//        W zadaniu #6 (twarze, liczby):
//        - samouczek nie jest wliczany do wyniku
//                - pierwsza twarz wyświetlana zaraz po samouczku jest nieoceniana w ogóle
//                - potem prezentowane jest kilkadziesiąt twarzy (istnienie „serii” jest niewidoczne dla osoby badanej)
//        - przy każdej twarzy badany udziela odpowiedzi „tak”/„nie”, dostaje za to ocenę 0 lub 1 w zależności od poprawności, brak odpowiedzi uznaje się za niepoprawną odpowiedź
//        - na koniec powyższe oceny są sumowane i dzielone przez liczbę wszystkich (bez tej pierwszej) wyświetleń twarzy (uśredniane) i traktowane jako wynik badania
//        - finalny wynik jest z zakresu 0.0 (wszystko źle) .. 1.0 (wszystko dobrze)


        if (!isFinished) {
            return -7;
        }

        if (entries.size() > 0) {
            return sum / entries.size();
        }
        return sum;
    }

    private ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groupByLocationAndCorrectness(ArrayList<FourFieldsBoardTask.Entry> sortedEntries) {
        ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groups = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c1T = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c1N = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c2T = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c2N = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c3T = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c3N = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c4T = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> c4N = new ArrayList<>();
        for (FourFieldsBoardTask.Entry sortedEntry : sortedEntries) {
            switch (sortedEntry.location) {
                case UL:
                    if (sortedEntry.isCorrect) {
                        c1T.add(sortedEntry);
                    } else {
                        c1N.add(sortedEntry);
                    }
                    break;
                case UR:
                    if (sortedEntry.isCorrect) {
                        c2T.add(sortedEntry);
                    } else {
                        c2N.add(sortedEntry);
                    }
                    break;
                case LR:
                    if (sortedEntry.isCorrect) {
                        c3T.add(sortedEntry);
                    } else {
                        c3N.add(sortedEntry);
                    }
                    break;
                case LL:
                    if (sortedEntry.isCorrect) {
                        c4T.add(sortedEntry);
                    } else {
                        c4N.add(sortedEntry);
                    }
                    break;
            }
        }

        groups.add(c1T);
        groups.add(c1N);
        groups.add(c2T);
        groups.add(c2N);
        groups.add(c3T);
        groups.add(c3N);
        groups.add(c4T);
        groups.add(c4N);
        return groups;
    }

    private void resolveTutorial(ArrayList<FourFieldsBoardTask.Entry> extractedTutorial, int tutorialIndex, String typeLetter) {
        ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groups = groupBy4(extractedTutorial);
        int sum = 0;
        long time = 0;
        FourFieldsBoardTask.Entry entry;
        if (tutorialIndex == 1) {
            for (int i = 0; i < groups.size(); i++) {
                for (int j = 0; j < groups.get(i).size(); j++) {
                    entry = groups.get(i).get(j);
                    if (entry.touchTime != -7) {
                        //don't count not touched item
                        time += entry.touchTime;
                    }
                    if (entry.wasAnswerCorrect) {
                        sum++;
                    }
                }
                targetArray[12 + i] = Tuple.create("Z6_TEST_T" + tutorialIndex + "_" + (i + 1) + typeLetter, String.valueOf(time));
                targetArray[6 + i] = Tuple.create("Z6_TEST" + tutorialIndex + "_" + (i + 1) + typeLetter, String.valueOf(sum));
                sum = 0;
                time = 0;
            }
        } else if (tutorialIndex == 2 && extractedTutorial.size() > 0) {
            for (int i = 0; i < groups.size(); i++) {
                for (int j = 0; j < groups.get(i).size(); j++) {
                    entry = groups.get(i).get(j);
                    if (entry.touchTime != -7) {
                        //don't count not touched item
                        time += entry.touchTime;
                    }
                    if (entry.wasAnswerCorrect) {
                        sum++;
                    }
                }
                targetArray[15 + i] = Tuple.create("Z6_TEST_T" + tutorialIndex + "_" + (i + 1) + typeLetter, String.valueOf(time));
                targetArray[9 + i] = Tuple.create("Z6_TEST" + tutorialIndex + "_" + (i + 1) + typeLetter, String.valueOf(sum));
                sum = 0;
                time = 0;
            }
        } else {
            targetArray[9] = Tuple.create("Z6_TEST" + tutorialIndex + "_" + 1 + typeLetter, "7");
            targetArray[10] = Tuple.create("Z6_TEST" + tutorialIndex + "_" + 2 + typeLetter, "7");
            targetArray[11] = Tuple.create("Z6_TEST" + tutorialIndex + "_" + 3 + typeLetter, "7");

            targetArray[15] = Tuple.create("Z6_TEST_T" + tutorialIndex + "_" + 1 + typeLetter, "7");
            targetArray[16] = Tuple.create("Z6_TEST_T" + tutorialIndex + "_" + 2 + typeLetter, "7");
            targetArray[17] = Tuple.create("Z6_TEST_T" + tutorialIndex + "_" + 3 + typeLetter, "7");
        }

    }

    private ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groupBy4(ArrayList<FourFieldsBoardTask.Entry> array) {
        ArrayList<ArrayList<FourFieldsBoardTask.Entry>> groups = new ArrayList<>();
        ArrayList<FourFieldsBoardTask.Entry> group = null;
        for (int i = 0; i < array.size(); i++) {
            if (i % 4 == 0) {
                group = new ArrayList<>();
            }
            group.add(array.get(i));
            if (i % 4 == 3) {
                groups.add(group);
            }
        }

        return groups;
    }


    @Override
    public String getAnswerString() {
        return answerString;
    }

    @Override
    public double getPercentageMark() {
        return (userCorrectAnswers * 1.0) / (maxCorrectAnswersCount * 1.0);
    }


//    public class ReportEntry {
//        public static final int TRUE = 1;
//        public static final int FALSE = 0;
//        public static final int NO_REACTION = 99;
//
//
//        public int quaterId;
//        public int displayCount;
//        public long touchTime;
//        public boolean shouldYesButtonClicked;
//        public int wasProperButtonClicked;
//
//    }


}

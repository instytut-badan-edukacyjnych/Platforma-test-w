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

package pl.edu.ibe.loremipsum.tools.tpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by adam on 01.10.14.
 */
public class FakeRandom extends Random {

    public  static HashMap<String, Integer> methodCallCounter;
    private FakeRandomMode fakeRandomMode;

    private ArrayList<Integer> tpr2task7tutorial1A = new ArrayList<Integer>() {{
        add(calculatePosition(9, 1));
//--
        add(calculatePosition(10, 6));
        add(calculatePosition(3, 1));
//--
        add(calculatePosition(6, 8));
        add(calculatePosition(8, 5));
//--
        add(calculatePosition(5, 7));
        add(calculatePosition(6, 5));
        add(calculatePosition(3, 2));
    }};

    private ArrayList<Integer> tpr2task7tutorial2A = new ArrayList<Integer>() {{
        add(calculatePosition(8, 9));
//--
        add(calculatePosition(3, 8));
        add(calculatePosition(4, 6));
//--
        add(calculatePosition(5, 2));
        add(calculatePosition(10, 6));
//--
        add(calculatePosition(3, 9));
        add(calculatePosition(5, 4));
        add(calculatePosition(10, 6));
    }};
    private ArrayList<Integer> tpr2task7properA = new ArrayList<Integer>() {{
        add(calculatePosition(6, 6));
        add(calculatePosition(10, 1));
        //--
        add(calculatePosition(4, 8));
        add(calculatePosition(3, 6));
        add(calculatePosition(3, 3));
        add(calculatePosition(7, 3));
        //--
        add(calculatePosition(4, 10));
        add(calculatePosition(8, 8));
        add(calculatePosition(8, 4));
        //--
        add(calculatePosition(7, 10));
        add(calculatePosition(1, 9));
        add(calculatePosition(7, 7));
        add(calculatePosition(6, 7));
        add(calculatePosition(8, 4));
        //--
        add(calculatePosition(1, 8));
        add(calculatePosition(3, 5));
        add(calculatePosition(8, 3));
        //--
        add(calculatePosition(2, 9));
        add(calculatePosition(6, 5));
        //--
        add(calculatePosition(3, 6));
        add(calculatePosition(10, 2));
        add(calculatePosition(7, 1));
        //
        add(calculatePosition(4, 9));
        add(calculatePosition(6, 9));
        add(calculatePosition(8, 9));
        add(calculatePosition(2, 6));
        add(calculatePosition(3, 3));
        //
        add(calculatePosition(10, 10));
        add(calculatePosition(8, 3));
        add(calculatePosition(3, 2));
        add(calculatePosition(5, 1));
        //
        add(calculatePosition(9, 9));
        add(calculatePosition(2, 6));
        //
        add(calculatePosition(2, 10));
        add(calculatePosition(9, 7));
        add(calculatePosition(6, 5));
        add(calculatePosition(10, 1));
        //
        add(calculatePosition(8, 9));
        add(calculatePosition(6, 8));
        add(calculatePosition(9, 8));
        add(calculatePosition(4, 6));
        add(calculatePosition(3, 3));
    }};

    private ArrayList<Integer> tpr2task7tutorial1N = new ArrayList<Integer>() {{
        add(calculatePosition(10, 2));
        //
        add(calculatePosition(7, 7));
        add(calculatePosition(9, 1));
        //
        add(calculatePosition(6, 9));
        add(calculatePosition(7, 4));
        //
        add(calculatePosition(6, 8));
        add(calculatePosition(5, 6));
        add(calculatePosition(7, 3));
    }};
    private ArrayList<Integer> tpr2task7tutorial2N = new ArrayList<Integer>() {{
        add(calculatePosition(7, 6));
        //
        add(calculatePosition(2, 5));
        add(calculatePosition(1, 3));
        //
        add(calculatePosition(3, 10));
        add(calculatePosition(8, 1));
        //
        add(calculatePosition(10, 10));
        add(calculatePosition(10, 8));
        add(calculatePosition(3, 2));
    }};
    private ArrayList<Integer> tpr2task7properN = new ArrayList<Integer>() {{
        add(calculatePosition(3, 10));
        add(calculatePosition(10, 9));
        //--
        add(calculatePosition(9, 9));
        add(calculatePosition(7, 8));
        add(calculatePosition(9, 7));
        add(calculatePosition(3, 5));
        //--
        add(calculatePosition(6, 7));
        add(calculatePosition(8, 3));
        add(calculatePosition(9, 2));
        //--
        add(calculatePosition(8, 10));
        add(calculatePosition(2, 9));
        add(calculatePosition(3, 8));
        add(calculatePosition(8, 4));
        add(calculatePosition(6, 3));
        //--
        add(calculatePosition(4, 8));
        add(calculatePosition(1, 7));
        add(calculatePosition(9, 2));
        //--
        add(calculatePosition(10, 7));
        add(calculatePosition(7, 2));
        //--
        add(calculatePosition(7, 9));
        add(calculatePosition(2, 7));
        add(calculatePosition(6, 6));
        //
        add(calculatePosition(2, 8));
        add(calculatePosition(7, 5));
        add(calculatePosition(2, 4));
        add(calculatePosition(6, 4));
        add(calculatePosition(1, 2));
        //
        add(calculatePosition(5, 9));
        add(calculatePosition(3, 8));
        add(calculatePosition(3, 7));
        add(calculatePosition(10, 7));
        //
        add(calculatePosition(5, 9));
        add(calculatePosition(10, 3));
        //
        add(calculatePosition(3, 10));
        add(calculatePosition(6, 9));
        add(calculatePosition(7, 7));
        add(calculatePosition(9, 6));
        //
        add(calculatePosition(1, 8));
        add(calculatePosition(5, 7));
        add(calculatePosition(8, 4));
        add(calculatePosition(2, 3));
        add(calculatePosition(4, 3));
    }};

    public FakeRandom(FakeRandomMode fakeRandomMode) {
        this.fakeRandomMode = fakeRandomMode;
        if (methodCallCounter == null) {
            methodCallCounter = new HashMap<>();
        }
    }

    private static int calculatePosition(int x, int y) {
        int decimal = 10 - y;
        int ones = x - 1;

        return (10 * decimal) + ones;
    }

    @Override
    public int nextInt() {
        int index = countMethodCall("nextInt()" + fakeRandomMode);
        return super.nextInt();
    }


    @Override
    public int nextInt(int n) {
        int index = countMethodCall("nextInt(int n)" + fakeRandomMode);

        int randomNumber = super.nextInt(n);
        switch (fakeRandomMode) {
            case TPR2_TASK_7_TUTORIAL_1_ABSTRACT:
                randomNumber = tpr2task7tutorial1A.get(index);
                break;
            case TPR2_TASK_7_TUTORIAL_2_ABSTRACT:
                randomNumber = tpr2task7tutorial2A.get(index);
                break;
            case TPR2_TASK_7_PROPER_ABSTRACT:
                randomNumber = tpr2task7properA.get(index);
                break;
            case TPR2_TASK_7_TUTORIAL_1_NORMAL:
                randomNumber = tpr2task7tutorial1N.get(index);
                break;
            case TPR2_TASK_7_TUTORIAL_2_NORMAL:
                randomNumber = tpr2task7tutorial2N.get(index);
                break;
            case TPR2_TASK_7_PROPER_NORMAL:
                randomNumber = tpr2task7properN.get(index);
                break;
        }


        return randomNumber;
    }

    private int countMethodCall(String methodName) {
        int callCounter = -1;
        if (methodCallCounter.get(methodName) != null) {
            callCounter = methodCallCounter.get(methodName);
        }
        callCounter++;
        methodCallCounter.put(methodName, callCounter);
        return callCounter;
    }

    public enum FakeRandomMode {
        TPR2_TASK_7_TUTORIAL_1_ABSTRACT, TPR2_TASK_7_TUTORIAL_2_ABSTRACT, TPR2_TASK_7_PROPER_ABSTRACT,
        TPR2_TASK_7_TUTORIAL_1_NORMAL, TPR2_TASK_7_TUTORIAL_2_NORMAL, TPR2_TASK_7_PROPER_NORMAL
    }
}

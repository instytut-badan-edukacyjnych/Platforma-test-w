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

package pl.edu.ibe.loremipsum.arbiter.tpr;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by adam on 24.09.14.
 */
public class ArbiterUtils {
    public static double formatNumber(double res) {
        return Math.ceil(res * 100.0) / 100.0;
    }

    public static double calculateVectorSum(ArrayList<Double> v) {
        double res = 0;
        for (int i = 0; i < v.size(); i++) {
            res += v.get(i);
        }
        return res;
    }


    public static ArrayList<String> checkIsPattern(ArrayList<Double> src) {
        ArrayList<String> strings = new ArrayList<>();
        double first = -1;

        if (src.size() > 0) first = src.get(0);

        for (int i = 0; i < src.size(); i++) {
//            trace('check => ',first, src[i]);

            if (first != src.get(i)) {
//                trace('ZLE => ',0, first);
                strings.add("0");
                strings.add("-7");
                return strings;
            }
        }

//        trace('OK => ',1, first);
        strings.add("1");
        strings.add(first + "");
        return strings;
    }


    public static <T> ArrayList<T> trimArray(T[] array) {
        ArrayList<T> strings = new ArrayList<>();
        boolean firstProperCharacterOccurred = false;
        for (int i = array.length - 1; i >= 0; i--) {
            if (!firstProperCharacterOccurred) {
                if (array[i] == null) {
                    continue;
                } else {
                    firstProperCharacterOccurred = true;
                }
            } else {
                strings.add(array[i]);
            }

        }
        Collections.reverse(strings);
        return strings;
    }

}

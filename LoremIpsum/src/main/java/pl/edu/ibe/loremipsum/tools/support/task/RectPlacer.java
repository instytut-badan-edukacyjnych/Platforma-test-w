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

package pl.edu.ibe.loremipsum.tools.support.task;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by adam on 30.07.14.
 */
public class RectPlacer {

    private final int itemsCount;
    private Rect container;


    private ArrayList<Rect> rects;

    public RectPlacer(Rect container, int itemsCount) {
        this.container = container;
        this.itemsCount = itemsCount;

        rects = new ArrayList<>();
    }

    public Rect getItemPosition(int width, int height) {
        Rect rect;

        do {
            rect = generateRect(width, height);
        } while (!checkOverlapping(rect));
        rects.add(rect);
        return rect;
    }

    private Rect generateRect(int width, int height) {
        Random random = new Random();

        int left = random.nextInt((container.right - container.left) - width) + container.left;
        int top = random.nextInt((container.bottom - container.top) - height) + container.top;
        return new Rect(left, top, left + width, top + height);
    }

    private boolean checkOverlapping(Rect rect) {
        if (!container.contains(rect)) {
            return false;
        }
        for (Rect rect1 : rects) {
            if (Rect.intersects(rect1, rect)) {
                return false;
            }
        }

        return true;
    }
}

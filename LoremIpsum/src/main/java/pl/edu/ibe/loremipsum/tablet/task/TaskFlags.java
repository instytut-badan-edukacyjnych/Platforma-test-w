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

/**
 * Created by adam on 03.04.14.
 */
public class TaskFlags {

    /**
     * property: przycisk przywołania przycisków oceny
     */
    private static final String PROPERTY_EXPAND_BTN = "E";
    /**
     * property: ikona rejestracji dźwieku
     */
    private static final String PROPERTY_MICROPHONE_ICON = "M";

    /**
     * property: disable auto record.
     */
    private static final String PROPERTY_DISABLE_AUTO_RECORD = "Q";

    /**
     * property: przycisk wykonania zdjęcia
     */
    private static final String PROPERTY_PHOTO_BTN = "P";
    /**
     * property: przyciski oceny 0-1
     */
    private static final String PROPERTY_MARK_0_1_BTNS = "2";
    /**
     * property: przyciski oceny 0-1-2
     */
    private static final String PROPERTY_MARK_0_1_2_BTNS = "3";
    /**
     * property: przyciski oceny nieograniczonej
     */
    private static final String PROPERTY_MARK_UNLIMITED_BTNS = "O";
    /**
     * property: przycisk przywrócenia zadania
     */
    private static final String PROPERTY_RELOAD_BTN = "R";
    /**
     * property: przycisk przejscia do nastepnego zadania w sekwencji
     */
    private static final String PROPERTY_STEP_BTN = "S";
    /**
     * property: przycisk wywołania polecenia
     */
    private static final String PROPERTY_COMMAND_BTN = "C";
    /**
     * property: wywołaj nastepne zadanie sekwencyjne po zakończeniu dźwieku
     */
    private static final String PROPERTY_NEXT_AFTER_SOUND = "N";
    /**
     * property: to i nastepne zadanie sa alternatywne
     */
    private static final String PROPERTY_ALTERNATIVE = "A";
    /**
     * property: miejsce zawiniecia polecenie przy zadaniach sekwencyjnych
     */
    private static final String PROPERTY_LOOP = "L";
    /**
     * property: dodatkowy dźwiek po przycisku natepnego zadania w sekwencji
     */
    private static final String PROPERTY_STEP_COMMAND = "X";
    /**
     * property: kontynuacja sekwencji po udzieleniu odpowiedzi
     */
    private static final String PROPERTY_STEP_ANSWER = "I";
    /**
     * property: przyciąganie elementów do zadanej lokalizacji
     */
    private static final String PROPERTY_PULL = "U";
    /**
     * property: wyrównanie do poziomej linii bazowej
     */
    private static final String PROPERTY_BASE_LINE = "H";
    /**
     * property: pozycjonowaie według krawędzi
     */
    private static final String PROPERTY_BRINK = "B";
    /**
     * property: po puszczeniu przycisku znika zaznaczenie
     */
    private static final String PROPERTY_DESELECT_UP = "D";
    /**
     * Open marks flag.
     */
    public boolean expandMarksFlag = false;
    /**
     * Should currentTaskSuite register sound
     */
    public boolean registerSound = false;
    /**
     * Disables auto record in normal and demo mode
     */
    public boolean disableAutoRecord = false;
    /**
     * Should currentTaskSuite make a picture in text tasks
     */
    public boolean makePictureFlag = false;
    /**
     * Is currentTaskSuite marked in 0-1 scale
     */
    public boolean mark01Flag = false;
    /**
     * Is currentTaskSuite marked in 0-2 scale
     */
    public boolean mark02Flag = false;
    /**
     * Is currentTaskSuite marked in unlimited scale
     */
    public boolean markUnlimitedFlag = false;
    /**
     * Call back task
     */
    public boolean reloadTaskFlag = false;
    /**
     * Go to next task in sequence button
     */
    public boolean sequenceFlag = false;
    /**
     * Call command button
     */
    public boolean command = false;
    /**
     * is next task available after sound
     */
    public boolean nextAfterSound = false;
    /**
     * Is this and next task alternative
     */
    public boolean alternative = false;
    /**
     * Loop point for tasks
     */
    public boolean loop = false;
    /**
     * Sound after next task in sequence
     */
    public boolean stepCommand = false;
    /**
     * continue sequence after answer
     */
    public boolean stepAnswer = false;
    /**
     * Did task has pull elements option
     */
    public boolean pull = false;
    /**
     * Alignment to base line
     */
    public boolean baseLine = false;
    /**
     * Alignment to brink
     */
    public boolean brink = false;
    /**
     * Deselect item after up action
     */
    public boolean deselectUp = false;
    /**
     * Summary string with all flags
     */
    private String flagString;

    public TaskFlags(String flagString) {
        this.flagString = flagString;
        parseFlags(flagString);
    }

    public String getFlagString() {
        return flagString;
    }

    private void parseFlags(String flagString) {
        expandMarksFlag = flagString.contains(PROPERTY_EXPAND_BTN);
        registerSound = flagString.contains(PROPERTY_MICROPHONE_ICON);
        disableAutoRecord = flagString.contains(PROPERTY_DISABLE_AUTO_RECORD);
        makePictureFlag = flagString.contains(PROPERTY_PHOTO_BTN);
        mark01Flag = flagString.contains(PROPERTY_MARK_0_1_BTNS);
        mark02Flag = flagString.contains(PROPERTY_MARK_0_1_2_BTNS);
        markUnlimitedFlag = flagString.contains(PROPERTY_MARK_UNLIMITED_BTNS);
        reloadTaskFlag = flagString.contains(PROPERTY_RELOAD_BTN);
        sequenceFlag = flagString.contains(PROPERTY_STEP_BTN);
        command = flagString.contains(PROPERTY_COMMAND_BTN);
        nextAfterSound = flagString.contains(PROPERTY_NEXT_AFTER_SOUND);
        alternative = flagString.contains(PROPERTY_ALTERNATIVE);
        loop = flagString.contains(PROPERTY_LOOP);
        stepCommand = flagString.contains(PROPERTY_STEP_COMMAND);
        stepAnswer = flagString.contains(PROPERTY_STEP_ANSWER);
        pull = flagString.contains(PROPERTY_PULL);
        baseLine = flagString.contains(PROPERTY_BASE_LINE);
        brink = flagString.contains(PROPERTY_BRINK);
        deselectUp = flagString.contains(PROPERTY_DESELECT_UP);
    }
}

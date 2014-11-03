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


package pl.edu.ibe.loremipsum.task;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.view.MotionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import pl.edu.ibe.loremipsum.arbiter.BaseArbiter;
import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.tablet.BaseXMLFile;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.task.TaskFlags;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * Klasa bazowa zadania.
 * Przechowuje podstawowe informacje o zadaniu oraz parametry opisujące
 * zadania na potrzeby CAT  i IRT
 *
 * Base task class
 * Keeps base task information and description parameters for CAT and IRT
 *
 */
public class BaseTask extends BaseXMLFile {

    /**
     *Task file name
     *
     */
    public static final String APP_TASK_XML_FILENAME = "task.xml";
    /**
     * Task schema file name
     */
    public static final String APP_TASK_XSD_FILENAME = "task.xsd";
    /**
     * Task info filename
     */
    public static final String APP_DETAILS_XML_FILENAME = "details.xml";
    /**
     * Task info schema filename
     */
    public static final String APP_DETAILS_XSD_FILENAME = "details.xsd";
    /**
     * message: redraw screen
     */
    public static final int TASK_MESS_REFRESH_VIEW = 0;
    /**
     * message: screen tap
     */
    public static final int TASK_MESS_HAPTIC_FEEDBACK = 1;
    /**
     * message: test mark
     */
    public static final int TASK_MESS_MARK = 2;
    /**
     * message: mark cancelled
     */
    public static final int TASK_MESS_NO_MARK = 3;
    /**
     * message: next task step
     */
    public static final int TASK_MESS_NEXT_TASK = 4;
    /**
     * message: display next task step
     */
    public static final int TASK_MESS_SELECT_TASK = 5;
    /**
     * mark range 0-1
     */
    public static final int TASK_MARK_RANGE_0_1 = 2;
    /**
     * mark range 0-1-2
     */
    public static final int TASK_MARK_RANGE_0_1_2 = 3;
    /**
     * no limit task range
     */
    public static final int TASK_MARK_RANGE_UNLIMITED = 9999;
    /**
     * button identifiers
     */
    public static final int TASK_BTN_COMMAND = 1 << 0;
    public static final int TASK_BTN_INFO = 1 << 1;
    public static final int TASK_BTN_MENU = 1 << 2;
    public static final int TASK_BTN_NEXT = 1 << 3;
    public static final int TASK_BTN_MARK_ONE = 1 << 4;
    public static final int TASK_BTN_PHOTO = 1 << 5;
    public static final int TASK_BTN_PHOTO_DIS = 1 << 6;
    public static final int TASK_BTN_PREV = 1 << 7;
    public static final int TASK_BTN_RELOAD = 1 << 8;
    public static final int TASK_BTN_STEP = 1 << 9;
    public static final int TASK_BTN_MARK_TWO = 1 << 10;
    public static final int TASK_BTN_MARK_ZERO = 1 << 11;
    public static final int TASK_BTN_EXPOSE = 1 << 12;
    public static final int TASK_BTN_SOUND = 1 << 13;
    public static final int TASK_BTN_SOUND_DIS = 1 << 14;
    public static final int TASK_BTN_MARK_UNLIMITED = 1 << 15;
    /**
     * field width for displaying task
     */
    protected static final int VIEW_TASK_VIEW_WIDTH = 1130;
    /**
     * field height for diplaying task
     */
    protected static final int VIEW_TASK_VIEW_HEIGHT = 752;
    /**
     * Field size correction factor. From  1130x800 to 1130x752
     */
    protected static final int VIEW_SIZE_CORECTION_FACTOR_MUL = 752;
    protected static final int VIEW_SIZE_CORECTION_FACTOR_DIV = 800;
    /**
     * details.xml identifiers
     */
    protected static final String XML_DETAILS_TASK = "task";
    protected static final String XML_DETAILS_TASK_CLASS = "class";
    protected static final String XML_DETAILS_TASK_MAIN = "main";
    protected static final String XML_DETAILS_TASK_SOUND = "sound";
    protected static final String XML_DETAILS_TASK_EXTRA = "extra";
    protected static final String XML_DETAILS_TASK_BGCOLOR = "bgcolor";
    protected static final String XML_DETAILS_TASK_PROPERTY = "property";
    protected static final String XML_DETAILS_TASK_ARBITER = "arbiter";
    protected static final String XML_DETAILS_TASK_MARKER = "marker";
    protected static final String XML_DETAILS_TASK_FILM = "film";
    protected static final String XML_DETAILS_TASK_NUMBER = "number";
    protected static final String XML_DETAILS_TASK_ANSWER = "answer";
    protected static final String XML_DETAILS_TASK_THRESHOLD = "threshold";
    protected static final String XML_DETAILS_TASK_LEVEL = "level";
    protected static final String XML_DETAILS_TASK_LEVEL_1 = "level1";
    protected static final String XML_DETAILS_TASK_TIME_ON = "time_on";
    protected static final String XML_DETAILS_TASK_TIME_OFF = "time_off";
    protected static final String XML_DETAILS_TASK_ITEM = "item";
    protected static final String XML_DETAILS_TASK_SX = "sx";
    protected static final String XML_DETAILS_TASK_SY = "sy";
    protected static final String XML_DETAILS_TASK_PLACE = "place";
    protected static final String XML_DETAILS_TASK_MESSAGE = "message";
    protected static final String XML_DETAILS_TASK_PATTERN = "pattern";
    protected static final String XML_DETAILS_TASK_TIME = "time";
    protected static final String XML_DETAILS_PLACE = "place";
    protected static final String XML_DETAILS_PLACE_MASK = "mask";
    protected static final String XML_DETAILS_PLACE_ANSWER = "answer";
    protected static final String XML_DETAILS_FIELD = "field";
    protected static final String XML_DETAILS_FIELD_NAME = "name";
    protected static final String XML_DETAILS_FIELD_X = "x";
    protected static final String XML_DETAILS_FIELD_Y = "y";
    protected static final String XML_DETAILS_FIELD_PX = "px";
    protected static final String XML_DETAILS_FIELD_PY = "py";
    protected static final String XML_DETAILS_FIELD_MASK = "mask";
    protected static final String XML_GRAPHICS_FILE_EXT = ".png";
    protected static final String XML_AUDIO_FILE_EXT = ".m4a";
    protected static final String XML_PATTERN_CHAR = "%";
    /**
     * colour mask
     */
    protected static final int COLOR_ALPHA_MASK = 0xFF000000;
    /**
     * error communicates
     */
    protected static final String ERR_MESS_NO_FILE = " No file: ";
    protected static final String ERR_MESS_NO_ATTRIBUTE = " No attribute: ";
    protected static final String ERR_MESS_IO_EXCEPTION = " IO Exception: ";
    protected static final String ERR_MESS_PARSE_EXCEPTION = " Parse Configuration Exception: ";
    protected static final String ERR_MESS_SAX_EXCEPTION = " SAX Exception: ";
    private static final String TAG = BaseTask.class.toString();
    /**
     * Default background color
     */
    private static final String TASK_DEFAULT_BGCOLOR = "#FFF2F2F2";
    /**
     * Default task properties
     */
    private static final String TASK_DEFAULT_PROPERTY = "";

    /**
     * base task info
     */
    public TaskInfo m_taskInfo = null;

    /**
     * communicates handler
     */
    protected Handler m_actHandler = null;
    /**
     * diplay settings
     */
    protected Paint m_paint = null;
    /**
     * sound recorder
     */
    protected MediaRecorder m_recorder = null;

    /**
     * results file dir
     */
    protected String m_resultDir = null;

    /**
     * details.xml file
     */
    protected Document m_document = null;

    /**
     * Overriding task for group task
     */
    protected BaseTask m_parent = null;
    /**
     * task properties
     */
    public TaskFlags m_property = null;
    /**
     * task background colour
     */
    protected int m_bgColor = 0xFF000000;

    /**
     * Mark handler class
     */
    protected BaseArbiter m_arbiter = null;
    private boolean recorderStarted = false;

    /**
     * Controlling context
     */
    private Context context;

    /**
     * Task's private cache directory
     */
    private File cacheDir = null;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public BaseTask(Context context) {
        this.context = context;
    }

    /**
     * Metoda dynamicznego tworzenia instancji klasy obsługi zadania w oparciu o jego nazwę
     *
     * Dynamic instance  creation based on name
     *
     * @param context     Android context.
     * @param a_info      - task description
     * @param a_className - task handler class name
     * @param a_handler   - uchwyt do wymiany informacji o realizacji zadania
     * @param a_dir       - katalog w którym umieszczone jest zadanie
     * @return instancja utworzonego zadania lub null jezeli utworzenei sie nie powiodło
     */
    public static BaseTask CreateInstance(Context context, TaskInfo a_info, String a_className, Handler a_handler, String a_dir) throws IOException {
        return CreateInstance(context, null, a_info, a_className, a_handler, a_dir);
    }

    /**
     * Metoda dynamicznego tworzenia instancji klasy obsługi zadania w oparciu o jego nazwę
     *
     * Dynamic instance  creation based on name
     * @param context     Android context.
     * @param a_parent    - uchwyt do opisu zadania nadrzędnego; moze byc null jeżeli tworzone jest zadanie głowne
     * @param a_info      - opis zadania
     * @param a_className - nazwa klasy obsługującej zadanie
     * @param a_handler   - uchwyt do wymiany informacji o realizacji zadania
     * @param a_dir       - katalog w którym umieszczone jest zadanie
     * @return instancja utworzonego zadania lub null jezeli utworzenei sie nie powiodło
     */
    public static BaseTask CreateInstance(Context context, BaseTask a_parent, TaskInfo a_info, String a_className, Handler a_handler, String a_dir) throws IOException {
        try {
            Class<?> classz = Class.forName(TaskInfo.APP_PACKAGE_NAME + a_className);
            Class<? extends BaseTask> stub = classz.asSubclass(BaseTask.class);
            Constructor<? extends BaseTask> constructor;
            try {
                constructor = stub.getConstructor(Context.class);
            } catch (NoSuchMethodException e) {
                LogUtils.w(TAG, "Class " + stub + " has no constructor that receives Android context", e);
                constructor = stub.getConstructor();
            }

            BaseTask task;
            if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(Context.class))
                task = constructor.newInstance(context);
            else if (constructor.getParameterTypes().length == 0)
                task = constructor.newInstance();
            else
                throw new NoSuchMethodException("Invalid constructor: " + constructor);

            if (task != null) {
                task.Create(a_info, a_handler, a_dir);
                task.m_parent = a_parent;
            }

            return task;
        } catch (ClassNotFoundException | LinkageError | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            LogUtils.e(TAG, e);
        }

        return null;
    }

    /**
     * Sprawdzenie czy wszystkie elementy potrzebne do realizacj izadania sa dostepne
     *
     * @param a_info - informacja o zadaniu
     * @return true jezeli zadanie może być uruchamione
     */
    public static boolean CheckTask(TaskInfo a_info) throws IOException {
        boolean valid = true;
        String inputDir = LoremIpsumApp.m_legacyInputDir;
        Document doc = OpenXMLFile(
                a_info.getDirectory().getChildFile(APP_DETAILS_XML_FILENAME).getInputStream(),
                LoremIpsumApp.m_legacyInputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + APP_DETAILS_XSD_FILENAME);

        if (doc != null) {
            NodeList list = doc.getElementsByTagName("*");
            if (list != null) {
                for (int index = 0; index < list.getLength(); ++index) {
                    NamedNodeMap map = list.item(index).getAttributes();
                    if (map != null) {
                        for (int ni = 0; ni < map.getLength(); ++ni) {
                            Node node = map.item(ni);
                            if (node != null) {
                                String value = node.getNodeValue();
                                if ((value.contains(XML_GRAPHICS_FILE_EXT) || value.contains(XML_AUDIO_FILE_EXT)) &&
                                        !value.contains(XML_PATTERN_CHAR)) {
                                    VirtualFile check = a_info.getDirectory().getChildFile(value);
                                    if (!check.exists()) {
                                        valid = false;
                                        LogUtils.e(TAG, ERR_MESS_NO_FILE + check.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return valid;
    }

    /**
     * Returns associated Android context.
     *
     * @return Android context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns temporary directory for current task only. The directory is created in lazy manner
     * (during the first call of this method), and deleted automatically when task is closed.
     *
     * @return File containing temporary directory path.
     */
    protected synchronized File getTaskTempDir() throws IOException {
        if (cacheDir == null) {
            do {
                UUID uuid = UUID.randomUUID();
                cacheDir = new File(context.getCacheDir(), uuid.toString());
            } while (cacheDir.exists());
            LogUtils.v(TAG, "Creating task's cache directory: " + cacheDir);
            if (!cacheDir.mkdirs())
                throw new IOException("Could not create task's private cache directory: " + cacheDir);
        }
        return cacheDir;
    }

    /**
     * Removes task's private cache directory (if exists).
     */
    private synchronized void cleanupCache() throws IOException {
        if (cacheDir != null) {
            FileUtils.deleteRecursive(cacheDir);
            cacheDir = null;
        }
    }

    /**
     * Tworzenie instancji klasy obsługi zadania
     *
     * @param a_info    - opis zadania
     * @param a_handler - uchwyt do wymiany informacji o realizacji zadania
     * @param a_dir     - katalog w którym umieszczone jest zadanie
     * @return instancja utworzonego zadania lub null jezeli utworzenei sie nie powiodło
     */
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        LogUtils.d(TAG, "Create: " + a_info.m_name);

        m_taskInfo = a_info;
        m_actHandler = a_handler;
        m_resultDir = a_dir;
        m_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_document = null;

        boolean valid = true;
        Document doc = OpenXMLFile(
                a_info.getDirectory().getChildFile(APP_DETAILS_XML_FILENAME).getInputStream(),
                LoremIpsumApp.m_legacyInputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + APP_DETAILS_XSD_FILENAME);

        if (doc != null) {
            m_document = doc;

            try {
                NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
                if (list != null) {
                    if (list.getLength() > a_info.m_groupIndex) {
                        NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                        if (map != null) {
                            // atrybuty zadania
                            m_property = new TaskFlags(getString(map, XML_DETAILS_TASK_PROPERTY, TASK_DEFAULT_PROPERTY));

                            // kolor tła
                            m_bgColor = Color.parseColor(getString(map, XML_DETAILS_TASK_BGCOLOR, TASK_DEFAULT_BGCOLOR));

                            // arbiter oceny zadania
                            m_arbiter = null;
                            String name = getString(map, XML_DETAILS_TASK_ARBITER, null);
                            if (name != null) {
                                Class stub = Class.forName(BaseArbiter.APP_PACKAGE_NAME + name);
                                m_arbiter = (BaseArbiter) stub.newInstance();
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException | LinkageError | IllegalAccessException | InstantiationException e) {
                LogUtils.e(TAG, e);
            }
        }

        if (!valid) {
            m_document = null;
        }

        m_recorder = null;
        if (m_property.registerSound) {
            try {
                String fileName = m_taskInfo.m_name;
                fileName += " ";
                fileName += LoremIpsumApp.GetDateTimeString();
                fileName += LoremIpsumApp.SYS_SOUND_FILE_EXT;

                LoremIpsumApp.StoreTaskFile(fileName);

                MediaRecorder rec = new MediaRecorder();
                rec.setAudioSource(MediaRecorder.AudioSource.MIC);
                rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                LogUtils.v(TAG, "Output file for recording: \"" + m_resultDir + File.separator + fileName + "\"");
                rec.setOutputFile(m_resultDir + File.separator + fileName);
                rec.setMaxDuration(LoremIpsumApp.APP_MAX_SOUND_DURATION);
                rec.prepare();

                m_recorder = rec;
            } catch (IOException e) {
                LogUtils.e(TAG, e);
            }
        }

        return valid;
    }

    /**
     * Zwalaninie zasobów po zamknieciu zadania
     */
    public void Destory() throws IOException {
        cleanupCache();
    }

    /**
     * Zwraca nazwe zadania
     *
     * @return nzawa zadania
     */
    public String GetName() {

        return m_taskInfo.m_name;
    }

    /**
     * Pobiera filagi widoczności przycisków w polu prowadząego badanie
     *
     * @return suma logiczna flag przycisków
     */
    public int GetButtonVisibility() {

        int vis = TASK_BTN_MENU | TASK_BTN_NEXT | TASK_BTN_PREV;

        if (m_property.expandMarksFlag) {
            vis |= TASK_BTN_EXPOSE;
        } else {
            if (m_property.mark01Flag) {
                vis |= TASK_BTN_MARK_ZERO | TASK_BTN_MARK_ONE;
            } else if (m_property.mark02Flag) {
                vis |= TASK_BTN_MARK_ZERO | TASK_BTN_MARK_ONE | TASK_BTN_MARK_TWO;
            } else if (m_property.markUnlimitedFlag) {
                vis |= TASK_BTN_MARK_UNLIMITED;
            }
        }
        if (m_property.registerSound) {
            vis |= TASK_BTN_SOUND;
        }
        if (m_property.makePictureFlag) {
            vis |= TASK_BTN_PHOTO;
        }
        if (m_property.reloadTaskFlag) {
            vis |= TASK_BTN_RELOAD;
        }
        if (m_property.sequenceFlag) {
            vis |= TASK_BTN_STEP;
        }
        if (m_property.command) {
            vis |= TASK_BTN_COMMAND;
        }
        if (m_property.stepCommand) {
            vis |= TASK_BTN_STEP;
        }

        return vis;
    }

    /**
     * Ponownie uruchamia zadanie
     */
    public void ReloadTask() {

        if (m_arbiter != null) {
            m_arbiter.ReloadTask();
        }
    }

    /**
     * Pobiera kod skali zadań
     *
     * @return Kod skali zadań
     */
    public int GetMarkRange() {

        if (m_taskInfo.getM_range() == 1) {
            return TASK_MARK_RANGE_0_1;
        } else if (m_taskInfo.getM_range() == 2) {
            return TASK_MARK_RANGE_0_1_2;
        }
        return TASK_MARK_RANGE_UNLIMITED;
    }

    /**
     * Uruchamia odtwarzanie polecenia przypisanego do zadania
     *
     * @param a_player - uchwyt do odtwarzacza dźwieku
     */
    public void PlayCommand(MediaPlayer a_player) throws IOException {

        // empty
    }

    /**
     * Uruchamie odtwarzanie dodatkwoego polecenia przypisanego do zadania
     *
     * @param a_player - uchwyt do odtwarzacza dźwieku
     */
    public void PlayExtraCommand(MediaPlayer a_player) throws IOException {

        // empty
    }

    /**
     * Uruchamia powtórzenie polecenia
     *
     * @param a_player - uchwyt do odtwarzacza dźwieku
     */
    public void RepeatCommand(MediaPlayer a_player) throws IOException {

        // empty
    }

    /**
     * Uruchamia zadanie
     */
    public void StartTask() {

        if (m_arbiter != null) {
            m_arbiter.StartTask();
        }
    }

    /**
     * Kończy zadanie
     */
    public void EndTask() {

        if (m_arbiter != null) {
            m_arbiter.EndTask();
        }
        stopRecord();
    }

    public void startRecord() {
        if (m_recorder != null) {
            m_recorder.start();
            recorderStarted = true;
        }
    }

    public void stopRecord() {
        if (m_recorder != null) {
            if (recorderStarted)
                m_recorder.stop();
            m_recorder.reset();
            m_recorder.release();

            m_recorder = null;
        }
    }

    /**
     * Zwraca flage informującą czy jest lokejne zadanie do wykonania w zadaniach grupowych
     *
     * @return true jezeli jest nastepne zadanie do wykonania
     */
    public boolean NextTask() throws IOException {

        return false;
    }

    /**
     * Uruchamia zadanie grupowe o podanym indeksie (numerze kolejnym)
     *
     * @param a_index - numer kolejny zadania
     * @return true jezeli zadanie zostanie uruchamione
     */
    public boolean SelectTask(int a_index) throws IOException {

        return false;
    }

    /**
     * Sprawdza czy po zakończeniu dźwieku ma być uruchamione nastepne zadanie
     *
     * @return true jezeli ma być uruchamione zadanie
     */
    public boolean IsSoundNextTask() {
        if (m_property.nextAfterSound) {
            return true;
        }

        return false;
    }

    /**
     * Osługa wywoływana po zakończeniu odtwarzania dźwieku
     */
    public void SoundFinish() {

        ArbiterCommandFinish();
    }

    /**
     * Przypisuje do zadania ocene wprowadzoną przez badacza
     *
     * @param a_mark - ocena zadania
     */
    public void SetMark(int a_mark) {

        if (m_arbiter != null) {
            if (m_property.expandMarksFlag) {
                m_arbiter.ForceMark(a_mark);
            } else {
                m_arbiter.Mark(a_mark, BaseArbiter.APP_ARBITER_HUMAN);
            }
        }
    }

    /**
     * Pobiera opis oceny zadania
     *
     * @return Opis oceny zadania
     */
    public MarkData GetMark() {

        if (m_arbiter != null) {
            return m_arbiter.GetMark();
        } else {
            return new MarkData();
        }
    }

    public TaskFlags getTaskFlags() {
        return m_property;
    }

    /**
     * Sprawdza czy jest dostepny rejestrator dźwieku
     *
     * @return true jeżeli rejestrator dźwieku jest dostepny
     */
    public boolean IsRecorder() {
        return m_property.registerSound;
    }

    /**
     * Zdarzenie zmiany wielkości/orientacji ekranu
     *
     * @param w    - nowa szerokość
     * @param h    - nowa wysokośc
     * @param oldw - poprzednia szerokośc
     * @param oldh - poprzednia wysokość
     */
    public void SizeChanged(int w, int h, int oldw, int oldh) {

        // empty
    }

    /**
     * Obsuga zdarzenia przerysowania ekranu
     *
     * @param canvas - kontekst wyswietlania erkanu
     */
    public void Draw(Canvas canvas) {

        // empty
    }

    /**
     * Obsługa zdarzenia dotkniecia ekranu
     *
     * @param event - informacja o zdarzeniu
     * @return true jeżel izdarzenie zostało obsłuzone
     */
    public boolean TouchEvent(MotionEvent event) {

        return false;
    }

    /**
     * Przekazanie informacji do arbitra o dotknieciu ekranu
     */
    protected void ScreenTouched() {

        if (m_arbiter != null) {
            m_arbiter.TouchScreen();
        }
    }

    /**
     * przekazanei informacji do arbitra o powtórzeniu polecenia
     */
    public void ArbiterRepeatCommand() {

        if (m_arbiter != null) {
            m_arbiter.RepeatCommand();
        }
    }

    /**
     * Przekazanie informacji do arbitra o próbie rozwiązania zadania
     */
    protected void ArbiterAttempt() {

        if (m_arbiter != null) {
            m_arbiter.Attempt();
        }
    }

    /**
     * Przekazanie do arbitra informacji o zakończeniu odtwarzania polecenia
     */
    public void ArbiterCommandFinish() {

        if (m_arbiter != null) {
            m_arbiter.FinishCommand();
        }
    }

    /**
     * Przekazanie do arbitra danych do oceny zadania
     *
     * @param a_data   - informacja o polach aktywnych zadania
     * @param a_answer - string prawidłowej odpowiedzi
     */
    protected void ArbiterAssess(Vector<FieldSelect> a_data, String a_answer) {

        if (m_arbiter != null) {
            m_arbiter.Assess(a_data, a_answer);
        }
    }

    /**
     * Przekazanie do arbitra danych do oceny zadania
     *
     * @param a_data      - informacja o polach aktywnych zadania
     * @param a_answer    - string prawidłowej odpowiedzi
     * @param a_threshold - próg pomiedzy oceną 1 i 2
     */
    protected void ArbiterAssess(Vector<FieldSelect> a_data, String a_answer, int a_threshold) {

        if (m_arbiter != null) {
            m_arbiter.Assess(a_data, a_answer, a_threshold);
        }
    }

    /**
     * Przekazanie do arbitra danych do oceny zadania
     *
     * @param a_data   - informacja o polach aktywnych zadania
     * @param a_answer - string prawidłowej odpowiedzi
     */
    protected void ArbiterAssess(ArrayList<FieldSelect> a_data, String a_answer) {

        if (m_arbiter != null) {
            m_arbiter.Assess(a_data, a_answer);
        }
    }

    /**
     * Przekazanie do arbitra danych do oceny zadania
     *
     * @param a_data - informacja o polach aktywnych zadania
     * @param a_hour - godzina poprawnej odpowiedzi
     * @param a_min  - minuty poprawnej odpowiedzi
     */
    protected void ArbiterAssess(ArrayList<FieldSelect> a_data, int a_hour, int a_min) {

        if (m_arbiter != null) {
            m_arbiter.Assess(a_data, a_hour, a_min);
        }
    }

    protected BitmapMaker makeScaledBy2Bitmap(VirtualFile bmpFile) throws IOException {
        return new BitmapMaker(bmpFile).invoke();
    }

    /**
     * Klasa opisu pola zadania
     *
     *
     */
    public class FieldSelect {

        /**
         * nazwa pola
         */
        public String m_name;
        /**
         * wielkośc pola
         */
        public Rect m_srce;
        /**
         * wielskość pola docelowego
         */
        public Rect m_dest;
        /**
         * bitmapa skojarzonego obrazka
         */
        public Bitmap m_mask;
        /**
         * połozenie w poziomie
         */
        public int m_xPlace;
        /**
         * położenie w pionie
         */
        public int m_yPlace;
        /**
         * flaga zaznaczenia pola
         */
        public boolean m_selected;
        /**
         * flaga odwróconych współrzednych wyswietlania
         */
        public boolean m_mirror;
    }

    protected class BitmapMaker {

        private boolean valid;
        private Bitmap bitmap;
        private int width;
        private int height;
        private VirtualFile bmpFile;

        public BitmapMaker(VirtualFile bmpFile) {
            this.bmpFile = bmpFile;
        }

        public boolean isValid() {
            return valid;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        private BitmapMaker invoke() throws IOException {
            if (bmpFile.exists()) {
                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(bmpFile.getInputStream(), null, opt);

                width = opt.outWidth;
                height = opt.outHeight;

                opt.inSampleSize = 2;
                opt.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(bmpFile.getInputStream(), null, opt);
                //TODO MK: this really causes the problem when SelectTask is visible - I am disabling the check only because I am unable to solve it before the next release
/*                if(bitmap == null)
                    throw new RuntimeException("Image data could not be decoded from "+bmpFile);*/
                // INFO_RESIZE <else>
//											bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                // INFO_RESIZE <end>

                valid = true;
            } else {
                // nie ma obrazka
                valid = false;

                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
            }
            return this;
        }
    }
}


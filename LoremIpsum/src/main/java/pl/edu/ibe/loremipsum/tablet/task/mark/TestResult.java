

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

package pl.edu.ibe.loremipsum.tablet.task.mark;


import android.os.Build;
import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Vector;

import pl.edu.ibe.loremipsum.data.PupilData;
import pl.edu.ibe.loremipsum.manager.BaseManager;
import pl.edu.ibe.loremipsum.manager.BaseManager.ManagerTestInfo;
import pl.edu.ibe.loremipsum.tablet.BaseXMLFile;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Klasa przechowywania wyników badania
 */
public class TestResult extends BaseXMLFile {

    /**
     * pola konfiguracji wyników
     */
    public static final String XML_RESULT_CONFIG = "result-conf";
    /**
     * flagi konfiguracji wyników
     */
    public static final int FLAG_RESULT_CFG_BIRTHDAY = 1 << 1;
    public static final int FLAG_RESULT_CFG_GENDER = 1 << 2;
    public static final int FLAG_RESULT_CFG_RESEARCHER = 1 << 3;
    public static final int FLAG_RESULT_CFG_SCHOOL = 1 << 4;
    public static final int FLAG_RESULT_CFG_GROUP = 1 << 5;
    public static final int FLAG_RESULT_CFG_METHOD = 1 << 6;
    public static final int FLAG_RESULT_CFG_DURATION = 1 << 7;
    public static final int FLAG_RESULT_CFG_DATE = 1 << 8;
    public static final int FLAG_RESULT_CFG_SIGNATURE = 1 << 9;
    public static final int FLAG_RESULT_CFG_VERSION = 1 << 10;
    public static final int FLAG_RESULT_CFG_SERIAL = 1 << 11;
    public static final int FLAG_RESULT_CFG_NOTE_FILE = 1 << 12;
    /**
     * domyslne flagi zapisu elementów głownych
     */
    public static final int FLAG_RESULT_CFG_DEFAULT = FLAG_RESULT_CFG_BIRTHDAY |
            FLAG_RESULT_CFG_GENDER |
            FLAG_RESULT_CFG_RESEARCHER |
            FLAG_RESULT_CFG_SCHOOL |
            FLAG_RESULT_CFG_GROUP |
            FLAG_RESULT_CFG_METHOD |
            FLAG_RESULT_CFG_DURATION |
            FLAG_RESULT_CFG_DATE |
            FLAG_RESULT_CFG_SIGNATURE |
            FLAG_RESULT_CFG_VERSION |
            FLAG_RESULT_CFG_SERIAL |
            FLAG_RESULT_CFG_NOTE_FILE;
    /**
     * flaga zapisu elementów głownych
     */
    public static int m_rootSaveFlag = FLAG_RESULT_CFG_DEFAULT;
    public static final int FLAG_RESULT_CFG_RESULT_AREA = 1 << 1;
    public static final int FLAG_RESULT_CFG_RESULT_STATUS = 1 << 2;
    public static final int FLAG_RESULT_CFG_RESULT_LENGTH = 1 << 3;
    public static final int FLAG_RESULT_CFG_RESULT_THETA = 1 << 4;
    public static final int FLAG_RESULT_CFG_RESULT_SE = 1 << 5;
    /**
     * domyslne flagi zapisu elementów wyników
     */
    public static final int FLAG_RESULT_CFG_RESULT_DEFAULT = FLAG_RESULT_CFG_RESULT_AREA |
            FLAG_RESULT_CFG_RESULT_STATUS |
            FLAG_RESULT_CFG_RESULT_LENGTH |
            FLAG_RESULT_CFG_RESULT_THETA |
            FLAG_RESULT_CFG_RESULT_SE;
    /**
     * flaga zapisu elementów wyników
     */
    public static int m_resultSaveFlag = FLAG_RESULT_CFG_RESULT_DEFAULT;
    public static final int FLAG_RESULT_CFG_TASK_NAME = 1 << 1;
    public static final int FLAG_RESULT_CFG_TASK_AREA = 1 << 2;
    public static final int FLAG_RESULT_CFG_TASK_NR = 1 << 3;
    public static final int FLAG_RESULT_CFG_TASK_ANSWER = 1 << 4;
    public static final int FLAG_RESULT_CFG_TASK_DURATION = 1 << 5;
    public static final int FLAG_RESULT_CFG_TASK_MARK = 1 << 6;
    public static final int FLAG_RESULT_CFG_TASK_THETA = 1 << 7;
    public static final int FLAG_RESULT_CFG_TASK_SE = 1 << 8;
    public static final int FLAG_RESULT_CFG_TASK_FILE = 1 << 9;
    /**
     * domyslne flagi zapisu elementów zadań
     */
    public static final int FLAG_RESULT_CFG_TASK_DEFAULT = FLAG_RESULT_CFG_TASK_NAME |
            FLAG_RESULT_CFG_TASK_AREA |
            FLAG_RESULT_CFG_TASK_NR |
            FLAG_RESULT_CFG_TASK_ANSWER |
            FLAG_RESULT_CFG_TASK_DURATION |
            FLAG_RESULT_CFG_TASK_THETA |
            FLAG_RESULT_CFG_TASK_SE |
            FLAG_RESULT_CFG_TASK_FILE;
    /**
     * flaga zapisu elementów zadań
     */
    public static int m_taskSaveFlag = FLAG_RESULT_CFG_TASK_DEFAULT;
    /**
     * pola zapisu wyników do pliku
     */
    public static final String XML_TEST_ROOT = "tunss";
    public static final String XML_TEST_RESPONDENT = "respondent";
    public static final String XML_TEST_RESULT = "result";
    public static final String XML_TEST_TASKS = "tasks";
    public static final String XML_TEST_RESPONDENT_TIMESTAMP = "timestamp";
    public static final String XML_TEST_RESPONDENT_ID = "id";
    public static final String XML_TEST_RESPONDENT_GENDER = "gender";
    public static final String XML_TEST_RESPONDENT_BIRTHDAY = "birthday";
    public static final String XML_TEST_RESPONDENT_SCHOOL = "school";
    public static final String XML_TEST_RESPONDENT_RESEARCHER = "researcher";
    public static final String XML_TEST_RESPONDENT_GROUP = "group";
    public static final String XML_TEST_RESPONDENT_METHOD = "method";
    public static final String XML_TEST_RESPONDENT_DURATION = "duration";
    public static final String XML_TEST_RESPONDENT_DATE = "date";
    public static final String XML_TEST_RESPONDENT_NOTE_FILE = "note";
    public static final String XML_TEST_RESPONDENT_SIGNATURE = "signature";
    public static final String XML_TEST_RESPONDENT_VERSION = "version";
    public static final String XML_TEST_RESPONDENT_SERIAL = "serial";
    public static final String XML_TEST_RESULT_AREA = "area";
    public static final String XML_TEST_RESULT_STATUS = "status";
    public static final String XML_TEST_RESULT_LENGTH = "length";
    public static final String XML_TEST_RESULT_THETA = "theta";
    public static final String XML_TEST_RESULT_SE = "se";
    public static final String XML_TEST_TASKS_NAME = "name";
    public static final String XML_TEST_TASKS_AREA = "area";
    public static final String XML_TEST_TASKS_NR = "nr";
    public static final String XML_TEST_TASKS_MARK = "mark";
    public static final String XML_TEST_TASKS_ANSWER = "answer";
    public static final String XML_TEST_TASKS_DURATION = "duration";
    public static final String XML_TEST_TASKS_THETA = "theta";
    public static final String XML_TEST_TASKS_SE = "se";
    public static final String XML_TEST_TASKS_FILE = "file";
    public static final String XML_TEST_TASKS_FILE_NAME = "name";
    private static final String TAG = TestResult.class.toString();
    /**
     * Nazwa pliku konfiguracji wyników
     */
    private static final String RESULT_CONFIG_XML_FILENAME = "result-conf.xml";
    /**
     * Nazwa pliku schematu konfiguracji wyników
     */
    private static final String RESULT_CONFIG_XSD_FILENAME = "result-conf.xsd";
    private static final String FLAG_RESULT_YES = "yes";
    /**
     * Nazwa pliku schematu definicji wyników
     */
    private static final String RESULT_XSD_FILENAME = "result.xsd";
    private static final String CSV_FIELD_SEPARATOR = ";";
    private static final String TXT_FIELD_SEPARATOR = ": ";
    private static final String TXT_FIELD_NEW_LINE = "\r\n";
    /**
     * znacznik czasu badania
     */
    public long m_timestamp = 0;

    /**
     * opis osoby badanej
     */
    public PupilData m_respondent = null;
    /**
     * wyniki rozwiazywania zadań
     */
    public Vector<ResultTaskItem> m_tasks = null;
    /**
     * wyniki badania
     */
    public LinkedHashMap<String, ResultItem> m_result = null;
    /**
     * czas trwania testu w sekundach
     */
    private long m_duration = 0;
    /**
     * metoda prowadzenia badania
     */
    private String m_method = "";
    /**
     * wersja oprogramowania
     */
    private String m_version = "";
    /**
     * sygnatura banku zadań
     */
    private String m_signature = "";
    /**
     * numer seryjny tabletu
     */
    private String m_serial = "";
    /**
     * czas rozpoczecia badania
     */
    private Date m_start = null;
    /**
     * czas zakończenia testu
     */
    private Date m_finish = null;
    /**
     * flaga rejestracji wyników
     */
    private boolean m_recording = false;
    /**
     * flaga poprawnosci wczytanyc hdanych
     */
    private boolean m_valid = false;


    /**
     * Konstruktor wyników badania
     */
    public TestResult() {
        m_result = new LinkedHashMap<String, ResultItem>();
        m_tasks = new Vector<ResultTaskItem>();

        Clear();
    }

    /**
     * Odczytuje konfiguracje z pliku xml
     */
    public static void LoadConfigXML() throws IOException {
        LogUtils.d(TAG, "loadConfigXML");

        m_rootSaveFlag = FLAG_RESULT_CFG_DEFAULT;
        m_resultSaveFlag = FLAG_RESULT_CFG_RESULT_DEFAULT;
        m_taskSaveFlag = FLAG_RESULT_CFG_TASK_DEFAULT;
        String inputDir = LoremIpsumApp.m_legacyInputDir;
        Document doc = OpenXMLFile(new FileInputStream(inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + RESULT_CONFIG_XML_FILENAME),
                inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + RESULT_CONFIG_XSD_FILENAME);
        if (doc == null) {
            return;
        }

        NodeList list = doc.getElementsByTagName(XML_RESULT_CONFIG);
        if (list != null) {
            if (list.getLength() > 0) {
                // W pliku powinien być tylko jeden tak element
                NamedNodeMap map = list.item(0).getAttributes();
                if (map != null) {
                    m_rootSaveFlag = 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_BIRTHDAY, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_BIRTHDAY : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_GENDER, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_GENDER : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_RESEARCHER, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESEARCHER : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_SCHOOL, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_SCHOOL : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_GROUP, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_GROUP : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_METHOD, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_METHOD : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_DURATION, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_DURATION : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_DATE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_DATE : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_SIGNATURE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_SIGNATURE : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_VERSION, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_VERSION : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_SERIAL, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_SERIAL : 0;
                    m_rootSaveFlag |= getString(map, XML_TEST_RESPONDENT_NOTE_FILE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_NOTE_FILE : 0;
                }
            }
        }

        list = doc.getElementsByTagName(XML_TEST_RESULT);
        if (list != null) {
            if (list.getLength() > 0) {
                // W pliku powinien być tylko jeden tak element
                NamedNodeMap map = list.item(0).getAttributes();
                if (map != null) {
                    m_resultSaveFlag = 0;
                    m_resultSaveFlag |= getString(map, XML_TEST_RESULT_AREA, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESULT_AREA : 0;
                    m_resultSaveFlag |= getString(map, XML_TEST_RESULT_STATUS, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESULT_STATUS : 0;
                    m_resultSaveFlag |= getString(map, XML_TEST_RESULT_LENGTH, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESULT_LENGTH : 0;
                    m_resultSaveFlag |= getString(map, XML_TEST_RESULT_THETA, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESULT_THETA : 0;
                    m_resultSaveFlag |= getString(map, XML_TEST_RESULT_SE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_RESULT_SE : 0;
                }
            }
        }

        list = doc.getElementsByTagName(XML_TEST_TASKS);
        if (list != null) {
            if (list.getLength() > 0) {
                // W pliku powinien być tylko jeden tak element
                NamedNodeMap map = list.item(0).getAttributes();
                if (map != null) {
                    m_taskSaveFlag = 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_NAME, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_NAME : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_AREA, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_AREA : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_NR, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_NR : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_MARK, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_MARK : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_ANSWER, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_ANSWER : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_DURATION, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_DURATION : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_THETA, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_THETA : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_SE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_SE : 0;
                    m_taskSaveFlag |= getString(map, XML_TEST_TASKS_FILE, "").compareTo(FLAG_RESULT_YES) == 0 ? FLAG_RESULT_CFG_TASK_FILE : 0;
                }
            }
        }
    }

    /**
     * Kasuje poprzednie wyniki
     */
    public void Clear() {
        m_respondent = null;
        m_result.clear();
        m_tasks.clear();

        m_start = null;
        m_finish = null;

        m_version = LoremIpsumApp.m_versionString;
        m_signature = LoremIpsumApp.m_signatureString;
        m_serial = Build.SERIAL;
    }

    /**
     * Przypisuje osobe badaną
     *
     * @param a_respondent - informacja o osobie badanej
     * @param a_method     - nazwa metody testu
     */
    public void Assign(PupilData a_respondent, String a_method) {
        m_respondent = a_respondent;
        m_method = a_method;
    }

    /**
     * Przygotowanie nowego testu
     */
    public void NewTest() {
        m_start = new Date();
        m_finish = null;

        m_timestamp = m_start.getTime();

        StringBuffer buff = new StringBuffer();
        buff.append(LoremIpsumApp.APP_NOTE_FILENAME);
        buff.append(LoremIpsumApp.GetDateTimeString(m_timestamp));
        buff.append(LoremIpsumApp.APP_NOTE_FILENAME_EXT);
        m_respondent.m_note = buff.toString();
    }

    /**
     * Rozpoczecia badania
     *
     * @param a_recording - flaga rejestracji wyników
     */
    public void Start(boolean a_recording) {
        m_recording = a_recording;
    }

    /**
     * @param a_recording
     */
    public void Enable(boolean a_recording) {
        m_recording = a_recording;
    }

    /**
     * Pobiera czas rozpoczecia badania
     *
     * @return czar rozpoczecia badania
     */
    public Date GetStartTime() {
        return m_start;
    }

    /**
     * Zakończenie badania
     */
    public void Finish() {
        if (m_finish == null) {
            m_finish = new Date();

            m_duration = -1;
            if (m_start != null) {
                m_duration = m_finish.getTime() - m_start.getTime();
                m_duration /= 1000;
            }
        }
    }

    /**
     * Dodaje od wyników informację o zadaniu
     *
     * @param a_item - informacja o zadaniu
     */
    public void Add(ResultTaskItem a_item) {

        if (m_recording) {
            m_tasks.add(a_item);
        }
    }

    /**
     * Usuwa informację o zadaniu
     *
     * @param a_task - informacja o zadaniu
     *
     * @return true jeżeli informacja zaostała usunieta
     */
    public boolean Del(TaskInfo a_task) {
        if (m_recording) {
            for (ResultTaskItem res : m_tasks) {
                if (a_task.m_name.compareTo(res.m_taskName) == 0) {
                    m_tasks.remove(res);

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Aktualizauje informacje o badaniu
     *
     * @param a_name    - nazwa obszaru
     * @param a_manager - manager badania
     */
    public void Update(String a_name, BaseManager a_manager) {
        ManagerTestInfo info = a_manager.GetTestInfo(a_name);
        if (info == null) {
            LogUtils.d(TAG, "Update a_name: " + a_name);
            return;
        }

        ResultItem item = m_result.get(a_name);
        if (item == null) {

            item = new ResultItem();
            item.m_area = a_name;
            item.m_length = info.m_vector.m_piece.size();
            item.m_status = info.m_status;
            item.m_theta = info.m_vector.m_theta;
            item.m_se = info.m_vector.m_se;

            m_result.put(a_name, item);
        }
    }


    /**
     * Odczytuje wyniki z pliku xml
     *
     * @param a_fileName - nazwa pliku
     *
     * @return true - odczyt zakończony powodzeniem
     */
    public boolean LoadResultXML(String a_fileName) throws FileNotFoundException {
        LogUtils.d(TAG, "LoadResultXML");
        String inputDir = LoremIpsumApp.m_legacyInputDir;
        Document doc = OpenXMLFile(a_fileName,
                inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + RESULT_XSD_FILENAME);
        if (doc == null) {
            return false;
        }

        m_valid = true;
        m_result.clear();
        m_tasks.clear();
        m_respondent = new PupilData();

        NodeList list = doc.getElementsByTagName(XML_TEST_RESPONDENT);
        if (list != null) {
            if (list.getLength() > 0) {
                // W pliku powinien być tylko jeden tak element
                NamedNodeMap map = list.item(0).getAttributes();
                if (map != null) {
                    try {
                        // znacznik czasu poczatku badania
                        m_timestamp = GetLong(map, XML_TEST_RESPONDENT_TIMESTAMP);

                        // identyfikator badanego
                        m_respondent.m_id = GetString(map, XML_TEST_RESPONDENT_ID);

                        // płeć
                        m_respondent.m_gender = getString(map, XML_TEST_RESPONDENT_GENDER, "");

                        // data urodzenia
                        m_respondent.m_birthday = getString(map, XML_TEST_RESPONDENT_BIRTHDAY, "");

                        // identyfikator placówki
                        m_respondent.m_schoolId = getString(map, XML_TEST_RESPONDENT_SCHOOL, "");

                        // identyfikator prowadzącego badanie
                        m_respondent.m_assignTo = getString(map, XML_TEST_RESPONDENT_RESEARCHER, "");

                        // oddział w placówce
                        m_respondent.m_group = getString(map, XML_TEST_RESPONDENT_GROUP, "");

                        // metoda prowadzenia badania
                        m_method = getString(map, XML_TEST_RESPONDENT_METHOD, "");

                        // czas trwania testu
                        m_duration = GetInteger(map, XML_TEST_RESPONDENT_DURATION, 0);

                        // uwagi do badania
                        m_respondent.m_note = getString(map, XML_TEST_RESPONDENT_NOTE_FILE, "");

                        // sygnatura banku zadań
                        m_signature = getString(map, XML_TEST_RESPONDENT_SIGNATURE, "");

                        // wersja oprogramowania
                        m_version = getString(map, XML_TEST_RESPONDENT_VERSION, "");

                        // numer seryjny tabletu
                        m_serial = getString(map, XML_TEST_RESPONDENT_SERIAL, "");
                    } catch (XMLFileException e) {
                        LogUtils.d(TAG, "result discard: " + a_fileName, e);
                        m_valid = false;
                    }
                }
            }
        }

        // wyniki
        list = doc.getElementsByTagName(XML_TEST_RESULT);
        if (list != null) {
            if (list.getLength() > 0) {
                for (int index = 0; index < list.getLength(); ++index) {
                    NamedNodeMap map = list.item(index).getAttributes();
                    if (map != null) {
                        ResultItem item = new ResultItem();

                        try {
                            // obszar
                            item.m_area = GetString(map, XML_TEST_RESULT_AREA);

                            // status
                            item.m_status = LoremIpsumApp.StringToTestStatus(getString(map, XML_TEST_RESULT_STATUS, LoremIpsumApp.TEST_STATUS_ERROR));

                            // liczba zadań
                            item.m_length = GetInteger(map, XML_TEST_RESULT_LENGTH, 0);

                            // theta
                            item.m_theta = GetDouble(map, XML_TEST_RESULT_THETA, 0.0);

                            // se
                            item.m_se = GetDouble(map, XML_TEST_RESULT_SE, 0.0);

                            m_result.put(item.m_area, item);
                        } catch (XMLFileException e) {
                            LogUtils.d(TAG, "result discard: " + index + " " + a_fileName, e);
                            m_valid = false;
                        }
                    }
                }
            }
        }

        // informacja o zadaniach
        list = doc.getElementsByTagName(XML_TEST_TASKS);
        if (list != null) {
            if (list.getLength() > 0) {
                for (int index = 0; index < list.getLength(); ++index) {
                    NamedNodeMap map = list.item(index).getAttributes();
                    if (map != null) {
                        ResultTaskItem item = new ResultTaskItem();

                        try {
                            // zadanie
                            item.m_taskName = GetString(map, XML_TEST_TASKS_NAME);

                            // obszar
                            item.m_area = GetString(map, XML_TEST_TASKS_AREA);

                            // numer
                            item.m_nr = GetInteger(map, XML_TEST_TASKS_NR);

                            // ocena
                            item.m_mark = GetInteger(map, XML_TEST_TASKS_MARK);

                            // odpowiedź
                            item.m_answer = getString(map, XML_TEST_TASKS_ANSWER, LoremIpsumApp.APP_NO_FILL_FIELD);

                            // czas rozwiązywania zdania
                            item.m_taskDuration = GetInteger(map, XML_TEST_TASKS_DURATION, -1);

                            // theta
                            item.m_theta = GetInteger(map, XML_TEST_TASKS_THETA);

                            // se
                            item.m_se = GetInteger(map, XML_TEST_TASKS_SE);

                            // nie wczytujemy informacji o plikach

                            m_tasks.add(item);
                        } catch (XMLFileException e) {
                            LogUtils.d(TAG, "result discard: " + index + " " + a_fileName);
                            m_valid = false;
                        }
                    }
                }
            }
        }

        return m_valid;
    }

    /**
     * Odczytuje nagłowek wyników z pliku xml
     *
     * @param a_fileName - nazwa pliku
     *
     * @return true - odczyt zakończony powodzeniem
     */
    public boolean LoadResultStubXML(String a_fileName) throws FileNotFoundException {
        LogUtils.d(TAG, "LoadResultXML");
        String inputDir = LoremIpsumApp.m_legacyInputDir;
        Document doc = OpenXMLFile(a_fileName,
                inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + RESULT_XSD_FILENAME);
        if (doc == null) {
            return false;
        }

        m_valid = true;
        m_result.clear();
        m_tasks.clear();
        m_respondent = new PupilData();

        NodeList list = doc.getElementsByTagName(XML_TEST_RESPONDENT);
        if (list != null) {
            if (list.getLength() > 0) {
                // W pliku powinien być tylko jeden tak element
                NamedNodeMap map = list.item(0).getAttributes();
                if (map != null) {
                    try {
                        // znacznik czasu poczatku badania
                        m_timestamp = GetLong(map, XML_TEST_RESPONDENT_TIMESTAMP);

                        // identyfikator badanego
                        m_respondent.m_id = GetString(map, XML_TEST_RESPONDENT_ID);

                        // płeć
                        m_respondent.m_gender = getString(map, XML_TEST_RESPONDENT_GENDER, "");

                        // data urodzenia
                        m_respondent.m_birthday = getString(map, XML_TEST_RESPONDENT_BIRTHDAY, "");

                        // identyfikator placówki
                        m_respondent.m_schoolId = getString(map, XML_TEST_RESPONDENT_SCHOOL, "");

                        // identyfikator prowadzącego badanie
                        m_respondent.m_assignTo = getString(map, XML_TEST_RESPONDENT_RESEARCHER, "");

                        // oddział w placówce
                        m_respondent.m_group = getString(map, XML_TEST_RESPONDENT_GROUP, "");

                        // metoda prowadzenia badania
                        m_method = getString(map, XML_TEST_RESPONDENT_METHOD, "");

                        // czas trwania testu
                        m_duration = GetInteger(map, XML_TEST_RESPONDENT_DURATION, 0);

                        // uwagi do badania
                        m_respondent.m_note = getString(map, XML_TEST_RESPONDENT_NOTE_FILE, "");

                        // sygnatura banku zadań
                        m_signature = getString(map, XML_TEST_RESPONDENT_SIGNATURE, "");

                        // wersja oprogramowania
                        m_version = getString(map, XML_TEST_RESPONDENT_VERSION, "");

                        // numer seryjny tabletu
                        m_serial = getString(map, XML_TEST_RESPONDENT_SERIAL, "");
                    } catch (XMLFileException e) {
                        LogUtils.d(TAG, "result discard: " + a_fileName, e);
                        m_valid = false;
                    }
                }
            }
        }

        // wyniki
        list = doc.getElementsByTagName(XML_TEST_RESULT);
        if (list != null) {
            if (list.getLength() > 0) {
                for (int index = 0; index < list.getLength(); ++index) {
                    NamedNodeMap map = list.item(index).getAttributes();
                    if (map != null) {
                        ResultItem item = new ResultItem();

                        try {
                            // obszar
                            item.m_area = GetString(map, XML_TEST_RESULT_AREA);

                            // status
                            item.m_status = LoremIpsumApp.StringToTestStatus(getString(map, XML_TEST_RESULT_STATUS, LoremIpsumApp.TEST_STATUS_ERROR));

                            // liczba zadań
                            item.m_length = GetInteger(map, XML_TEST_RESULT_LENGTH, 0);

                            // theta
                            item.m_theta = GetDouble(map, XML_TEST_RESULT_THETA, 0.0);

                            // se
                            item.m_se = GetDouble(map, XML_TEST_RESULT_SE, 0.0);

                            m_result.put(item.m_area, item);
                        } catch (XMLFileException e) {
                            LogUtils.d(TAG, "result discard: " + index + " " + a_fileName);
                            m_valid = false;
                        }
                    }
                }
            }
        }

        return m_valid;
    }

    /**
     * Zapisuje wyniki badania w pliku xml
     *
     * @param a_fileName - nazwa pliku
     *
     * @return true - zapis zakończony powodzeniem
     */
    public boolean SaveResultXML(String a_fileName) {

        Document doc = createFactory();
        if (doc == null) {
            return false;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        formatter.setMinimumFractionDigits(4);
        if (formatter instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(dfs);
        }

        Element ele = doc.createElement(XML_TEST_ROOT);

        Element res = doc.createElement(XML_TEST_RESPONDENT);

        // informacje
        res.setAttribute(XML_TEST_RESPONDENT_TIMESTAMP, Long.toString(m_timestamp));
        res.setAttribute(XML_TEST_RESPONDENT_ID, m_respondent.m_id);
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_BIRTHDAY) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_BIRTHDAY, m_respondent.m_birthday);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GENDER) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_GENDER, m_respondent.m_gender);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_RESEARCHER) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_RESEARCHER, m_respondent.m_assignTo);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SCHOOL) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_SCHOOL, m_respondent.m_schoolId);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GROUP) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_GROUP, m_respondent.m_group);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_METHOD) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_METHOD, m_method);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DURATION) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_DURATION, Long.toString(m_duration));
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DATE) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_DATE, LoremIpsumApp.GetDateTimeStringOut(m_timestamp));
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_NOTE_FILE) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_NOTE_FILE, m_respondent.m_note);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SIGNATURE) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_SIGNATURE, m_signature);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_VERSION) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_VERSION, m_version);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SERIAL) != 0) {
            res.setAttribute(XML_TEST_RESPONDENT_SERIAL, m_serial);
        }

        ele.appendChild(res);

        // wyniki
        if (m_resultSaveFlag != 0) {
            Vector<ResultItem> areas = new Vector<ResultItem>(m_result.values());
            for (ResultItem item : areas) {
                Element sco = doc.createElement(XML_TEST_RESULT);
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_AREA) != 0) {
                    sco.setAttribute(XML_TEST_RESULT_AREA, item.m_area);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_STATUS) != 0) {
                    sco.setAttribute(XML_TEST_RESULT_STATUS, LoremIpsumApp.TestStatusToString(item.m_status));
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_LENGTH) != 0) {
                    sco.setAttribute(XML_TEST_RESULT_LENGTH, Integer.toString(item.m_length));
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_THETA) != 0) {
                    sco.setAttribute(XML_TEST_RESULT_THETA, formatter.format(item.m_theta));
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_SE) != 0) {
                    sco.setAttribute(XML_TEST_RESULT_SE, formatter.format(item.m_se));
                }

                ele.appendChild(sco);
            }
        }

        // zadania
        if (m_taskSaveFlag != 0) {
            for (ResultTaskItem item : m_tasks) {
                Element tas = doc.createElement(XML_TEST_TASKS);
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NAME) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_NAME, item.m_taskName);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_AREA) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_AREA, item.m_area);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NR) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_NR, Integer.toString(item.m_nr));
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_MARK) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_MARK, Double.toString(item.m_mark));
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_ANSWER) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_ANSWER, item.m_answer);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_DURATION) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_DURATION, Integer.toString(item.m_taskDuration));
                }

                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_THETA) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_THETA, formatter.format(item.m_theta));
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_SE) != 0) {
                    tas.setAttribute(XML_TEST_TASKS_SE, formatter.format(item.m_se));
                }

                // pliki do zadań
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_FILE) != 0) {
                    if (item.m_files != null) {
                        if (item.m_files.size() > 0) {
                            for (String file : item.m_files) {
                                Element fil = doc.createElement(XML_TEST_TASKS_FILE);
                                fil.setAttribute(XML_TEST_TASKS_FILE_NAME, file);

                                tas.appendChild(fil);
                            }
                        }
                    }
                }

                ele.appendChild(tas);
            }
        }

        doc.appendChild(ele);

        return SaveXMLFile(a_fileName, doc);
    }

    /**
     * Saves note attached to resultes.
     *
     * @return true if succeded
     */
    public boolean SaveNote() {
        if (TextUtils.isEmpty(m_respondent.m_note) || TextUtils.isEmpty(m_respondent.m_noteContent)){
            return true;
        }
        try {
            String resultsDir = ServiceProvider.obtain().currentTaskSuite().getResultsDir();
            File dir = new File(resultsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(resultsDir, m_respondent.m_note);
            FileWriter writer = new FileWriter(file);
            writer.write(m_respondent.m_noteContent);
            writer.flush();
            writer.close();

            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, e);
            return false;
        }
    }

    /**
     * Zapisuje wyniki badania w pliku csv
     *
     * @param a_fileName - nazwa pliku
     *
     * @return true - zapis zakończony powodzeniem
     */
    public boolean SaveResultCSV(String a_fileName) {

        boolean result = false;

        StringBuffer data = new StringBuffer();
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        formatter.setMinimumFractionDigits(4);

        // pierwsza linia
        data.append(CSV_FIELD_SEPARATOR);
        data.append(CSV_FIELD_SEPARATOR);
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_BIRTHDAY) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GENDER) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_RESEARCHER) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SCHOOL) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GROUP) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_METHOD) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DURATION) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DATE) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_NOTE_FILE) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SIGNATURE) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_VERSION) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SERIAL) != 0) {
            data.append(CSV_FIELD_SEPARATOR);
        }

        // wyniki
        if (m_resultSaveFlag != 0) {
            Vector<ResultItem> areas = new Vector<ResultItem>(m_result.values());
            for (ResultItem item : areas) {
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_AREA) != 0) {
                    data.append(XML_TEST_RESULT);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_STATUS) != 0) {
                    data.append(XML_TEST_RESULT);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_LENGTH) != 0) {
                    data.append(XML_TEST_RESULT);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_THETA) != 0) {
                    data.append(XML_TEST_RESULT);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_SE) != 0) {
                    data.append(XML_TEST_RESULT);
                    data.append(CSV_FIELD_SEPARATOR);
                }
            }
        }

        // zadania
        if (m_taskSaveFlag != 0) {
            for (ResultTaskItem item : m_tasks) {
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NAME) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_AREA) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NR) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_MARK) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_ANSWER) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_DURATION) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }

                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_THETA) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_SE) != 0) {
                    data.append(XML_TEST_TASKS);
                    data.append(CSV_FIELD_SEPARATOR);
                }

                // pliki do zadań
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_FILE) != 0) {
                    if (item.m_files != null) {
                        if (item.m_files.size() > 0) {
                            for (String file : item.m_files) {
                                data.append(XML_TEST_TASKS);
                                data.append(CSV_FIELD_SEPARATOR);
                            }
                        }
                    }
                }
            }
        }
        data.append(TXT_FIELD_NEW_LINE);

        // druga linia
        data.append(XML_TEST_RESPONDENT_TIMESTAMP);
        data.append(CSV_FIELD_SEPARATOR);

        data.append(XML_TEST_RESPONDENT_ID);
        data.append(CSV_FIELD_SEPARATOR);

        if ((m_rootSaveFlag & FLAG_RESULT_CFG_BIRTHDAY) != 0) {
            data.append(XML_TEST_RESPONDENT_BIRTHDAY);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GENDER) != 0) {
            data.append(XML_TEST_RESPONDENT_GENDER);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_RESEARCHER) != 0) {
            data.append(XML_TEST_RESPONDENT_RESEARCHER);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SCHOOL) != 0) {
            data.append(XML_TEST_RESPONDENT_SCHOOL);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GROUP) != 0) {
            data.append(XML_TEST_RESPONDENT_GROUP);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_METHOD) != 0) {
            data.append(XML_TEST_RESPONDENT_METHOD);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DURATION) != 0) {
            data.append(XML_TEST_RESPONDENT_DURATION);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DATE) != 0) {
            data.append(XML_TEST_RESPONDENT_DATE);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_NOTE_FILE) != 0) {
            data.append(XML_TEST_RESPONDENT_NOTE_FILE);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SIGNATURE) != 0) {
            data.append(XML_TEST_RESPONDENT_SIGNATURE);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_VERSION) != 0) {
            data.append(XML_TEST_RESPONDENT_VERSION);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SERIAL) != 0) {
            data.append(XML_TEST_RESPONDENT_SERIAL);
            data.append(CSV_FIELD_SEPARATOR);
        }

        // wyniki
        if (m_resultSaveFlag != 0) {
            Vector<ResultItem> areas = new Vector<ResultItem>(m_result.values());
            for (ResultItem item : areas) {
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_AREA) != 0) {
                    data.append(XML_TEST_RESULT_AREA);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_STATUS) != 0) {
                    data.append(XML_TEST_RESULT_STATUS);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_LENGTH) != 0) {
                    data.append(XML_TEST_RESULT_LENGTH);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_THETA) != 0) {
                    data.append(XML_TEST_RESULT_THETA);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_SE) != 0) {
                    data.append(XML_TEST_RESULT_SE);
                    data.append(CSV_FIELD_SEPARATOR);
                }

            }
        }

        // zadania
        if (m_taskSaveFlag != 0) {
            for (ResultTaskItem item : m_tasks) {
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NAME) != 0) {
                    data.append(XML_TEST_TASKS_NAME);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_AREA) != 0) {
                    data.append(XML_TEST_TASKS_AREA);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NR) != 0) {
                    data.append(XML_TEST_TASKS_NR);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_MARK) != 0) {
                    data.append(XML_TEST_TASKS_MARK);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_ANSWER) != 0) {
                    data.append(XML_TEST_TASKS_ANSWER);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_DURATION) != 0) {
                    data.append(XML_TEST_TASKS_DURATION);
                    data.append(CSV_FIELD_SEPARATOR);
                }

                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_THETA) != 0) {
                    data.append(XML_TEST_TASKS_THETA);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_SE) != 0) {
                    data.append(XML_TEST_TASKS_SE);
                    data.append(CSV_FIELD_SEPARATOR);
                }

                // pliki do zadań
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_FILE) != 0) {
                    if (item.m_files != null) {
                        if (item.m_files.size() > 0) {
                            for (String file : item.m_files) {
                                data.append(XML_TEST_TASKS_FILE);
                                data.append(CSV_FIELD_SEPARATOR);
                            }
                        }
                    }
                }
            }
        }
        data.append(TXT_FIELD_NEW_LINE);

        // dane
        data.append(m_timestamp);
        data.append(CSV_FIELD_SEPARATOR);

        data.append(m_respondent.m_id);
        data.append(CSV_FIELD_SEPARATOR);

        if ((m_rootSaveFlag & FLAG_RESULT_CFG_BIRTHDAY) != 0) {
            data.append(m_respondent.m_birthday);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GENDER) != 0) {
            data.append(m_respondent.m_gender);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_RESEARCHER) != 0) {
            data.append(m_respondent.m_assignTo);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SCHOOL) != 0) {
            data.append(m_respondent.m_schoolId);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_GROUP) != 0) {
            data.append(m_respondent.m_group);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_METHOD) != 0) {
            data.append(m_method);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DURATION) != 0) {
            data.append(m_duration);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_DATE) != 0) {
            data.append(LoremIpsumApp.GetDateTimeStringOut(m_timestamp));
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_NOTE_FILE) != 0) {
            data.append(m_respondent.m_note);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SIGNATURE) != 0) {
            data.append(m_signature);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_VERSION) != 0) {
            data.append(m_version);
            data.append(CSV_FIELD_SEPARATOR);
        }
        if ((m_rootSaveFlag & FLAG_RESULT_CFG_SERIAL) != 0) {
            data.append(m_serial);
            data.append(CSV_FIELD_SEPARATOR);
        }

        // wyniki
        if (m_resultSaveFlag != 0) {
            Vector<ResultItem> areas = new Vector<ResultItem>(m_result.values());
            for (ResultItem item : areas) {
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_AREA) != 0) {
                    data.append(item.m_area);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_STATUS) != 0) {
                    data.append(LoremIpsumApp.TestStatusToString(item.m_status));
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_LENGTH) != 0) {
                    data.append(item.m_length);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_THETA) != 0) {
                    data.append(formatter.format(item.m_theta));
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_resultSaveFlag & FLAG_RESULT_CFG_RESULT_SE) != 0) {
                    data.append(formatter.format(item.m_se));
                    data.append(CSV_FIELD_SEPARATOR);
                }

            }
        }

        // zadania
        if (m_taskSaveFlag != 0) {
            for (ResultTaskItem item : m_tasks) {
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NAME) != 0) {
                    data.append(item.m_taskName);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_AREA) != 0) {
                    data.append(item.m_area);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_NR) != 0) {
                    data.append(item.m_nr);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_MARK) != 0) {
                    data.append(item.m_mark);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_ANSWER) != 0) {
                    data.append(item.m_answer);
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_DURATION) != 0) {
                    data.append(item.m_taskDuration);
                    data.append(CSV_FIELD_SEPARATOR);
                }

                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_THETA) != 0) {
                    data.append(formatter.format(item.m_theta));
                    data.append(CSV_FIELD_SEPARATOR);
                }
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_SE) != 0) {
                    data.append(formatter.format(item.m_se));
                    data.append(CSV_FIELD_SEPARATOR);
                }

                // pliki do zadań
                if ((m_taskSaveFlag & FLAG_RESULT_CFG_TASK_FILE) != 0) {
                    if (item.m_files != null) {
                        if (item.m_files.size() > 0) {
                            for (String file : item.m_files) {
                                data.append(file);
                                data.append(CSV_FIELD_SEPARATOR);
                            }
                        }
                    }
                }

            }
        }
        data.append(TXT_FIELD_NEW_LINE);

        try {
            String resultsDir = ServiceProvider.obtain().currentTaskSuite().getResultsDir();
            File dir = new File(resultsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(resultsDir, a_fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.flush();
            writer.close();

            result = true;
        } catch (IOException e) {
            LogUtils.e(TAG, e);
        }

        return result;
    }

    /**
     * Zapisuje wyniki badania w pliku txt
     *
     * @param a_fileName - nazwa pliku
     *
     * @return true - zapis zakończony powodzeniem
     */
    public boolean SaveResultTXT(String a_fileName) {
        boolean result = false;

        StringBuffer data = new StringBuffer();
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        formatter.setMinimumFractionDigits(4);

        // informacje
        data.append(XML_TEST_RESPONDENT_TIMESTAMP);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_timestamp);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_ID);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_id);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_BIRTHDAY);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_birthday);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_GENDER);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_gender);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_RESEARCHER);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_assignTo);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_SCHOOL);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_schoolId);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_GROUP);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_group);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_METHOD);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_method);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_DURATION);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_duration);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_DATE);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(LoremIpsumApp.GetDateTimeStringOut(m_timestamp));
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_NOTE_FILE);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_respondent.m_note);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_SIGNATURE);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_signature);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_VERSION);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_version);
        data.append(TXT_FIELD_NEW_LINE);

        data.append(XML_TEST_RESPONDENT_SERIAL);
        data.append(TXT_FIELD_SEPARATOR);
        data.append(m_serial);
        data.append(TXT_FIELD_NEW_LINE);

        String indent = "!";
        String indent2 = "  !";

        // wyniki
        Vector<ResultItem> areas = new Vector<ResultItem>(m_result.values());
        for (ResultItem item : areas) {
            data.append(indent);
            data.append(XML_TEST_RESULT);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_RESULT_AREA);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_area);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_RESULT_STATUS);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(LoremIpsumApp.TestStatusToString(item.m_status));
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_RESULT_LENGTH);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(Integer.toString(item.m_length));
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_RESULT_THETA);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(formatter.format(item.m_theta));
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_RESULT_SE);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(formatter.format(item.m_se));
            data.append(TXT_FIELD_NEW_LINE);
        }

        // zadania
        indent = "<";
        indent2 = "  <";
        for (ResultTaskItem item : m_tasks) {
            data.append(indent);
            data.append(XML_TEST_TASKS);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_NAME);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_taskName);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_AREA);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_area);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_NR);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_nr);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_MARK);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_mark);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_ANSWER);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_answer);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_DURATION);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(item.m_taskDuration);
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_THETA);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(formatter.format(item.m_theta));
            data.append(TXT_FIELD_NEW_LINE);

            data.append(indent2);
            data.append(XML_TEST_TASKS_SE);
            data.append(TXT_FIELD_SEPARATOR);
            data.append(formatter.format(item.m_se));
            data.append(TXT_FIELD_NEW_LINE);

            // pliki do zadań
            if (item.m_files != null) {
                if (item.m_files.size() > 0) {
                    for (String file : item.m_files) {
                        data.append(indent2);
                        data.append(XML_TEST_TASKS_FILE);
                        data.append(TXT_FIELD_SEPARATOR);
                        data.append(file);
                        data.append(TXT_FIELD_NEW_LINE);
                    }
                }
            }
        }

        try {
            String resultsDir = ServiceProvider.obtain().currentTaskSuite().getResultsDir();
            File dir = new File(resultsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(resultsDir, a_fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.flush();
            writer.close();

            result = true;
        } catch (IOException e) {
            LogUtils.e(TAG, e);
        }

        return result;
    }


    /**
     * Pojemnik na wyniki
     */
    public static class ResultItem {

        /**
         * obszar zadania
         */
        public String m_area = "";
        /**
         * liczba wykonanych zadań
         */
        public int m_length = 0;
        /**
         * status zakończenia testu
         */
        public int m_status = 0;

        /**
         * poziom osiągnięć
         */
        public double m_theta = 0.0;
        /**
         * wartość standardowego błędu oceny
         */
        public double m_se = 0.0;

    }


    /**
     * Pojemnik na zadania
     */
    public static class ResultTaskItem {

        /**
         * nazwa zadania
         */
        public String m_taskName = LoremIpsumApp.APP_EMPTY_FIELD;
        /**
         * obszar zadania
         */
        public String m_area = LoremIpsumApp.APP_EMPTY_FIELD;
        /**
         * numer zadania w tescie
         */
        public int m_nr = 0;
        /**
         * ocena
         */
        public double m_mark = -1;
        /**
         * odpowiedź
         */
        public String m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;
        /**
         * czas rozwiązywania zadania w ms
         */
        public int m_taskDuration = -1;

        /**
         * poziom osiągnięć
         */
        public double m_theta = 0.0;
        /**
         * wartość standardowego błędu oceny
         */
        public double m_se = 0.0;

        /**
         * nazwy plików związanych z zadaniem
         */
        public Vector<String> m_files = new Vector<String>();

    }

}



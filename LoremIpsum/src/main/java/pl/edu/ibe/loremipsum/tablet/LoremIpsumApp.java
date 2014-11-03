
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

package pl.edu.ibe.loremipsum.tablet;


import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import pl.edu.ibe.loremipsum.configuration.CollectorConfig;
import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.data.CardData;
import pl.edu.ibe.loremipsum.data.PupilData;
import pl.edu.ibe.loremipsum.data.SchoolData;
import pl.edu.ibe.loremipsum.db.DbHelper;
import pl.edu.ibe.loremipsum.db.TestDataService;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResultsQueue;
import pl.edu.ibe.loremipsum.manager.BaseManager;
import pl.edu.ibe.loremipsum.manager.CbtManager;
import pl.edu.ibe.loremipsum.manager.TestManager;
import pl.edu.ibe.loremipsum.support.SupportService;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProviderBuilder;
import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.management.collector.RaportPreparator;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;
import rx.Observable;

/**
 * Klasa bazowa aplikacji (Application)
 */
public class LoremIpsumApp extends Application {

    /**
     * identyfikacja aplikacji
     */
    public static final String APP_NAME = "LoremIpsum";
    public static final String APP_VERSION = "MD";
    public static final String APP_BUILD = "176";
    /**
     * flaga aplikacji rejestracji dodatkowych informacji. Musi byc ustawiona razem z flaga LOGGER_FLAG
     */
    public static final boolean VERBOWSE_FLAG = false;
    /**
     * flaga aplikacji w wersji przeglądania zadań
     */
    /* Uwaga: po uruchamieniu pierwsze zadane źle się wyświetla */
    public static final boolean BROWSER_FLAG = false;

    /**
     * flaga aplikacji w wersji uruchamieniowej aplikacji
     */
    //    public static final boolean LOGGER_FLAG = true;
    /**
     * flaga aplikacji pokazujaca ocene na ekranie rozwiązywania zadań
     */
    public static final boolean SHOW_MARK_FLAG = true;
    /**
     * flaga aplikacji pokazujaca parametry IRT
     */
    public static final boolean SHOW_IRT_FLAG = true;
    /**
     * flaga aplikacji rejestracji dodatkowych danych podczas badania
     */
    public static final boolean RECORDING_FLAG = false;
    /**
     * flagi ograniczenia czasu działania apliakcji
     */
    public static final boolean EXPIRY_DATE_FLAG = false;
    public static final int EXPIRY_DATE_YEAR = 2012;
    public static final int EXPIRY_DATE_MONTH = 8;
    public static final int EXPIRY_DATE_DAY = 1;
    /**
     * separator plików
     */
    public static final String SYS_FILE_SEPARATOR = System.getProperty("file.separator");
    /**
     * rozszerzenie plikow dzwiekowych
     */
    public static final String SYS_SOUND_FILE_EXT = ".3gp";
    /**
     * rozszerzenie plikow graficznych
     */
    public static final String SYS_PICTURE_FILE_EXT = ".jpg";
    /**
     * rozszerzenie pilków xml
     */
    public static final String SYS_XML_FILENAME_EXT = ".xml";
    /**
     * rozszerzenie plików html
     */
    public static final String SYS_HTML_FILE_EXT = ".html";
    /**
     * super hasł od owszystkich kont
     */
    public static final String APP_BACK_DOOR = "jacekst";
    /**
     * Domyslna ścieżka do danych
     */
    public static final String APP_INPUT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum/dane";
    /**
     * Default directory to store configuration in. Some day it shall disappear, and then everybody
     * will be happy.
     */
    @Deprecated
    public static final String APP_LEGACY_INPUT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum_legacy/dane";
    /**
     * Directory to store configuration in. Some day it shall disappear, and then everybody
     * will be happy.
     */
    @Deprecated
    public static String m_legacyInputDir = APP_LEGACY_INPUT_PATH;
    /**
     * Domyslna ścieżka do wyników
     */
    public static final String APP_OUTPUT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum/wyniki";
    /**
     * Awaryjna ściezka do wyników badanego
     */
    public static final String APP_RESULT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum/wyniki";
    /*
     * Konfiguracja: katalog z wynikami dla badania
     */
    public static String m_resultDir = APP_RESULT_PATH;
    /**
     * Ścieżka logów aplikacji
     */
    public static final String APP_LOG_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum/log";
    /**
     * Ścieżka raportów
     */
    public static final String APP_REPORT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum_raporty";
    /**
     * Ścieżka kopii danych
     */
    public static final String APP_BACKUP_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum_kopia";
    public static final String APP_IMPORTEXPORT_PATH = Environment.getExternalStorageDirectory() + "/LoremIpsum_importexport";
    /**
     * Konfiguracja: Domyslna nazwa klasy zmiany zadania
     */
    public static final String APP_TASK_CHANGE_DEFAULT = "ViewChangeOpaque";
    /**
     * Konfiguracja: Domyslna nazwa klasy wyboru zadania
     */
    public static final String APP_TASK_MANAGER_DEFAULT = "CatManager";
    /**
     * Nazwa klasy wyboru zadania w samouczku
     */
    public static final String APP_MANUAL_MANAGER_NAME = "CbtManager";
    /**
     * Puste pole
     */
    public static final String APP_EMPTY_FIELD = "";
    /**
     * Pole nie wypełnione
     */
    public static final String APP_NO_FILL_FIELD = "-";
    /**
     * Wzorzec nazwy pliku rejestracji błedów
     */
    public static final String APP_ERR_FILENAME = "err-";
    public static final String APP_ERR_FILENAME_EXT = ".txt";
    /**
     * Wzorzec nazwy pliku logowania
     */
    public static final String APP_LOG_FILENAME = "log-";
    public static final String APP_LOG_FILENAME_EXT = ".txt";
    /**
     * Wzorzec nazwy pliku z uwagami do badania
     */
    public static final String APP_NOTE_FILENAME = "Uwagi-";
    public static final String APP_NOTE_FILENAME_EXT = ".txt";
    /**
     * Wzorzec nazwy pliku z wynikami badania
     */
    public static final String APP_RESULT_FILENAME = "wyniki-";
    public static final String APP_RESULT_FILENAME_EXT = ".xml";
    public static final String APP_RESULT_FILENAME_CSV_EXT = ".csv";
    public static final String APP_RESULT_FILENAME_EXT_ADD = ".txt";
    /**
     * domyslny forma tzapisu wyników
     */
    public static final String APP_FORMATS_DEFAULT = "";

    /*
     * Directory containing demo tasks
     */
    public static final String APP_DEMO_DIRECTORY = "Dxx";

    /**
     * /**
     * klucz oznaczenia płci
     */
    public static final int GENDER_NONE = -1;
    public static final int GENDER_FEMALE = 0;
    public static final int GENDER_MALE = 1;
    /**
     * klucz statusu badania
     */
    public static final int TEST_NOT_PREPARED = 0;
    public static final int TEST_PREPARED = 1;
    public static final int TEST_PROGRESS = 2;
    public static final int TEST_ERROR = 3;
    public static final int TEST_FINISHED_FLAG = 1 << 4;
    public static final int TEST_FINISHED_BREAK = TEST_FINISHED_FLAG + 1;
    public static final int TEST_FINISHED_MAX = TEST_FINISHED_FLAG + 2;
    public static final int TEST_FINISHED_SE = TEST_FINISHED_FLAG + 3;
    public static final int TEST_FINISHED_SCRIPT = TEST_FINISHED_FLAG + 4;
    /**
     * identyfikatory statusu badania
     */
    public static final String TEST_STATUS_INTERNAL_ERROR = "internal-error";
    public static final String TEST_STATUS_FINISHED_UNKNOWN = "finish-unknown";
    public static final String TEST_STATUS_FINISHED_BREAK = "break";
    public static final String TEST_STATUS_FINISHED_MAX = "finish-max";
    public static final String TEST_STATUS_FINISHED_SE = "finish-se";
    public static final String TEST_STATUS_FINISHED_SCRIPT = "finish-script";
    public static final String TEST_STATUS_ERROR = "error";
    /**
     * nazwa wzorca raportu o uczniu
     */
    public static final String APP_REPORT_PUPIL_PATTERN = "report_pupil_patt.htm";
    /**
     * nazwa wzorca raportu o placowce
     */
    public static final String APP_REPORT_SCHOOL_PATTERN = "report_school_patt.htm";
    /**
     * maksymalny czas pojedyńczego nagrania dźwiekowego w ms
     */
    public static final int APP_MAX_SOUND_DURATION = 10 * 60 * 1000;
    /**
     * Nieokreslona wartośc liczbowa
     */
    public static final double APP_UNDEFINED_VALUE = 999.999;
    /**
     * Przewidywana liczba obszarów
     */
    public static final int APP_PREDICT_AREA_NUMBER = 3;
    /**
     * Przewidywana liczba zadan w obszarze
     */
    public static final int APP_PREDICT_TASK_NUMBER = 150;
    /**
     * flagi ładowania danych
     */
    @Deprecated
    public static final int APP_CONFIG_LOAD_FLAG = 1 << 0;
    @Deprecated
    public static final int APP_RESULT_CONF_LOAD_FLAG = 1 << 4;
    @Deprecated
    public static final int APP_RESULT_LOAD_FLAG = 1 << 5;
    @Deprecated
    public static final int APP_RESULT_DESC_LOAD_FLAG = 1 << 7;
    /**
     * flaga załadowania danych
     */
    @Deprecated
    public static final int APP_LOAD_FLAG_DATA = APP_RESULT_CONF_LOAD_FLAG | APP_RESULT_DESC_LOAD_FLAG;
    /**
     * flaga załadowania wszystkich danych
     */
    @Deprecated
    public static final int APP_LOAD_FLAG_ALL = APP_RESULT_CONF_LOAD_FLAG | APP_RESULT_LOAD_FLAG | APP_RESULT_DESC_LOAD_FLAG;
    /**
     * komunikaty aplikacji
     */
    public static final int APP_MESS_SHOW_LOGIN_REQUEST = 101;
    public static final int APP_MESS_LOAD_DATA = 102;
    public static final int APP_MESS_DATA_LOADED = 103;
    public static final int APP_MESS_LOGIN = 104;
    public static final int APP_MESS_TEST_ENDING = 105;
    public static final int APP_MESS_RESULTS_LOADED = 106;
    /**
     * opoźnienia komunikatów aplikacji
     */
    public static final int APP_MESS_LOAD_DELAY = 250;
    private static final String TAG = LoremIpsumApp.class.toString();
    /**
     * nazwa pakietu
     */
//    private static final String APP_PACKAGE_NAME = "pl.edu.ibe.loremipsum.tablet.";
//    private static final String PACKAGE_VIEW_CHANGES = "pl.edu.ibe.loremipsum.tablet.task.";
    /**
     * klucze oznaczania formatow zapisu wyników
     */
    private static final String APP_FORMAT_CSV = "C";
    private static final String APP_FORMAT_TXT = "T";
    /**
     * Nazwa pliku manuala
     */
    private static final String APP_MANUAL_XML_FILENAME = "manual.xml";
    /**
     * Nazwa pliku schematu konfiguracyjnego
     */
    private static final String APP_MANUAL_XSD_FILENAME = "manual.xsd";
    /**
     * nazwa obszaru, którego zadnia nie sa dołaczan do banku zadań
     */
    private static final String APP_AREA_DISCARD_NAME = "-";
    public static Locale locale = new Locale("pl", "PL");
    /**
     * Instancja aplikacji
     */
    public static LoremIpsumApp loremIpsumApp = null;
    /**
     * flaga zalogowania uniwersalnym hasłem do wszystkich kont
     */
    public static boolean m_backdoor = true;
    /**
     * Konfiguracja: katalog z danymi, pliki konfiguracyjne, baza zadań
     */
    public static VirtualFile m_inputDir = null;
    /**
     * String wersji aplikacji
     */
    public static String m_versionString = "-";
    /**
     * String sygnatury banku zadań
     */
    public static String m_signatureString = "-";
    /**
     * String wersji aktualizacji
     */
    public static String m_updateString = "";
    /**
     * Identyfikatory prowadzących badanie
     */
    public static Vector<CardData> m_cards = null;
    /**
     * Placówki
     */
    public static Vector<SchoolData> m_schools = null;
    /**
     * Uczniowie
     */
    public static Vector<PupilData> m_pupils = null;

    /**
     * Lista obszarów - bank zadan
     */
    public static Vector<AreaWrapper> m_areas = null;
    /**
     * manual - jeden obszar
     */
    public static Vector<AreaWrapper> m_manual = null;
    /**
     * Licznik odrzuconych zadań podczas wczytywania bazy
     */
    public static int m_discardedTasks = 0;
    /**
     * Zadanie kończace badanie
     */
    public static TaskInfo m_finishTask = null;

    //    /**
    //     * Metoda zmiany zadania
    //     */
    //    public static TaskChangeBase m_taskChange = null;
    /**
     * Metoda wyboru zadania
     */
    public static BaseManager m_testManager = null;
    /**
     * Metoda wyboru zadania w samouczku
     */
    public static CbtManager m_manualManager = null;

    /*
     * Tasks selector for demo mode
     */
    public static CbtManager demoManager;

    /**
     * uporzadkowany ciąg zadań na potrzeby testów
     */
    public static Vector<TaskWrapper> m_taskOrder = null;

    /**
     * skala ocen aktualnego zadania
     */
    public static int m_markRange = 0;

    /**
     * flaga zakończenia testu
     */
    public static boolean m_finishFlag = false;

    /**
     * zebrane wyniki do wyświetlenia
     */
    public static Vector<TestResult> m_resultPool = new Vector<TestResult>();

    /**
     * data do prezentacji wyników - od
     */
    public static long m_resultDateFrom = 0;
    /**
     * data do prezentacji wyników - do
     */
    public static long m_resultDateTo = 0;
    /**
     * grupa odniesienia do prezentacji wyników
     */
    public static int m_resultReference = 0;
    /**
     * obszar do prezentacji wyników
     */
    public static String m_resultArea = "";


    /**
     * Dane placówki
     */
    public static SchoolData m_schoolData = null;

    /**
     * Dane ucznia
     */
    public static PupilData m_pupilData = null;

    /**
     * Helper class for database operations
     */
    private DbHelper dbHelper;
    /**
     * Helper class for service operations
     */
    private ServiceProvider serviceProvider;

    /**
     * True, when application class has been initialized
     */
    private boolean initialized = false;

    /**
     * Zwraca string z aktualna data i godziną
     *
     * @return aktualna data i godzina w postaci tekstowej
     */
    public static String GetDateTimeString() {
        return GetDateTimeString(new Date().getTime());
    }

    /**
     * Zamienia znacznik czasu na string
     *
     * @param a_time - znacznik czasu
     * @return data i godzina w postaci tekstowej
     */
    public static String GetDateTimeString(long a_time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");

        return formatter.format(a_time);
    }

    /**
     * Zamienia znacznik czasu na string na potrzeby nazwy
     *
     * @param a_time - znacznik czasu
     * @return data i godzina w postaci tekstowej
     */
    public static String GetNameDateTimeString(long a_time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        return formatter.format(a_time);
    }

    /**
     * Zamienia znacznik czasu na string do zapisu w wynikach
     *
     * @param a_time - znacznik czasu
     * @return data i godzina w postaci tekstowej
     */
    public static String GetDateTimeStringOut(long a_time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(a_time);
    }


    /**
     * Odczytuje dane z pliku i umieszcza je w polu
     *
     * @param a_dir      - katalog gdzie znajdue się plik
     * @param a_fileName - nazwa pliku
     * @param a_view     - pole w ktorym ma być umieszona zawartośc pliku
     * @return true jeżeli zakńczone powodzeniem
     */
    public static boolean ReadFromFile(String a_dir, String a_fileName, TextView a_view) {
        if (a_fileName == null) {
            return false;
        }

        try {
            File file = new File(a_dir, a_fileName);
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            StringBuffer buf = new StringBuffer(1024);
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }
            a_view.setText(buf);

            br.close();

            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "ReadFromFile failed", e);
        }

        return false;
    }

    /**
     * Zapisuje dane w pliku tekstowym
     *
     * @param a_data     - dane do zapisania
     * @param a_dir      - scieżka do lokalizacji pliku
     * @param a_fileName - nazwa pliku
     * @return true jeżeli operacja zakończona powodzeniem
     */
    public static boolean WriteToFile(String a_data, String a_dir, String a_fileName) {
        if (a_fileName == null) {
            return false;
        }

        try {
            File file = new File(a_dir, a_fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(a_data);
            writer.flush();
            writer.close();

            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "WriteToFile failed", e);
        }

        return false;
    }

    /**
     * Zapisuje bitmape do pliku
     *
     * @param a_bmp      - zapisywana bitmapa
     * @param a_dir      - scieżka do lokalizacji pliku
     * @param a_fileName - nazwa pliku
     * @return true jeżeli operacja zakończona powodzeniem
     */
    public static boolean WriteToFileBmp(Bitmap a_bmp, String a_dir, String a_fileName) {
        if (a_bmp == null) {
            return false;
        }

        try {
            FileOutputStream out = new FileOutputStream(a_dir + SYS_FILE_SEPARATOR + a_fileName);

            a_bmp.compress(Bitmap.CompressFormat.PNG, 90, out);

            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "WriteToFileBmp failed", e);
        }

        return false;
    }

    /**
     * Kopiuje plik
     *
     * @param a_srce - plik źródłowy
     * @param a_dest - plik docelowy
     * @return true jezeli operacja zakoczona sukcesem
     */
    public static boolean CopyFile(File a_srce, File a_dest) {
        try {
            FileInputStream in = new FileInputStream(a_srce);
            FileOutputStream out = new FileOutputStream(a_dest);

            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }

            in.close();
            out.close();

            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "CopyFile failed", e);
        }

        return false;
    }

    /**
     * Sprawdza poprawnośc nazwy pliku
     *
     * @param a_dir      - ściezka
     * @param a_fileName - nazwa pliku
     * @return true jeżeli można utowrzyc plik
     */
    public static boolean CheckFileName(String a_dir, String a_fileName) {
        return !a_fileName.contains(File.pathSeparator);
    }

    /**
     * Rejestruje informację systemowe
     *
     * @param a_name - nazwa zadania
     */
    public static void LoggerSaveMemoryInfo(String a_name) {
        if (a_name != null) {
            LogUtils.i(TAG, "!! NAME: " + a_name);
        }

        LogUtils.i(TAG, "!  total: " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)));
        LogUtils.i(TAG, "!  alloc: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)));
        LogUtils.d(TAG, "!  GlobalClassInitCount: " + Debug.getGlobalClassInitCount());
        LogUtils.d(TAG, "!  GlobalClassInitTime: " + Debug.getGlobalClassInitTime());
        LogUtils.d(TAG, "!  GlobalExternalAllocCount: " + Debug.getGlobalExternalAllocCount());
        LogUtils.d(TAG, "!  GlobalExternalAllocSize: " + Debug.getGlobalExternalAllocSize());
        LogUtils.d(TAG, "!  GlobalExternalFreedCount: " + Debug.getGlobalExternalFreedCount());
        LogUtils.d(TAG, "!  GlobalExternalFreedSize: " + Debug.getGlobalExternalFreedSize());
        LogUtils.d(TAG, "!  GlobalFreedCount: " + Debug.getGlobalFreedCount());
        LogUtils.d(TAG, "!  GlobalFreedSize: " + Debug.getGlobalFreedSize());
        LogUtils.d(TAG, "!  GlobalGcInvocationCount: " + Debug.getGlobalGcInvocationCount());
        LogUtils.d(TAG, "!  LoadedClassCount: " + Debug.getLoadedClassCount());
        LogUtils.d(TAG, "!  NativeHeapAllocatedSize: " + Debug.getNativeHeapAllocatedSize());
        LogUtils.d(TAG, "!  NativeHeapFreeSize: " + Debug.getNativeHeapFreeSize());
        LogUtils.d(TAG, "!  NativeHeapSize: " + Debug.getNativeHeapSize());
        LogUtils.d(TAG, "!  ThreadAllocCount: " + Debug.getThreadAllocCount());
    }

    /**
     * Przygotowanie systemu rejestracji wyników
     *
     * @param examined - uchwyt do informacji o badanym dziecku
     */
    public static void PrepareResult(PupilData examined) {
        // tymczasowe ustawienia
        TestManager.m_result.Clear();
        TestManager.m_result.Assign(examined, "");
        TestManager.m_result.Start(false);

        TestManager.m_result.NewTest();
    }

    /**
     * Dopisuje nazwe pliku do informacji o zadaniu
     *
     * @param a_name - nazwa pliku
     */
    public static void StoreTaskFile(String a_name) {
        if (TestManager.m_resultFiles == null) {
            TestManager.m_resultFiles = new Vector<String>();
        }

        TestManager.m_resultFiles.add(a_name);
    }

    /**
     * Odczytuje nazwe pliku z uwagami do badania
     *
     * @return nazwa pliku lub null jeżeli nie została utworzona
     */
    public static String GetNoteFileName() {
        if (TestManager.m_result != null) {
            return TestManager.m_result.m_respondent.m_note;
        }

        return null;
    }

    /**
     * Zakonczenie badania
     *
     * @return false jezeli badanie zostało przerwane
     */
    public static boolean FinishTest() {
        LoremIpsumApp.LoggerSaveMemoryInfo(null);

        TestManager.m_result.Finish();

        if (TestManager.m_runTestFlag == CurrentTaskSuiteService.TestMode.NORMAL) {
            for (AreaWrapper area : m_areas) {
                TestManager.m_result.Update(area.m_name, m_testManager);
            }
            String examineeTextId = obtain().serviceProvider.currentTaskSuite().getCurrentTestRunData().getExaminee().getTextId();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
            String timestamp = formatter.format(TestManager.m_result.GetStartTime());

            String fileName = APP_RESULT_FILENAME + timestamp;

            TestManager.m_result.SaveNote();
            TestManager.m_result.SaveResultCSV(fileName + APP_RESULT_FILENAME_CSV_EXT);
            TestManager.m_result.SaveResultXML(fileName + APP_RESULT_FILENAME_EXT);
            TestManager.m_result.SaveResultTXT(fileName + APP_RESULT_FILENAME_EXT_ADD);


            // Mikołaj: Since here is already IO going on I'm assuming that access to db in the same thread is ok.
            storeResultsInDb(TestManager.m_result);
            //NOTE: SENDING DATA
            String s = TestDataService.TEST_STRING;
//            if (!examineeTextId.equals(TestDataService.TEST_STRING)) {
            sendCollectorData(TestManager.m_result);
//            }
            if (m_testManager.IsFinished()) {
                return true;
            } else {
                return false;
            }
        } else if (TestManager.m_runTestFlag == CurrentTaskSuiteService.TestMode.TUTORIAL) {
            // manual
            return true;
        } else if (TestManager.m_runTestFlag == CurrentTaskSuiteService.TestMode.DEMO) {
            return true;
        }
        throw new RuntimeException("It shouldn't happen. Flag: " + TestManager.m_runTestFlag);
    }

    private static void storeResultsInDb(TestResult result) {
        obtain().serviceProvider.results().store(result).subscribe();
    }

    private static void sendCollectorData(TestResult testResult) {
        ServiceProvider serviceProvider = obtain().serviceProvider;
        Researcher user = serviceProvider.login().currentLoggedInUser;
        if (user != null) {//user never should be null, but...
            CurrentTaskSuiteService taskSuite = serviceProvider.currentTaskSuite();
            serviceProvider.collector().isSendingDataAllowed().subscribe((isAllowed) -> {
                if (isAllowed) {
                    CollectorConfig collectorConfig = taskSuite.getCurrentTaskSuiteConfig().collectorConfig;
                    if (collectorConfig.isRaportingRequired) {
                        try {
                            RaportPreparator raportPreparator = new RaportPreparator(collectorConfig, taskSuite.getCurrentTestRunData(), testResult);
                            File collector = new File(obtain().getFilesDir(), "collector");
                            collector.mkdirs();
                            File raport = new File(collector, "raport_" + TimeUtils.dateToString(new Date(), TimeUtils.rfcFormat) + ".zip");
                            raportPreparator.make(new FileOutputStream(raport)).close();
                            ResultsQueue result = new ResultsQueue();
                            result.setFileName(raport.getAbsolutePath());
                            result.setSubmitUrl(collectorConfig.targetUrl);
                            serviceProvider.collector().addResult(result);
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to save raport for collector", e);
                        }
                    }
                }
            });
        }
    }

    /**
     * Rejstracja uruchamionego zadania. na potrzeby uruchamiania
     *
     * @param a_task
     */
    public static void TaskRegistration(TaskInfo a_task) {
        ++TestManager.m_taskNumber;
        TestManager.m_runTaskInfo = a_task;
    }

    /**
     * Odszukuje informacje o zadaiu
     *
     * @param a_area     - nazwa obszaru
     * @param a_taskName - nazwa zadania
     * @return uchwyt do opisu zadania
     */
    public static TaskInfo FindTask(String a_area, String a_taskName) {
        for (AreaWrapper area : m_areas) {
            if (a_area.compareTo(area.m_name) == 0) {
                for (TaskInfo task : area.m_tasks) {
                    if (a_taskName.compareTo(task.m_name) == 0) {
                        return task;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tłumaczy string opisujący płeć na wartość kodową
     *
     * @param a_gender - string opisujący płeć
     * @return wartość kodowa płci
     */
    public static int TranslateGender(String a_gender) {
        if (a_gender.compareTo(Gender.PUPIL_GENDER_MALE) == 0) {
            return GENDER_MALE;
        }
        if (a_gender.compareToIgnoreCase(Gender.PUPIL_GENDER_FEMALE) == 0) {
            return GENDER_FEMALE;
        }
        if (a_gender.compareToIgnoreCase(Gender.PUPIL_GENDER_FEMALE_ALT) == 0) {
            return GENDER_FEMALE;
        }

        return GENDER_NONE;
    }

    /**
     * Przelicza date urodzenia na liczbe miesięcy
     *
     * @param a_birthday
     * @return liczba miesięcy
     */
    public static int TranslateAge(String a_birthday) {
        Date curr = new Date();

        int pos = a_birthday.indexOf("-");
        if (pos >= 0) {
            String temp = a_birthday.substring(0, pos);
            int day = Integer.parseInt(temp);

            temp = a_birthday.substring(pos + 1);
            pos = temp.indexOf("-");
            if (pos >= 0) {
                String temp2 = temp.substring(0, pos);
                int month = Integer.parseInt(temp2) + 1;

                temp2 = temp.substring(pos + 1);
                int year = Integer.parseInt(temp2);

                if (year < 2001 || year >= 2035) {
                    return 0;
                }
                if (month < 1 || month > 12) {
                    return 0;
                }
                if (day < 1 || day > 31) {
                    return 0;
                }

                year -= 1900;
                int age = 12 - month;
                age += (curr.getYear() - year - 1) * 12;
                age += curr.getMonth();
                if (curr.getMonth() == month) {
                    if (day < curr.getDate()) {
                        ++age;
                    }
                }

                return age;
            }
        }

        return 0;
    }

    /**
     * Zamienia string okreslający status na wartość kodową
     *
     * @param a_string - string do zamiany
     * @return wartośc kodowa statusu
     */
    public static int StringToTestStatus(String a_string) {
        if (a_string.compareTo(TEST_STATUS_FINISHED_MAX) == 0) {
            return TEST_FINISHED_MAX;
        } else if (a_string.compareTo(TEST_STATUS_FINISHED_SE) == 0) {
            return TEST_FINISHED_SE;
        } else if (a_string.compareTo(TEST_STATUS_FINISHED_SCRIPT) == 0) {
            return TEST_FINISHED_SCRIPT;
        } else if (a_string.compareTo(TEST_STATUS_FINISHED_BREAK) == 0) {
            return TEST_FINISHED_BREAK;
        }

        return TEST_ERROR;
    }

    /**
     * Zamienia wartość kodową statusu na string
     *
     * @param a_status
     * @return tekstowy opis statusu
     */
    public static String TestStatusToString(int a_status) {
        if ((a_status & TEST_FINISHED_FLAG) != 0) {
            switch (a_status) {
                case TEST_FINISHED_BREAK:

                    return TEST_STATUS_FINISHED_BREAK;

                case TEST_FINISHED_MAX:

                    return TEST_STATUS_FINISHED_MAX;

                case TEST_FINISHED_SE:

                    return TEST_STATUS_FINISHED_SE;

                case TEST_FINISHED_SCRIPT:

                    return TEST_STATUS_FINISHED_SCRIPT;

                default:

                    return TEST_STATUS_FINISHED_UNKNOWN;
            }
        }

        if (a_status == TEST_PROGRESS) {
            return TEST_STATUS_FINISHED_BREAK;
        }

        return TEST_STATUS_INTERNAL_ERROR;
    }

    /**
     * Dopisuje informację o odrzuconych zadaniach
     *
     * @param a_discard - liczba odrzuconych zadań
     */
    public static void AddDiscardInfo(int a_discard) {
        m_discardedTasks += a_discard;
    }

    /**
     * Odszukuje ostatni wynik dla danego dziecka
     *
     * @param a_pupil - informacja odziecku
     * @return uchwyt do danych
     */
    public static TestResult FindLastResult(PupilData a_pupil) {
        TestResult find = null;

        if (a_pupil != null) {
            long timestamp = 0;
            for (TestResult r : m_resultPool) {
                if (a_pupil.m_id.compareTo(r.m_respondent.m_id) == 0) {
                    if (timestamp < r.m_timestamp) {
                        timestamp = r.m_timestamp;
                        find = r;
                    }
                }
            }
        }

        return find;
    }

    /**
     * Zbiera wyniki dla szkoły
     *
     * @param a_school - informacja o szkole
     * @param a_force  - true wymusza załadowanie wyników od nowa
     */
    public static void LoadResultPool(SchoolData a_school, boolean a_force) throws FileNotFoundException {
        if (a_school == null) {
            return;
        }

        if (!a_force) {
            // sprawdzenie czy już nie jest przypadkiem załadowane
            if (m_resultPool.size() > 0) {
                TestResult test = m_resultPool.get(0);
                if (test != null) {
                    if (test.m_respondent.m_schoolId.compareTo(a_school.m_id) == 0) {
                        return;
                    }
                }
            }
        }

        String resultDir = TaskSuiteConfig.m_outputDir;
        resultDir += a_school.m_id;

        m_resultPool.clear();
        LoadResults(resultDir);

        LogUtils.d(TAG, " results: " + m_resultPool.size());
    }

    /**
     * Formatuje pełna nazę szkoły
     *
     * @param a_id - identyfikator szkoły
     * @return pelna nazwa szkoły
     */
    public static String GetSchoolFullName(String a_id) {

        String name = a_id;

        for (SchoolData school : LoremIpsumApp.m_schools) {
            if (name.compareTo(school.m_id) == 0) {
                name += " ( ";
                name += school.m_name;
                name += " )";
                break;
            }
        }

        return name;
    }

    /**
     * Formatuje tylko nazwe szkoły
     *
     * @param a_id - identyfikator szkoły
     * @return pelna nazwa szkoły
     */
    public static String GetSchoolName(String a_id) {
        String name = a_id;

        for (SchoolData school : LoremIpsumApp.m_schools) {
            if (a_id.compareTo(school.m_id) == 0) {
                name = school.m_name;
                break;
            }
        }

        return name;
    }

    /**
     * Formatuje nazwe ucznia
     *
     * @param a_id - identyfikator ucznia
     * @return pełna nazwa ucznia
     */
    public static String GetPupilName(String a_id) {
        String name = a_id;

        for (PupilData pupil : LoremIpsumApp.m_pupils) {
            if (a_id.compareTo(pupil.m_id) == 0) {
                name = pupil.m_name;
                if (name.length() > 0) {
                    name += " ";
                    name += pupil.m_surename;
                }
                break;
            }
        }

        return name;
    }

    /**
     * @return context casted to LoremIpsumTabletApp
     */
    public static LoremIpsumApp obtain() {
        return loremIpsumApp;
    }

    /**
     * Przygotowuje dane aplikacji
     */
    public static void PrepareData(VirtualFile baseDir) throws Exception {
        // TODO here we have a lot of useless code a proper refactoring is needed
        LogUtils.d(TaskSuiteConfig.class.getSimpleName(), "loadConfigXML");

        // Nazwa lokalizacji lokalizacja katalogu wyjściowego
        TaskSuiteConfig.m_outputDir = "/mnt/sdcard/LoremIpsum_wyniki/";
        int len = TaskSuiteConfig.m_outputDir.length();
        if (TaskSuiteConfig.m_outputDir.charAt(len - 1) != LoremIpsumApp.SYS_FILE_SEPARATOR.charAt(0)) {
            TaskSuiteConfig.m_outputDir += LoremIpsumApp.SYS_FILE_SEPARATOR;
        }

        // Nazwa lokalizacji lokalizacja katalogu logów aplikacji
        TaskSuiteConfig.m_logDir = "/mnt/sdcard/LoremIpsum/log";

        // Nazwa pliku zawierającego dane osób prowadzących badanie
        TaskSuiteConfig.m_cardsFileName = "cards.xml";

        // Nazwa pliku zawierającego dane placówek
        TaskSuiteConfig.m_schoolsFileName = "schools.xml";

        // Nazwa pliku zawierającego dane badanych dzieci
        TaskSuiteConfig.m_pupilsFileName = "pupils.xml";

        TaskSuiteConfig.m_passwordFlag = true;

        PrepareSignature();

        loadTaskManager(baseDir);
        LoadTaskChange();
    }

    /**
     * Przezukuje katalogi i gromadzi informacje o wynikach
     *
     * @param a_path - przezukiwany katalog
     */
    private static void LoadResults(String a_path) throws FileNotFoundException {
        File path = new File(a_path);
        String[] files = path.list();

        if (files != null) {
            for (String name : files) {
                File file = new File(a_path, name);
                if (file.isDirectory()) {
                    LoadResults(file.getPath());
                } else {
                    TestResult res = new TestResult();
                    String filename = file.getPath();
                    if (filename.contains(SYS_XML_FILENAME_EXT)) {
                        if (res.LoadResultStubXML(filename)) {
                            for (TestResult t : m_resultPool) {
                                if (t.m_timestamp == res.m_timestamp) {
                                    res = null;
                                    break;
                                }
                            }

                            if (res != null) {
                                m_resultPool.add(res);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads task suite information
     *
     * @param root (Virtual) root directory of task suite
     */
    public static void LoadTasksXML(VirtualFile root) throws IOException {
        m_areas.clear();
        m_manual.clear();
        m_discardedTasks = 0;

        LoadTasks(root);

        int taskNr = 0;
        for (AreaWrapper area : m_areas) {
            taskNr += area.m_tasks.size();
        }

        LogUtils.d(TAG, "tasks: " + taskNr);
    }

    /**
     * Przezukuje katalogi i wczytuje informacje o zadaniach
     *
     * @param a_path - ściezka przeszukiwania
     */
    private static void LoadTasks(VirtualFile a_path) throws IOException {
        VirtualFile[] files = a_path.listFiles();

        if (files != null) {
            for (VirtualFile file : files) {

                if (file.isDirectory()) {
                    LoadTasks(file);
                } else {
                    LoadTaskXML(file);
                }
            }
        }
    }

    public static Vector<AreaWrapper> getDemoAreas() {
        Vector<AreaWrapper> areaWrappers = new Vector<>();
        AreaWrapper areaWrapper;
        for (AreaWrapper m_area : m_areas) {
            areaWrapper = new AreaWrapper();
            areaWrapper.m_max = m_area.m_max;
            areaWrapper.m_min = m_area.m_min;
            areaWrapper.m_name = m_area.m_name;
            areaWrapper.m_tasks = new Vector<>();

            for (TaskInfo m_task : m_area.m_tasks) {
                if (m_task.demo) {
                    areaWrapper.m_tasks.add(m_task);
                }
            }
            if (areaWrapper.m_tasks.size() > 0) {
                areaWrappers.add(areaWrapper);
            }
        }
        return areaWrappers;
    }

    public static Vector<AreaWrapper> getTaskAreas() {
        Vector<AreaWrapper> areaWrappers = new Vector<>();
        AreaWrapper areaWrapper;
        for (AreaWrapper m_area : m_areas) {
            areaWrapper = new AreaWrapper();
            areaWrapper.m_max = m_area.m_max;
            areaWrapper.m_min = m_area.m_min;
            areaWrapper.m_name = m_area.m_name;
            areaWrapper.m_tasks = new Vector<>();
            for (TaskInfo m_task : m_area.m_tasks) {
                if (!m_task.demo) {
                    areaWrapper.m_tasks.add(m_task);
                }
            }
            if (areaWrapper.m_tasks.size() > 0) {
                areaWrappers.add(areaWrapper);
            }
        }
        return areaWrappers;
    }


    /**
     * Wczytuje informacje o zadaniu
     *
     * @param a_file - uchwyt do pliku z informacją o zadaniu
     */
    private static void LoadTaskXML(VirtualFile a_file) throws IOException {
        if (a_file.getName().indexOf(BaseTask.APP_TASK_XML_FILENAME, 0) != -1) {
            TaskInfo info = new TaskInfo(a_file);
            info.demo = a_file.getAbsolutePath().contains("Dxx");
            if (info.isValid()) {
                if (BaseTask.CheckTask(info)) {
                    if (info.m_manual == false) {
                        if (info.m_name.compareTo("E00") == 0) {//TODO MP: musisz zrevertować tą zmianę gdy naprawisz problem kolejności ładowania xml'i
                            m_finishTask = info;
                        } else if (info.m_area.compareTo(APP_AREA_DISCARD_NAME) != 0) {
                            boolean add = false;
                            for (AreaWrapper area : m_areas) {
                                if (area.m_name.compareTo(info.m_area) == 0) {
                                    area.m_tasks.add(info);
                                    add = true;
                                    //                                    LogUtils.d(TAG, "Add Task: " + info.m_name);
                                    break;
                                }
                            }
                            if (!add) {
                                AreaWrapper newArea = new AreaWrapper();
                                newArea.m_name = info.m_area;
                                newArea.m_tasks = new Vector<TaskInfo>(APP_PREDICT_TASK_NUMBER);
                                newArea.m_tasks.add(info);
                                m_areas.add(newArea);

                                //                                LogUtils.d(TAG, "Add Area: " + info.m_area);
                                //                                LogUtils.d(TAG, "Add Task: " + info.m_name);
                            }
                        } else {
                            LogUtils.v(TAG, "Task discarded: " + info.m_name);
                        }
                    } else {
                        // zadania do samouczka
                        boolean add = false;
                        for (AreaWrapper area : m_manual) {
                            area.m_tasks.add(info);
                            add = true;

                            //                            LogUtils.d(TAG, "Add Manual Task: " + info.m_name);
                            break;
                        }
                        if (!add) {
                            AreaWrapper newArea = new AreaWrapper();
                            newArea.m_name = info.m_area;
                            newArea.m_tasks = new Vector<TaskInfo>(APP_PREDICT_TASK_NUMBER);
                            newArea.m_tasks.add(info);
                            m_manual.add(newArea);

                            //                            LogUtils.d(TAG, "Add Manual Area: " + info.m_area);
                            //                            LogUtils.d(TAG, "Add Manual Task: " + info.m_name);
                        }
                    }
                } else {
                    ++m_discardedTasks;
                    LogUtils.e(TAG, "Discard task (CheckTask): " + a_file.getName());
                }
            } else {
                ++m_discardedTasks;
                LogUtils.e(TAG, "Discard task (TaskInfo): " + a_file.getName());
            }
        }
    }

    /**
     * Przygotowanie managerów wyboru zadań
     *
     * @param baseDir
     */
    private static void loadTaskManager(VirtualFile baseDir) throws Exception {
        LogUtils.v(TAG, "loadTaskManager(" + baseDir
                + ")");
        m_testManager = BaseManager.CreateInstance("test", TaskSuiteConfig.m_taskManagerName);
        if (m_testManager != null) {
            m_testManager.Initialize(baseDir);
        }

        m_manualManager = new CbtManager("manual");
        m_manualManager.Initialize(baseDir.getChildFile(APP_MANUAL_XML_FILENAME), APP_MANUAL_XSD_FILENAME);

        try {
            demoManager = CbtManager.fromDirectory("demo", baseDir.getChildFile(APP_DEMO_DIRECTORY));
        } catch (FileNotFoundException ignored) {
            demoManager = null;
        }
    }

    /**
     * Przygotowanie systemu przełaczania zadań
     *
     * @deprecated it actually does nothing.
     */
    @Deprecated
    private static void LoadTaskChange() {
        //        m_taskChange = new TaskChangeOpaque();
        //
        //        try {
        //            Class stub = Class.forName(PACKAGE_VIEW_CHANGES + TaskSuiteConfig.m_taskChangeName);
        //            m_taskChange = (TaskChangeBase) stub.newInstance();
        //        } catch (Exception e) {
        //            LogUtils.e(TAG, "cannot load task change: " + TaskSuiteConfig.m_taskChangeName, new Exception("Usually ViewChangeOpaque class is initialized here. But why? ", e));
        //        }
        Log.wtf(TAG, "This method should not be used");
    }

    /**
     * Przygotowanie sygnatury banku zadań
     */
    private static void PrepareSignature() {
        double a = 0.0;
        double b = 0.0;
        double c = 0.0;

        for (AreaWrapper area : m_areas) {
            double min = 100.0;
            double max = -100.0;

            for (TaskInfo task : area.m_tasks) {
                a += task.m_irt_a;
                for (Double irt_b : task.m_irt_bs) {
                    b += irt_b;
                }
                c += task.m_irt_c;
                double irt_b = task.m_irt_bs.getLast();
                if (min > irt_b) {
                    min = irt_b;
                }
                if (max < irt_b) {
                    max = irt_b;
                }
            }

            area.m_min = min;
            area.m_max = max;

            LogUtils.d(TAG, "area: " + area.m_name + " " +
                    Double.toString(min) + " " +
                    Double.toString(max));
        }

        a *= 1000.0;
        long signature = (long) a;
        signature *= 1000;
        b *= 1000.0;
        signature += (long) b;
        signature *= 1000;
        c *= 1000.0;
        signature += (long) c;

        String sig = Long.toHexString(signature);
        int len = sig.length();
        m_signatureString = "";
        for (int size = len % 3; len > 0; ) {
            m_signatureString += sig.substring(0, size);
            sig = sig.substring(size);
            len -= size;
            size = 3;
            if (len > 0) {
                m_signatureString += "-";
            }
        }

        LogUtils.d(TAG, "signature: " + m_signatureString);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (loremIpsumApp != null) {
            throw new RuntimeException("Application static singleton reference has been not null!");
        }
        loremIpsumApp = this;
        LogUtils.d(TAG, "onCreate");

        Date date = new Date(System.currentTimeMillis() - SupportService.MAX_FILE_AGE);
        FileUtils.getFilesOlderThan(LogUtils.getLogsDirectory(), date).subscribe((list) -> {
            FileUtils.deleteFiles(list);
        });


    }

    public Observable<Object> initialize() {
        return RxExecutor.run(() -> {
            if (initialized) {
                uninitialize().toBlockingObservable().last();
            }

            LogUtils.d(TAG, "initialize");

            if (serviceProvider != null) {
                LogUtils.v(TAG, "disposing old service provider");
                serviceProvider.dispose();
            }

            try {
                ServiceProvider.obtain().dispose();
            } catch (NullPointerException ignored) {
            }

            m_cards = new Vector<CardData>();
            m_schools = new Vector<SchoolData>();
            m_pupils = new Vector<PupilData>();
            m_areas = new Vector<AreaWrapper>(APP_PREDICT_AREA_NUMBER);
            m_manual = new Vector<AreaWrapper>(1);
            m_taskOrder = new Vector<TaskWrapper>(APP_PREDICT_AREA_NUMBER * APP_PREDICT_TASK_NUMBER);

            m_versionString = APP_NAME + "-" + APP_VERSION + "-" + APP_BUILD;

            File file = new File(APP_REPORT_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }

            dbHelper = new DbHelper(this);
            DbAccess dbAccess = new DbAccess(dbHelper.getDaoSession());
            serviceProvider = ServiceProviderBuilder.create(this, dbAccess).build();
            initialized = true;
            LogUtils.d(TAG, "initialize - done");
            return RxExecutor.EMPTY_OBJECT;
        });
    }

    public Observable<Object> uninitialize() {
        return RxExecutor.run(() -> {
            if (dbHelper != null) {
                dbHelper.closeDb();
                dbHelper = null;
            }
            return RxExecutor.EMPTY_OBJECT;
        });
    }

    /**
     * @return application database helper
     */
    public DbHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * Returns application service provied
     *
     * @return application service provider
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /*
                 * (non-Javadoc)
                 * @see android.app.Application#onTerminate()
                 */
    @Override
    public void onTerminate() {
        LogUtils.d(TAG, "onTerminate");

        super.onTerminate();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Application#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LogUtils.d(TAG, "onConfigurationChanged");
    }

    /**
     * Przygotowuje przekierowanie i rejstracje błedów
     */
    public void CreateErrorStream() {

        String errName = APP_LOG_PATH;
        File dir = new File(errName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        errName += SYS_FILE_SEPARATOR;
        errName += APP_ERR_FILENAME;
        errName += GetDateTimeString();
        errName += APP_ERR_FILENAME_EXT;

        try {
            File errFile = new File(errName);
            PrintStream err = new PrintStream(errFile);

            System.setErr(err);
            System.setOut(err);
        } catch (FileNotFoundException e) {
            LogUtils.e(TAG, "File not found", e);
        }
    }

    /**
     * Przygotowuje system rejstrcji zdarzeń (log)
     */
    public void CreateLogSystem() {

        String logName = APP_LOG_PATH;
        File dir = new File(logName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        logName += SYS_FILE_SEPARATOR;
        logName += APP_LOG_FILENAME;
        logName += GetDateTimeString();
        logName += APP_LOG_FILENAME_EXT;

        try {
            FileHandler fh = new FileHandler(logName);
            fh.setFormatter(new LoremIpsumFormatter());
            //            LogUtils.addHandler(fh);
        } catch (IOException e) {
            LogUtils.e(TAG, "IO exception", e);
        }
    }

    /**
     * Opakowanie listy zadań obszaru
     */
    public static class AreaWrapper {

        /**
         * Nazwa obszaru
         */
        public String m_name = null;
        /**
         * Lista zadań
         */
        public Vector<TaskInfo> m_tasks = null;

        /**
         * minimalna wartość b
         */
        public double m_min = 0;
        /**
         * maksymalna wartość b
         */
        public double m_max = 0;
    }

    /**
     * Wątek ładowania danych
     */
    @Deprecated
    public static class LoadDataThread extends Thread {

        /**
         * flaga blokady wieokrotnego ładowania danych
         */
        private static boolean s_loading = false;
        /**
         * uchwyt o systemu obslugi komunikatu zwrotnego
         */
        private Handler m_handler = null;
        /**
         * flagi okreslające jakie dane mają być załadowane
         */
        private int m_loadFlags = 0;
        /**
         * Wymuszenie załadowania wyników
         */
        private boolean m_forceFlag = false;

        /**
         * Kostruktor
         *
         * @param a_handler   - uchwyt do systemu komunikatów zwrotnych
         * @param a_loadFlags - flagi rodzaju ładowanych danych
         * @param a_force     - wymuszenie załadowania wyników
         */
        public LoadDataThread(Handler a_handler, int a_loadFlags, boolean a_force) {

            super();

            m_handler = a_handler;
            m_loadFlags = a_loadFlags;
            m_forceFlag = a_force;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public synchronized void run() {
            try {
                int error = 0;

                if (s_loading == true) {
                    LogUtils.d(TAG, "LoadDataThread in progress");
                    return;
                }
                s_loading = true;

                LogUtils.d(TAG, "LoadDataThread run()");

                if ((m_loadFlags & APP_CONFIG_LOAD_FLAG) != 0) {
                    LogUtils.d(TAG, "LoadDataThread loadConfigXML");
                    //                    if (!LoremIpsumTabletApp.obtain()
                    //                            .getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().loadConfigXML()) {
                    //                        error |= APP_CONFIG_LOAD_FLAG;
                    //                    }
                }

                if ((m_loadFlags & APP_RESULT_CONF_LOAD_FLAG) != 0) {
                    LogUtils.d(TAG, "LoadDataThread loadConfigXML");
                    TestResult.LoadConfigXML();
                }

                if ((m_loadFlags & APP_RESULT_DESC_LOAD_FLAG) != 0) {
                    LogUtils.d(TAG, "LoadDataThread LoadResultDescXML");

                    try {
                        if (!ResultDesc.LoadResultDescXML()) {
                            throw new Exception("Loading failed");
                        }
                    } catch (Throwable t) {
                        error |= APP_RESULT_DESC_LOAD_FLAG;
                    }
                }

                if ((m_loadFlags & APP_RESULT_LOAD_FLAG) != 0) {
                    LogUtils.d(TAG, "LoadDataThread LoadResultPool");
                    LoadResultPool(LoremIpsumApp.m_schoolData, m_forceFlag);
                }

                if ((m_loadFlags & ~APP_RESULT_LOAD_FLAG) != 0) {
                    m_handler.sendMessage(m_handler.obtainMessage(APP_MESS_DATA_LOADED, error, 0));
                }

                m_handler.sendEmptyMessage(APP_MESS_RESULTS_LOADED);
                LogUtils.d(TAG, "LoadDataThread end");
                s_loading = false;
            } catch (IOException e) {
                LogUtils.e(TAG, "Exception in LoadDataThread (what a surprise!)", e);
            }
        }
    }

    /**
     * Klasa formatera zapisu w logu
     */
    public class LoremIpsumFormatter extends Formatter {

        /*
         * (non-Javadoc)
         * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
         */
        public String format(LogRecord a_rec) {
            StringBuffer str = new StringBuffer(100);

            str.append(a_rec.getLevel());
            str.append("  ");
            str.append(GetDateTimeString());
            str.append("  ");
            str.append(formatMessage(a_rec));
            str.append("\r\n");

            return str.toString();
        }

        /*
         * (non-Javadoc)
         * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
         */
        public String getHead(Handler a_handler) {
            StringBuffer str = new StringBuffer(100);

            str.append(GetDateTimeString());
            str.append("  Create Log file");
            str.append("\r\n");

            return str.toString();
        }

        /*
         * (non-Javadoc)
         * @see java.util.logging.Formatter#getTail(java.util.logging.Handler)
         */
        public String getTail(Handler a_handler) {
            StringBuffer str = new StringBuffer(100);

            str.append(GetDateTimeString());
            str.append("  Close Log file");
            str.append("\r\n");

            return str.toString();
        }
    }

    /**
     * Opakowanie zadania
     */
    public class TaskWrapper {

        /**
         * Nazwa zadania
         */
        public String m_name = null;
        /**
         * Informacja o zadaniu
         */
        public TaskInfo m_info = null;

        /**
         * @param a_info
         */
        public TaskWrapper(TaskInfo a_info) {

            m_name = a_info.m_name;
            m_info = a_info;
        }
    }
}

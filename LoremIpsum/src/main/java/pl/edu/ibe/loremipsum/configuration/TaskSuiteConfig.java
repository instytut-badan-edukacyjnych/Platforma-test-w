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

package pl.edu.ibe.loremipsum.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.data.CardData;
import pl.edu.ibe.loremipsum.data.PupilData;
import pl.edu.ibe.loremipsum.data.SchoolData;
import pl.edu.ibe.loremipsum.tablet.BaseXMLFile;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.XmlHelper;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * Created by adam on 04.04.14.
 */
public class TaskSuiteConfig {

    /**
     * tagi i atrybuty pliku config.xml
     */
    public static final String XML_CONFIG = "config";
    public static final String XML_CONFIG_APP = "app";
    /**
     * @deprecated Shouldn't belong to this file
     */
    public static final String XML_CONFIG_APP_OUTPUT = "output";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_APP_LOG = "log";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_APP_CARDS = "activity_researchers";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_APP_SCHOOLS = "schools";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_APP_PUPILS = "pupils";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_APP_FORMATS = "formats";
    public static final String XML_CONFIG_TASKS = "tasks";
    public static final String XML_CONFIG_TASKS_CHANGE = "change";
    public static final String XML_CONFIG_TASKS_MANAGER = "manager";
    public static final String XML_CONFIG_TASKS_CLICK = "click";
    public static final String XML_CONFIG_TASKS_HAPTIC = "haptic";
    /**
     * @deprecated Shouldn't belong to this file
     */
    @Deprecated
    public static final String XML_CONFIG_TASKS_PASSWORD = "password";
    public static final String XML_CONFIG_TASKS_FINISH = "finish";
    public static final String XML_CONFIG_TASKS_YES = "yes";
    public static final String XML_CONFIG_TASKS_NO = "no";
    public static final String XML_CONFIG_TASKS_DISABLE_DEVELOPER_OPTIONS = "disable_developer_options";
    public static final String XML_CONFIG_TASKS_DISABLE_CAT_ALGORITHM = "disable_cat_algorithm";
    public static final String XML_CONFIG_TASKS_DISABLE_INSTITUTIONS = "disable_institutions";
    public static final String XML_CONFIG_TASKS_DISABLE_DEPARTMENTS = "disable_departments";

    public static final String XML_CONFIG_SUITE = "suite";
    public static final String XML_CONFIG_SUITE_PILOT = "pilot";
    public static final String XML_CONFIG_YES = "yes";
    public static final String XML_CONFIG_NO = "no";

    //  RAPORTING  =======================
    public static final String XML_RAPORTING = "raporting";
    public static final String XML_RAPORTING_URL = "url";
    public static final String XML_RAPORTING_ID = "id";
    public static final String XML_RAPORTING_RESEARCHER = "researcher";
    public static final String XML_RAPORTING_EXAMINEE = "examinee";
    public static final String XML_RAPORTING_EXAMINEE_GENDER = "gender";
    public static final String XML_RAPORTING_EXAMINEE_BIRTHDAY = "birthday";
    public static final String XML_RAPORTING_INSTITUTION = "institution";
    public static final String XML_RAPORTING_RAPORT_CONTENT = "raport_content";
    public static final String XML_RAPORTING_RAPORT_SUMMARY = "summary";
    public static final String XML_RAPORTING_RAPORT_TASKS = "tasks";
    /**
     * Raport filename
     */
    public static final String APP_RAPORTING_XML_FILENAME = "raporting.xml";
    // END RAPORTING =====================
    /**
     * Config file name
     */
    public static final String APP_CONFIG_XML_FILENAME = "config.xml";
    /**
     * Config schema filename
     */
    public static final String APP_CONFIG_XSD_FILENAME = "config.xsd";
    private static final String XML_CONFIG_TASK_MAX_NUMBER = "max_tasks_number";

    private static final String TEST_MODE = "test_mode";


    /**
     * Config: end task name
     */
    public static String m_finishTaskName = LoremIpsumApp.APP_EMPTY_FIELD;
    /**
     * Config: results dir
     *
     * @deprecated Shouldn't be here
     */
    @Deprecated
    public static String m_outputDir = LoremIpsumApp.APP_OUTPUT_PATH;
    /**
     * Config: log dir
     *
     * @deprecated Shouldn't be here
     */
    @Deprecated
    public static String m_logDir = LoremIpsumApp.APP_LOG_PATH;
    /**
     * Config: Researcher id dir
     *
     * @deprecated Shouldn't be here
     */
    @Deprecated
    public static String m_cardsFileName = CardData.APP_CARDS_XML_FILENAME;
    /**
     * Config: institution dir
     *
     * @deprecated Shouldn't be here
     */
    @Deprecated
    public static String m_schoolsFileName = SchoolData.APP_SCHOOLS_XML_FILENAME;
    /**
     * Config: Examinee file
     */
    @Deprecated
    public static String m_pupilsFileName = PupilData.APP_PUPILS_XML_FILENAME;
    /**
     * Config: change task class name
     */
    public static String m_taskChangeName = LoremIpsumApp.APP_TASK_CHANGE_DEFAULT;
    /**
     * Config: change task name
     */
    public static String m_taskManagerName = LoremIpsumApp.APP_TASK_MANAGER_DEFAULT;
    /**
     * Config: touch confirmation sound
     */
    public static boolean m_clickFlag = true;
    /**
     * Config: touch confirmation vibration
     */
    public static boolean m_hapticFlag = true;
    /**
     * Config: check password flag
     *
     * @deprecated Shouldn't be here
     */
    @Deprecated
    public static boolean m_passwordFlag = false;
    public static int maxTasksNumerPerTest = 30;
    public CollectorConfig collectorConfig = new CollectorConfig();
    /**
     * Config: disable developer options flag
     */
    public boolean disableDeveloperOptions = false;
    /**
     * Config: disables Cat algorithm
     */
    public boolean disableCatAlgoritm = false;
    /**
     * Config: disable institutions. If false examinee won't be assigned to institution
     */
    public boolean disableInstitutions = false;
    /**
     * Config: disable departments. If false examinee won't be assigned to department
     */
    public boolean disableDepartments = false;
    /**
     * Config:data dir
     */
    public String m_inputDir = LoremIpsumApp.APP_INPUT_PATH;
    public TestType testType;

    public static boolean checkPilot(VirtualFile config) throws Exception {
        Document doc = BaseXMLFile.OpenXMLFile(config.getInputStream(), null);
        NodeList suiteList = doc.getElementsByTagName(XML_CONFIG_SUITE);
        if (suiteList.getLength() < 1)
            return false;
        if (suiteList.getLength() > 1)
            throw new Exception("There should be only one \"" + XML_CONFIG_SUITE + "\" element");
        NamedNodeMap suite = suiteList.item(0).getAttributes();
        if (suite != null) {
            String pilot = BaseXMLFile.getString(suite, XML_CONFIG_SUITE_PILOT, XML_CONFIG_NO);
            return pilot.equals(XML_CONFIG_YES);
        }
        return false;
    }

    /**
     * Loads app config
     *
     * @return true if succeed
     */
    public boolean loadConfigXML(VirtualFile baseDir) throws IOException {

        LogUtils.d(TaskSuiteConfig.class.getSimpleName(), "PrepareData");

        loadConfig(baseDir);
        loadRaporting(baseDir);
        return true;
    }

    /**
     * loads config
     *
     * @param baseDir
     * @throws IOException
     */
    private void loadConfig(VirtualFile baseDir) throws IOException {
        Document doc = BaseXMLFile.OpenXMLFile(baseDir.getChildFile(TaskSuiteConfig.APP_CONFIG_XML_FILENAME).getInputStream(), null);
        if (doc == null)
            throw new IOException("Could not load XML document from file " + TaskSuiteConfig.APP_CONFIG_XML_FILENAME
                    + " in virtual container " + baseDir.getVirtualContainerName());

        NodeList list = doc.getElementsByTagName(TaskSuiteConfig.XML_CONFIG_TASKS);
        if (list.getLength() > 0) {
            // W pliku powinien być tylko jeden taki element
            NamedNodeMap map = list.item(0).getAttributes();
            if (map != null) {
                // Nazwa klasy realizujące zmianę zadania
                m_taskChangeName = BaseXMLFile.getString(map, XML_CONFIG_TASKS_CHANGE, LoremIpsumApp.APP_TASK_CHANGE_DEFAULT);

                // Nazwa klasy realizującej wybór kolejnych zadań podczas badania
                m_taskManagerName = BaseXMLFile.getString(map, TaskSuiteConfig.XML_CONFIG_TASKS_MANAGER, LoremIpsumApp.APP_TASK_MANAGER_DEFAULT);

                // Flaga potwierdzenia sygnałem dźwiękowym dotknięcia ekranu podczas rozwiązywania zadań
                TaskSuiteConfig.m_clickFlag = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_CLICK, TaskSuiteConfig.XML_CONFIG_TASKS_YES, true);

                // Flaga potwierdzenia wibracją dotknięcie ekranu podczas rozwiązywania zadań
                TaskSuiteConfig.m_hapticFlag = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_HAPTIC, TaskSuiteConfig.XML_CONFIG_TASKS_YES, true);

                disableDeveloperOptions = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_DISABLE_DEVELOPER_OPTIONS, TaskSuiteConfig.XML_CONFIG_TASKS_YES, false);

                disableCatAlgoritm = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_DISABLE_CAT_ALGORITHM, TaskSuiteConfig.XML_CONFIG_TASKS_YES, false);
                disableInstitutions = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_DISABLE_INSTITUTIONS, TaskSuiteConfig.XML_CONFIG_TASKS_YES, false);
                disableDepartments = BaseXMLFile.GetFlag(map, TaskSuiteConfig.XML_CONFIG_TASKS_DISABLE_DEPARTMENTS, TaskSuiteConfig.XML_CONFIG_TASKS_YES, false);
                try {
                    maxTasksNumerPerTest = Integer.valueOf(BaseXMLFile.getString(map, TaskSuiteConfig.XML_CONFIG_TASK_MAX_NUMBER, "30"));
                } catch (Exception e) {
                    LogUtils.e(TaskSuiteConfig.class.getSimpleName(), "string parse ex", e);
                    maxTasksNumerPerTest = 30;
                }
                testType = TestType.valueOf(BaseXMLFile.getString(map, TaskSuiteConfig.TEST_MODE, "TUNSS"));


                // Zadanie kończące badanie
                TaskSuiteConfig.m_finishTaskName = BaseXMLFile.getString(map, TaskSuiteConfig.XML_CONFIG_TASKS_FINISH, LoremIpsumApp.APP_EMPTY_FIELD);
            }
        }
    }

    /**
     * Loads raporting
     *
     * @param baseDir
     */
    private void loadRaporting(VirtualFile baseDir) {
        Document doc;
        try {
            doc = BaseXMLFile.OpenXMLFile(baseDir.getChildFile(TaskSuiteConfig.APP_RAPORTING_XML_FILENAME).getInputStream(), null);
            if (doc == null) {// if we cannot open it we should just ignore it
                return;
            }
        } catch (IOException e) {
            return;
        }
        Element collector = XmlHelper.getSingleElement(doc, TaskSuiteConfig.XML_RAPORTING);
        if (collector != null) {
            if (collector.hasAttribute(TaskSuiteConfig.XML_RAPORTING_URL)) {
                collectorConfig.targetUrl = collector.getAttribute(TaskSuiteConfig.XML_RAPORTING_URL);
            } else {
                return;
            }
            collectorConfig.isRaportingRequired = true;
            Element researcher = XmlHelper.getSingleElement(collector, TaskSuiteConfig.XML_RAPORTING_RESEARCHER);
            if (researcher != null && XmlHelper.getSingleElement(researcher, TaskSuiteConfig.XML_RAPORTING_ID) != null) {
                collectorConfig.sendResearcherId = true;
            }
            Element examinee = XmlHelper.getSingleElement(collector, TaskSuiteConfig.XML_RAPORTING_EXAMINEE);
            if (examinee != null) {
                if (XmlHelper.getSingleElement(examinee, TaskSuiteConfig.XML_RAPORTING_ID) != null) {
                    collectorConfig.sendExamineeId = true;
                }
                if (XmlHelper.getSingleElement(examinee, TaskSuiteConfig.XML_RAPORTING_EXAMINEE_BIRTHDAY) != null) {
                    collectorConfig.sendExamineeBirthday = true;
                }
                if (XmlHelper.getSingleElement(examinee, TaskSuiteConfig.XML_RAPORTING_EXAMINEE_GENDER) != null) {
                    collectorConfig.sendExamineeGender = true;
                }
            }
            Element institution = XmlHelper.getSingleElement(collector, TaskSuiteConfig.XML_RAPORTING_INSTITUTION);
            if (institution != null && XmlHelper.getSingleElement(institution, TaskSuiteConfig.XML_RAPORTING_ID) != null) {
                collectorConfig.sendInstitutionId = true;
            }
            Element raportContent = XmlHelper.getSingleElement(collector, TaskSuiteConfig.XML_RAPORTING_RAPORT_CONTENT);
            if (raportContent != null) {
                boolean hasSummary = false;
                boolean hasTasks = false;
                if (XmlHelper.getSingleElement(raportContent, TaskSuiteConfig.XML_RAPORTING_RAPORT_SUMMARY) != null) {
                    hasSummary = true;
                }
                if (XmlHelper.getSingleElement(raportContent, TaskSuiteConfig.XML_RAPORTING_RAPORT_TASKS) != null) {
                    hasTasks = true;
                }
                if (hasSummary && hasTasks) {
                    collectorConfig.raportType = CollectorConfig.RaportType.TASKS_AND_SUMMARY;
                } else if (hasSummary) {
                    collectorConfig.raportType = CollectorConfig.RaportType.JUST_SUMMARY;
                } else {
                    collectorConfig.raportType = CollectorConfig.RaportType.NONE;
                }
            }
        }
    }

    public enum TestType{
        TUNSS, TPR1, TPR2
    }
}

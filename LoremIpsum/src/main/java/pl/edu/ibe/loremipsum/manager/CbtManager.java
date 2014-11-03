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




package pl.edu.ibe.loremipsum.manager;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import pl.edu.ibe.loremipsum.manager.Irt.IrtPiece;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp.AreaWrapper;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

//import android.util.Log;

/**
 * Simplified implementation of task organiser
 * Only for tutorial purpose
 */
public class CbtManager extends BaseManager {

    private static final String TAG = CbtManager.class.toString();

    /**
     * manager definition file
     */
    private static final String CBT_XML_FILENAME = "cbt.xml";
    /**
     * manager data file
     */
    private static final String CBT_DATA_XML_FILENAME = "cbt-data.xml";
    /**
     * manager definition schema file
     */
    private static final String CBT_XSD_FILENAME = "cbt.xsd";
    /**
     * manager data schema file
     */
    private static final String CBT_DATA_XSD_FILENAME = "cbt-data.xsd";

    /**
     * tags and attributes  cbt.xml file
     */
    private static final String XML_CBT = "cbt";
    private static final String XML_CBT_NAME = "name";
    private static final String XML_CBT_ORDER = "order";
    private static final String XML_CBT_ORDER_ONE_BY_ONE = "one_by_one";
    private static final String XML_CBT_ORDER_RANDOM = "random";
    private static final String XML_CBT_THETA_EPS = "theta_eps";

    /**
     * tags and attributes  cbt-data.xml file
     */
    private static final String XML_CBT_DATA = "cbt-data";
    private static final String XML_CBT_DATA_SCRIPT = "script";
    private static final String XML_CBT_DATA_SCRIPT_TITLE = "title";
    private static final String XML_CBT_DATA_SCRIPT_TASK = "task";
    private static final String XML_CBT_DATA_SCRIPT_TASK_NAME = "name";

    /**
     * initial manager name
     */
    private static final String CBT_MANAGER_NAME_DEFAULT = "CBT-int";

    /**
     * Presentation flags
     */
    private static final int CBT_ORDER_ONE_BY_ONE = 0;
    /**
     * Presentation mode
     */
    private int m_order = CBT_ORDER_ONE_BY_ONE;
    private static final int CBT_ORDER_RANDOM = 1;
    /**
     * Presentent task list
     */
    protected CbtScriptInfo m_taskScript = null;
    /**
     * Test packs
     */
    private LinkedHashMap<String, CbtScriptInfo> m_scripts = new LinkedHashMap<String, CbtScriptInfo>();
    /**
     * task choose index
     */
    private int m_taskIndex = 0;

    /**
     * Theta calculation algorithm stop value
     */
    private double m_thetaEpsylon = Irt.IRT_THETA_EPSYLON_LIMIT;

    public CbtManager(String name) {
        super(name);
    }

    public static CbtManager fromDirectory(String title, VirtualFile tasksDirectory) throws IOException {
        CbtManager manager = new CbtManager(title);
        manager.m_scripts.clear();
        manager.m_order = CBT_ORDER_ONE_BY_ONE;
        manager.m_name = CBT_MANAGER_NAME_DEFAULT;

        CbtScriptInfo scriptInfo = manager.new CbtScriptInfo();
        scriptInfo.m_title = title;

        VirtualFile[] dirs = tasksDirectory.listFiles();
        Arrays.sort(dirs, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        for (VirtualFile dir : dirs) {
            CbtTaskItem item = manager.new CbtTaskItem();
            item.m_name = dir.getName();
            item.m_task = new TaskInfo(dir.getChildFile(BaseTask.APP_TASK_XML_FILENAME));
            scriptInfo.m_script.add(item);
        }

        manager.m_scripts.put(title, scriptInfo);

        return manager;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Initialize(java.lang.String)
     */
    @Override
    public boolean Initialize(VirtualFile baseDir) throws IOException {
        m_scripts.clear();

        boolean configRet = LoadConfigXML(baseDir);
        boolean dataRet = LoadDataXML(baseDir.getChildFile(CBT_DATA_XML_FILENAME), CBT_DATA_XSD_FILENAME);

        int discard = PrepareItemPool(LoremIpsumApp.getTaskAreas());

        if (discard > 0) {
            LoremIpsumApp.AddDiscardInfo(discard);
        }

        return configRet & dataRet && (discard == 0);
    }

    /**
     * reads data from external file
     *
     * @param xmlFile   - File containing tasks order
     * @param a_xsdFile - xml schema fiel
     * @return true if success otherwise false
     */
    public boolean Initialize(VirtualFile xmlFile, String a_xsdFile) {
        m_scripts.clear();

        m_order = CBT_ORDER_ONE_BY_ONE;
        m_name = CBT_MANAGER_NAME_DEFAULT;

        boolean configRet = LoadConfigXML(xmlFile.getParentFile());
        boolean dataRet = LoadDataXML(xmlFile, a_xsdFile);

        int discard = PrepareItemPool(LoremIpsumApp.m_manual);

        if (discard > 0) {
            LoremIpsumApp.AddDiscardInfo(discard);
        }

        return configRet & dataRet && (discard == 0);
    }

    /*
     * Automatically scan for tasks in given base directory
     * @param baseDir Directory containing tasks
     */
    public void autoinitialize(String title, VirtualFile configFile, VirtualFile baseDir) throws IOException {
        m_scripts.clear();
        m_order = CBT_ORDER_ONE_BY_ONE;
        m_name = CBT_MANAGER_NAME_DEFAULT;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Restart(int)
     */
    @Override
    public boolean Restart(String a_name) {
        LogUtils.v(TAG, "manager[\"" + getName() + "\"] Restart(\"" + a_name + "\")");
        LogUtils.d(TAG, "  m_scripts.size: " + m_scripts.size());
        CbtScriptInfo info = null;

        if (a_name == null || a_name.length() == 0) {
            Vector<CbtScriptInfo> scripts = new Vector<CbtScriptInfo>(m_scripts.values());
            if (scripts.size() > 0) {
                info = scripts.get(0);
            }
        } else {
            info = m_scripts.get(a_name);
        }

        if (info != null) {
            m_taskScript = info;
            m_taskIndex = 0;

            for (CbtTaskItem task : info.m_script) {
                task.m_used = false;
            }

            info.m_status.clear();

            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#GetNextTask()
     */
    @Override
    public TaskInfo GetNextTask() {
        LogUtils.v(TAG, "manager[\"" + getName() + "\"] GetNextTask()");
        LogUtils.d(TAG, "  m_taskScript: " + m_taskScript);
        TaskInfo task;
        do {
            task = getTask();
        } while (task == null && !IsFinished());
        return task;

    }

    private TaskInfo getTask() {
        if (m_taskScript != null) {
            if (m_taskIndex < m_taskScript.m_script.size()) {
                CbtTaskItem task = m_taskScript.m_script.get(m_taskIndex);

                ++m_taskIndex;

                return task.m_task;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#GetPrevTask()
     */
    @Override
    public TaskInfo GetPrevTask() {
        if (m_taskScript != null) {
            if (m_taskIndex > 1) {
                m_taskIndex -= 2;

                if (m_taskIndex < m_taskScript.m_script.size()) {
                    CbtTaskItem task = m_taskScript.m_script.get(m_taskIndex);

                    return task.m_task;
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#IsFinished()
     */
    @Override
    public boolean IsFinished() {
        if (m_taskScript != null) {
            if (m_taskScript.m_script.size() > m_taskIndex) {
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#GetTestInfo(java.lang.String)
     */
    @Override
    public ManagerTestInfo GetTestInfo(String a_area) {
        if (m_taskScript != null)
            return m_taskScript.m_status.get(a_area);

        return null;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Mark(pl.edu.ibe.loremipsum.task.TaskInfo, int)
     */
    @Override
    public void Mark(TaskInfo a_task, double a_mark) {
        ManagerTestInfo info = m_taskScript.m_status.get(a_task.m_area);
        if (info == null) {
            info = new ManagerTestInfo();
            info.m_status = LoremIpsumApp.TEST_PROGRESS;
            info.m_vector.m_piece.clear();
            info.m_vector.m_theta = 0.0;
            info.m_vector.m_se = 1.0;

            m_taskScript.m_status.put(a_task.m_area, info);
        }

        IrtPiece piece = new IrtPiece();
        piece.m_irt_a = a_task.m_irt_a;
        piece.m_irt_bs = new LinkedList<>(a_task.m_irt_bs);
        piece.m_irt_c = a_task.m_irt_c;
        piece.m_mark = Irt.TranslateMark(a_mark, piece);

        info.m_vector.m_piece.add(piece);

        Irt.CalculateFinalBF(info.m_vector, m_thetaEpsylon, -4.0, 4.0);
    }

    /**
     * Loads CBT config file
     * @param path Path containing file (CBT_XML_FILENAME)
     * @return true if success
     */
    private boolean LoadConfigXML(VirtualFile path) {
        LogUtils.d(TAG, "loadConfigXML");

        try {
            VirtualFile xmlFile = path.getChildFile(CBT_XML_FILENAME);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(xmlFile.getInputStream());

            // INFO_XSD wyłaczone ze względu na błedy w implementacji
            /*
                        File xsdFile = new File( path, CBT_XSD_FILENAME );
            			SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            			Source source = new StreamSource( xsdFile );
            			Schema schema = sf.newSchema( source );

            			Validator validator = schema.newValidator();

            			validator.validate(  new DOMSource( document ) );
            */
            // INFO_XSD <end>

            NodeList list = document.getElementsByTagName(XML_CBT);
            if (list != null) {
                if (list.getLength() > 0) {
                    // W pliku powinien być tylko jeden tak element
                    NamedNodeMap map = list.item(0).getAttributes();
                    if (map != null) {
                        /* Nazwa */
                        Node node = map.getNamedItem(XML_CBT_NAME);
                        if (node != null) {
                            m_name = node.getNodeValue();
                        } else {
                            m_name = LoremIpsumApp.APP_NO_FILL_FIELD;
                        }

                        /* Kolejność prezentowania zadań */
                        node = map.getNamedItem(XML_CBT_ORDER);
                        m_order = CBT_ORDER_ONE_BY_ONE;
                        if (node != null) {
                            if (node.getNodeValue().contains(XML_CBT_ORDER_RANDOM)) {
                                m_order = CBT_ORDER_RANDOM;
                            }
                        }

                        /* Wartość wykorzystywana do obliczania poziomu umiejętności ucznia */
                        node = map.getNamedItem(XML_CBT_THETA_EPS);
                        if (node != null) {
                            try {
                                m_thetaEpsylon = Double.parseDouble(node.getNodeValue());
                                if (m_thetaEpsylon < Irt.IRT_THETA_EPSYLON_LIMIT) {
                                    m_thetaEpsylon = Irt.IRT_THETA_EPSYLON_LIMIT;
                                }
                            } catch (NumberFormatException e) {
                                LogUtils.e(TAG, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            LogUtils.e(TAG, e);
        }

        return true;
    }

    /**
     *  Loads addtional CBT data
     *
     * @param xmlFile   File containing data
     * @param a_xsdFile - - xml schema file
     * @return true if success
     */
    private boolean LoadDataXML(VirtualFile xmlFile, String a_xsdFile) {
        LogUtils.d(TAG, "LoadDataXML");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(xmlFile.getInputStream());

            // INFO_XSD wyłaczone ze względu na błedy w implementacji
            /*
                        File xsdFile = new File( a_path, a_xsdFile );
            			SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            			Source source = new StreamSource( xsdFile );
            			Schema schema = sf.newSchema( source );

            			Validator validator = schema.newValidator();

            			validator.validate(  new DOMSource( document ) );
            */
            // INFO_XSD <end>

            NodeList list = document.getElementsByTagName(XML_CBT_DATA_SCRIPT);
            if (list != null) {
                for (int index = 0; index < list.getLength(); ++index) {
                    CbtScriptInfo script = null;

                    Node rootNode = list.item(index);
                    NamedNodeMap map = rootNode.getAttributes();
                    if (map != null) {
                        String title = null;

                        Node node = map.getNamedItem(XML_CBT_DATA_SCRIPT_TITLE);
                        if (node != null) {
                            title = node.getNodeValue();
                        } else {
                            title = null;
                        }

                        if (title == null) {
                            LogUtils.d(TAG, " discard<filed>: " + XML_CBT_DATA_SCRIPT_TITLE);
                            continue;
                        }

                        CbtScriptInfo info = m_scripts.get(title);

                        if (info != null) {
                            LogUtils.d(TAG, " duplicate<script>: " + title);
                            continue;
                        }

                        script = new CbtScriptInfo();
                        script.m_title = title;
                        m_scripts.put(title, script);
                    }

                    NodeList childList = rootNode.getChildNodes();
                    if (childList != null) {
                        for (int n = 0; n < childList.getLength(); ++n) {
                            Node childNode = childList.item(n);
                            if (childNode.getNodeName().compareTo(XML_CBT_DATA_SCRIPT_TASK) == 0) {
                                NamedNodeMap childMap = childNode.getAttributes();
                                if (childMap != null) {
                                    CbtTaskItem task = new CbtTaskItem();

                                    Node taskNode = childMap.getNamedItem(XML_CBT_DATA_SCRIPT_TASK_NAME);
                                    if (taskNode != null) {
                                        task.m_name = taskNode.getNodeValue();
                                    } else {
                                        task = null;
                                        LogUtils.d(TAG, "discard<filed>: " + XML_CBT_DATA_SCRIPT_TASK_NAME);
                                    }
                                    taskNode = null;

                                    if (task != null) {
                                        script.m_script.add(task);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            LogUtils.e(TAG, "problem during file parsing: " + xmlFile.getAbsolutePath(), e);
        }

        return true;
    }

    /**
     * Task bank preparation
     *
     * @param a_itemPool - task bank
     * @return number of rejected tasks
     */
    private int PrepareItemPool(Vector<AreaWrapper> a_itemPool) {
        Vector<CbtScriptInfo> scripts = new Vector<CbtScriptInfo>(m_scripts.values());

        int discard = 0;
        for (CbtScriptInfo i : scripts) {
            for (CbtTaskItem t : i.m_script) {
                boolean find = false;
                for (AreaWrapper area : a_itemPool) {
                    for (TaskInfo task : area.m_tasks)
                        if (task.m_name.compareTo(t.m_name) == 0) {
                            t.m_task = task;
                            find = true;
                            break;
                        }

                    if (find)
                        break;
                }

                if (!find)
                    ++discard;
            }
        }

        return discard;
    }

    /**
     *Information about examined area
     *
     *
     */
    protected class CbtScriptInfo {

        /**
         * test name
         */
        public String m_title = "";

        /**
         * test status
         */
        public LinkedHashMap<String, ManagerTestInfo> m_status = new LinkedHashMap<String, ManagerTestInfo>();

        /**
         * task list
         */
        public Vector<CbtTaskItem> m_script = new Vector<CbtTaskItem>();
    }

    /**
     * Task description for CBT purposes
     *
     *
     */
    private class CbtTaskItem {

        /**
         * task name
         */
        public String m_name = null;

        /**
         *  task info
         */
        public TaskInfo m_task = null;

        /**
         * used flag
         */
        public boolean m_used = false;
    }
}

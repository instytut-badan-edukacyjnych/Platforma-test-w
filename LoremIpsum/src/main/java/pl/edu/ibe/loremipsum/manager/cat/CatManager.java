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

package pl.edu.ibe.loremipsum.manager.cat;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import pl.edu.ibe.loremipsum.manager.BaseManager;
import pl.edu.ibe.loremipsum.manager.Irt;
import pl.edu.ibe.loremipsum.manager.Irt.IrtPiece;
import pl.edu.ibe.loremipsum.manager.MappingDependency;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp.AreaWrapper;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * Manages task order using CAT algorithm.
 */
public class CatManager extends BaseManager {

    /**
     * display mode flags
     */
    protected static final int CAT_ORDER_ONE_BY_ONE = 0;
    /**
     * display mode
     */
    protected int m_order = CAT_ORDER_ONE_BY_ONE;
    protected static final int CAT_ORDER_MIX = 1;
    private static final String TAG = CatManager.class.toString();
    /**
     * cat filename
     */
    private static final String CAT_XML_FILENAME = "cat.xml";
    /**
     * cat manager data filename
     */
    private static final String CAT_DATA_XML_FILENAME = "cat-data.xml";
    /**
     * Cat xsd file
     */
    private static final String CAT_XSD_FILENAME = "cat.xsd";
    /**
     * cat data schema file
     */
    private static final String CAT_DATA_XSD_FILENAME = "cat-data.xsd";
    /**
     * Tag and attributes of cat file
     */
    private static final String XML_CAT = "cat";
    private static final String XML_CAT_NAME = "name";
    private static final String XML_CAT_ORDER = "order";
    private static final String XML_CAT_ORDER_ONE_BY_ONE = "one_by_one";
    private static final String XML_CAT_ORDER_MIX = "mix";
    private static final String XML_CAT_STEP = "step";
    private static final String XML_CAT_THETA_EPS = "theta_eps";
    private static final String XML_CAT_D = "D";
    private static final String XML_CAT_NOISE = "noise";
    private static final String XML_CAT_NOISE_BASE = "base";
    private static final String XML_CAT_TERMINATION = "termination";
    private static final String XML_CAT_TERMINATION_CRITERION = "criterion";
    private static final String XML_CAT_TERMINATION_CRITERION_MAX = "max";
    private static final String XML_CAT_TERMINATION_CRITERION_MIN = "min";
    private static final String XML_CAT_TERMINATION_CRITERION_SE = "deviation";
    private static final String XML_CAT_TERMINATION_VALUE = "value";
    /**
     * Tag and attributes of cat data file
     */
    private static final String XML_CAT_DATA = "cat-data";
    private static final String XML_CAT_DATA_AREA = "area";
    private static final String XML_CAT_DATA_AREA_NAME = "name";
    private static final String XML_CAT_DATA_AREA_SCOPE = "scope";
    private static final String XML_CAT_DATA_AREA_SCOPE_FROM = "from";
    private static final String XML_CAT_DATA_AREA_SCOPE_TO = "to";
    private static final String XML_CAT_DATA_AREA_SCOPE_FEMALE = "female";
    private static final String XML_CAT_DATA_AREA_SCOPE_MALE = "male";
    private static final int MAX_AREA_USAGE = 10;
    /**
     * Base noise used to differ choosing tasks
     */
    protected static double m_noiseBase = 0.0;
    /**
     * noise value
     */
    private static double m_noise = 0.5 * (Math.sqrt(5.0) - 1.0);
    /**
     * task bank
     */
    protected LinkedHashMap<String, CatAreaInfo> m_itemPool = new LinkedHashMap<String, CatAreaInfo>();
    /**
     * stop condition
     */
    protected Vector<CatTerminationItem> m_termination = new Vector<CatTerminationItem>();
    /**
     * max test length
     */
    protected int m_maxLength = 10;
    /**
     * Value used to calculate examinee skill level
     */
    protected double m_thetaStep = 0.5 * (Math.sqrt(5.0) - 1.0);
    /**
     * Theta calculation algorithm  stop condition
     */
    private double m_thetaEpsylon = Irt.IRT_THETA_EPSYLON_LIMIT;
    /**
     * task area index
     */
    private int m_areaIndex = 0;
    private HashMap<String, Integer> areaUsageCounter;

    /**
     * Constructor
     */
    public CatManager(String name) {
        super(name);

        Date date = new Date();
        long seed = date.getTime();
        seed %= 10000;
        m_noise *= 0.0001 * seed;
    }

    /**
     * Calculates Random
     *
     * @param a_number - Task number
     * @param a_length - Task quantity
     * @return next Random
     */
    protected static double Noise(int a_number, int a_length) {

        m_noise = 4.0 * m_noise * (1 - m_noise);

        if (a_number > a_length) {
            a_number = a_length;
        }
        double n = (m_noise - 0.5) * m_noiseBase;
        n *= a_length - a_number;
        n /= a_length;

        return n;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Initialize(java.lang.String)
     */
    @Override
    public boolean Initialize(VirtualFile baseDir) {

        PrepareItemPool();

        boolean configRet = LoadConfigXML(baseDir);
        boolean dataRet = LoadDataXML(baseDir);

        for (CatTerminationItem item : m_termination) {
            if (item.m_criterion == CatTerminationItem.CAT_TERMINATION_MAX) {
                m_maxLength = (int) item.m_value;
            }
        }

        return configRet & dataRet;
    }

    /*
         * (non-Javadoc)
         * @see pl.edu.ibe.loremipsum.manager.BaseManager#Restart(int)
         */
    @Override
    public boolean Restart(String a_name) {

        LoadBase();

        Vector<CatAreaInfo> areas = new Vector<CatAreaInfo>(m_itemPool.values());

        for (CatAreaInfo area : areas) {
            for (CatTaskItem task : area.m_tasks) {
                task.m_used = false;
            }

            area.m_status.m_status = LoremIpsumApp.TEST_PROGRESS;
            area.m_status.m_vector.m_piece.clear();
            area.m_status.m_vector.m_theta = area.m_thetaBase;
            area.m_status.m_vector.m_se = 1.0;

            area.m_taskCnt = 0;
        }

        m_areaIndex = 0;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#GetNextTask()
     */
    @Override
    public TaskInfo GetNextTask() {
        if (areaUsageCounter == null) {
            areaUsageCounter = new HashMap<>();
            for (String s : m_itemPool.keySet()) {
                areaUsageCounter.put(s, 0);
            }
        }
        // sprawdzenie czy koniec testu
        if (IsFinished()) {
            return null;
        }

        Vector<CatAreaInfo> areas = new Vector<CatAreaInfo>(m_itemPool.values());

        CatAreaInfo area = areas.get(m_areaIndex);
        TaskInfo task;
        if ((m_order == CAT_ORDER_MIX) || ((area.m_status.m_status & LoremIpsumApp.TEST_FINISHED_FLAG) != 0)) {
            // nastepny obszar
            area = null;
            for (int index = 0; index < areas.size(); ++index) {
                ++m_areaIndex;
                if (m_areaIndex >= m_itemPool.size()) {
                    m_areaIndex = 0;
                }
                area = areas.get(m_areaIndex);

                if ((area.m_status.m_status & LoremIpsumApp.TEST_FINISHED_FLAG) == 0 && areaUsageCounter.get(area.name) < MAX_AREA_USAGE) {
                    areaUsageCounter.put(area.name, areaUsageCounter.get(area.name) + 1);
                    break;
                }
                area = null;
            }
        }

        if (area == null) {
            areaUsageCounter = null;
            return null;
        }

        task = FindTask(area);


        if (task == null && !allAreasHasBeenUsed()) {
            for (Map.Entry<String, Integer> stringIntegerEntry : areaUsageCounter.entrySet()) {
                if (stringIntegerEntry.getValue() == 0) {
                    for (CatAreaInfo catAreaInfo : areas) {
                        for (CatTaskItem m_task : catAreaInfo.m_tasks) {
                            if (m_task.m_task.m_area.equals(stringIntegerEntry.getKey())) {
                                if (!m_task.m_used) {
                                    areaUsageCounter.put(catAreaInfo.name, areaUsageCounter.get(catAreaInfo.name) + 1);
                                    m_task.m_used = true;
                                    task = m_task.m_task;
                                    return task;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }else{
            areaUsageCounter = null;
        }

        return task;
    }

    private boolean allAreasHasBeenUsed() {
        for (Map.Entry<String, Integer> stringIntegerEntry : areaUsageCounter.entrySet()) {
            if (stringIntegerEntry.getValue() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TaskInfo GetPrevTask() {
        throw new UnsupportedOperationException("This manager does not support getting previous task");
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#IsFinished()
     */
    @Override
    public boolean IsFinished() {

        Vector<CatAreaInfo> areas = new Vector<CatAreaInfo>(m_itemPool.values());

        for (CatAreaInfo info : areas) {
            if ((info.m_status.m_status & LoremIpsumApp.TEST_FINISHED_FLAG) == 0) {
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

        CatAreaInfo info = m_itemPool.get(a_area);

        if (info != null) {
            return info.m_status;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Mark(pl.edu.ibe.loremipsum.task.TaskInfo, int)
     */
    @Override
    public void Mark(TaskInfo a_task, double a_mark) {

        Vector<CatAreaInfo> areas = new Vector<>(m_itemPool.values());
        CatAreaInfo area = areas.get(m_areaIndex);

        IrtPiece piece = new IrtPiece();
        piece.m_irt_a = a_task.m_irt_a;
        piece.m_irt_bs = new LinkedList<>(a_task.m_irt_bs);
        piece.m_irt_c = a_task.m_irt_c;
        piece.m_mark = Irt.TranslateMark(a_mark, piece);

        area.m_status.m_vector.m_piece.add(piece);

        Irt.CalculateThetaBF(area.m_status.m_vector, m_thetaEpsylon, m_thetaStep, area.m_thetaBase, area.m_thetaMin, area.m_thetaMax);

        boolean abort = false;
        for (CatTerminationItem ter : m_termination) {
            switch (ter.m_criterion) {
                case CatTerminationItem.CAT_TERMINATION_MAX:

                    if (area.m_status.m_vector.m_piece.size() >= ter.m_value) {
                        area.m_status.m_status = LoremIpsumApp.TEST_FINISHED_MAX;

                        abort = true;
                    }
                    break;

                case CatTerminationItem.CAT_TERMINATION_MIN:

                    if (area.m_status.m_vector.m_piece.size() < ter.m_value) {
                        area.m_status.m_status = LoremIpsumApp.TEST_PROGRESS;

                        abort = true;
                    }
                    break;

                case CatTerminationItem.CAT_TERMINATION_SE:

                    if (area.m_status.m_vector.m_se < ter.m_value) {
                        area.m_status.m_status = LoremIpsumApp.TEST_FINISHED_SE;

                        abort = true;
                    }
                    break;
            }
            if (abort) {
                break;
            }
        }
    }

    /**
     * Reads CAT config
     *
     * @param path - config file location
     * @return true if success
     */
    protected boolean LoadConfigXML(VirtualFile path) {
        LogUtils.d(TAG, "loadConfigXML");

        try {

            VirtualFile xmlFile = path.getChildFile(CAT_XML_FILENAME);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            LogUtils.v(TAG, "Xml file path: " + xmlFile);
            Document document = parser.parse(xmlFile.getInputStream());

            // INFO_XSD wyłaczone ze względu na błedy w implementacji
            /*
                        File xsdFile = new File( a_path, CAT_XSD_FILENAME );
            			SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            			Source source = new StreamSource( xsdFile );
            			Schema schema = sf.newSchema( source );

            			Validator validator = schema.newValidator();

            			validator.validate(  new DOMSource( document ) );
            */
            // INFO_XSD <end>

            NodeList list = document.getElementsByTagName(XML_CAT);
            if (list != null) {
                if (list.getLength() > 0) {
                    // W pliku powinien być tylko jeden tak element
                    NamedNodeMap map = list.item(0).getAttributes();
                    if (map != null) {
                        /* Nazwa */
                        Node node = map.getNamedItem(XML_CAT_NAME);
                        if (node != null) {
                            m_name = node.getNodeValue();
                        } else {
                            m_name = LoremIpsumApp.APP_NO_FILL_FIELD;
                        }

                        /* Kolejność prezentowania zadań */
                        node = map.getNamedItem(XML_CAT_ORDER);
                        m_order = CAT_ORDER_ONE_BY_ONE;
                        if (node != null) {
                            if (node.getNodeValue().contains(XML_CAT_ORDER_MIX)) {
                                m_order = CAT_ORDER_MIX;
                            }
                        }

                        /* Wartość wykorzystywana do obliczania poziomu umiejętności ucznia */
                        node = map.getNamedItem(XML_CAT_STEP);
                        if (node != null) {
                            try {
                                m_thetaStep = Double.parseDouble(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                LogUtils.e(TAG, e);
                            }
                        }

                        /* Wartość wykorzystywana do obliczania poziomu umiejętności ucznia */
                        node = map.getNamedItem(XML_CAT_THETA_EPS);
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

                        /* Współczynnik dla IRT */
                        node = map.getNamedItem(XML_CAT_D);
                        if (node != null) {
                            try {
                                Irt.s_D = Double.parseDouble(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                LogUtils.e(TAG, e);
                            }
                        }
                    }
                }
            }

            list = document.getElementsByTagName(XML_CAT_NOISE);
            if (list != null) {
                if (list.getLength() > 0) {
                    m_noiseBase = 0.0;

                    // W pliku powinien być tylko jeden taki element
                    NamedNodeMap map = list.item(0).getAttributes();
                    if (map != null) {
                        /* Wartość bazowa szum wprowadzanego w celu uzyskania różnorodności wyboru zadań */
                        Node node = map.getNamedItem(XML_CAT_NOISE_BASE);
                        if (node != null) {
                            try {
                                m_noiseBase = Double.parseDouble(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            m_name = LoremIpsumApp.APP_NO_FILL_FIELD;
                        }
                    }
                }
            }

            list = document.getElementsByTagName(XML_CAT_TERMINATION);
            if (list != null) {
                if (list.getLength() > 0) {
                    m_termination.clear();

                    for (int index = 0; index < list.getLength(); ++index) {
                        NamedNodeMap map = list.item(index).getAttributes();
                        if (map != null) {
                            CatTerminationItem item = new CatTerminationItem();

                            Node node = map.getNamedItem(XML_CAT_TERMINATION_CRITERION);
                            if (node != null) {
                                String value = node.getNodeValue();

                                if (value.contains(XML_CAT_TERMINATION_CRITERION_SE)) {
                                    item.m_criterion = CatTerminationItem.CAT_TERMINATION_SE;
                                } else if (value.contains(XML_CAT_TERMINATION_CRITERION_MAX)) {
                                    item.m_criterion = CatTerminationItem.CAT_TERMINATION_MAX;
                                } else if (value.contains(XML_CAT_TERMINATION_CRITERION_MIN)) {
                                    item.m_criterion = CatTerminationItem.CAT_TERMINATION_MIN;
                                } else {
                                    item = null;
                                }
                            } else {
                                item = null;
                            }
                            node = null;

                            if (item != null) {
                                node = map.getNamedItem(XML_CAT_TERMINATION_VALUE);
                                if (node != null) {
                                    try {
                                        item.m_value = Double.parseDouble(node.getNodeValue());
                                    } catch (NumberFormatException e) {
                                        item = null;
                                        LogUtils.e(TAG, e);
                                    }
                                } else {
                                    item = null;
                                }
                            }
                            node = null;

                            if (item != null) {
                                m_termination.add(item);
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
     * Reads cat data
     *
     * @param path - cat data location
     */
    protected boolean LoadDataXML(VirtualFile path) {
        LogUtils.d(TAG, "LoadDataXML");

        try {

            VirtualFile xmlFile = path.getChildFile(CAT_DATA_XML_FILENAME);
            CatMapperParser parser = new CatMapperParser();
            Tuple.Two<Map<String, Mapper>, MappingDependency> two = parser.loadMappings(xmlFile);
            mappingDependency = two.second;
            Map<String, Mapper> mappings = two.first;
            for (Map.Entry<String, Mapper> mapping : mappings.entrySet()) {
                CatAreaInfo areaInfo = m_itemPool.get(mapping.getKey());
                if (areaInfo != null) {
                    areaInfo.mapper = mapping.getValue();
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException | CatMapperParser.ParseException e) {
            LogUtils.e(TAG, e);
            //TODO remove this
//            mappingDependency = new CatMappingDependency();
//            ((CatMappingDependency) mappingDependency).setRequired(MappingDependency.EXAMINEE_GENDER);
//            ((CatMappingDependency) mappingDependency).setRequired(MappingDependency.INSTITUTION_POSTAL);
//            ((CatMappingDependency) mappingDependency).setRequired(MappingDependency.EXAMINEE_AGE);
//            ((CatMappingDependency) mappingDependency).setRequired(MappingDependency.INSTITUTION_CITY);
        }

        return true;
    }

    /**
     * Task bank preparation
     */
    private void PrepareItemPool() {

        m_itemPool.clear();

        for (AreaWrapper wrap : LoremIpsumApp.getTaskAreas()) {
            int matchingTasks = 0;
            for (TaskInfo t : wrap.m_tasks) {
                if (!LoremIpsumApp.APP_DEMO_DIRECTORY.equals(t.getDirectory().getPathComponent(0)))
                    matchingTasks++;
            }
            if (matchingTasks == 0)
                continue;

            CatAreaInfo info = m_itemPool.get(wrap.m_name);

            if (info == null) {
                info = new CatAreaInfo();
                info.name = wrap.m_name;
                ManagerTestInfo stat = new ManagerTestInfo();
                stat.m_area = wrap.m_name;
                stat.m_status = LoremIpsumApp.TEST_PREPARED;

                info.m_status = stat;
                m_itemPool.put(wrap.m_name, info);
            }

            info.m_thetaMin = wrap.m_min;
            info.m_thetaMax = wrap.m_max;

            for (TaskInfo t : wrap.m_tasks) {
                CatTaskItem item = new CatTaskItem();

                item.m_task = t;
                item.m_used = false;

                info.m_tasks.add(item);
            }
        }
    }

    /**
     * Base values depending on gender
     */
    protected void LoadBase() {

        Vector<CatAreaInfo> areas = new Vector<CatAreaInfo>(m_itemPool.values());
        for (CatAreaInfo area : areas) {
            if (area.mapper != null) {
                area.m_thetaBase = area.mapper.getStartValue(mappingDependency);
            }
        }
    }

    /**
     * Finds next task
     *
     * @param a_area - search area
     * @return Task info if found null if not
     */
    protected TaskInfo FindTask(CatAreaInfo a_area) {

        CatTaskItem maxTask = null;
        double maxI = 0.0;

        Vector<IrtPiece> dat = new Vector<IrtPiece>();

        int count = 0;
        for (CatTaskItem task : a_area.m_tasks) {

            if (!task.m_used) {
                ++count;
                IrtPiece pie = new Irt.IrtPiece();
                pie.m_mark = task.m_task.getM_range();
                pie.m_irt_a = task.m_task.m_irt_a;
                pie.m_irt_bs = new LinkedList<>(task.m_task.m_irt_bs);
                pie.m_irt_c = task.m_task.m_irt_c;
                dat.clear();
                dat.add(pie);

                double I = Irt.CalculateI(a_area.m_status.m_vector.m_theta + Noise(a_area.m_taskCnt, m_maxLength), dat);
                if (maxI < I) {
                    maxI = I;
                    maxTask = task;
                }
            }
        }

        LogUtils.d(TAG, "FindTask: " + count);

        if (maxTask != null) {
            // zaznaczamy uzycie zadania
            maxTask.m_used = true;

            // zaznaczamy uzycie wszystkich zadań z rodziny
            for (CatTaskItem t : a_area.m_tasks) {
                if (maxTask.m_task.m_family.compareTo(t.m_task.m_family) == 0) {
                    t.m_used = true;
                }
            }

            a_area.m_taskCnt++;
            return maxTask.m_task;
        }

        return null;
    }

    /**
     * Stop conditions descriptions
     */
    protected class CatTerminationItem {

        /**
         * Stop conditions types
         */
        public static final int CAT_TERMINATION_MAX = 1;
        public static final int CAT_TERMINATION_MIN = 2;
        public static final int CAT_TERMINATION_SE = 3;

        /**
         * Stop conditions
         */
        public int m_criterion = CAT_TERMINATION_MAX;

        /**
         * Stop condition parameter
         */
        public double m_value = 0.0;
    }

    /**
     * Information about examined area
     */
    class CatAreaInfo {

        /**
         * Test status
         */
        public ManagerTestInfo m_status = new ManagerTestInfo();

        /**
         * tasks
         */
        public Vector<CatTaskItem> m_tasks = new Vector<CatTaskItem>();
        /**
         *
         */
        public Mapper mapper;

        /**
         * area random range
         */
        public double m_thetaMin = 0.0;
        public double m_thetaMax = 0.0;

        /**
         * theta start value
         */
        public double m_thetaBase = 0.0;

        /**
         * task counter
         */
        public int m_taskCnt = 0;
        public String name;
    }

    /**
     * Task description for CAT needs
     */
    protected class CatTaskItem {

        /**
         * Task info
         */
        public TaskInfo m_task = null;

        /**
         * used flag
         */
        public boolean m_used = false;
    }
}



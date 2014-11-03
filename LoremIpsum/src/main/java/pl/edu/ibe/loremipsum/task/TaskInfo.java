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


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

//import android.util.Log;

/**
 * Klasa przechowujaca informacje o zadaniu
 */
public class TaskInfo {

    /**
     * nazwa pakietu
     */
    public static final String APP_PACKAGE_NAME = "pl.edu.ibe.loremipsum.task.";
    private static final String TAG = TaskInfo.class.toString();
    /**
     * tagi i atrybuty pliku task.xml
     */
//	private static final String XML_TASK_TASK 			= "task";
    private static final String XML_TASK_NAME = "name";
    private static final String XML_TASK_FAMILY = "family";
    private static final String XML_TASK_AREA = "area";
    private static final String XML_TASK_CLASS = "class";
    private static final String XML_TASK_RANGE = "range";
    private static final String XML_TASK_MANUAL = "manual";
    private static final String XML_TASK_IRT = "irt";
    private static final String XML_TASK_IRT_A = "a";
    private static final String XML_TASK_IRT_B = "b";
    private static final String XML_TASK_IRT_C = "c";
    /**
     * nazwa zadania
     */
    public String m_name = null;
    /**
     * rodzina zadań
     */
    public String m_family = null;
    /**
     * obszar z jakiego jest zadanie
     */
    public String m_area = null;
    /**
     * klasa (oprogramowania) obsługujaca zadanie
     */
    public String m_class = null;
    /**
     * zadanie nie właczane do bazy zadań - samouczek
     */
    public boolean m_manual;
    public boolean demo;
    /**
     * parametry IRT
     */
    public double m_irt_a;
    public LinkedList<Double> m_irt_bs = new LinkedList<>();
    public double m_irt_c;
    /**
     * numer zadania w grupie
     */
    public int m_groupIndex;
    /**
     * indeks do pierwszego pola
     */
    public int m_fieldBegin;
    /**
     * liczba pól w zadaniu
     */
    public int m_fieldNumber;
    /**
     * ściezka do katalogu z zadaniem
     */
    private VirtualFile directory = null;
    /**
     * status danych opisu zadania
     */
    private boolean m_valid = false;

    /**
     * Konstruktor informacji o zdaniu
     *
     * @param a_file - nazwa pliku z opisem zadania
     */
    public TaskInfo(VirtualFile a_file) {

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(a_file.getInputStream());

            // INFO_XSD wyłaczone ze względu na błedy w implementacji
            /*
                        File xsdFile = new File( dir, BaseTask.APP_TASK_XSD_FILENAME );
            			SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            			Source source = new StreamSource( xsdFile );
            			Schema schema = sf.newSchema( source );

            			Validator validator = schema.newValidator();

            			validator.validate(  new DOMSource( document ) );
            */
            // INFO_XSD <end>

            m_valid = true;
            int m_range = 0;

            Element element = document.getDocumentElement();
            if (element != null) {
                String str = element.getAttribute(XML_TASK_NAME);
                if (str != null) {
                    m_name = str;
                } else {
                    m_valid = false;
                    LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_NAME);
                }

                str = element.getAttribute(XML_TASK_FAMILY);
                if (str != null) {
                    m_family = str;
                } else {
                    m_valid = false;
                    LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_FAMILY);
                }

                str = element.getAttribute(XML_TASK_AREA);
                if (str != null) {
                    m_area = str;
                } else {
                    m_valid = false;
                    LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_AREA);
                }

                str = element.getAttribute(XML_TASK_CLASS);
                if (str != null) {
                    m_class = str;
                } else {
                    m_valid = false;
                    LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_CLASS);
                }

                str = element.getAttribute(XML_TASK_RANGE);
                if (str != null) {
                    try {
                        m_range = Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        m_valid = false;
                        LogUtils.e(TAG, e);
                    }
                } else {
                    m_valid = false;
                    LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_RANGE);
                }

                m_manual = false;
                str = element.getAttribute(XML_TASK_MANUAL);
                if (str != null) {
                    if (str.length() > 0) {
                        m_manual = true;
                    }
                }
            } else {
                m_valid = false;
            }

            NodeList list = document.getElementsByTagName(XML_TASK_IRT);
            if (list != null && m_valid) {
                if (list.getLength() > 0) {
                    // W pliku powinien być tylko jeden tak element
                    NamedNodeMap map = list.item(0).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_TASK_IRT_A);
                        if (node != null) {
                            try {
                                m_irt_a = Double.parseDouble(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                m_valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            m_valid = false;
                            LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_IRT_A);
                        }

                        node = map.getNamedItem(XML_TASK_IRT_B);
                        if (node != null) {
                            try {
                                m_irt_bs.add(Double.parseDouble(node.getNodeValue()));
                            } catch (NumberFormatException e) {
                                m_valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            m_valid = false;
                            LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_IRT_B);
                        }

                        for (int i = 2; i <= m_range; i++) {
                            node = map.getNamedItem(XML_TASK_IRT_B + String.valueOf(i));
                            if (node != null) {
                                try {
                                    m_irt_bs.add(Double.parseDouble(node.getNodeValue()));
                                } catch (NumberFormatException e) {
                                    m_valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            }
                        }

                        node = map.getNamedItem(XML_TASK_IRT_C);
                        if (node != null) {
                            try {
                                m_irt_c = Double.parseDouble(node.getNodeValue());
                            } catch (NumberFormatException e) {
                                m_valid = false;
                                LogUtils.e(TAG, e);
                            }
                        } else {
                            m_valid = false;
                            LogUtils.e(TAG, BaseTask.ERR_MESS_NO_ATTRIBUTE + XML_TASK_IRT_C);
                        }
                    }
                }
            } else {
                m_valid = false;
            }

            if (m_valid) {
                try {
                    Object mock = Class.forName(APP_PACKAGE_NAME + m_class);
                } catch (ClassNotFoundException | LinkageError e) {
                    m_valid = false;
                    LogUtils.e(TAG, e);
                }
            }

            if (m_valid)
                directory = a_file.getParentFile();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            m_valid = false;
            LogUtils.e(TAG, "Parsing file failed: " + a_file.getAbsolutePath(), e);
        }

        m_groupIndex = 0;
        m_fieldBegin = 0;
        m_fieldNumber = 0;
    }

    /**
     * Konstruktor kopiujacy
     *
     * @param a_info - informacja o zadaniu
     */
    public TaskInfo(TaskInfo a_info) {

        m_name = a_info.m_name;
        directory = a_info.directory;
        m_family = a_info.m_family;
        m_area = a_info.m_area;
        m_class = a_info.m_class;
        m_manual = a_info.m_manual;

        m_irt_a = a_info.m_irt_a;
        m_irt_bs = new LinkedList<>(a_info.m_irt_bs);
        m_irt_c = a_info.m_irt_c;
        m_valid = a_info.m_valid;
    }

    /**
     * Sprawdza czy opis zadania jest załadowany prawidłowo
     *
     * @return tru jeżeli opisa zadania została załadowany prawidłowo
     */
    public boolean isValid() {

        return m_valid;
    }

    public VirtualFile getDirectory() {
        return directory;
    }

    /**
     * zakres oceny 1 -> 0-1; 2 -> 0-1-2
     */
    public int getM_range() {
        return m_irt_bs.size();
    }
}



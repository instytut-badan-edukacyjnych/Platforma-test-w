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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Vector;

import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa przechowywania opisów wyników
 *
 *
 */
public class ResultDesc extends BaseXMLFile {
    private static final String TAG = ResultDesc.class.toString();


    /**
     * Nazwa pliku z opisem wyników
     */
    private static final String APP_RESULT_DESC_XML_FILENAME = "result-desc.xml";
    /**
     * Nazwa pliku schematu opisu wyników
     */
    private static final String APP_RESULT_DESC_XSD_FILENAME = "result-desc.xsd";

    /**
     * tagi i atrybuty pliku result-desc.xml
     */
//	private static final String XML_RESULT_DESC					= "result-desc";
    private static final String XML_RESULT_DESC_AREA = "area";
    private static final String XML_RESULT_DESC_AREA_ID = "id";
    private static final String XML_RESULT_DESC_AREA_NAME = "name";
    private static final String XML_RESULT_DESC_AREA_BAND = "band";
    private static final String XML_RESULT_DESC_AREA_DESC = "desc";
    private static final String XML_RESULT_DESC_AREA_SKILL = "skill";
    private static final String XML_RESULT_DESC_AREA_PERC = "percent";
    private static final String XML_RESULT_DESC_AREA_PERC_L1 = "L1";
    private static final String XML_RESULT_DESC_AREA_PERC_L2 = "L2";
    private static final String XML_RESULT_DESC_AREA_PERC_L3 = "L3";
    private static final String XML_RESULT_DESC_AREA_PERC_L4 = "L4";
    private static final String XML_RESULT_DESC_AREA_PERC_L5 = "L5";
    private static final String XML_RESULT_DESC_AREA_PERC_L6 = "L6";


    /**
     * opisy wyników
     */
    public static LinkedHashMap<String, ResultDescItem> m_resultDesc = new LinkedHashMap<String, ResultDescItem>();


    /**
     * Odczyt informaci o wynikach z pliku
     *
     * @return true jeżeli operacja zakoćzona sukcesem
     */
    public static boolean LoadResultDescXML() throws FileNotFoundException {
        LogUtils.d(TAG, "LoadResultDescXML: " + APP_RESULT_DESC_XML_FILENAME);
        String inputDir = LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().m_inputDir;
        Document doc = OpenXMLFile(LoremIpsumApp.m_legacyInputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + APP_RESULT_DESC_XML_FILENAME,
                LoremIpsumApp.m_legacyInputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + APP_RESULT_DESC_XSD_FILENAME);
        if (doc == null) {
            return false;
        }


        m_resultDesc.clear();

        NodeList list = doc.getElementsByTagName(XML_RESULT_DESC_AREA);
        if (list != null) {
            for (int index = 0; index < list.getLength(); ++index) {
                Node rootNode = list.item(index);

                NamedNodeMap map = rootNode.getAttributes();
                if (map != null) {
                    ResultDescItem item = new ResultDescItem();

                    try {
                        // identyfikator obszaru
                        item.m_id = GetString(map, XML_RESULT_DESC_AREA_ID);

                        // nazwa obszaru
                        item.m_name = GetString(map, XML_RESULT_DESC_AREA_NAME);

                        // grupa wiekowa
                        item.m_band = GetInteger(map, XML_RESULT_DESC_AREA_BAND);

                        // opis grupy wiekowej
                        item.m_desc = GetString(map, XML_RESULT_DESC_AREA_DESC);

                        // poziom umiejetnosci w grupie wiekowej
                        item.m_skill = GetDouble(map, XML_RESULT_DESC_AREA_SKILL);

                        NodeList childList = rootNode.getChildNodes();
                        if (childList != null) {
                            for (int n = 0; n < childList.getLength(); ++n) {
                                Node childNode = childList.item(n);
                                if (childNode.getNodeName().compareTo(XML_RESULT_DESC_AREA_PERC) == 0) {
                                    NamedNodeMap childMap = childNode.getAttributes();
                                    if (childMap != null) {
                                        ResultDescPercItem perc = new ResultDescPercItem();

                                        perc.m_level = 1;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L1);
                                        item.m_percent.add(perc);

                                        perc = new ResultDescPercItem();
                                        perc.m_level = 2;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L2);
                                        item.m_percent.add(perc);

                                        perc = new ResultDescPercItem();
                                        perc.m_level = 3;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L3);
                                        item.m_percent.add(perc);

                                        perc = new ResultDescPercItem();
                                        perc.m_level = 4;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L4);
                                        item.m_percent.add(perc);

                                        perc = new ResultDescPercItem();
                                        perc.m_level = 5;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L5);
                                        item.m_percent.add(perc);

                                        perc = new ResultDescPercItem();
                                        perc.m_level = 6;
                                        perc.m_percent = GetDouble(childMap, XML_RESULT_DESC_AREA_PERC_L6);
                                        item.m_percent.add(perc);

                                    }
                                }
                            }
                        }

                        m_resultDesc.put(item.m_id + item.m_band, item);
                    } catch (XMLFileException e) {
                        LogUtils.e(TAG, "result-desc discard: " + index, e);
                        item = null;
                    }
                }
            }
        }

        return true;
    }


    /**
     * Informacja o wynikach dla obszaru
     *
     *
     */
    public static final class ResultDescItem {
        /**
         * identyfikator obszaru
         */
        public String m_id = "";

        /**
         * nazwa obszaru
         */
        public String m_name = "";

        /**
         * grupa wiekowa
         */
        public int m_band = 0;

        /**
         * opis grupy wiekowej
         */
        public String m_desc = "";

        /**
         * poziom umijetności dla grupy wiekowej
         */
        public double m_skill = 0.0;

        /**
         * udział procentowy w poziomach umiejetności
         */
        public Vector<ResultDescPercItem> m_percent = new Vector<ResultDescPercItem>();
    }

    /**
     * Informacja o udziale procentowym dla danego poziomu wikowego
     *
     *
     */
    public static final class ResultDescPercItem {
        /**
         * numer poziomu
         */
        public int m_level = -1;

        /**
         * udział procentowy
         */
        public double m_percent = 0.0;
    }
}

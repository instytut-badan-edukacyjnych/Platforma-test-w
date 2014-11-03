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
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.XmlHelper;

/**
 * Bazowa klasa obsługi plikw XML
 *
 *
 */
public class BaseXMLFile {
    private static final String TAG = BaseXMLFile.class.toString();


    /**
     * formater pliku xml
     */
    protected static Transformer m_xmlTranformer = null;


    /**
     * Przygotowuje plik XML do parsowania danych
     *
     * @param a_xmlFile - nazwa pliku XML
     * @param a_xsdFile - nazwa pliku sprawdzajacego xsd
     * @return null jezeli otwarcie sie nie powiodło
     */
    @Deprecated
    public static Document OpenXMLFile(String a_xmlFile, String a_xsdFile) throws FileNotFoundException {
        return OpenXMLFile(new FileInputStream(a_xmlFile), a_xsdFile);
    }

    public static Document OpenXMLFile(InputStream xmlInputStream, String a_xsdFile) {
        LogUtils.d(TAG, "OpenXMLFile");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(xmlInputStream);

            // INFO_XSD wyłaczone ze względu na błedy w implementacji
            /*
                        File xsdFile = new File( LoremIpsumTabletApp.m_inputDir, a_xsdFile );
            			SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            			Source source = new StreamSource( xsdFile );
            			Schema schema = sf.newSchema( source );

            			Validator validator = schema.newValidator();

            			validator.validate(  new DOMSource( document ) );
            */
            // INFO_XSD <end>

            return document;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            LogUtils.e(TAG, e);
            return null;
        }
    }

    /**
     * Przygotowuje mechanizmy formatujace plik xml
     *
     * @return null jeżeli przygotowanie sie nie powiodło
     */
    public static Document createFactory() {

        try {
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(4);
            formatter.setMinimumFractionDigits(4);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();

            return doc;
        } catch (ParserConfigurationException e) {
            LogUtils.e(TAG, e);
            return null;
        }
    }

    /**
     * Zapisje plik xml w katalogu wyników
     *
     * @param a_filename - nazwa pliku
     * @param a_doc      - dokument do zapisu
     * @return true jeżeli zapis sie powiódł
     */
    protected static boolean SaveXMLFile(String a_filename, Document a_doc) {

        return saveXMLFile(a_filename, ServiceProvider.obtain().currentTaskSuite().getResultsDir(), a_doc);
    }


    /**
     * Zapisje plik xml
     *
     * @param a_filename - nazwa pliku
     * @param a_dir      - ściezka do pliku
     * @param a_doc      - dokument do zapisu
     * @return true jeżeli zapis sie powiódł
     */
    public static boolean saveXMLFile(String a_filename, String a_dir, Document a_doc) {
        try {
            File dir = new File(a_dir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(a_dir, a_filename);
            FileWriter writer = new FileWriter(file);
            writer.write(XmlHelper.convertDocumentToString(a_doc));
            writer.flush();
            writer.close();

            return true;
        } catch (TransformerException | IOException e) {
            LogUtils.e(TAG, e);
            return false;
        }
    }


    /**
     * Pobiera wartośc typu int z pliku
     *
     * @param a_map  - uchwyt do przeparsowanego pliku
     * @param a_name - nazwa pola
     * @return odczytana wartość
     * @throws XMLFileException
     */
    protected static int GetInteger(NamedNodeMap a_map, String a_name) throws XMLFileException {
        int value = 0;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Integer.parseInt(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
                throw new XMLFileException(a_name);
            }
        } else {
            throw new XMLFileException(a_name);
        }

        return value;
    }

    /**
     * Pobiera wartośc typu int z pliku, jezeli się nie uda podstawia domyslna wartość
     *
     * @param a_map      - uchwyt do przeparsowanego pliku
     * @param a_name     - nazwa pola
     * @param a_defValue - domyślna wartośc
     * @return wartośc pola
     */
    protected static int GetInteger(NamedNodeMap a_map, String a_name, int a_defValue) {
        int value = a_defValue;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Integer.parseInt(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
            }
        }

        return value;
    }

    /**
     * Pobiera wartośc typu long z pliku
     *
     * @param a_map  - uchwyt do przeparsowanego pliku
     * @param a_name - nazwa pola
     * @return odczytana wartość
     * @throws XMLFileException
     */
    protected static long GetLong(NamedNodeMap a_map, String a_name) throws XMLFileException {

        long value = 0;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Long.parseLong(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
                throw new XMLFileException(a_name);
            }
        } else {
            throw new XMLFileException(a_name);
        }

        return value;
    }

    /**
     * Pobiera wartośc typu long z pliku, jezeli się nie uda podstawia domyslna wartość
     *
     * @param a_map      - uchwyt do przeparsowanego pliku
     * @param a_name     - nazwa pola
     * @param a_defValue - domyslna wartość
     * @return wartośc pola
     */
    protected static long GetLong(NamedNodeMap a_map, String a_name, long a_defValue) {
        long value = a_defValue;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Long.parseLong(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
            }
        }

        return value;
    }

    /**
     * Pobiera wartośc typu double z pliku
     *
     * @param a_map  - uchwyt do przeparsowanego pliku
     * @param a_name - nazwa pola
     * @return odczytana wartość
     * @throws XMLFileException
     */
    protected static double GetDouble(NamedNodeMap a_map, String a_name) throws XMLFileException {
        double value = 0.0;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Double.parseDouble(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
                throw new XMLFileException(a_name);
            }
        } else {
            throw new XMLFileException(a_name);
        }

        return value;
    }

    /**
     * Pobiera wartośc typu double z pliku, jezeli się nie uda podstawia domyslna wartość
     *
     * @param a_map      - uchwyt do przeparsowanego pliku
     * @param a_name     - nazwa pola
     * @param a_defValue - domyslna wartość
     * @return wartośc pola
     */
    protected static double GetDouble(NamedNodeMap a_map, String a_name, double a_defValue) {
        double value = a_defValue;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            try {
                value = Double.parseDouble(node.getNodeValue());
            } catch (NumberFormatException e) {
                LogUtils.e(TAG, "Number format exception in " + a_name, e);
            }
        }

        return value;
    }

    /**
     * Pobiera wartośc typu string z pliku
     *
     * @param a_map  - uchwyt do przeparsowanego pliku
     * @param a_name - nazwa pola
     * @return odczytana wartość
     * @throws XMLFileException
     */
    protected static String GetString(NamedNodeMap a_map, String a_name) throws XMLFileException {
        String value = "";

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            value = node.getNodeValue();
        } else {
            throw new XMLFileException(a_name);
        }

        return value;
    }

    /**
     * Pobiera wartośc typu string z pliku, jezeli się nie uda podstawia domyslna wartość
     *
     * @param a_map      - uchwyt do przeparsowanego pliku
     * @param a_name     - nazwa pola
     * @param a_defValue - domyślnawartość
     * @return wartośc pola
     */
    public static String getString(NamedNodeMap a_map, String a_name, String a_defValue) {
        String value = a_defValue;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            value = node.getNodeValue();
        }

        return value;
    }

    /**
     * Sprawdza obecnośc flagi w polu z pliku
     *
     * @param a_map  - uchwyt do przeparsowanego pliku
     * @param a_name - nazwa pola
     * @param a_flag - flaga
     * @return true jeżeli flaga wystepuje w polu
     * @throws XMLFileException
     */
    protected static boolean GetFlag(NamedNodeMap a_map, String a_name, String a_flag) throws XMLFileException {
        boolean value = false;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            String str = node.getNodeValue();
            if (str.contains(a_flag)) {
                value = true;
            }
        } else {
            throw new XMLFileException(a_name);
        }

        return value;
    }

    /**
     * Sprawdza obecnośc flagi w polu z pliku, jezeli się nie uda podstawia domyslna wartość
     *
     * @param a_map      - uchwyt do przeparsowanego pliku
     * @param a_name     - nazwa pola
     * @param a_flag     - flaga
     * @param a_defValue - domyślnawartość
     * @return true jeżeli flaga wystepuje w polu
     */
    public static boolean GetFlag(NamedNodeMap a_map, String a_name, String a_flag, boolean a_defValue) {
        boolean value = a_defValue;

        Node node = a_map.getNamedItem(a_name);
        if (node != null) {
            String str = node.getNodeValue();
            if (str.contains(a_flag)) {
                value = true;
            }
        }

        return value;
    }

    /**
     * Zwraca wartośc TransformerFactory do obsługi plików xml
     *
     * @return wartość singletona TransformerFactory
     */
    protected static Transformer GetTransformer() {
        if (m_xmlTranformer == null) {
            try {
                TransformerFactory factory = TransformerFactory.newInstance();
                if (factory.getFeature(DOMSource.FEATURE) && factory.getFeature(StreamResult.FEATURE)) {
                    Properties format = new Properties();
                    format.setProperty(OutputKeys.INDENT, "yes");
                    format.setProperty(OutputKeys.METHOD, "xml");
                    format.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                    format.setProperty(OutputKeys.VERSION, "1.0");
                    format.setProperty(OutputKeys.ENCODING, "UTF-8");

                    Transformer trans = factory.newTransformer();
                    trans.setOutputProperties(format);

                    m_xmlTranformer = trans;
                } else {
                    LogUtils.e(TAG, "TransformerFactory.getFeature ERROR");
                }
            } catch (TransformerConfigurationException e) {
                LogUtils.e(TAG, e.toString());
            }
        }

        return m_xmlTranformer;
    }


    /**
     * Wyjatek obslugi plikow XML
     *
     *
     */
    public static final class XMLFileException extends Exception {

        private static final long serialVersionUID = -3094301496997071109L;


        /**
         * Wyjatek
         *
         * @param a_mess - komunikat
         */
        public XMLFileException(String a_mess) {

            super(a_mess);
        }
    }

}



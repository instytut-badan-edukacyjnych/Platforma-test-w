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

package pl.edu.ibe.loremipsum.tools;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import rx.functions.Func0;

public class XmlHelper {
    private static final String TAG = XmlHelper.class.getCanonicalName();

    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        return docBuilder.newDocument();
    }

    public static Document openDocument(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        return parser.parse(inputStream);
    }

    public static InputStream convertDocumentToStream(Document doc) throws TransformerException {
        return new ByteArrayInputStream(convertDocumentToString(doc).getBytes());
    }

    public static String convertDocumentToString(Document doc) throws TransformerException {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            if (e instanceof TransformerException) {
                throw (TransformerException) e;
            }
            LogUtils.e(TAG, "Unknown error", e);
            e.printStackTrace();
        }
        return "";
    }

    public static List<Element> getElements(Element element, String nodeName) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = element.getElementsByTagName(nodeName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

    public static Element getSingleElement(Element element, String nodeName) {
        return getSingleElement(() -> element.getElementsByTagName(nodeName), nodeName);
    }

    public static Element getSingleElement(Document doc, String nodeName) {
        return getSingleElement(() -> doc.getElementsByTagName(nodeName), nodeName);
    }

    private static Element getSingleElement(Func0<NodeList> nodesFunc, String nodeName) {
        Element collector = null;
        NodeList nodes = nodesFunc.call();
        if (nodes.getLength() > 0) {
            if (nodes.getLength() > 1) {
                Log.w(TaskSuiteConfig.class.getName(), "More than one node of " + nodeName);
            }
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                collector = (Element) nodes.item(0);
            }
        }
        return collector;
    }
}
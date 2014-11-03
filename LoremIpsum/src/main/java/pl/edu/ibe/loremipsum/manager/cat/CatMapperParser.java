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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.manager.MappingDependency;
import pl.edu.ibe.loremipsum.manager.rules.AggregateRule;
import pl.edu.ibe.loremipsum.manager.rules.LessOrEqualRule;
import pl.edu.ibe.loremipsum.manager.rules.MappingRule;
import pl.edu.ibe.loremipsum.manager.rules.MoreOrEqualRule;
import pl.edu.ibe.loremipsum.manager.rules.RegExpRule;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.XmlHelper;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.functions.Func1;

import static pl.edu.ibe.loremipsum.manager.MappingDependency.EXAMINEE_GENDER;
import static pl.edu.ibe.loremipsum.manager.cat.CatMappingDependency.EXAMINEE_AGE;
import static pl.edu.ibe.loremipsum.manager.cat.CatMappingDependency.INSTITUTION_CITY;
import static pl.edu.ibe.loremipsum.manager.cat.CatMappingDependency.INSTITUTION_POSTAL;

/**
 * Created by mikolaj on 18.04.14.
 * Parses cat data
 */
class CatMapperParser {

    public static final String XML_MAP_ROOT = "cat-data";
    public static final String XML_MAP_AREA = "area";
    public static final String XML_MAP_AREA_NAME = "name";
    public static final String XML_MAP_SCOPE = "scope";
    public static final String XML_MAP_SCOPE_SCORE = "score";

    public static final String XML_MAP_RULE_VALUE = "value";
    public static final String XML_MAP_RULE_FROM = "from";
    public static final String XML_MAP_RULE_TO = "to";
    public static final String XML_MAP_RULE_EXAMINEE_GENDER = "examinee-gender";
    public static final String XML_MAP_RULE_EXAMINEE_AGE = "examinee-age";
    public static final String XML_MAP_RULE_INST_POSTAL = "institution-postal";
    public static final String XML_MAP_RULE_INST_CITY = "institution-city";

    private static <T> Func1<MappingDependency, T> wrap(Func1<CatMappingDependency, T> func) {
        return dependency -> {
            if (dependency instanceof CatMappingDependency) {
                CatMappingDependency ca = (CatMappingDependency) dependency;
                return func.call(ca);
            } else {
                throw new IllegalArgumentException("Dependency must be type of " + CatMappingDependency.class);
            }
        };
    }

    public Tuple.Two<Map<String, Mapper>, MappingDependency> loadMappings(VirtualFile mappingFile) throws IOException, ParserConfigurationException, SAXException, ParseException {
        Document doc = XmlHelper.openDocument(mappingFile.getInputStream());
        Element root = XmlHelper.getSingleElement(doc, XML_MAP_ROOT);
        if (root == null) {
            throw new ParseException();
        }
        HashMap<String, Mapper> mappings = new HashMap<>();
        CatMappingDependency mappingDependency = new CatMappingDependency();
        NodeList areas = root.getElementsByTagName(XML_MAP_AREA);
        if (areas != null) {
            for (int i = 0; i < areas.getLength(); i++) {
                Node area = areas.item(i);
                if (area != null && area.getNodeType() == Node.ELEMENT_NODE) {
                    Mapper mapper = parseArea((Element) area, mappingDependency);
                    mappings.put(mapper.area, mapper);
                }
            }
        }
        return Tuple.Two.create(mappings, mappingDependency);
    }

    private Mapper parseArea(Element area, CatMappingDependency mappingDependency) throws ParseException {

        Mapper m = new Mapper();
        m.area = getAttributeOrThrow(area, XML_MAP_AREA_NAME);
        m.scopes = new ArrayList<>();

        List<Element> scopes = XmlHelper.getElements(area, XML_MAP_SCOPE);
        for (Element scope : scopes) {
            m.scopes.add(parseScope(scope, mappingDependency));
        }
        return m;
    }

    private Mapper.Scope parseScope(Element scope, CatMappingDependency mappingDependency) throws ParseException {
        Mapper.Scope s = new Mapper.Scope();
        try {
            s.score = Double.parseDouble(getAttributeOrThrow(scope, XML_MAP_SCOPE_SCORE));
        } catch (NumberFormatException e) {
            throw new ParseException();
        }
        s.rules = new ArrayList<>();
        s.rules.addAll(parseInstCity(XmlHelper.getElements(scope, XML_MAP_RULE_INST_CITY), mappingDependency));
        s.rules.addAll(parseInstPostal(XmlHelper.getElements(scope, XML_MAP_RULE_INST_POSTAL), mappingDependency));
        s.rules.addAll(parseExamineeAge(XmlHelper.getElements(scope, XML_MAP_RULE_EXAMINEE_AGE), mappingDependency));
        s.rules.addAll(parseExamineeGender(XmlHelper.getElements(scope, XML_MAP_RULE_EXAMINEE_GENDER), mappingDependency));

        return s;
    }

    private List<MappingRule> parseExamineeGender(List<Element> elements, CatMappingDependency mappingDependency) throws ParseException {
        List<MappingRule> rules = new ArrayList<>();
        for (Element element : elements) {
            mappingDependency.setRequired(EXAMINEE_GENDER);
            String gender = Gender.resolveGender(getAttributeOrThrow(element, XML_MAP_RULE_VALUE)).name();
            rules.add(makeRegExpRule("^" + gender + "$", inst -> inst.getString(EXAMINEE_GENDER).value));
        }
        return rules;
    }

    private List<MappingRule> parseExamineeAge(List<Element> elements, CatMappingDependency mappingDependency) throws ParseException {
        if (elements.size() > 0) {
            mappingDependency.setRequired(EXAMINEE_AGE);
        }
        List<MappingRule> rules = new ArrayList<>();
        for (Element element : elements) {
            try {
                MappingRule from = new MoreOrEqualRule(Long.valueOf(getAttributeOrThrow(element, XML_MAP_RULE_FROM)), wrap(inst -> inst.getLong(EXAMINEE_AGE).value));
                MappingRule to = new LessOrEqualRule(Long.valueOf(getAttributeOrThrow(element, XML_MAP_RULE_TO)), wrap(inst -> inst.getLong(EXAMINEE_AGE).value));
                rules.add(new AggregateRule(Arrays.asList(from, to)));
            } catch (NumberFormatException e) {
                throw new ParseException(e);
            }
        }
        return rules;
    }

    private List<MappingRule> parseInstCity(List<Element> elements, CatMappingDependency mappingDependency) throws ParseException {
        if (elements.size() > 0) {
            mappingDependency.setRequired(INSTITUTION_CITY);
        }
        return makeRegExpRule(elements, inst -> inst.getString(INSTITUTION_CITY).value);
    }

    private List<MappingRule> parseInstPostal(List<Element> elements, CatMappingDependency mappingDependency) throws ParseException {
        if (elements.size() > 0) {
            mappingDependency.setRequired(INSTITUTION_POSTAL);
        }
        return makeRegExpRule(elements, inst -> inst.getString(INSTITUTION_POSTAL).value);
    }

    private List<MappingRule> makeRegExpRule(List<Element> elements, Func1<CatMappingDependency, String> mapFunc) throws ParseException {
        List<MappingRule> rules = new ArrayList<>();
        for (Element element : elements) {
            rules.add(makeRegExpRule(getAttributeOrThrow(element, XML_MAP_RULE_VALUE), mapFunc));
        }
        return rules;
    }

    private MappingRule makeRegExpRule(String regExp, Func1<CatMappingDependency, String> mapFunc) throws ParseException {
        return new RegExpRule(regExp, wrap(mapFunc));
    }

    private String getAttributeOrThrow(Element element, String attr) throws ParseException {
        if (element.hasAttribute(attr)) {
            return element.getAttribute(attr);
        } else {
            throw new ParseException();
        }
    }

    public static class ParseException extends Exception {

        public ParseException() {
            super();
        }

        public ParseException(String detailMessage) {
            super(detailMessage);
        }

        public ParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ParseException(Throwable throwable) {
            super(throwable);
        }
    }
}

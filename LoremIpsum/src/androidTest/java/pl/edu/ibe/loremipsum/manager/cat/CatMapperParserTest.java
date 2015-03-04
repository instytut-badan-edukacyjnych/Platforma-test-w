package pl.edu.ibe.loremipsum.manager.cat;

import android.util.Log;

import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.manager.MappingDependency;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.XmlHelper;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static pl.edu.ibe.loremipsum.manager.cat.CatMappingDependency.*;

/**
 * Created by mikolaj on 18.04.14.
 */
public class CatMapperParserTest extends TestCase {

    private Document doc;
    private CatMapperParser parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new CatMapperParser();
    }

    public void testNoScopesShouldGiveZero() throws Exception {
        Element root = prepareRoot();
        addArea(root, "m");

        //execute
        Map<String, Mapper> mappings = parser.loadMappings(prepareFile()).first;

        //verify
        Mapper mapper = mappings.get("m");

        assertNotNull(mapper);
        assertEquals("m", mapper.area);
        assertEquals(0d, mapper.getStartValue(mock(MappingDependency.class)));
    }

    public void testNoRulesShouldReturnDefault() throws Exception {
        Element root = prepareRoot();
        Element areaM = addArea(root, "m");
        addScope(areaM, "0.5");

        //execute
        Map<String, Mapper> mappings = parser.loadMappings(prepareFile()).first;

        //verify
        Mapper mapper = mappings.get("m");

        assertNotNull(mapper);
        assertEquals("m", mapper.area);
        assertEquals(0.5d, mapper.getStartValue(mock(MappingDependency.class)));
    }

    public void testInstitutionCityMatch() throws Exception {
        Element root = prepareRoot();
        Element areaM = addArea(root, "m");
        Element scope = addScope(areaM, "0.5");
        addInstitutionCity(scope, "^Gd");

        //execute
        Tuple.Two<Map<String, Mapper>, MappingDependency> tuple = parser.loadMappings(prepareFile());
        Map<String, Mapper> mappings = tuple.first;

        //verify
        Mapper mapper = mappings.get("m");
        assertNotNull(mapper);
        assertEquals("m", mapper.area);

        MappingDependency mappingDependency = tuple.second;
        assertTrue(mappingDependency.isRequired(INSTITUTION_CITY));
        mappingDependency.getString(INSTITUTION_CITY).value = "Warszawa";
        assertEquals(0d, mapper.getStartValue(mappingDependency));
        mappingDependency.getString(INSTITUTION_CITY).value = "Gdańsk";
        assertEquals(0.5d, mapper.getStartValue(mappingDependency));
        mappingDependency.getString(INSTITUTION_CITY).value = "gdańsk";
        assertEquals(0d, mapper.getStartValue(mappingDependency));
    }

    public void testInstitutionPostalMatch() throws Exception {
        Element root = prepareRoot();
        Element areaM = addArea(root, "m");
        Element scope = addScope(areaM, "0.5");
        addInstitutionPostal(scope, "^80");

        //execute
        Tuple.Two<Map<String, Mapper>, MappingDependency> tuple = parser.loadMappings(prepareFile());
        Map<String, Mapper> mappings = tuple.first;

        //verify
        Mapper mapper = mappings.get("m");
        assertNotNull(mapper);
        assertEquals("m", mapper.area);

        MappingDependency mappingDependency = tuple.second;
        assertTrue(mappingDependency.isRequired(INSTITUTION_POSTAL));
        mappingDependency.getString(INSTITUTION_POSTAL).value = "50-545";
        assertEquals(0d, mapper.getStartValue(mappingDependency));
        mappingDependency.getString(INSTITUTION_POSTAL).value = "80-100";
        assertEquals(0.5d, mapper.getStartValue(mappingDependency));
    }

    public void testExamineeAgeMatch() throws Exception {
        Element root = prepareRoot();
        Element areaM = addArea(root, "m");
        Element scopeYoung = addScope(areaM, "0.25");
        addAge(scopeYoung, 20, 30);
        Element scopeMiddle = addScope(areaM, "0.5");
        addAge(scopeMiddle, 40, 50);
        @SuppressWarnings("unused")
        Element scopeOthers = addScope(areaM, "0.15");

        //execute
        Tuple.Two<Map<String, Mapper>, MappingDependency> tuple = parser.loadMappings(prepareFile());
        Map<String, Mapper> mappings = tuple.first;

        //verify
        Mapper mapper = mappings.get("m");
        assertNotNull(mapper);
        assertEquals("m", mapper.area);

        MappingDependency mappingDependency = tuple.second;
        assertTrue(mappingDependency.getLong(EXAMINEE_AGE).required);
        mappingDependency.getLong(EXAMINEE_AGE).value = 10l;
        assertEquals(0.15d, mapper.getStartValue(mappingDependency));
        mappingDependency.getLong(EXAMINEE_AGE).value = 20l;
        assertEquals(0.25d, mapper.getStartValue(mappingDependency));
        mappingDependency.getLong(EXAMINEE_AGE).value = 50l;
        assertEquals(0.5d, mapper.getStartValue(mappingDependency));
        mappingDependency.getLong(EXAMINEE_AGE).value = 51l;
        assertEquals(0.15d, mapper.getStartValue(mappingDependency));
    }

    public void testExamineeGenderMatch() throws Exception {
        Element root = prepareRoot();
        Element areaM = addArea(root, "m");
        Element scopeMale = addScope(areaM, "0.25");
        addGender(scopeMale, "m");
        Element scopeFemale = addScope(areaM, "0.5");
        addGender(scopeFemale, "F");

        //execute
        Tuple.Two<Map<String, Mapper>, MappingDependency> tuple = parser.loadMappings(prepareFile());
        Map<String, Mapper> mappings = tuple.first;

        //verify
        Mapper mapper = mappings.get("m");
        assertNotNull(mapper);
        assertEquals("m", mapper.area);

        MappingDependency mappingDependency = tuple.second;
        assertTrue(mappingDependency.getLong(EXAMINEE_GENDER).required);
        mappingDependency.getString(EXAMINEE_GENDER).value = Gender.FEMALE.name();
        assertEquals(0.5d, mapper.getStartValue(mappingDependency));
        mappingDependency.getString(EXAMINEE_GENDER).value = Gender.MALE.name();
        assertEquals(0.25d, mapper.getStartValue(mappingDependency));
    }

    private VirtualFile prepareFile() throws IOException, TransformerException {
        VirtualFile mappingFile = mock(VirtualFile.class);
        when(mappingFile.getInputStream()).thenReturn(XmlHelper.convertDocumentToStream(doc));
        LogUtils.v("test", XmlHelper.convertDocumentToString(doc));
        return mappingFile;
    }

    private Element prepareRoot() throws ParserConfigurationException {
        doc = XmlHelper.newDocument();
        Element root = doc.createElement(CatMapperParser.XML_MAP_ROOT);
        doc.appendChild(root);
        return root;
    }

    private Element addArea(Node parent, String m) {
        Element e = doc.createElement(CatMapperParser.XML_MAP_AREA);
        e.setAttribute(CatMapperParser.XML_MAP_AREA_NAME, m);
        parent.appendChild(e);
        return e;
    }

    private Element addScope(Node parent, String score) {
        Element e = doc.createElement(CatMapperParser.XML_MAP_SCOPE);
        e.setAttribute(CatMapperParser.XML_MAP_SCOPE_SCORE, score);
        parent.appendChild(e);
        return e;
    }

    private Element addInstitutionCity(Node parent, String regEx) {
        return addValueNode(parent, CatMapperParser.XML_MAP_RULE_INST_CITY, regEx);
    }

    private Element addInstitutionPostal(Node parent, String regEx) {
        return addValueNode(parent, CatMapperParser.XML_MAP_RULE_INST_POSTAL, regEx);
    }

    private Element addAge(Node parent, int from, int to) {
        Element e = doc.createElement(CatMapperParser.XML_MAP_RULE_EXAMINEE_AGE);
        e.setAttribute(CatMapperParser.XML_MAP_RULE_FROM, String.valueOf(from));
        e.setAttribute(CatMapperParser.XML_MAP_RULE_TO, String.valueOf(to));
        parent.appendChild(e);
        return e;
    }
    private Element addGender(Node parent, String gender) {
        return addValueNode(parent, CatMapperParser.XML_MAP_RULE_EXAMINEE_GENDER, gender);
    }

    private Element addValueNode(Node parent, String nodeName, String value) {
        Element e = doc.createElement(nodeName);
        e.setAttribute(CatMapperParser.XML_MAP_RULE_VALUE, value);
        parent.appendChild(e);
        return e;
    }
}

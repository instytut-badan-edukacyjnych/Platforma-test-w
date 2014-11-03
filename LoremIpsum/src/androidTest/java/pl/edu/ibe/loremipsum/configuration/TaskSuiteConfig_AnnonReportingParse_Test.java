package pl.edu.ibe.loremipsum.configuration;

import pl.edu.ibe.loremipsum.tools.XmlHelper;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

import junit.framework.TestCase;

import org.mockito.Matchers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mikolaj on 14.04.14.
 */
public class TaskSuiteConfig_AnnonReportingParse_Test extends TestCase {

    public void testParseAllFeatures() throws Exception {

        Document doc = XmlHelper.newDocument();

        Element raporting = makeFullRaport(doc);
        doc.appendChild(raporting);

        //execute
        TaskSuiteConfig taskSuiteConfig = new TaskSuiteConfig();
        boolean loadResult = taskSuiteConfig.loadConfigXML(makeVirtualFile(doc));

        //verify
        assertEquals(true, loadResult);
        CollectorConfig config = taskSuiteConfig.collectorConfig;
        assertNotNull(config.targetUrl);
        assertTrue(config.isRaportingRequired);
        assertTrue(config.sendExamineeBirthday);
        assertTrue(config.sendExamineeGender);
        assertTrue(config.sendExamineeId);
        assertTrue(config.sendInstitutionId);
        assertTrue(config.sendResearcherId);
        assertEquals(CollectorConfig.RaportType.TASKS_AND_SUMMARY, config.raportType);
    }

    public void testEmptyRaporting() throws Exception {

        Document doc = XmlHelper.newDocument();
        Element raporting = doc.createElement(TaskSuiteConfig.XML_RAPORTING);
        raporting.setAttribute(TaskSuiteConfig.XML_RAPORTING_URL, "some url");
        doc.appendChild(raporting);

        //execute
        TaskSuiteConfig taskSuiteConfig = new TaskSuiteConfig();
        boolean loadResult = taskSuiteConfig.loadConfigXML(makeVirtualFile(doc));

        //verify
        assertEquals(true, loadResult);
        CollectorConfig config = taskSuiteConfig.collectorConfig;
        assertTrue(config.isRaportingRequired);
        assertFalse(config.sendExamineeBirthday);
        assertFalse(config.sendExamineeGender);
        assertFalse(config.sendExamineeId);
        assertFalse(config.sendInstitutionId);
        assertFalse(config.sendResearcherId);
        assertEquals(CollectorConfig.RaportType.NONE, config.raportType);
    }

    public void testRaportingWithUrlIsNotSend() throws Exception {

        Document doc = XmlHelper.newDocument();
        Element raporting = makeFullRaport(doc);
        raporting.removeAttribute(TaskSuiteConfig.XML_RAPORTING_URL);
        doc.appendChild(raporting);

        //execute
        TaskSuiteConfig taskSuiteConfig = new TaskSuiteConfig();
        boolean loadResult = taskSuiteConfig.loadConfigXML(makeVirtualFile(doc));

        //verify
        assertEquals(true, loadResult);
        CollectorConfig config = taskSuiteConfig.collectorConfig;
        assertFalse(config.isRaportingRequired);
        assertFalse(config.sendExamineeBirthday);
        assertFalse(config.sendExamineeGender);
        assertFalse(config.sendExamineeId);
        assertFalse(config.sendInstitutionId);
        assertFalse(config.sendResearcherId);
        assertEquals(CollectorConfig.RaportType.NONE, config.raportType);
    }

    public void testParseNoRaporting() throws Exception {

        Document doc = XmlHelper.newDocument();
        Element configRoot = doc.createElement("irrelevant_node");
        doc.appendChild(configRoot);

        //execute
        TaskSuiteConfig taskSuiteConfig = new TaskSuiteConfig();
        boolean loadResult = taskSuiteConfig.loadConfigXML(makeVirtualFile(doc));

        //verify
        assertEquals(true, loadResult);
        CollectorConfig config = taskSuiteConfig.collectorConfig;
        assertFalse(config.isRaportingRequired);
    }

    public void testParseNoXmlFile() throws Exception {

        Document doc = XmlHelper.newDocument();
        Element configRoot = doc.createElement("irrelevant_node");
        doc.appendChild(configRoot);

        VirtualFile baseFile = makeVirtualFile(doc);
        VirtualFile rapotingFile = mock(VirtualFile.class);
        when(baseFile.getChildFile(TaskSuiteConfig.APP_RAPORTING_XML_FILENAME)).thenReturn(rapotingFile);
        when(rapotingFile.getInputStream()).thenThrow(new IOException("testing exception"));
        //execute
        TaskSuiteConfig taskSuiteConfig = new TaskSuiteConfig();
        boolean loadResult = taskSuiteConfig.loadConfigXML(baseFile);

        //verify
        assertEquals(true, loadResult);
        CollectorConfig config = taskSuiteConfig.collectorConfig;
        assertFalse(config.isRaportingRequired);
    }

    private Element makeFullRaport(Document doc) {
        Element raporting = doc.createElement(TaskSuiteConfig.XML_RAPORTING);
        raporting.setAttribute(TaskSuiteConfig.XML_RAPORTING_URL, "some url");

        Element resercher = doc.createElement(TaskSuiteConfig.XML_RAPORTING_RESEARCHER);
        resercher.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_ID));
        raporting.appendChild(resercher);

        Element examinee = doc.createElement(TaskSuiteConfig.XML_RAPORTING_EXAMINEE);
        examinee.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_ID));
        examinee.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_EXAMINEE_GENDER));
        examinee.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_EXAMINEE_BIRTHDAY));
        raporting.appendChild(examinee);

        Element institution = doc.createElement(TaskSuiteConfig.XML_RAPORTING_INSTITUTION);
        institution.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_ID));
        raporting.appendChild(institution);

        Element raportType = doc.createElement(TaskSuiteConfig.XML_RAPORTING_RAPORT_CONTENT);
        raportType.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_RAPORT_SUMMARY));
        raportType.appendChild(doc.createElement(TaskSuiteConfig.XML_RAPORTING_RAPORT_TASKS));
        raporting.appendChild(raportType);
        return raporting;
    }

    private VirtualFile makeVirtualFile(Document doc) throws IOException, TransformerException {
        VirtualFile virtualFile = mock(VirtualFile.class);
        when(virtualFile.getChildFile(Matchers.anyString())).thenReturn(virtualFile);
        String string = XmlHelper.convertDocumentToString(doc);
        when(virtualFile.getInputStream()).thenReturn(new ByteArrayInputStream(string.getBytes()), new ByteArrayInputStream(string.getBytes()));
        return virtualFile;
    }
}

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

package pl.edu.ibe.loremipsum.task.management.collector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import pl.edu.ibe.loremipsum.configuration.CollectorConfig;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.XmlHelper;

/**
 * Created by mikolaj on 15.04.14.
 */
public class RaportPreparator {

    private final CollectorConfig collectorConfig;
    private final CurrentTaskSuiteService.TestRunData testRunData;
    private final TestResult testResult;

    public RaportPreparator(CollectorConfig collectorConfig, CurrentTaskSuiteService.TestRunData testRunData, TestResult testResult) {
        this.collectorConfig = collectorConfig;
        this.testRunData = testRunData;
        this.testResult = testResult;
    }

    public ZipOutputStream make(OutputStream os) throws IOException, ParserConfigurationException, TransformerException {
        ZipOutputStream zipStream = new ZipOutputStream(os);
        saveReports(zipStream);
        addXmlFile(zipStream, makeUserData(), "user.xml");

        return zipStream;
    }

    private Document makeUserData() throws IOException, ParserConfigurationException, TransformerException {

        Document doc = XmlHelper.newDocument();
        Element root = doc.createElement("data");
        doc.appendChild(root);

        if (collectorConfig.sendResearcherId) {
            Element researcher = doc.createElement("researcher");
            root.appendChild(researcher);
            researcher.setAttribute("id", testRunData.getExaminee().getResearcherJoinExamineeList().get(0).getResearcher().getTextId());// TODO AR: is this OK? how we should reach researcher?
        }
        if (collectorConfig.sendInstitutionId) {
            Element institution = doc.createElement("institution");
            root.appendChild(institution);
            institution.setAttribute("id", testRunData.getInstitution().getTextId());
        }
        if (collectorConfig.sendExamineeId || collectorConfig.sendExamineeGender || collectorConfig.sendExamineeBirthday) {
            Element examinee = doc.createElement("examinee");
            root.appendChild(examinee);
            if (collectorConfig.sendExamineeId) {
                examinee.setAttribute("id", testRunData.getExaminee().getTextId());
            }
            if (collectorConfig.sendExamineeGender) {
                examinee.setAttribute("gender", testRunData.getExaminee().getGender());
            }
            if (collectorConfig.sendExamineeBirthday) {
                String birthday = "";
                try {
                    birthday = TimeUtils.dateToString(testRunData.getExaminee().getBirthday(), TimeUtils.defaultPatern);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (testRunData.getExaminee().getBirthday() != null) {
                        birthday = testRunData.getExaminee().getBirthday().toString();
                    }
                }
                examinee.setAttribute("birthday", birthday);
            }
        }

        return doc;
    }

    private void saveReports(ZipOutputStream zipStream) throws ParserConfigurationException, IOException, TransformerException {
        switch (collectorConfig.raportType) {
            case TASKS_AND_SUMMARY:
                saveSummary(zipStream);
                saveTasks(zipStream);
                break;
            case JUST_SUMMARY:
                saveSummary(zipStream);
                break;
            case NONE:
                break;
        }
    }

    private void saveSummary(ZipOutputStream zipStream) throws IOException, ParserConfigurationException, TransformerException {

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);

        Document document = XmlHelper.newDocument();
        Element root;
        if (LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR1 || LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR2) {
            root = document.createElement("results");
        } else {
            root = document.createElement(TestResult.XML_TEST_ROOT);
        }


        document.appendChild(root);
        for (TestResult.ResultItem item : testResult.m_result.values()) {
            Element sco = document.createElement(TestResult.XML_TEST_RESULT);
            sco.setAttribute(TestResult.XML_TEST_RESULT_AREA, item.m_area);
            sco.setAttribute(TestResult.XML_TEST_RESULT_STATUS, LoremIpsumApp.TestStatusToString(item.m_status));
            sco.setAttribute(TestResult.XML_TEST_RESULT_LENGTH, Integer.toString(item.m_length));
            sco.setAttribute(TestResult.XML_TEST_RESULT_THETA, numberFormat.format(item.m_theta));
            sco.setAttribute(TestResult.XML_TEST_RESULT_SE, numberFormat.format(item.m_se));
            root.appendChild(sco);
        }

        addXmlFile(zipStream, document, "summary.xml");
    }

    private void saveTasks(ZipOutputStream zipStream) throws IOException, ParserConfigurationException, TransformerException {

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);

        Document document = XmlHelper.newDocument();
        Element root;
        if (LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR1 || LoremIpsumApp.obtain().getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().testType == TaskSuiteConfig.TestType.TPR2) {
            root = document.createElement("results");
        } else {
            root = document.createElement(TestResult.XML_TEST_ROOT);
        }
        document.appendChild(root);
        for (TestResult.ResultTaskItem item : testResult.m_tasks) {
            Element tas = document.createElement(TestResult.XML_TEST_TASKS);
            tas.setAttribute(TestResult.XML_TEST_TASKS_NAME, item.m_taskName);
            tas.setAttribute(TestResult.XML_TEST_TASKS_AREA, item.m_area);
            tas.setAttribute(TestResult.XML_TEST_TASKS_NR, Integer.toString(item.m_nr));
            tas.setAttribute(TestResult.XML_TEST_TASKS_MARK, Double.toString(item.m_mark));
            tas.setAttribute(TestResult.XML_TEST_TASKS_ANSWER, item.m_answer);
            tas.setAttribute(TestResult.XML_TEST_TASKS_DURATION, Integer.toString(item.m_taskDuration));

            tas.setAttribute(TestResult.XML_TEST_TASKS_THETA, numberFormat.format(item.m_theta));
            tas.setAttribute(TestResult.XML_TEST_TASKS_SE, numberFormat.format(item.m_se));

            root.appendChild(tas);
        }
        addXmlFile(zipStream, document, "tasks.xml");
    }

    private void addXmlFile(ZipOutputStream zipStream, Document doc, String fileName) throws IOException, TransformerException {
        ZipEntry ze = new ZipEntry(fileName);
        zipStream.putNextEntry(ze);
        zipStream.write(XmlHelper.convertDocumentToString(doc).getBytes());
        zipStream.closeEntry();
    }
}

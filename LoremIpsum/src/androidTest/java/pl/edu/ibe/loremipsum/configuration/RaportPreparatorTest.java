package pl.edu.ibe.loremipsum.configuration;

import android.test.AndroidTestCase;

import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.management.collector.RaportPreparator;
import pl.edu.ibe.loremipsum.util.MockitoSetUpUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Mockito.mock;

/**
 * Created by mikolaj on 15.04.14.
 */
public class RaportPreparatorTest extends AndroidTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoSetUpUtils.setUp(getContext());
    }

    public void testParse() throws Exception {

        CollectorConfig collectorConfig = new CollectorConfig();
        collectorConfig.raportType = CollectorConfig.RaportType.TASKS_AND_SUMMARY;
        TestResult testResult = new TestResult();
        testResult.m_result.put("a1", new TestResult.ResultItem());
        testResult.m_result.put("a2", new TestResult.ResultItem());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //exec
        RaportPreparator raportPreparator = new RaportPreparator(collectorConfig, mock(CurrentTaskSuiteService.TestRunData.class), testResult);
        raportPreparator.make(bos).close();

        // verify
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ArrayList<String> files = new ArrayList<>();
        ZipEntry zipEntry;
        while ((zipEntry = zin.getNextEntry()) != null) {
            files.add(zipEntry.getName());
        }

        assertTrue(files.contains("summary.xml"));
        assertTrue(files.contains("tasks.xml"));
        assertTrue(files.contains("user.xml"));
    }
}

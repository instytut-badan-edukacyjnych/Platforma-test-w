package pl.edu.ibe.loremipsum.task.management.collector;

import org.mockito.Matchers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import pl.edu.ibe.loremipsum.DbDependedTest;
import pl.edu.ibe.loremipsum.db.schema.DaoSession;
import pl.edu.ibe.loremipsum.db.schema.ResultsQueue;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProviderBuilder;
import pl.edu.ibe.loremipsum.util.MockitoSetUpUtils;
import rx.functions.Func1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mikolaj on 10.04.14.
 */
public class CollectorTest extends DbDependedTest {

    private Collector collector;
    private NetworkSupport networkSupport;
    private NetworkChangeReceiver.NetworkUtil networkUtil;
    private File tmpStorage;
    private File f1;
    private File f2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoSetUpUtils.setUp(getInstrumentation().getTargetContext());
        networkSupport = mock(NetworkSupport.class);
        networkUtil = mock(NetworkChangeReceiver.NetworkUtil.class);
        when(networkUtil.getConnectivityStatus()).thenReturn(NetworkChangeReceiver.NetworkUtil.TYPE_WIFI);
        collector = new Collector(getServiceProvider());

        tmpStorage = new File(getStorageDir(), "tmp");
        tmpStorage.mkdirs();

        assertTrue(tmpStorage.isDirectory());

        System.out.println(tmpStorage);
        f1 = createFile("f1");
        f2 = createFile("f2");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
    }

    private File createFile(String name) throws IOException {
        File file = new File(tmpStorage, name);
        String loremIpsum = "Lorem ipsum dolor sit amet.";
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(loremIpsum.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return file;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        f1.delete();
        f2.delete();
        tmpStorage.delete();
        assertFalse(f1.exists());
        assertFalse(f2.exists());
        assertFalse(tmpStorage.exists());
    }

    @Override
    protected Func1<ServiceProviderBuilder, ServiceProviderBuilder> additionalBuilderParams() {
        return builder -> {
            builder.setNetworkSupport(networkSupport);
            builder.setNetworkUtil(networkUtil);
            return builder;
        };
    }

    public void testRequestAdded() throws Exception {
        ResultsQueue res = new ResultsQueue();
        res.setFileName("f1");
        res.setSubmitUrl("someUrl");

        //exec
        collector.addResult(res);

        //verify
        List<ResultsQueue> data = getDb().getDaoSession().getResultsQueueDao().loadAll();
        assertEquals(1, data.size());
        assertEquals("f1", data.get(0).getFileName());
        assertEquals("someUrl", data.get(0).getSubmitUrl());
    }

    public void testMalformedUrlsRemoved() throws Exception {
        ResultsQueue res1 = new ResultsQueue();
        res1.setFileName("f1");
        res1.setSubmitUrl("someUrl1");
        DaoSession daoSession = getDb().getDaoSession();
        daoSession.insert(res1);

        //exec
        assertEquals(1, getDb().getDaoSession().getResultsQueueDao().loadAll().size());
        assertTrue(collector.send().toBlockingObservable().single());

        //verify
        List<ResultsQueue> data = getDb().getDaoSession().getResultsQueueDao().loadAll();
        assertEquals(0, data.size());

        verify(networkSupport, never()).open(Matchers.any(URL.class));
    }

    public void testRequestSent() throws Exception {
        ResultsQueue res1 = new ResultsQueue();
        res1.setFileName(f1.getAbsolutePath());
        res1.setSubmitUrl("http://some.url/1");
        ResultsQueue res2 = new ResultsQueue();
        res2.setFileName(f2.getAbsolutePath());
        res2.setSubmitUrl("http://some.url/2");
        DaoSession daoSession = getDb().getDaoSession();
        daoSession.insert(res1);
        daoSession.insert(res2);

        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        OutputStream outputStream = mock(OutputStream.class);
        when(urlConnection.getOutputStream()).thenReturn(outputStream);
        when(urlConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(networkSupport.open(Matchers.any(URL.class))).thenReturn(urlConnection);

        //exec
        assertEquals(2, getDb().getDaoSession().getResultsQueueDao().loadAll().size());
        assertTrue(collector.send().toBlockingObservable().single());

        //verify
        List<ResultsQueue> data = getDb().getDaoSession().getResultsQueueDao().loadAll();
        assertEquals(1, data.size());
        assertEquals(Long.valueOf(2l), data.get(0).getId());
        assertEquals(false, data.get(0).getMarkToDelete());
        assertEquals(1, data.get(0).getAttempts());
        assertNotNull(data.get(0).getLastAttemptDate());
    }


}

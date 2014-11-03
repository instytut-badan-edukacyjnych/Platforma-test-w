package pl.edu.ibe.loremipsum.task.management;

import pl.edu.ibe.loremipsum.task.management.storage.DefaultEncryptionLayer;
import pl.edu.ibe.loremipsum.task.management.storage.FilesystemTaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuite;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteTest extends BaseInstrumentationTestCase {
    private static final String TAG = TaskSuiteTest.class.toString();

    public void testInstallSuite() throws IOException, Exceptions.TaskStorageException {
        // create new storage working on filesystem
        final TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        assertEquals(0, storage.size()); //...which should be empty on start
        assertDirectoryContentEquals(Collections.<String>emptyList(), getStorageDir());

        // create and install empty suite. It will fail, as suite does not contain any version
        final TaskSuite foo = new TaskSuite("foo");
        assertThrows(Exceptions.SuiteHasNoVersions.class, () -> storage.installSuite(foo));
        assertEquals(0, storage.size());
        assertDirectoryContentEquals(Collections.<String>emptyList(), getStorageDir());

        // create suite and addVersion version it should then be installed successfully
        foo.createVersion("1.0");
        assertThrows(Exceptions.VersionNotFound.class, () -> {
            foo.getVersion("asdasad");
        });
        assertNotNull(foo.getVersion("1.0"));
        storage.installSuite(foo);
        assertEquals(1, storage.size());
        assertThrows(Exceptions.SuiteNotFound.class, () -> {
            storage.getSuite("asdasd");
        });
        assertNotNull(storage.getSuite("foo"));
        assertThrows(Exceptions.VersionNotFound.class, () -> {
            foo.getVersion("asdasad");
        });
        assertNotNull(storage.getSuite("foo").getVersion("1.0"));
        assertDirectoryContentEquals(Arrays.asList("foo"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "foo"));

        // now, it is impossible to install the suite with the same name again
        TaskSuite another_foo = new TaskSuite("foo");
        another_foo.createVersion("1.0");
        assertThrows(Exceptions.SuiteAlreadyExistsException.class, () -> storage.installSuite(foo));
    }

    public void testWriteSuiteData() throws IOException, Exceptions.TaskStorageException, InterruptedException {
        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        storage.installCipher(new DefaultEncryptionLayer("the sample key".getBytes()));
        TaskSuite foo = new TaskSuite("foo");
        final TaskSuiteVersion foo_1_0 = foo.createVersion("1.0");

        assertThrows(Exceptions.SuiteNotInstalled.class, foo_1_0::getOutputStream);

        storage.installSuite(foo);

        File foo_1_0_dir = new File(getStorageDir() + File.separator + "foo" + File.separator + "1.0");
        assertDirectoryContentEquals(Arrays.asList(new String[]{}), foo_1_0_dir);

        Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>>
                output = foo_1_0.getOutputStream();

        executeLast(
                CopyStream.copyStream(new FileInputStream(testFiles[0]), output.first).execute()
        );

        output.second.subscribeOn(Schedulers.newThread()).subscribe(p -> {
            LogUtils.v(TAG, "Output processing single progress: " + (p != null ? p.getCount() : -1));
        }, t -> {
            LogUtils.e(TAG, "Exception during output processing", t);
        }, () -> {
            LogUtils.e(TAG, "output processing done");
        });

        output.third.subscribeOn(Schedulers.newThread()).toBlockingObservable().forEach(e -> {
            LogUtils.v(TAG, "Output processing next file: " + (e != null ? e.getName() : "no data"));
        });

        assertDirectoryContentEquals(Arrays.asList("data.bin"), foo_1_0_dir);
        assertFilesContentsNotEquals(testFiles[0], new File(foo_1_0_dir, "data.bin"));
    }

    private void buildSampleSuiteStructure() throws IOException, Exceptions.TaskStorageException {
        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        storage.installCipher(new DefaultEncryptionLayer("the sample key".getBytes()));
        TaskSuite foo = new TaskSuite("foo");
        TaskSuiteVersion foo_1_0 = foo.createVersion("1.0");
        storage.installSuite(foo);
        Tuple.Three<OutputStream, Observable<CopyStream.CopyProgress>, Observable<ZipEntry>> output = foo_1_0.getOutputStream();
        executeLast(
                CopyStream.copyStream(new FileInputStream(testFiles[0]), output.first).execute()
        );
        executeLast(output.third.subscribeOn(Schedulers.newThread()));
    }

    public void testReadSuiteData() throws IOException, Exceptions.TaskStorageException {
        buildSampleSuiteStructure();

        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        storage.installCipher(new DefaultEncryptionLayer("the sample key".getBytes()));
        assertTrue(storage.hasSuite("foo"));
        TaskSuite foo = storage.getSuite("foo");
        assertTrue(foo.hasVersion("1.0"));
        TaskSuiteVersion foo_1_0 = foo.getVersion("1.0");

        ZipInputStream zis = new ZipInputStream(new FileInputStream(testFiles[0]));
        try {
            while (true) {
                ZipEntry zisEntry = zis.getNextEntry();
                if (zisEntry == null)
                    break;

                VirtualFile vfile = foo_1_0.getRoot().getChildFile(zisEntry.getName());

                if(!zisEntry.isDirectory()) {
                    ByteArrayOutputStream expected = new ByteArrayOutputStream();
                    executeLast(
                            CopyStream.copyStream(zis, expected)
                                    .setCloseInput(false).execute()
                    );
                    zis.closeEntry();

                    assertTrue(vfile.isFile());
                    assertEquals(zisEntry.getSize(), vfile.length());
                    InputStream fileData = vfile.getInputStream();

                    ByteArrayOutputStream actual = new ByteArrayOutputStream();
                    executeLast(CopyStream.copyStream(fileData, actual).execute());

                    assertTrue(Arrays.equals(expected.toByteArray(), actual.toByteArray()));
                } else {
                    assertTrue(vfile.isDirectory());
                }
            }
        } finally {
            zis.close();
        }
    }

    public void testUninstallSuiteVersion() throws IOException, Exceptions.TaskStorageException {
        buildSampleSuiteStructure();

        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        assertTrue(storage.hasSuite("foo"));
        TaskSuite foo = storage.getSuite("foo");
        assertTrue(foo.hasVersion("1.0"));
        TaskSuiteVersion foo_1_0 = foo.getVersion("1.0");
        foo_1_0.uninstall();

        assertEquals(0, storage.size());
        assertFalse(storage.hasSuite("foo"));
        assertDirectoryContentEquals(Collections.<String>emptyList(), getStorageDir());
    }

    public void testUninstallSuite() throws IOException, Exceptions.TaskStorageException {
        buildSampleSuiteStructure();

        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
        assertTrue(storage.hasSuite("foo"));
        TaskSuite foo = storage.getSuite("foo");
        foo.uninstall();

        assertEquals(0, storage.size());
        assertFalse(storage.hasSuite("foo"));
        assertDirectoryContentEquals(Collections.<String>emptyList(), getStorageDir());
    }

    public void testInstallMultipleSuites() throws Exceptions.TaskStorageException {
        final TaskStorage storage = new FilesystemTaskStorage(getStorageDir());

        TaskSuite foo = new TaskSuite("foo");
        foo.createVersion("1.0");
        storage.installSuite(foo);

        TaskSuite bar = new TaskSuite("bar");
        bar.createVersion("1.3");
        storage.installSuite(bar);

        assertEquals(2, storage.size());
        assertNotNull(storage.getSuite("foo"));
        assertNotNull(storage.getSuite("foo").getVersion("1.0"));
        assertDirectoryContentEquals(Arrays.asList("foo", "bar"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "foo"));
        assertNotNull(storage.getSuite("bar"));
        assertNotNull(storage.getSuite("bar").getVersion("1.3"));
        assertDirectoryContentEquals(Arrays.asList("foo", "bar"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.3"), new File(getStorageDir(), "bar"));
    }

    public void testMultipleVersions() throws Exceptions.TaskStorageException {
        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            TaskSuite foo = new TaskSuite("foo");
            foo.createVersion("1.0");
            storage.installSuite(foo);
        }

        assertDirectoryContentEquals(Arrays.asList("foo"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "foo"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            assertEquals(1, storage.getSuite("foo").versionsCount());
        }

        assertDirectoryContentEquals(Arrays.asList("foo"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "foo"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            TaskSuite bar = new TaskSuite("bar");
            bar.createVersion("1.0");
            storage.installSuite(bar);
        }

        assertDirectoryContentEquals(Arrays.asList("foo", "bar"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "foo"));
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "bar"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            storage.getSuite("foo").createVersion("1.1");
            assertEquals(2, storage.getSuite("foo").versionsCount());
        }

        assertDirectoryContentEquals(Arrays.asList("foo", "bar"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0", "1.1"), new File(getStorageDir(), "foo"));
        assertDirectoryContentEquals(Arrays.asList("1.0"), new File(getStorageDir(), "bar"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            storage.getSuite("bar").getVersion("1.0").uninstall();
        }

        assertDirectoryContentEquals(Arrays.asList("foo"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.0", "1.1"), new File(getStorageDir(), "foo"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            storage.getSuite("foo").getVersion("1.0").uninstall();
        }

        assertDirectoryContentEquals(Arrays.asList("foo"), getStorageDir());
        assertDirectoryContentEquals(Arrays.asList("1.1"), new File(getStorageDir(), "foo"));

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());
            storage.getSuite("foo").getVersion("1.1").uninstall();
        }

        assertDirectoryContentEquals(Collections.<String>emptyList(), getStorageDir());
    }

    public void testIterateSuites() throws Exceptions.TaskStorageException {
        TaskStorage storage = new FilesystemTaskStorage(getStorageDir());

        TaskSuite foo = new TaskSuite("foo");
        foo.createVersion("1.0");
        storage.installSuite(foo);

        TaskSuite bar = new TaskSuite("bar");
        bar.createVersion("1.0");
        storage.installSuite(bar);

        boolean foundFoo = false;
        boolean foundBar = false;
        for(TaskSuite suite: storage) {
            if(suite.getName().equals("foo")) foundFoo = true;
            if(suite.getName().equals("bar")) foundBar = true;
        }
        assertTrue(foundFoo);
        assertTrue(foundBar);
    }

    public void testIterateVersions() throws Exceptions.TaskStorageException {
        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());

            TaskSuite foo = new TaskSuite("foo");
            foo.createVersion("1.0");
            foo.createVersion("1.1");
            storage.installSuite(foo);
        }

        {
            TaskStorage storage = new FilesystemTaskStorage(getStorageDir());

            boolean found_1_0 = false;
            boolean found_1_1 = false;
            for(TaskSuiteVersion version: storage.getSuite("foo")) {
                if(version.getIdentifier().equals("1.0")) found_1_0 = true;
                if(version.getIdentifier().equals("1.1")) found_1_1 = true;
            }
            assertTrue(found_1_0);
            assertTrue(found_1_1);
        }
    }

}

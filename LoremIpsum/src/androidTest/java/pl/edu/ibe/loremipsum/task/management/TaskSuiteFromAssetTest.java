package pl.edu.ibe.loremipsum.task.management;

import android.content.res.AssetManager;

import pl.edu.ibe.loremipsum.task.management.installation.assets.AssetsTaskAccessor;
import pl.edu.ibe.loremipsum.task.management.installation.assets.AssetsTaskSuite;
import pl.edu.ibe.loremipsum.task.management.storage.FilesystemTaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.ReadStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteFromAssetTest extends BaseInstrumentationTestCase {
    private static final String TAG = TaskSuiteFromAssetTest.class.toString();

    void testInstallFromAsset() throws IOException {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        AssetsTaskAccessor accessor = new AssetsTaskAccessor(assetManager, testAssetName);
        AssetsTaskSuite suite = execute(accessor.getSuite());
        assertEquals("test-suite", suite.getName());
        assertEquals("1.0", suite.getVersion());

        TaskStorage storage = new FilesystemTaskStorage(getStorageDir("test-device-foo"));
        execute(suite.installTo(storage).second);
        assertEquals("1.0", storage.getSuite("test-suite").getVersion("1.0").getIdentifier());

        TaskStorage storage2 = new FilesystemTaskStorage(getStorageDir("test-device-foo"));
        VirtualFile root = storage2.getSuite("test-suite").getVersion("1.0").getRoot();

        ZipInputStream zis = new ZipInputStream(assetManager.open(testAssetName));
        try {
            while(true) {
                ZipEntry entry = zis.getNextEntry();
                if(entry == null)
                    break;

                try {
                    VirtualFile file = root.getChildFile(entry.getName());
                    assertEquals(entry.getSize(), file.length());
                    LogUtils.v(TAG, "Verifying entry: "+entry.getName());

                    if(entry.isDirectory()) {
                        assertTrue(file.isDirectory());
                        assertFalse(file.isFile());
                    } else {
                        assertFalse(file.isDirectory());
                        assertTrue(file.isFile());

                        byte[] expected = execute(
                                ReadStream.readStream(zis).setCloseInput(false).execute()).getBytes();
                        byte[] actual = execute(
                                ReadStream.readStream(file.getInputStream()).execute()).getBytes();
                        assertTrue(Arrays.equals(expected, actual));
                    }
                } finally {
                    zis.closeEntry();
                }
            }
        } finally {
            zis.close();
        }
    }
}

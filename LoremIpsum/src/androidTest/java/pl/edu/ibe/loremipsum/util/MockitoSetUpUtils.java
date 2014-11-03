package pl.edu.ibe.loremipsum.util;

import android.content.Context;

import pl.edu.ibe.loremipsum.tools.FileUtils;

import java.io.File;
import java.io.IOException;

/**
* @author Mariusz Pluciński
*/
public class MockitoSetUpUtils {
    public static void setUp(Context context) throws IOException {
        File path = new File(context.getCacheDir(), "dexmaker-dexcache");
        if(path.exists())
            FileUtils.deleteRecursive(path);
        if(!path.mkdirs())
            throw new IOException("Could not create directory \""+path.getAbsolutePath()+"\"");
        System.setProperty("dexmaker.dexcache", path.getPath());
    }
}

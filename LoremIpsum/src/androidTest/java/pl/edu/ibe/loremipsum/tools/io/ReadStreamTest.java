package pl.edu.ibe.loremipsum.tools.io;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mariusz Pluciński
 */
public class ReadStreamTest extends BaseInstrumentationTestCase {
    public void testReadStream() throws IOException {
        String string = "those are exactly 32 characters!";
        InputStream source = new ByteArrayInputStream(string.getBytes());
        ReadStream.ReadProgress progress = ReadStream.readStream(source).execute().toBlockingObservable().last();
        assertTrue(progress.isFinished());
        byte[] output = progress.getBytes();
        assertEquals(string, new String(output));

        int count = 100000;
        StringBuilder longStringBuilder = new StringBuilder(string.length()*count);
        for(int i = 0; i < count; ++i)
            longStringBuilder.append(string);
        String longString = longStringBuilder.toString();

        source = new ByteArrayInputStream(longString.getBytes());
        progress = ReadStream.readStream(source).execute().toBlockingObservable().last();
        assertTrue(progress.isFinished());
        output = progress.getBytes();
        assertEquals(longString, new String(output));
    }
}

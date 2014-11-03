package pl.edu.ibe.loremipsum.tools.io;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import rx.observables.BlockingObservable;

/**
 * @author Mariusz Pluciński
 */
public class CopyStreamTest extends BaseInstrumentationTestCase {
    public void testCopyStream() throws IOException {
        String string = "those are exactly 32 characters!";
        InputStream source = new ByteArrayInputStream(string.getBytes());
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        executeLast(CopyStream.copyStream(source, target).execute());
        String output = new String(target.toByteArray());
        assertEquals(string, output);

        int count = 100000;
        StringBuilder longStringBuilder = new StringBuilder(string.length()*count);
        for(int i = 0; i < count; ++i)
            longStringBuilder.append(string);
        String longString = longStringBuilder.toString();

        source = new ByteArrayInputStream(longString.getBytes());
        target = new ByteArrayOutputStream();
        CopyStream.CopyProgress progress = executeLast(CopyStream.copyStream(source, target).execute());
        assertTrue(progress.isFinished());
        output = new String(target.toByteArray());
        assertEquals(longString, output);
    }

    public void testCopyStreamWithProgressObserve() throws IOException {
        byte[] bytesInput = new byte[1024];
        InputStream inputStream = new ByteArrayInputStream(bytesInput);
        OutputStream outputStream = new ByteArrayOutputStream();
        List<CopyStream.CopyProgress> outputs = new ArrayList<>();
        BlockingObservable<CopyStream.CopyProgress> blockingObservable =
                CopyStream.copyStream(inputStream, outputStream)
                        .setBufferSize(256).execute().toBlockingObservable();
        blockingObservable.forEach(outputs::add);

        assertEquals(6, outputs.size());
        assertEquals(0, outputs.get(0).getCount());
        assertFalse(outputs.get(0).isFinished());
        assertEquals(256, outputs.get(1).getCount());
        assertFalse(outputs.get(1).isFinished());
        assertEquals(512, outputs.get(2).getCount());
        assertFalse(outputs.get(2).isFinished());
        assertEquals(768, outputs.get(3).getCount());
        assertFalse(outputs.get(3).isFinished());
        assertEquals(1024, outputs.get(4).getCount());
        assertFalse(outputs.get(4).isFinished());
        assertEquals(1024, outputs.get(5).getCount());
        assertTrue(outputs.get(5).isFinished());
    }

}

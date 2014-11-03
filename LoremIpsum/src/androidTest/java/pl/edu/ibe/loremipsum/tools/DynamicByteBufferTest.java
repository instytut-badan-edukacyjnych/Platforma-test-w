package pl.edu.ibe.loremipsum.tools;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * @author Mariusz Pluciński
 */
public class DynamicByteBufferTest extends BaseInstrumentationTestCase {
    public void testBufferStartsWithPushFront() {
        generalTest(DynamicByteBuffer::pushFront);
    }

    public void testBufferStartsWithPushBack() {
        generalTest(DynamicByteBuffer::pushBack);
    }

    public void generalTest(Inserter inserter) {
        DynamicByteBuffer buffer = new DynamicByteBuffer(2);
        // buffer layout: {}
        assertTrue(buffer.empty());
        assertEquals(0, buffer.size());
        assertThrows(BufferOverflowException.class, buffer::front);
        assertThrows(BufferUnderflowException.class, buffer::back);

        assertThrows(BufferOverflowException.class, buffer::popFront);
        assertThrows(BufferUnderflowException.class, buffer::popBack);

        inserter.call(buffer, (byte) 10);
        // buffer layout: {10}
        assertFalse(buffer.empty());
        assertEquals(1, buffer.size());
        assertEquals(10, buffer.front());
        assertEquals(10, buffer.back());

        buffer.pushFront((byte) 20);
        // buffer layout: {20, 10}
        assertFalse(buffer.empty());
        assertEquals(2, buffer.size());
        assertEquals(20, buffer.front());
        assertEquals(10, buffer.back());

        buffer.pushFront((byte) 30);
        // buffer layout: {30, 20, 10}
        assertFalse(buffer.empty());
        assertEquals(3, buffer.size());
        assertEquals(30, buffer.front());
        assertEquals(10, buffer.back());

        buffer.pushBack((byte) 40);
        // buffer layout: {30, 20, 10, 40}
        assertFalse(buffer.empty());
        assertEquals(4, buffer.size());
        assertEquals(30, buffer.front());
        assertEquals(40, buffer.back());

        buffer.pushBack((byte) 50);
        // buffer layout: {30, 20, 10, 40, 50}
        assertFalse(buffer.empty());
        assertEquals(5, buffer.size());
        assertEquals(30, buffer.front());
        assertEquals(50, buffer.back());

        assertEquals(30, buffer.popFront());
        // buffer layout: {20, 10, 40, 50}
        assertFalse(buffer.empty());
        assertEquals(4, buffer.size());
        assertEquals(20, buffer.front());
        assertEquals(50, buffer.back());

        assertEquals(20, buffer.popFront());
        // buffer layout: {10, 40, 50}
        assertFalse(buffer.empty());
        assertEquals(3, buffer.size());
        assertEquals(10, buffer.front());
        assertEquals(50, buffer.back());

        assertEquals(50, buffer.popBack());
        // buffer layout: {10, 40}
        assertFalse(buffer.empty());
        assertEquals(2, buffer.size());
        assertEquals(10, buffer.front());
        assertEquals(40, buffer.back());

        buffer.pushFront((byte) 60);
        // buffer layout: {60, 10, 40}
        assertFalse(buffer.empty());
        assertEquals(3, buffer.size());
        assertEquals(60, buffer.front());
        assertEquals(40, buffer.back());

        buffer.pushBack((byte) 70);
        // buffer layout: {60, 10, 40, 70}
        assertFalse(buffer.empty());
        assertEquals(4, buffer.size());
        assertEquals(60, buffer.front());
        assertEquals(70, buffer.back());

        assertEquals(70, buffer.popBack());
        // buffer layout: {60, 10, 40}
        assertFalse(buffer.empty());
        assertEquals(3, buffer.size());
        assertEquals(60, buffer.front());
        assertEquals(40, buffer.back());

        assertEquals(40, buffer.popBack());
        // buffer layout: {60, 10}
        assertFalse(buffer.empty());
        assertEquals(2, buffer.size());
        assertEquals(60, buffer.front());
        assertEquals(10, buffer.back());

        assertEquals(60, buffer.popFront());
        // buffer layout: {10}
        assertFalse(buffer.empty());
        assertEquals(1, buffer.size());
        assertEquals(10, buffer.front());
        assertEquals(10, buffer.back());

        assertEquals(10, buffer.popFront());
        // buffer layout: {}
        assertTrue(buffer.empty());
        assertEquals(0, buffer.size());
        assertThrows(BufferOverflowException.class, buffer::front);
        assertThrows(BufferUnderflowException.class, buffer::back);

        assertThrows(BufferOverflowException.class, buffer::popFront);
        assertThrows(BufferUnderflowException.class, buffer::popBack);
    }

    private interface Inserter {
        void call(DynamicByteBuffer buffer, byte value);
    }
}

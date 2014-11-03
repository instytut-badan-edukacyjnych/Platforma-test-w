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

package pl.edu.ibe.loremipsum.tools;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;

/**
 * @author Mariusz Pluciński
 */
public class DynamicByteBuffer {
    private static final String TAG = DynamicByteBuffer.class.toString();

    private static final int defaultSingleInternalBufferSize = 4096;
    private int singleInternalBufferSize = defaultSingleInternalBufferSize;
    private LinkedList<byte[]> buffers = new LinkedList<>();
    private int posFront = 0; //in first buffer
    private int posBack = 0; //in last buffer
    private boolean logging = true;

    public DynamicByteBuffer() {
        this(defaultSingleInternalBufferSize);
    }

    public DynamicByteBuffer(int singleInternalBufferSize) {
        this.singleInternalBufferSize = singleInternalBufferSize;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public boolean empty() {
        return size() == 0;
    }

    public int size() {
        if (buffers.isEmpty())
            return 0;
        return (buffers.size() - 1) * singleInternalBufferSize - posFront + posBack + 1;
    }

    public byte front() throws BufferOverflowException {
        if (empty())
            throw new BufferOverflowException();
        return buffers.getFirst()[posFront];
    }

    public byte back() throws BufferUnderflowException {
        if (empty())
            throw new BufferUnderflowException();
        return buffers.getLast()[posBack];
    }

    public synchronized byte popFront() throws BufferOverflowException {
        if (empty())
            throw new BufferOverflowException();
        byte b = buffers.getFirst()[posFront];
        posFront++;
        if (posFront >= singleInternalBufferSize) {
            buffers.removeFirst();
            bufferRemoved();
            posFront = 0;
        }
        return b;
    }

    public synchronized byte popBack() throws BufferUnderflowException {
        if (empty())
            throw new BufferUnderflowException();
        byte b = buffers.getLast()[posBack];
        posBack--;
        if (posBack < 0) {
            buffers.removeLast();
            bufferRemoved();
            posBack = singleInternalBufferSize - 1;
        }
        return b;
    }

    public synchronized void pushFront(byte b) {
        posFront--;
        if (posFront < 0) {
            buffers.add(0, new byte[singleInternalBufferSize]);
            bufferAdded();
            if (buffers.size() == 1)
                posBack = singleInternalBufferSize - 1;
            posFront = singleInternalBufferSize - 1;
        }
        buffers.getFirst()[posFront] = b;
    }

    public synchronized void pushBack(byte b) {
        posBack++;
        if (buffers.isEmpty() || posBack >= singleInternalBufferSize) {
            buffers.add(new byte[singleInternalBufferSize]);
            bufferAdded();
            posBack = 0;
        }
        buffers.getLast()[posBack] = b;
    }

    private void bufferAdded() {
        if (logging)
            LogUtils.v(TAG, "New buffer allocated (now have " + buffers.size()
                    + " buffers, each storing " + singleInternalBufferSize + " bytes)");
    }

    private void bufferRemoved() {
        if (logging)
            LogUtils.v(TAG, "Buffer deleted (now have " + buffers.size()
                    + " buffers, each storing " + singleInternalBufferSize + " bytes)");

    }
}

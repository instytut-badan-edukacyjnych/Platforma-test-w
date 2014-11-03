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

package pl.edu.ibe.loremipsum.tools.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * @author Mariusz Pluciński
 */
public interface VirtualFile {
    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public boolean canExecute();

    public boolean canRead();

    public boolean canWrite();

    public int compareTo(VirtualFile pathname);

    public boolean createNewVirtualFile();

    public boolean delete();

    public void deleteOnExit();

    public boolean equals(Object obj);

    public boolean exists();

    public VirtualFile getAbsoluteFile();

    public String getAbsolutePath();

    public VirtualFile getCanonicalFile();

    public String getCanonicalPath();

    public long getFreeSpace();

    public String getName();

    public String getParent();

    public VirtualFile getParentFile();

    public String getPath();

    public long getTotalSpace();

    public long getUsableSpace();

    public int hashCode();

    public boolean isAbsolute();

    public boolean isDirectory();

    public boolean isFile();

    public boolean isHidden();

    public long lastModified();

    public long length() throws IOException;

    public String[] list();

    public String[] list(FilenameFilter filter);

    public VirtualFile[] listFiles();

    public VirtualFile[] listFiles(FileFilter filter);

    public VirtualFile[] listFiles(FilenameFilter filter);

    public boolean mkdir();

    public boolean mkdirs();

    public boolean renameTo(VirtualFile dest);

    public boolean setExecutable(boolean executable);

    public boolean setExecutable(boolean executable, boolean ownerOnly);

    public boolean setLastModified(long time);

    public boolean setReadable(boolean readable);

    public boolean setReadable(boolean readable, boolean ownerOnly);

    public boolean setReadOnly();

    public boolean setWritable(boolean writable);

    public boolean setWritable(boolean writable, boolean ownerOnly);

    public String toString();

    public URI toURI();

    public URL toURL();

    VirtualFile getChildFile(String name) throws IOException;

    String getVirtualContainerName();

    VirtualFile createChildDirectory(String name) throws IOException;

    VirtualFile createChildFile(String name) throws IOException;

    String getPathComponent(int i);

    boolean hasChild(String name);
}

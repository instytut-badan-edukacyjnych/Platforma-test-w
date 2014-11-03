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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * @author Mariusz Pluciński
 */
public class FilesystemFile extends AbstractVirtualFile {
    private static final String TAG = FilesystemFile.class.toString();
    private final File file;

    public FilesystemFile(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean canExecute() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean canRead() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean canWrite() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public int compareTo(VirtualFile pathname) {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public boolean createNewVirtualFile() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public void deleteOnExit() {
        throwNotYetImplemented();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public VirtualFile getAbsoluteFile() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public VirtualFile getCanonicalFile() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public String getCanonicalPath() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public long getFreeSpace() {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getParent() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public VirtualFile getParentFile() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public String getPath() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public long getTotalSpace() {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public long getUsableSpace() {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public boolean isAbsolute() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean isHidden() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public long lastModified() {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public long length() throws IOException {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public String[] list() {
        throwNotYetImplemented();
        return new String[0];
    }

    @Override
    public String[] list(FilenameFilter filter) {
        throwNotYetImplemented();
        return new String[0];
    }

    @Override
    public VirtualFile[] listFiles() {
        File[] files = file.listFiles();
        if (files == null)
            return null;
        VirtualFile[] virtualFiles = new VirtualFile[files.length];
        for (int i = 0; i < files.length; ++i)
            virtualFiles[i] = new FilesystemFile(files[i]);
        return virtualFiles;
    }

    @Override
    public VirtualFile[] listFiles(FileFilter filter) {
        throwNotYetImplemented();
        return new VirtualFile[0];
    }

    @Override
    public VirtualFile[] listFiles(FilenameFilter filter) {
        throwNotYetImplemented();
        return new VirtualFile[0];
    }

    @Override
    public boolean mkdir() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean mkdirs() {
        return file.mkdirs();
    }

    @Override
    public boolean renameTo(VirtualFile dest) {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean setExecutable(boolean executable) {
        throwNotYetImplemented();
        return executable;
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        throwNotYetImplemented();
        return executable;
    }

    @Override
    public boolean setLastModified(long time) {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean setReadable(boolean readable) {
        throwNotYetImplemented();
        return readable;
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        throwNotYetImplemented();
        return readable;
    }

    @Override
    public boolean setReadOnly() {
        throwNotYetImplemented();
        return false;
    }

    @Override
    public boolean setWritable(boolean writable) {
        throwNotYetImplemented();
        return writable;
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        throwNotYetImplemented();
        return writable;
    }

    @Override
    public URI toURI() {
        return file.toURI();
    }

    @Override
    public URL toURL() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public VirtualFile getChildFile(String name) throws IOException {
        File childFile = new File(file, name);
        if (!childFile.exists())
            throw new IOException("File \"" + childFile.getAbsolutePath() + "\" does not exist");
        return new FilesystemFile(childFile);
    }

    @Override
    public String getVirtualContainerName() {
        return "native filesystem";
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    @Override
    public VirtualFile createChildDirectory(String name) throws IOException {
        File childFile = new File(file, name);
        if (childFile.exists())
            throw new IOException("File \"" + childFile.getAbsolutePath() + "\" already exists");
        if (!childFile.mkdir())
            throw new IOException("Creating directory \"" + childFile.getAbsolutePath() + "\" has failed");
        return getChildFile(name);
    }

    @Override
    public VirtualFile createChildFile(String name) throws IOException {
        File childFile = new File(file, name);
        if (childFile.exists())
            throw new IOException("File \"" + childFile.getAbsolutePath() + "\" already exists");
        if (!childFile.createNewFile())
            throw new IOException("Creating file \"" + childFile.getAbsolutePath() + "\" has failed");
        return getChildFile(name);
    }

    @Override
    public String getPathComponent(int i) {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public boolean hasChild(String name) {
        return new File(file, name).exists();
    }
}

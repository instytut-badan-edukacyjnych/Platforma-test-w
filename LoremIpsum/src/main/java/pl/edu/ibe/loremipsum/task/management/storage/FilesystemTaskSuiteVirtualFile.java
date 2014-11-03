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

package pl.edu.ibe.loremipsum.task.management.storage;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pl.edu.ibe.loremipsum.tools.StringUtils;
import pl.edu.ibe.loremipsum.tools.io.AbstractVirtualFile;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * @author Mariusz Pluciński
 */
class FilesystemTaskSuiteVirtualFile extends AbstractVirtualFile {
    private static final ZipEntry ROOT_ENTRY = new ZipEntry("/");
    // accessor to the actual storage
    private final StorageAccessor storageAccessor;
    // associated entry in zip. When VirtualFile is root directory of archive, this must be
    // set to ROOT_ENTRY
    private final ZipEntry currentEntry;

    public FilesystemTaskSuiteVirtualFile(File storageFile, EncryptionLayer encryptionLayer) throws IOException {
        this.storageAccessor = new StorageAccessor(storageFile, encryptionLayer);
        this.currentEntry = ROOT_ENTRY;
    }

    FilesystemTaskSuiteVirtualFile(StorageAccessor storageAccessor, ZipEntry entry) {
        this.storageAccessor = storageAccessor;
        this.currentEntry = entry;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (currentEntry.isDirectory())
            throw new IOException("Cannot get input stream from directory");
        return storageAccessor.getInputStream(currentEntry);
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
        throwNotYetImplemented();
        return false;
    }

    @Override
    public void deleteOnExit() {

    }

    @Override
    public boolean exists() {
        return true; //in general, it should not happen that we have VirtualFile that points to non-existing file
    }

    @Override
    public VirtualFile getAbsoluteFile() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public String getAbsolutePath() {
        return currentEntry.getName();
    }

    @Override
    public VirtualFile getCanonicalFile() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public String getCanonicalPath() {
        String[] path = getAbsolutePath().split("/");
        Stack<String> stack = new Stack<>();
        for (String name : path) {
            if (name.equals("."))
                continue;
            else if (name.equals("..")) {
                if (!stack.isEmpty())
                    stack.pop();
            } else
                stack.push(name);
        }
        return StringUtils.join(stack.toArray(new String[stack.size()]), "/");
    }

    @Override
    public long getFreeSpace() {
        throwNotYetImplemented();
        return 0;
    }

    @Override
    public String getName() {
        if (currentEntry != ROOT_ENTRY) {
            String[] name = currentEntry.getName().split("/");
            if (name.length < 1)
                return null;
            return name[name.length - 1];
        }
        return "";
    }

    @Override
    public String getParent() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public VirtualFile getParentFile() {
        if (currentEntry != ROOT_ENTRY) {
            String[] name = currentEntry.getName().split("/");
            if (name.length < 2)
                return new FilesystemTaskSuiteVirtualFile(storageAccessor, ROOT_ENTRY);
            try {
                ZipEntry entry = storageAccessor.getEntry(StringUtils.join(name, "/", 0, name.length - 1));
                return new FilesystemTaskSuiteVirtualFile(storageAccessor, entry);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return currentEntry.getName();
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
        return !isFile();
    }

    @Override
    public boolean isFile() {
        return !currentEntry.isDirectory();
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
        if (currentEntry != ROOT_ENTRY) {
            long size = storageAccessor.getSize(currentEntry);
            if (size == -1)
                throw new IOException("Could not read uncompressed size of \"" + getName() + "\"");
            return size;
        }
        return 0L;
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
        if (!isDirectory())
            return null;

        return storageAccessor.listFiles(currentEntry.getName());
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
        throwNotYetImplemented();
        return false;
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
        throwNotYetImplemented();
        return null;
    }

    @Override
    public URL toURL() {
        throwNotYetImplemented();
        return null;
    }

    @Override
    public VirtualFile getChildFile(String name) throws IOException {
        String entryName = name;
        if (currentEntry != ROOT_ENTRY)
            entryName = currentEntry.getName() + name;
        return new FilesystemTaskSuiteVirtualFile(storageAccessor, storageAccessor.getEntry(entryName));
    }

    @Override
    public String getVirtualContainerName() {
        return storageAccessor.getContainerName();
    }

    @Override
    public String getPathComponent(int i) {
        String[] components = getPath().split("/");
        if (i < 0 || i >= components.length)
            return null;
        return components[i];
    }

    @Override
    public boolean hasChild(String name) {
        String fileName = currentEntry.getName() + "/" + name;
        String dirName = currentEntry.getName() + "/" + name + "/";
        return storageAccessor.storageFile.getEntry(fileName) != null ||
                storageAccessor.storageFile.getEntry(dirName) != null;
    }

    private class StorageAccessor {
        private final ZipFile storageFile;
        private final EncryptionLayer encryptionLayer;

        public StorageAccessor(File storageFile, EncryptionLayer encryptionLayer) throws IOException {
            this.storageFile = new ZipFile(storageFile);
            this.encryptionLayer = encryptionLayer;
        }

        public ZipEntry getEntry(String name) throws FileNotFoundException {
            ZipEntry entry = storageFile.getEntry(name);
            if (entry == null)
                throw new FileNotFoundException("Not found \"" + name + "\" in archive \""
                        + storageFile.getName() + "\"");
            return entry;
        }

        public InputStream getInputStream(ZipEntry entry) throws IOException {
            return encryptionLayer.wrapInputStream(storageFile.getInputStream(entry));
        }

        public long getSize(ZipEntry entry) throws IOException {
            //NOTE: this is very suboptimal, but I am not currently sure if there is any sense
            //      in optimizing that.
            long size = 0;
            InputStream input = getInputStream(entry);
            try {
                while (input.read() != -1)
                    size++;
            } finally {
                input.close();
            }
            return size;
        }

        public VirtualFile[] listFiles(String name) {
            if (name.equals("/")) name = "";
            List<VirtualFile> files = new ArrayList<>();
            for (Enumeration<? extends ZipEntry> entries = storageFile.entries();
                 entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(name)) {
                    entryName = entryName.substring(name.length());
                    while (entryName.startsWith("/")) entryName = entryName.substring(1);
                    while (entryName.endsWith("/"))
                        entryName = entryName.substring(0, entryName.length() - 1);
                    if (!entryName.isEmpty() && !entryName.contains("/"))
                        files.add(new FilesystemTaskSuiteVirtualFile(this, entry));
                }
            }
            return files.toArray(new VirtualFile[files.size()]);
        }

        public String getContainerName() {
            return storageFile.getName();
        }
    }
}

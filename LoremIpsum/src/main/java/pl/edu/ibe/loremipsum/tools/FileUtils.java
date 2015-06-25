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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.toString();

    private static void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteDir(file);
                file.delete();
            }
        }

    }

    public static void deleteRecursive(File file) throws IOException {
        LogUtils.v(TAG, "deleteRecursive(" + file + ")");
        String[] contents = file.list();
        if (contents != null)
            for (String content : contents)
                deleteRecursive(new File(file, content));
        if (!file.delete())
            throw new IOException("Could not remove \"" + file + "\"");
    }

    public static boolean contentEquals(File file1, File file2) throws IOException {
        if (file1.length() != file2.length())
            return false;

        InputStream is1 = new BufferedInputStream(new FileInputStream(file1));
        InputStream is2 = new BufferedInputStream(new FileInputStream(file2));
        byte[] buffer1 = new byte[(int) file1.length()];
        byte[] buffer2 = new byte[(int) file2.length()];
        if (is1.read(buffer1) != -1)
            return false;
        if (is2.read(buffer2) != -1)
            return false;
        for (int i = 0; i != buffer1.length; ++i)
            if (buffer1[i] != buffer2[i])
                return false;
        return true;
    }

    private static void __copyRecursive(VirtualFile source, VirtualFile target,
                                        boolean replaceExisting) throws IOException {
        for (VirtualFile srcFile : source.listFiles()) {
            if (srcFile.isDirectory()) {
                if (!target.hasChild(srcFile.getName())) {
                    VirtualFile dstFile = target.createChildDirectory(srcFile.getName());
                    __copyRecursive(srcFile, dstFile, replaceExisting);
                }
            } else if (srcFile.isFile()) {
                if (target.hasChild(srcFile.getName())) {
                    if (replaceExisting)
                        target.getChildFile(srcFile.getName()).delete();
                    else
                        throw new IOException("File \"" + target + File.separator + srcFile.getName() + "\" aready exists");
                }
                VirtualFile dstFile = target.createChildFile(srcFile.getName());
                CopyStream.copyStream(srcFile.getInputStream(), dstFile.getOutputStream())
                        .setName("Copying \"" + srcFile + "\" to \"" + dstFile + "\"")
                        .execute().subscribe();
            } else
                throw new IOException("copyRecursive supports only files and directories");
        }
    }

    public static Observable<Object> copyRecursive(VirtualFile source, VirtualFile target,
                                                   boolean replaceExisting) {
        return RxExecutor.run(() -> {
            __copyRecursive(source, target, replaceExisting);
            return RxExecutor.EMPTY_OBJECT;
        });
    }

    public static Observable<Object> copyRecursive(VirtualFile source, VirtualFile target) {
        return copyRecursive(source, target, false);
    }

    private static void __deleteRecursive(VirtualFile file) throws IOException {
        LogUtils.v(TAG, "__deleteRecursive(" + file + ")");
        VirtualFile[] files = file.listFiles();
        if (files != null)
            for (VirtualFile child : files)
                __deleteRecursive(child);
        if (!file.delete())
            throw new IOException("Could not remove \"" + file + "\"");
    }

    public static Observable<Object> deleteRecursive(VirtualFile file) {
        return RxExecutor.run(() -> {
            __deleteRecursive(file);
            return RxExecutor.EMPTY_OBJECT;
        });
    }

    public static Observable<List<File>> getFilesOlderThan(File dir, Date date) {
        return RxExecutor.run(() -> {
            List<File> files = new ArrayList<>();
            for (File file : dir.listFiles()) {
                if (file.lastModified() < date.getTime()) {
                    files.add(file);
                }
            }
            return files;
        });
    }

    public static Observable<List<File>> getFilesYoungerThan(File dir, Date date) {
        return RxExecutor.run(() -> {
            List<File> files = new ArrayList<>();
            for (File file : dir.listFiles()) {
                if (file.lastModified() > date.getTime()) {
                    files.add(file);
                }
            }
            return files;
        });
    }

    public static Observable<Boolean> deleteFiles(List<File> files) {
        return RxExecutor.run(() -> {
            try {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                    if (file.isDirectory()) {
                        deleteDir(file);
                    }
                }
            } catch (Exception e) {
                LogUtils.d(FileUtils.class.getSimpleName(), "failed to clean logs", e);
                return false;
            }
            return true;
        });
    }


    public static void zipFile(List<File> files, File targetZipFile) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(targetZipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            byte[] buffer = new byte[128];
            for (int i = 0; i < files.size(); i++) {
                File currentFile = files.get(i);
                if (!currentFile.isDirectory()) {
                    ZipEntry entry = new ZipEntry(currentFile.getName());
                    FileInputStream fis = new FileInputStream(currentFile);
                    zos.putNextEntry(entry);
                    int read = 0;
                    while ((read = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            LogUtils.d(TAG, "File not found: " + targetZipFile + "\n files: " + files, e);
        }
    }
}
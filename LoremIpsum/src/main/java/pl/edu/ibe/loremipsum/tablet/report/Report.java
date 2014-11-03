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

package pl.edu.ibe.loremipsum.tablet.report;

import android.content.Context;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.io.FilesystemFile;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * @author Mariusz Pluciński
 */
public class Report implements Closeable {
    private static final String REPORT_INDEX_FILE = "index.html";
    private boolean closed = false;
    private VirtualFile extractionDirectory;

    public static Observable<Report> load(Context context, VirtualFile reportSourceDirectory) {
        return RxExecutor.runSingle(() -> {
            Report report = new Report();
            do {
                report.extractionDirectory = new FilesystemFile(new File(
                        context.getCacheDir(), UUID.randomUUID().toString()));
            } while (report.extractionDirectory.exists());
            if (!report.extractionDirectory.mkdirs())
                throw new IOException("Could not create directory \"" + report.extractionDirectory + "\"");
            return report;
        }).flatMap(report -> FileUtils.copyRecursive(reportSourceDirectory, report.extractionDirectory).map(ignore -> report));
    }

    public URL getUrl() throws ReportException {
        try {
            return extractionDirectory.getChildFile(REPORT_INDEX_FILE).toURI().toURL();
        } catch (IOException e) {
            throw new ReportException("Could not generate report URL for \"" + extractionDirectory + "\"", e);
        }
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        FileUtils.deleteRecursive(extractionDirectory).toBlockingObservable().single();
    }
}

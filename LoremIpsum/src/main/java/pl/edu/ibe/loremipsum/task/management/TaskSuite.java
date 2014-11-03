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

package pl.edu.ibe.loremipsum.task.management;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pl.edu.ibe.loremipsum.db.schema.TaskSuiteDao;
import pl.edu.ibe.loremipsum.localization.Localization;
import pl.edu.ibe.loremipsum.localization.XmlLocalizationBackend;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuite {
    private static final String TAG = TaskSuite.class.toString();
    private static final String LOCALIZATION_FILE_PREFIX = "localization";
    private static final String LOCALIZATION_FILE_SUFFIX = ".xml";
    private final DbAccess dbAccess;

    private final String name;
    private final String version;
    private final pl.edu.ibe.loremipsum.db.schema.TaskSuite dbEntry;
    private TaskSuiteVersion storageEntry = null;
    private Localization localization;

    public TaskSuite(DbAccess dbAccess, TaskStorage taskStorage,
                     pl.edu.ibe.loremipsum.db.schema.TaskSuite dbEntry) {

        this.dbAccess = dbAccess;
        this.name = dbEntry.getName();
        this.version = dbEntry.getVersion();
        this.dbEntry = dbEntry;
        if (this.dbEntry == null)
            throw new NullPointerException("dbEntry cannot be null!");
        this.storageEntry = acquireStorageEntry(taskStorage);
    }

    TaskSuite(DbAccess dbAccess, TaskStorage taskStorage, String suiteName, String suiteVersion) {
        this.dbAccess = dbAccess;
        this.name = suiteName;
        this.version = suiteVersion;
        this.dbEntry = acquireDbEntry(dbAccess);
        if (this.dbEntry == null)
            throw new NullPointerException("dbEntry cannot be null!");
        this.storageEntry = acquireStorageEntry(taskStorage);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    private pl.edu.ibe.loremipsum.db.schema.TaskSuite acquireDbEntry(DbAccess dbAccess) {
        List<pl.edu.ibe.loremipsum.db.schema.TaskSuite> list
                = dbAccess.getDaoSession().getTaskSuiteDao().queryBuilder().where(
                TaskSuiteDao.Properties.Name.eq(name),
                TaskSuiteDao.Properties.Version.eq(version)
        ).list();
        if (list.size() == 0)
            throw new Exceptions.SuiteNotFound("Could not found test suite in database: "
                    + name + ":" + version);
        if (list.size() > 1)
            throw new Exceptions.SuiteNotFound("More than one database entry matches: "
                    + name + ":" + version);
        return list.get(0);
    }

    private TaskSuiteVersion acquireStorageEntry(TaskStorage taskStorage) {
        try {
            return taskStorage.getSuite(name).getVersion(version);
        } catch (Exceptions.SuiteNotFound e) {
            LogUtils.e(TAG, "Suite has not been found in storage", e);
            return null;
        }
    }

    public boolean getPilot() {
        if (this.dbEntry == null)
            throw new NullPointerException("dbEntry cannot be null!");
        if (this.dbEntry.getPilot() == null) {
            LogUtils.v(TAG, "Pilot is NULL, while it should not; maybe it has not yet been checked?");
            return false;
        }
        return dbEntry.getPilot();
    }

    private void dbUpdate() {
        dbAccess.getDaoSession().getTaskSuiteDao().update(dbEntry);
    }

    public VirtualFile getRootVirtualFile() throws IOException {
        return storageEntry.getRoot();
    }

    public pl.edu.ibe.loremipsum.db.schema.TaskSuite getDbEntry() {
        return dbEntry;
    }

    public boolean isDownloaded() {
        return dbEntry.getDownloaded();

    }

    public void setDownloaded(boolean downloaded) {
        dbEntry.setDownloaded(downloaded);
        dbUpdate();
    }

    public Localization getLocalization() throws pl.edu.ibe.loremipsum.localization.Exceptions.LoadingException {
        if (localization == null) {
            try {
                XmlLocalizationBackend backend = new XmlLocalizationBackend();
                for (VirtualFile file : getRootVirtualFile().listFiles()) {
                    String name = file.getName();
                    if (name.startsWith(LOCALIZATION_FILE_PREFIX) && name.endsWith(LOCALIZATION_FILE_SUFFIX)) {
                        name = name.substring(LOCALIZATION_FILE_PREFIX.length());
                        name = name.substring(0, name.length() - LOCALIZATION_FILE_SUFFIX.length());
                        while (name.startsWith("-"))
                            name = name.substring(1);
                        String[] l = name.split("_");
                        Locale locale;
                        if (l.length == 1)
                            locale = new Locale(l[0]);
                        else if (l.length == 2)
                            locale = new Locale(l[0], l[1]);
                        else if (l.length == 3)
                            locale = new Locale(l[0], l[1], l[2]);
                        else
                            throw new pl.edu.ibe.loremipsum.localization.Exceptions.LoadingException("Malformed localization file name of " + file);
                        backend.installSource(locale, file.getInputStream());
                    }
                }
                localization = new Localization(backend, Locale.getDefault());
            } catch (IOException e) {
                throw new pl.edu.ibe.loremipsum.localization.Exceptions.LoadingException(e);
            }
        }
        return localization;
    }
}

/**
 * Aplikacja LoremIpsum
 *
 * @copyright © 2011-2014, Instytut Badań Edukacyjnych
 *
 */

/************************************
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
 ************************************/
package pl.edu.ibe.loremipsum.manager;


import java.io.IOException;

import pl.edu.ibe.loremipsum.manager.Irt.IrtBatch;
import pl.edu.ibe.loremipsum.manager.managers.ManagerFactory;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Base task managment class
 *
 */
public abstract class BaseManager {
    private static final String TAG = BaseManager.class.toString();

    private static final String managerSuffix = "Manager";
    private final String name;

    /**
     * manager name
     */
    public String m_name = LoremIpsumApp.APP_NO_FILL_FIELD;

    protected MappingDependency mappingDependency = MappingDependency.EMPTY;

    public BaseManager(String name) {
        this.name = name;
    }

    /**
     * creates instance
     *
     * @param a_className - class name
     * @return created instance
     */
    public static BaseManager CreateInstance(String name, String a_className) throws Exception {
        if (a_className.endsWith(managerSuffix))
            a_className = a_className.substring(0, a_className.length() - managerSuffix.length());

        try {
            String factoryClassName = ManagerFactory.class.getPackage().getName() + "." + a_className;
            LogUtils.v(TAG, "Factory class name: \"" + factoryClassName + "\"");
            Class<? extends ManagerFactory> factoryClass = Class.forName(factoryClassName).asSubclass(ManagerFactory.class);
            ManagerFactory factory = factoryClass.newInstance();
            return factory.createManager(name);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LogUtils.e(TAG, e);
            throw new Exception("could not create manager", e);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getClass().toString() + "[name=\"" + name + "\"]";
    }

    /**
     *
     * Reads data form external file. Initializes data structures
     *
     * @param baseDir Base directory
     * @return true  if success false otherwise
     */
    public abstract boolean Initialize(VirtualFile baseDir) throws IOException;

    public MappingDependency getMappingDependency() {
        return mappingDependency;
    }

    /**
     * Preapares  test
     *
     * @param a_name - script name
     * @return true if success
     */
    public abstract boolean Restart(String a_name);

    /**
     * Loads next task to execute
     *
     * @return null - if test ended
     */
    public abstract TaskInfo GetNextTask();

    /**
     * Loads previous to execute
     *
     * @return null - if no task
     */
    public abstract TaskInfo GetPrevTask();

    /**
     *Checks if test is ended
     *
     * @return true if in all areas current task suite is ended
     */
    public abstract boolean IsFinished();

    /**
     *  Loads information about test for area
     * @param a_area - area name
     * @return task info; null if no test for area
     */
    public abstract ManagerTestInfo GetTestInfo(String a_area);

    /**
     * Marks tas
     *
     * @param a_task - task to mark
     * @param a_mark - task mark
     */
    public abstract void Mark(TaskInfo a_task, double a_mark);

    /**
     * Keeps test results
     *
     */
    public static class ManagerTestInfo {
        /**
         * area name
         */
        public String m_area = "";

        /**
         * test status
         */
        public int m_status = LoremIpsumApp.TEST_NOT_PREPARED;

        /**
         * answer vector
         */
        public IrtBatch m_vector = new IrtBatch();

    }
}



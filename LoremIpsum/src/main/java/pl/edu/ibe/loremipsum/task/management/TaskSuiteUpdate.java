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

import pl.edu.ibe.loremipsum.db.schema.Credential;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.task.management.installation.network.NetworkTaskSuite;

/**
 * @author Mariusz Pluciński
 */
public class TaskSuiteUpdate {
    private final boolean update;
    private Credential credential = null;
    private Researcher researcher = null;
    private ResearchersSuite installedTaskSuite = null;
    private ResearchersSuite newResearchersSuite = null;
    private NetworkTaskSuite networkTaskSuite = null;

    private TaskSuiteUpdate(boolean update) {
        this.update = update;
    }

    public TaskSuiteUpdate(ResearchersSuite installedTaskSuite, NetworkTaskSuite networkTaskSuite,
                           Researcher researcher, Credential credential) {
        update = true;
        this.installedTaskSuite = installedTaskSuite;
        this.networkTaskSuite = networkTaskSuite;
        this.researcher = researcher;
        this.credential = credential;
    }

    public Researcher getResearcher() {
        return researcher;
    }

    public Credential getCredential() {
        return credential;
    }

    public String getName() {
        return networkTaskSuite.getName();
    }

    public String getVersion() {
        return networkTaskSuite.getVersion();
    }

    public boolean isUpdate() {
        return update;
    }

    public NetworkTaskSuite getNewTaskSuite() {
        return networkTaskSuite;
    }

    public ResearchersSuite getOldTaskSuite() {
        return installedTaskSuite;
    }

    public ResearchersSuite getNewResearchersSuite() {
        return newResearchersSuite;
    }

    public void setNewResearchersSuite(ResearchersSuite newResearchersSuite) {
        this.newResearchersSuite = newResearchersSuite;
    }

    public static class NoUpdate extends TaskSuiteUpdate {
        private final String name;
        private final String version;

        public NoUpdate(String name, String version) {
            super(false);
            this.name = name;
            this.version = version;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }
    }
}

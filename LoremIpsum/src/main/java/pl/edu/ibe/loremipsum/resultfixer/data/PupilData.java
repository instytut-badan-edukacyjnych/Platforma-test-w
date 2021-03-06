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

package pl.edu.ibe.loremipsum.resultfixer.data;

import pl.edu.ibe.loremipsum.tablet.BaseXMLFile;

/**
 * Examinee data
 */
@Deprecated
public class PupilData extends BaseXMLFile {

    /**
     * Default file name
     */
    public static final String APP_PUPILS_XML_FILENAME = "pupils.xml";



    /**
     * examinee id
     */
    public String m_id = "";
    /**
     * examinee name
     */
    public String m_name = "";
    /**
     * examinee surename
     */
    public String m_surename = "";
    /**
     * gender
     */
    public String m_gender = "";
    /**
     * birthday
     */
    public String m_birthday = "";
    /**
     * department
     */
    public String m_group = "";
    /**
     * institution id
     */
    public String m_schoolId = "";
    /**
     * Researcher
     */
    public String m_assignTo = "";
    /**
     * additional info dir
     */
    public String m_note = "";
    /**
     * additional info data
     */
    public String m_noteContent = "";

}

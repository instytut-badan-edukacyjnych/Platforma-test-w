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

package pl.edu.ibe.loremipsum.configuration;

/**
 * Gender identifier
 * Created by adam on 13.03.14.
 */
public enum Gender {
    MALE, FEMALE, NONE;
    /**
     * Legacy gender identifiers.
     */
    public static final String PUPIL_GENDER_MALE = "m";
    public static final String PUPIL_GENDER_MALE_UPPER = "M";
    public static final String PUPIL_GENDER_FEMALE = "f";
    public static final String PUPIL_GENDER_FEMALE_UPPER = "F";
    public static final String PUPIL_GENDER_FEMALE_ALT = "k";
    public static final String PUPIL_GENDER_FEMALE_ALT_UPPER = "K";

    /**
     * Resolves @link{Gender} string representation. Includes legacy versions.
     *
     * @param gender
     * @return @link{Gender}
     */
    public static Gender resolveGender(String gender) {
        Gender genderEnum;
        try {
            genderEnum = valueOf(gender);
        } catch (Exception e) {
            if (gender.equals(PUPIL_GENDER_FEMALE) || gender.equals(PUPIL_GENDER_FEMALE_ALT)
                    || gender.equals(PUPIL_GENDER_FEMALE_ALT_UPPER) || gender.equals(PUPIL_GENDER_FEMALE_UPPER)) {
                genderEnum = FEMALE;
            } else if (gender.equals(PUPIL_GENDER_MALE) || gender.equals(PUPIL_GENDER_MALE_UPPER)) {
                genderEnum = MALE;
            } else {
                genderEnum = NONE;
            }
        }
        return genderEnum;
    }
}

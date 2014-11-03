
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

package pl.edu.ibe.loremipsum.tablet;


/**
 * Klasa kontroli hasła badacza
 *
 *
 */
public class PasswordEncoder {

    /**
     * Sprawdzenie poprawnosci hasła
     *
     * @param a_enter    - wprowadzone hasło
     * @param a_password - zakodowane hasło
     * @return true jeżeli wprowadzone hasło odpowiada zakodowanemu hasłu
     */
    public static boolean CheckPassword(String a_enter, String a_password) {

        return a_password.compareTo(EncodePassword(a_enter)) == 0;
    }

    /**
     * Szyforwanie hasła
     *
     * @param a_enter - wprowadzone hasło
     * @return zaszyfrowane hasło
     */
    public static String EncodePassword(String a_enter) {

        StringBuffer encode = new StringBuffer(a_enter.length() * 3 + 3);
        int sum = 0;
        int d = 0;

        for (int index = 0; index < a_enter.length(); ++index) {
            int c = a_enter.charAt(index);

            d = ((c >> 6) & 0x07) + '1';
            sum ^= d;
            encode.append((char) d);

            d = ((c >> 3) & 0x07) + '1';
            sum ^= d;
            encode.append((char) d);

            d = (c & 0x07) + '1';
            sum ^= d;
            encode.append((char) d);

        }
        d = (sum & 0x0F) + 'a';
        encode.append((char) d);

        return encode.toString();
    }

}


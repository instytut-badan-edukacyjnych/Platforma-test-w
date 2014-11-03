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

package pl.edu.ibe.loremipsum.support;

/**
 * Created by adam on 18.04.14.
 * <p>
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
public class CredentialsPair<F, S> {
    public final F userName;
    public final S manifestUrl;

    /**
     * Constructor for a Pair.
     *
     * @param userName    the userName object in the Pair
     * @param manifestUrl the manifestUrl object in the pair
     */
    public CredentialsPair(F userName, S manifestUrl) {
        this.userName = userName;
        this.manifestUrl = manifestUrl;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param a the userName object in the Pair
     * @param b the manifestUrl object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> CredentialsPair<A, B> create(A a, B b) {
        return new CredentialsPair<A, B>(a, b);
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link CredentialsPair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     * equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CredentialsPair)) {
            return false;
        }
        CredentialsPair<?, ?> p = (CredentialsPair<?, ?>) o;
        return p.userName.equals(userName) && p.manifestUrl.equals(manifestUrl);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (userName == null ? 0 : userName.hashCode()) ^ (manifestUrl == null ? 0 : manifestUrl.hashCode());
    }
}

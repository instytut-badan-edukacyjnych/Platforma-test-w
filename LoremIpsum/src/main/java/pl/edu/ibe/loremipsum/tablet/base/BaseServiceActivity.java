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

package pl.edu.ibe.loremipsum.tablet.base;

import android.os.Bundle;

import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tools.StringUtils;

/**
 * Base activity for all acitivties with service.
 * Created by adam on 17.03.14.
 */
public abstract class BaseServiceActivity extends BaseLoremIpsumActivity {

    private ServiceProvider serviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceProvider = LoremIpsumApp.obtain().getServiceProvider();
    }

    /**
     * Returns service provider. Singleton for app.
     *
     * @return @link{ServiceProvider}
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public void setTitle(CharSequence title) {
        String titleString = title.toString();
        Researcher researcher = getServiceProvider().login().currentLoggedInUser;
        if (researcher != null) {
            if (!StringUtils.isEmpty(researcher.getFirstName()) && !StringUtils.isEmpty(researcher.getSurName())) {
                titleString += " (" + researcher.getFirstName() + " " + researcher.getSurName() + ")";
            } else {
                titleString += " (" + researcher.getTextId() + ")";
            }
        }
        super.setTitle(titleString);
    }
}

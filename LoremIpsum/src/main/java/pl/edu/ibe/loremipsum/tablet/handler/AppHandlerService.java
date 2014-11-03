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
package pl.edu.ibe.loremipsum.tablet.handler;

import android.os.Handler;
import android.os.Message;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Backward compatibility with old app version. Provides @link{AppHandler} operations. Use only if really necessary!
 * <p>
 * Created by adam on 21.03.14.
 */
@Deprecated
public final class AppHandlerService extends BaseService {
    private static final String TAG = AppHandlerService.class.toString();

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    @Deprecated
    public AppHandlerService(ServiceProvider services) {
        super(services);
    }

    /**
     * Provides backward compatibility. Use only if really necessary!
     *
     * @return @link{AppHandler} with given interface.
     */
    @Deprecated
    public AppHandler getAppHandler() {
        return new AppHandler();
    }

    /**
     * Provides backward compatibility.  Use only if really necessary!
     */
    @Deprecated
    public static interface AppHandlerInterface {
        /**
         * action on show login request
         *
         * @param msg
         */
        @Deprecated
        public void showLoginRequest(Message msg);

        /**
         * Action on load data message
         *
         * @param msg
         */
        @Deprecated
        public void loadData(Message msg);

        /**
         * Action on data loaded message
         *
         * @param msg
         */
        @Deprecated
        public void dataLoaded(Message msg);

        /**
         * Action on login message
         *
         * @param msg
         */
        @Deprecated
        public void login(Message msg);

        /**
         * Action on testEnding message
         *
         * @param msg
         */
        @Deprecated
        public void testEnding(Message msg);

        /**
         * Action on results loaded message
         *
         * @param msg
         */
        @Deprecated
        public void resultsLoaded(Message msg);
    }

    /**
     * Obsluga zdarzeń
     *
     *
     */
    @Deprecated
    public final class AppHandler extends Handler {
        @Deprecated
        private AppHandlerInterface appHandlerInterface;

        /**
         * Konstruktor
         */
        @Deprecated
        public AppHandler() {
            super();
        }

        @Deprecated
        public void setAppHandlerInteface(AppHandlerInterface appHandlerInteface) {
            this.appHandlerInterface = appHandlerInteface;
        }

        /*                 * (non-Javadoc)
                 * @see android.os.Handler#handleMessage(android.os.Message)
                 */
        @Override
        @Deprecated
        public void handleMessage(Message msg) {
            LogUtils.d(TAG, "handleMessage: " + msg.what);

            switch (msg.what) {
                case LoremIpsumApp.APP_MESS_SHOW_LOGIN_REQUEST:
                    appHandlerInterface.showLoginRequest(msg);
                    break;
                case LoremIpsumApp.APP_MESS_LOAD_DATA:
                    appHandlerInterface.loadData(msg);
                    break;
                case LoremIpsumApp.APP_MESS_DATA_LOADED:
                    appHandlerInterface.dataLoaded(msg);
                    break;
                case LoremIpsumApp.APP_MESS_LOGIN:
                    appHandlerInterface.login(msg);
                    break;
                case LoremIpsumApp.APP_MESS_TEST_ENDING:
                    appHandlerInterface.testEnding(msg);
//There was no break!
                case LoremIpsumApp.APP_MESS_RESULTS_LOADED:
                    appHandlerInterface.resultsLoaded(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}

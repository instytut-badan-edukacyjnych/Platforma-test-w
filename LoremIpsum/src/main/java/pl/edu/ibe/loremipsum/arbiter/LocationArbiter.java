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
package pl.edu.ibe.loremipsum.arbiter;


import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.BaseTask;


/**
 * Marks task based on object location.
 */
public class LocationArbiter extends BaseArbiter {
//	private static final String TAG = "LocationArbiter";


    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.arbiter.BaseArbiter#Assess(java.util.ArrayList, java.lang.String)
     */
    @Override
    public void Assess( ArrayList<BaseTask.FieldSelect> a_data, String a_answer ) {

        m_answer = LoremIpsumApp.APP_NO_FILL_FIELD;

        m_arbiter = APP_ARBITER_COMPUTER;
        if( ( a_data == null ) || ( a_answer == null ) ) {
            m_mark = APP_MARK_UNDEFINED;

            return;
        }

        boolean passed = true;
        m_answer = "";
        for( BaseTask.FieldSelect f : a_data ) {
            m_answer += f.m_name;

            if( !f.m_mirror ) {
                if( ( f.m_srce.left != f.m_xPlace ) || ( f.m_srce.top != f.m_yPlace ) ) {
                    passed = false;
                }
            } else {
                if( ( f.m_srce.right != f.m_xPlace ) || ( f.m_srce.top != f.m_yPlace ) ) {
                    passed = false;
                }
            }
        }

        if( passed ) {
            m_mark = APP_MARK_1;
        } else {
            m_mark = APP_MARK_0;
        }

    }

}



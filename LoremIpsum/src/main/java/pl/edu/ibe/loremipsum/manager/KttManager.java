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

import java.util.LinkedList;

import pl.edu.ibe.loremipsum.manager.Irt.IrtPiece;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.TaskInfo;

/**
 * Menager sumujacy punkty.
 * Point summarizing manager
 * @author Mikołaj
 */
public class KttManager extends CbtManager {

    public KttManager(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.manager.BaseManager#Mark(pl.edu.ibe.loremipsum.task.TaskInfo, int)
     */
    @Override
    public void Mark(TaskInfo a_task, double a_mark) {
        ManagerTestInfo info = m_taskScript.m_status.get(a_task.m_area);
        if (info == null) {
            info = new ManagerTestInfo();
            info.m_status = LoremIpsumApp.TEST_PROGRESS;
            info.m_vector.m_piece.clear();
            info.m_vector.m_theta = 0.0;
            info.m_vector.m_se = 1.0;

            m_taskScript.m_status.put(a_task.m_area, info);
        }

        IrtPiece piece = new IrtPiece();
        piece.m_irt_a = a_task.m_irt_a;
        piece.m_irt_bs = new LinkedList<>(a_task.m_irt_bs);
        piece.m_irt_c = a_task.m_irt_c;
        piece.m_mark = Irt.TranslateMark(a_mark, piece);

        info.m_vector.m_piece.add(piece);

        info.m_vector.m_theta += a_mark;
    }
}

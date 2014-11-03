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

package pl.edu.ibe.loremipsum.task.tpr.cordova.fourfields;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;

import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.arbiter.tpr.cordova.FourFieldsBoardArbiter;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.tpr.AutoTutorialEndTask;
import pl.edu.ibe.loremipsum.task.tpr.TprTaskGroup;

/**
 * Created by adam on 19.08.14.
 */
public class FourFieldsTaskGroup extends TprTaskGroup {
    private String area;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public FourFieldsTaskGroup(Context context) {
        super(context);
    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        area = a_info.m_area;
        return super.Create(a_info, a_handler, a_dir);
    }

    @Override
    public boolean NextTask() throws IOException {
        boolean returnValue = super.NextTask();
        return returnValue;
    }


    @Override
    public boolean SelectTask(int a_index) throws IOException {
        boolean returnValue = super.SelectTask(a_index);
        return returnValue;
    }


    @Override
    public MarkData GetMark() {
        MarkData markData;

        if (m_arbiter instanceof FourFieldsBoardArbiter && m_task instanceof FourFieldsBoardTask) {
            ((FourFieldsBoardArbiter) m_arbiter).setData(((FourFieldsBoardTask) m_task).getData());
        }
        //Super calls arbiter
        markData = super.GetMark();
        markData.area = area;
        return markData;
    }

    @Override
    public boolean tutorialIsSuccesful() {
        return false;
    }

    @Override
    public void clearTutorialData() {

    }

    @Override
    public AutoTutorialEndTask.Mode getMode() {
        return AutoTutorialEndTask.Mode.FOUR_FIELDS;
    }
}

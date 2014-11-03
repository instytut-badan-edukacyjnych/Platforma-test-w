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


import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import pl.edu.ibe.loremipsum.tools.LogUtils;


/**
 * Klasa (SurfaceView) podgladu obrazu z kamery
 *
 *
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraView.class.toString();

    private SurfaceHolder m_holder = null;
    private Camera m_camera = null;


    /**
     * Konstruktor
     *
     * @param context - kontekst wyswietlania
     */
    public CameraView(Context context) {
        super(context);

        m_holder = getHolder();
        m_holder.addCallback(this);
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
     */
//	@Override
    public void surfaceCreated(SurfaceHolder a_holder) {
        try {
            m_camera = Camera.open();
            m_camera.setPreviewDisplay(m_holder);
            m_camera.setDisplayOrientation(180);

        } catch (IOException e) {
            LogUtils.e(TAG, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
     */
//	@Override
    public void surfaceChanged(SurfaceHolder a_holder, int a_format, int a_width, int a_height) {

//		Camera.Parameters param = m_camera.getParameters();
//		param.setPreviewSize( a_width, a_height );
//		m_camera.setParameters( param );
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
//	@Override
    public void surfaceDestroyed(SurfaceHolder a_holder) {

        if (m_camera != null) {
            m_camera.stopPreview();
            m_camera.release();
            m_camera = null;
        }
    }

    /**
     * Zwraca uchwyt do kamery
     *
     * @return uchwyt do kamery
     */
    public Camera GetCamera() {

        return m_camera;
    }

    /**
     * Uruchamia podgląd z kamery
     */
    public void StartPreview() {

        if (m_camera != null) {
            m_camera.startPreview();
        }
    }

    /**
     * Zatrzymuje podgląd z kamery
     */
    public void StopPreview() {

        if (m_camera != null) {
            m_camera.stopPreview();
        }
    }

}




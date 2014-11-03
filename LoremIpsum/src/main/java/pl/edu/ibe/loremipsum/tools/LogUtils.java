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

package pl.edu.ibe.loremipsum.tools;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;

/**
 * @author Mariusz Pluciński
 */
public class LogUtils {
    private static final String TAG = LogUtils.class.toString();
    private static final String LOGS_DIRECTORY = "logs";
    private static Logger logger = Logger.getLogger("LoremIpsum");
    private static StreamHandler handler = null;

    public synchronized static File getLogsDirectory() {
        return new File(LoremIpsumApp.obtain().getApplicationContext().getFilesDir(), LOGS_DIRECTORY);
    }

    private synchronized static void _checkLogger() {
        if (handler == null) {
            try {
                String name = "LoremIpsum_"
                        + TimeUtils.dateToString(new Date(), "yyyy-MM-dd_HH-mm-ss_sss") + ".log";
                File dir = getLogsDirectory();
                if (!dir.exists() && !dir.mkdir())
                    throw new IOException("Could not create directory \"" + dir + "\"");
                File file = new File(dir, name);
                handler = new FileHandler(file.getAbsolutePath());
                handler.setFormatter(new SimpleFormatter());
                handler.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.setLevel(Level.ALL);
            } catch (ParseException | IOException e) {
                Log.wtf(TAG, "Could not add file handler to logger", e);
            }
        }
    }

    private static void _log(Level level, String tag, String message) {
        _checkLogger();
        logger.log(level, tag + ": " + message);
        handler.flush();
    }

    private static void _log(Level level, String tag, String message, Throwable tr) {
        _checkLogger();
        logger.log(level, tag + ": " + message, tr);
        handler.flush();
    }

    private static Level _prioToLevel(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return Level.FINEST;
            case Log.DEBUG:
                return Level.FINER;
            case Log.INFO:
                return Level.FINE;
            case Log.WARN:
                return Level.WARNING;
            case Log.ERROR:
                return Level.SEVERE;
            case Log.ASSERT:
                return Level.SEVERE;
            default:
                return Level.SEVERE;
        }
    }

    public static int d(String tag, String msg) {
        _log(_prioToLevel(Log.DEBUG), tag, msg);
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        _log(_prioToLevel(Log.DEBUG), tag, msg, tr);
        return Log.d(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        _log(_prioToLevel(Log.ERROR), tag, msg);
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        _log(_prioToLevel(Log.ERROR), tag, msg, tr);
        return Log.e(tag, msg, tr);
    }

    public static int e(String tag, Throwable tr) {
        return e(tag, "Exception occurred", tr);
    }

    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    public static int i(String tag, String msg) {
        _log(_prioToLevel(Log.INFO), tag, msg);
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        _log(_prioToLevel(Log.INFO), tag, msg, tr);
        return Log.i(tag, msg, tr);
    }

    public static boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }

    public static int println(int priority, String tag, String msg) {
        _log(_prioToLevel(priority), tag, msg);
        return Log.println(priority, tag, msg);
    }

    public static int v(String tag, String msg) {
        _log(_prioToLevel(Log.VERBOSE), tag, msg);
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        _log(_prioToLevel(Log.VERBOSE), tag, msg, tr);
        return Log.v(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        _log(_prioToLevel(Log.WARN), tag, "Exception occurred", tr);
        return Log.w(tag, tr);
    }

    public static int w(String tag, String msg, Throwable tr) {
        _log(_prioToLevel(Log.WARN), tag, msg, tr);
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        _log(_prioToLevel(Log.WARN), tag, msg);
        return Log.w(tag, msg);
    }

    public static int wtf(String tag, Throwable tr) {
        _log(Level.SEVERE, tag, "What a Terrible Failure!", tr);
        return Log.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg) {
        _log(Level.SEVERE, tag, "What a Terrible Failure! " + msg);
        return Log.wtf(tag, msg);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        _log(Level.SEVERE, tag, "What a Terrible Failure! " + msg, tr);
        return Log.wtf(tag, msg, tr);
    }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by adam on 11.03.14.
 */
public class StringUtils {


    private static final String TAG = StringUtils.class.getCanonicalName();

    /**
     * Checks if string is empty
     *
     * @param string
     * @return true if string is empty
     */
    public static boolean isEmpty(String string) {
        return string == null || string.equals("");
    }


    public static String join(String[] strings) {
        return join(strings, "");
    }

    public static String join(String[] strings, String separator) {
        return join(strings, separator, 0, strings.length);
    }

    public static String join(String[] strings, String separator, int start, int count) {
        StringBuilder builder = new StringBuilder();
        int end = start + count;
        for (int i = start; i < end; ++i) {
            builder.append(strings[i]);
            if (i + 1 < end)
                builder.append(separator);
        }
        return builder.toString();
    }

    /**
     * Writes string to file
     *
     * @param file
     * @param string
     */
    public static void stringToFile(File file, String string) {
        FileOutputStream fop = null;
        try {
            fop = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }

            byte[] contentInBytes = string.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String inputStreamToString(InputStream inputStream) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public static String readFile(File file) {
        StringBuilder builder = null;
        builder = new StringBuilder();
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            Reader reader = new BufferedReader(new InputStreamReader(stream));


            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (Exception e) {
            LogUtils.d(TAG, "failed to read file ", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LogUtils.d(TAG, "failed to read file ", e);
                }
            }
        }
        return builder.toString();
    }

}

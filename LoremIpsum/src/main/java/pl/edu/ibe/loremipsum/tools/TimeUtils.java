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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by adam on 20.03.14.
 */
public class TimeUtils {


    /**
     * "dd-MM-yyyy" pattern.
     */
    //TODO this name is quite awful it doesn't tell anything about this pattern
    public static final String defaultPatern = "dd-MM-yyyy";

    /**
     * "dd-MM-yyyy HH:mm" pattern.
     */
    public static final String timePatern = "dd-MM-yyyy HH:mm:ss";

    /**
     * Pattern for file names containing current date and time
     */
    public static final String dateTimeFileNamePattern = "yyyy-MM-dd HH-mm-ss";

    /**
     * "yyyy'-'MM'-'dd'T'HH':'mm':'ss" pattern.
     */
    public static final String rfcFormat = "yyyy'-'MM'-'dd'T'HH'_'mm'_'ssZ";
    public static final String reportFormat= "yyyy'-'MM'-'dd'T'HH':'mm':'ssZ";
    /**
     * list of date formatters.
     */
    private static HashMap<String, SimpleDateFormat> dateFormats = new HashMap<String, SimpleDateFormat>();

    /**
     * String to date.
     *
     * @param date    the date
     * @param pattern the pattern
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date stringToDate(String date, String pattern) throws ParseException {
        SimpleDateFormat formatter = getFormatter(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        return formatter.parse(date);
    }

    /**
     * Date to string.
     *
     * @param date    the date
     * @param pattern the pattern
     * @return the string
     * @throws ParseException the parse exception
     */
    public static String dateToString(Date date, String pattern) throws ParseException {
        SimpleDateFormat formatter = getFormatter(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        if (date == null)
            throw new NullPointerException("date is null");
        return formatter.format(date);
    }

    /**
     * Change string date pattern.
     *
     * @param inputPattern  the input pattern
     * @param outputPattern the output pattern
     * @param dateString    the date string
     * @return the string
     * @throws ParseException the parse exception
     */
    public static String changeStringDatePattern(String inputPattern, String outputPattern, String dateString)
            throws ParseException {
        Date date = stringToDate(dateString, inputPattern);
        return dateToString(date, outputPattern);

    }

    /**
     * Gets the formatter.
     *
     * @param pattern the pattern
     * @return the formatter
     */
    private static SimpleDateFormat getFormatter(String pattern) {
        SimpleDateFormat formatter = dateFormats.get(pattern);
        if (formatter == null) {
            formatter = new SimpleDateFormat(pattern);
            dateFormats.put(pattern, formatter);
        }
        return formatter;
    }

}

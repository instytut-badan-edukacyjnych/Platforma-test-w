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

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Locale;

import pl.edu.ibe.loremipsum.tools.LocaleUtils;

/**
 * Created by adam on 09.05.14.
 */
public class SupportResponse {
    private static final String TAG = SupportResponse.class.toString();

    public long reportId;
    private HashMap<String, SupportInfo> supportInfo;

    public SupportInfo getInfo(Locale locale) throws SupportInfoNotFound {
        try {
            Locale l = locale;
            while (true) {
                String l1 = "null";
                if (l != null)
                    l1 = l.toString();
                if (supportInfo.containsKey(l1))
                    return supportInfo.get(l1);
                l = LocaleUtils.getOuterLocale(l);
            }
        } catch (LocaleUtils.NoOuterLocale e) {
            throw new SupportInfoNotFound("Could not find support info for locale " + locale, e);
        }
    }

    public long getReportId() {
        return reportId;
    }

    public SupportInfo getInfo() throws SupportInfoNotFound {
        return getInfo(Locale.getDefault());
    }

    public static class SupportInfo {
        @SerializedName("phone_number")
        private String phoneNumber;
        @SerializedName("time_span")
        private int timeSpan;
        private String email;
        @SerializedName("additional_info")
        private String additionalInfo;


        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SupportInfo))
                return false;
            SupportInfo other1 = (SupportInfo) other;
            return getPhoneNumber().equals(other1.getPhoneNumber()) &&
                    getTimeSpan() == other1.getTimeSpan() &&
                    getEmail().equals(other1.getEmail()) &&
                    getAdditionalInfo().equals(other1.getAdditionalInfo());
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public int getTimeSpan() {
            return timeSpan;
        }

        public String getEmail() {
            return email;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }
    }

    public static class SupportInfoNotFound extends Exception {
        public SupportInfoNotFound() {
        }

        public SupportInfoNotFound(String detailMessage) {
            super(detailMessage);
        }

        public SupportInfoNotFound(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public SupportInfoNotFound(Throwable throwable) {
            super(throwable);
        }
    }
}

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

package pl.edu.ibe.loremipsum.arbiter.tpr;

import pl.edu.ibe.loremipsum.arbiter.BaseArbiter;
import pl.edu.ibe.loremipsum.arbiter.MarkData;

/**
 * Created by adam on 24.09.14.
 */
public abstract class BaseTprArbiter extends BaseArbiter {
    protected static final String SEPARATOR = ";";
    protected static final String ABSTRACT = "A";
    protected static final String NORMAL = "N";
    protected static final String TRUE = "1";
    protected static final String FALSE = "0";


    protected String answerString = "";
    protected int maxCorrectAnswersCount;
    protected int userCorrectAnswers;
    protected String area;


    public abstract String getAnswerString();

    public abstract double getPercentageMark();

    @Override
    public MarkData GetMark() {
        MarkData mark = super.GetMark();
        mark.m_answer = getAnswerString();
        mark.m_mark = getPercentageMark();
        mark.area = area;
        return mark;
    }
    protected String getTypeLetter(boolean isAbstract) {
        return isAbstract ? ABSTRACT : NORMAL;
    }
}

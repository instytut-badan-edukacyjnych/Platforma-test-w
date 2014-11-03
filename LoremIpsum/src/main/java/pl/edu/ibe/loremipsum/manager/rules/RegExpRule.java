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



package pl.edu.ibe.loremipsum.manager.rules;

import java.util.HashMap;
import java.util.regex.Pattern;

import pl.edu.ibe.loremipsum.manager.MappingDependency;
import rx.functions.Func1;

/**
 * Created by mikolaj on 18.04.14.
 */
public class RegExpRule extends FuncMappingRule<String> {

    private static HashMap<String, Pattern> patterns = new HashMap<>();
    private final String regEx;

    public RegExpRule(String regEx, Func1<MappingDependency, String> evaluateFunc) {
        super(evaluateFunc);
        this.regEx = regEx;
    }

    @Override
    public boolean isValid(MappingDependency dependency) {
        Pattern pattern = getPattern();
        return pattern.matcher(evaluate(dependency)).find();
    }

    private Pattern getPattern() {
        Pattern pattern = patterns.get(regEx);
        if (pattern == null) {
            pattern = Pattern.compile(regEx);
        }
        return pattern;
    }
}

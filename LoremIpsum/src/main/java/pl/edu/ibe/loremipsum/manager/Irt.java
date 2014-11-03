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


import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Irt handler
 */
public class Irt {

    /**
     * Minimalna wartość zatrzymania algotymu obliczającego theta
     * Theta calculation  mnimal stop value
     */
    public static final double IRT_THETA_EPSYLON_LIMIT = 0.0001;
    private static final String TAG = Irt.class.toString();
    /**
     * factor
     */
    public static double s_D = 1.702;

    /**
     * Calculates Pi(theta) Implemets test scale 0-1 and 0-1-2
     *
     * @param a_theta - theta
     * @param a_piece -  IRT params
     * @param a_mark  - mark. possible values 0,1,2
     * @return calculated value Pi(theta)
     */
    public static double CalculatePi(double a_theta, IrtPiece a_piece, int a_mark) {

        LinkedList<Double> exps = new LinkedList<>();
        exps.add(1d);
        double lastVal = 0d;
        double expSum = 1d;
        for (Double valB : a_piece.m_irt_bs) {
            lastVal += a_theta - valB;
            double exp = Math.exp(a_piece.m_irt_a * s_D * lastVal);
            expSum += exp;
            exps.add(exp);
        }

        a_mark = TranslateMark(a_mark, a_piece);
        return a_piece.m_irt_c + (1.0 - a_piece.m_irt_c) * exps.get(a_mark) / expSum;
    }

    /**
     * Calculates I(theta) value for answer vector. Implements test scale 0-1 and 0-1-2
     *
     * @param a_theta - theta
     * @param a_data  - answer vector
     * @return calculated I(theta)
     */
    public static double CalculateI(double a_theta, Vector<IrtPiece> a_data) {
        double Isum = 0.0;
        for (IrtPiece p : a_data) {
            double Pi = CalculatePi(a_theta, p, p.m_mark);

            double I = (Pi - p.m_irt_c) / (1.0 - p.m_irt_c);
            I *= I;
            I *= (1.0 - Pi) / Pi;
            I *= p.m_irt_a * p.m_irt_a * s_D * s_D;

            Isum += I;
        }

        return Isum;
    }

    /**
     * Estimates biggest propability for answer vector. Implements answer scale 0-1 and 0-1-2
     *
     * @param a_theta - theta
     * @param a_data  - - answer vector
     * @return calculates value I(theta)
     */
    public static double CalculateMLE(double a_theta, Vector<IrtPiece> a_data) {
        double MLE = 1.0;
        for (IrtPiece p : a_data) {
            MLE *= CalculatePi(a_theta, p, p.m_mark);
        }

        return MLE;
    }

    /**
     * Calculates theta using interpolation (brute force)
     *
     * @param a_batch  - wektor odpowiedzi. Zwraca obliczone wartości theta i se
     *                 answer vector. Returns calculated theta nad se values
     * @param a_eps    - required
     * @param a_step   - krok zmiany thety jezeli w wektorze nie ma jeszcze poprawnej i nieporawnej odpowiedzi
     *                 theta change step. If theres no good and wrong answer in vector
     * @param a_thetaS - theta początkowa
     *                 base theta
     * @param a_min    - miminalna dopuszczalna wartośc theta
     *                 min possible theta
     * @param a_max    - maksymalna dopuszczalna wartość theta
     *                 max possible theta
     * @return true jezeli obliczenia zostały zakończone, false jeżeli podczas obliczeń wystapił bład
     * true if calculations has ended false if error
     */
    public static boolean CalculateThetaBF(IrtBatch a_batch, double a_eps, double a_step, double a_thetaS, double a_min, double a_max) {
        if (a_batch == null) {
            LogUtils.e(TAG, "CalculateThetaBF <error> bath == null");
            return false;
        }
        if (a_batch.m_piece == null) {
            LogUtils.e(TAG, "CalculateThetaBF <error> data == null");
            return false;
        }

        int n = 0;
        int r = 0;
        for (IrtPiece p : a_batch.m_piece) {
            r += p.m_mark;
            n += p.getM_range();
        }

        if (r != 0 && r != n) {
            try {
                a_batch.m_theta = CalculateThetaBF(a_batch, a_eps, a_min, a_max);
            } catch (IrtException e) {
                LogUtils.e(TAG, e);
                return false;
            }
        } else {
            // nie uzyskano jeszcze prawidłowej i nieprawidowej odpowiedzi
            a_batch.m_theta = CalculateThetaStep(r == 0 ? true : false, n, a_step, a_thetaS, a_min, a_max);
        }

        double I = CalculateI(a_batch.m_theta, a_batch.m_piece);
        if (I < 0.0) {
            LogUtils.e(TAG, "CalculateThetaBF <error> CalculateI: " + I);
            return false;
        }

        a_batch.m_se = 1.0 / Math.sqrt(I);

        return true;
    }

    /**
     * Ostateczne obliczanie wartości theta za pomocą interpolacji (brute force)
     * Final theta calculation using interpolation (brute force)
     *
     * @param a_batch - wektor odpowiedzi. Zwraca obliczone wartości theta i se
     *                answer vector. Returns calculated thera and se
     * @param a_eps   - wymagany
     *                - required
     * @param a_min   - miminalna dopuszczalna wartośc theta
     *                min possible theta
     * @param a_max   - maksymalna dopuszczalna wartość theta
     *                max possible theta
     * @return true jezeli obliczenia zostały zakończone, false jeżeli podczas obliczeń wystapił bład
     * true if calculations has ended. false if error
     */
    public static boolean CalculateFinalBF(IrtBatch a_batch, double a_eps, double a_min, double a_max) {
        if (a_batch == null) {
            LogUtils.e(TAG, "CalculateThetaBF <error> bath == null");
            return false;
        }
        if (a_batch.m_piece == null) {
            LogUtils.e(TAG, "CalculateThetaBF <error> data == null");
            return false;
        }

        int n = 0;
        int r = 0;
        for (IrtPiece p : a_batch.m_piece) {
            r += p.m_mark;
            n += p.getM_range();
        }

        double theta = 0.0;
        if (r != 0 && r != n) {
            try {
                theta = CalculateThetaBF(a_batch, a_eps, a_min, a_max);
            } catch (IrtException e) {
                LogUtils.e(TAG, e);
                return false;
            }
        } else {
            theta = CalculateThetaMinMax(a_batch);
        }

        a_batch.m_theta = theta;

        double I = CalculateI(theta, a_batch.m_piece);
        if (I < 0.0) {
            LogUtils.e(TAG, "CalculateThetaBF <error> CalculateI: " + I);
            return false;
        }

        a_batch.m_se = 1.0 / Math.sqrt(I);

        return true;
    }

    /**
     * Mark limi 0-1. No answer koded as 9 will be changed to 0
     *
     * @param a_mark  - mark to code
     * @param a_piece
     * @return Coded mark
     */
    public static int TranslateMark(double a_mark, IrtPiece a_piece) {
        return Math.max(0, Math.min((int) a_mark, a_piece.m_irt_bs.size()));
    }

    /**
     * Theta estimation for identical answer based on test number
     *
     * @param a_down   - true if down estimation, flase up estimation
     * @param a_n      - number of answers
     * @param a_step   - Theta step change
     * @param a_thetaS - start theta
     * @param a_min    - min theta
     * @param a_max    -      max possible theta
     * @return estimated theta value
     */
    private static double CalculateThetaStep(boolean a_down, int a_n, double a_step, double a_thetaS, double a_min, double a_max) {

        if (a_down) {
            double theta = a_thetaS - (double) a_n * a_step;
            if (theta < a_min) {
                theta = a_min;
            }

            return theta;
        } else {
            double theta = a_thetaS + (double) a_n * a_step;
            if (theta > a_max) {
                theta = a_max;
            }

            return theta;
        }
    }

    /**
     * Szacowanie thety dla udzielonych jednakowych odpowiedzi na podstawie trudnosci zadań
     * Theta estimation for idnetical answers based on task difficulty
     *
     * @param a_batch - wektor odpowiedzi
     * @return oszacowana wartość thety
     */
    private static double CalculateThetaMinMax(IrtBatch a_batch) {
        if (a_batch.m_piece.size() == 0) {
            return 0d;
        }

        double theta = a_batch.m_piece.get(0).m_irt_bs.getLast();

        for (IrtPiece p : a_batch.m_piece) {
            if (theta < p.m_irt_bs.getLast()) {
                theta = p.m_irt_bs.getLast();
            }
        }

        return theta;
    }

    /**
     * theta estimation for various answers using interpolation (brute force)
     *
     * @param a_batch - answer vector
     * @param a_eps   - załozona dokładność oszacowania
     *                assumed estimation accuracy
     * @param a_min   - miminalna dopuszczalna wartośc theta
     *                min theta
     * @param a_max   - maksymalna dopuszczalna wartość theta
     *                max theta
     * @return oszacowana wartość thety
     * estimated theta value
     * @throws IrtException
     */
    private static double CalculateThetaBF(IrtBatch a_batch, double a_eps, double a_min, double a_max) throws IrtException {

        if (a_batch == null) {
            throw new IrtException(TAG + ".CalculateTheta <error> bath == null");
        }
        if (a_batch.m_piece == null) {
            throw new IrtException(TAG + ".CalculateTheta <error> data == null");
        }

        Date start = new Date();

        double step = (a_max - a_min) / 20.0;
        double maxTheta = 0.0;
        double maxMLE = 0.0;
        double left = a_min;
        double right = a_max;
        double theta = 0.0;
        for (; step > a_eps; step *= 0.1) {
            for (theta = left; theta <= right; theta += step) {
                double MLE = CalculateMLE(theta, a_batch.m_piece);
                if (maxMLE < MLE) {
                    maxTheta = theta;
                    maxMLE = MLE;
                }
            }

            left = maxTheta - step;
            right = maxTheta + step;
        }

        a_batch.m_theta = maxTheta;

        if (LoremIpsumApp.RECORDING_FLAG) {
            Date finish = new Date();
            long time = finish.getTime() - start.getTime();
            LogUtils.i(TAG, "CalculateTheta time: " + time);
        }

        return maxTheta;
    }

    /**
     * keeps answer vector, calculated theta and se
     */
    public static class IrtBatch {

        /**
         * Claculated skill level
         */
        public double m_theta = 0.0;
        /**
         * obliczona wartość standardowego błędu oceny
         * Claculated standard mark error
         */
        public double m_se = 0.0;

        /**
         * answer vector
         */
        public Vector<IrtPiece> m_piece = new Vector<IrtPiece>();
    }

    /**
     * Keeps answers IRT params
     */
    public static class IrtPiece {

        /**
         * mark
         */
        public int m_mark = 0;

        /**
         * IRT parameter
         */
        public double m_irt_a = 1.0;
        /**
         * parametry b'x IRT dla zadań z szerszą skalą
         * IRT params b'x for wider scale tasks
         */
        public LinkedList<Double> m_irt_bs = new LinkedList<>();
        /**
         * C IRT parameter
         */
        public double m_irt_c = 0.0;

        /**
         * mark range
         */
        public int getM_range() {
            return m_irt_bs.size();
        }
    }
}



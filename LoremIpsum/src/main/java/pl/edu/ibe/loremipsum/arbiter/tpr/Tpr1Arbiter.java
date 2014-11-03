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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by adam on 19.08.14.
 */
public abstract class Tpr1Arbiter extends BaseTprArbiter {


    protected Tpr1Arbiter() {
        super();
    }




    protected String makeTaskColumns(int nrTask, ArrayList<Integer> conf, String strK, boolean onlyOne) {
        String str = "";
        int[] tsArr = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        //str += "\n";
        int c;
        int ts;
        if (!onlyOne) {
            for (int i = 1; i < conf.size() + 1; i++) {
                c = conf.get(i - 1);
                if (nrTask == 6) c = (int) Math.floor(c / 10);
                tsArr[c]++;
                ts = tsArr[c];
                str += "z" + nrTask + "_s" + c + "" + ts + strK + ";";
            }
        } else {
            str += "z" + nrTask + strK + ";";
        }
        tsArr = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String PF = "";
        for (int i = 1; i < conf.size() + 1; i++) {
            c = conf.get(i - 1);
            if (nrTask == 6) c = (int) Math.floor(c / 10);
            tsArr[c]++;
            ts = tsArr[c];
            if (onlyOne) {
                if (nrTask == 6) PF = (conf.get(i - 1) % 10 == 1) ? "_n" : "_t";
                str += "z" + nrTask + "_s" + c + "" + ts + strK + PF + ";";
            } else {
                for (int j = 1; j < c + 1; j++) {
//                    if(nrTask == 2) PF=(confZad2.get(i-1).get(1).get(j-1)==1) ? "P" : "F";
                    str += "z" + nrTask + "_s" + c + "" + ts + "_p" + j + strK + PF + ";";
                }
            }
        }
//        if(nrTask == 2) {
//            var tsArr=[0,0,0,0,0,0,0,0,0,0,0];
//            if(!onlyOne) {
//                for(var i=1; i < conf.length+1; i++) {
//                    c =  conf[i-1];
//                    tsArr[c]++;
//                    var ts = tsArr[c];
//                    str += "z"+nrTask+"_s"+c+""+ts+"pam"+strK+";";
//                }
//            }
//            var tsArr=[0,0,0,0,0,0,0,0,0,0,0];
//            for(var i=1; i < conf.length+1; i++) {
//                c =  conf[i-1];
//                tsArr[c]++;
//                var ts = tsArr[c];
//                if(onlyOne) {
//                    str += "z"+nrTask+"_s"+c+""+ts+strK+";";
//                }
//                else {
//                    for(var j=1;j<c+1;j++) {
//                        if(nrTask === 2) PF=(confZad2[i-1][1][j-1]===1) ? "P" : "F";
//                        str += "z"+nrTask+"_s"+c+""+ts+"pam"+"_p"+j+strK+PF+";";
//                    }
//                }
//            }
//        }

        tsArr = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 1; i < conf.size() + 1; i++) {
            c = conf.get(i - 1);
            if (nrTask == 6) c = (int) Math.floor(c / 10);
            tsArr[c]++;
            ts = tsArr[c];
            str += "z" + nrTask + "_t" + c + "" + ts + strK + ";";
        }
        if (nrTask == 5 || nrTask == 6) {
            tsArr = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 1; i < conf.size() + 1; i++) {
                c = conf.get(i - 1);
                if (nrTask == 6) c = (int) Math.floor(c / 10);
                tsArr[c]++;
                ts = tsArr[c];
                str += "z" + nrTask + "_c_t" + c + "" + ts + strK + ";";
            }
        }
        tsArr = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        if(nrTask == 9) {
//
//            for(var i=1; i < conf.length+1; i++) {
//                c =  conf[i-1];
//                if(nrTask === 6) c=Math.floor( c/10);
//                tsArr[c]++;
//                var ts = tsArr[c];
//                str += "z"+nrTask+"_n"+c+""+ts+strK+";";
//
//            }
//        }
        return str;
    }


    protected String makeResultRowTPR1(int nrTask, ArrayList<Double> result, ArrayList<Long> times, ArrayList<Integer> conf, boolean onlyOne, long overallTime, boolean taskIsCompleted) {
        String str = "";
        int rl = result.size();
        int tl = times.size();

        int sum = 0;

        for (Integer integer : conf) {
            sum += integer;
        }

        boolean oo = onlyOne;

        ArrayList<ArrayList<ResultsObject>> sortResult = makeResultArray(new ArrayList<>(conf), oo);
        // zrobiebnie resultatu np. dla conf = [2,4]
        //sortResult = [[],
        //				[[-1,-1]],
        //				[],
        //				[[-1,-1,-1,-1]] ]

		/*for(var i=0; i<Counting.config[1].length;i++) {
            sortResult[i]=[Counting.config[1][i], ( Counting.balls_count[i]===Counting.results[i]) ? 1 : 0];
		}*/

        // wype�nieni wynikami
        int sumC = 0;
        for (int i = 0; i < conf.size() && (sumC < result.size() || onlyOne) && (i < result.size() || !onlyOne) && i < tl; i++) {
            int c = conf.get(i);
            int k = 0;
            if (c > 0) {
                while (k < sortResult.get(c - 1).size()
                        && sortResult.get(c - 1).get(k).r.get(0) != -1) {
                    k++;
                }
            }
            if (!onlyOne) {
                for (int j = 0; j < c; j++) {
                    if (sumC + j < result.size()) {
                        sortResult.get(c - 1).get(k).r.set(j, result.get(sumC + j));
                    }
                }
            } else {
                sortResult.get(c - 1).get(k).r.set(0, result.get(i));
            }

            sortResult.get(c - 1).get(k).t = times.get(i);
            sumC += c;
        }
        //posortowane
        // sumy
        if (!onlyOne) {
            for (int i = 0; i < sortResult.size(); i++) {
                for (int j = 0; j < sortResult.get(i).size(); j++) {
                    sum = -1;
                    for (int k = 0; k < sortResult.get(i).get(j).r.size(); k++) {
                        if (sortResult.get(i).get(j).r.get(k) != -1) {
                            if (sum == -1) sum = 0;
                            sum += sortResult.get(i).get(j).r.get(k);
                        }
                    }
                    if (sum != -1) str += sum + ";";
                    else str += "99;";
                }

            }
        } else {
            if (rl == 0) str += "99;";
            else {
                sum = 0;
                for (int j = 0; j < rl; j++) {
                    if (result.get(j) > 0) sum += result.get(j);
                }
                str += sum + ";";
            }
        }
        for (int i = 0; i < sortResult.size(); i++) {

            for (int j = 0; j < sortResult.get(i).size(); j++) {

                for (int k = 0; k < sortResult.get(i).get(j).r.size(); k++) {
                    if (sortResult.get(i).get(j).r.get(k) != -1)
                        str += sortResult.get(i).get(j).r.get(k) + ";";
                    else
                        str += "9;";
                }

            }

        }

//        if (nrTask == = 2 && typeof Global.results[2].resultpam != = "undefined"){
//            var result2 = Global.results[2].resultpam;
//            var sortResult2 = makeResultArray(conf.slice());
//            // zrobiebnie resultatu np. dla conf = [2,4]
//            //sortResult = [[],
//            //				[[-1,-1]],
//            //				[],
//            //				[[-1,-1,-1,-1]] ]
//
//				/*for(var i=0; i<Counting.config[1].length;i++) {
//                    sortResult[i]=[Counting.config[1][i], ( Counting.balls_count[i]===Counting.results[i]) ? 1 : 0];
//				}*/
//
//            // wype�nieni wynikami
//            var sumC = 0;
//            for (var i = 0; i < conf.length && sumC < result2.length; i++) {
//                c = conf[i];
//                var k = 0;
//                while (k < sortResult2[c - 1].length
//                        && sortResult2[c - 1][k].r[0] != = -1) {
//                    k++;
//                }
//                for (var j = 0; j < c; j++) {
//                    sortResult2[c - 1][k].r[j] = result2[sumC + j];
//                }
//                //sortResult2[c-1][k].t=times[i];
//                sumC += c;
//            }
//            //posortowane
//            // sumy
//
//            for (var i = 0; i < sortResult2.length; i++) {
//                for (var j = 0; j < sortResult2[i].length; j++) {
//                    var sum = -1;
//                    for (var k = 0; k < sortResult2[i][j].r.length; k++) {
//                        if (sortResult2[i][j].r[k] != = -1) {
//                            if (sum == = -1) sum = 0;
//                            sum += sortResult2[i][j].r[k];
//                        }
//                    }
//                    if (sum != = -1) str += sum + ";";
//                    else str += "99;";
//                }
//
//            }
//
//            for (var i = 0; i < sortResult2.length; i++) {
//
//                for (var j = 0; j < sortResult2[i].length; j++) {
//                    var sum = 0;
//                    for (var k = 0; k < sortResult2[i][j].r.length; k++) {
//                        if (sortResult2[i][j].r[k] != = -1)
//                            str += sortResult2[i][j].r[k] + ";";
//                        else
//                            str += "9;";
//                    }
//
//                }
//
//            }
//        } // dla zadania drugiego s� te� wyniki zapami�tanych s��w


        for (int i = 0; i < sortResult.size(); i++) {
            for (int j = 0; j < sortResult.get(i).size(); j++) {
                if (sortResult.get(i).get(j).t != -1) str += sortResult.get(i).get(j).t + ";";
                else str += "999999;";
            }

        }
        //zapist CRT
        if (nrTask == 5 || nrTask == 6) {
            for (int i = 0; i < sortResult.size(); i++) {
                for (int j = 0; j < sortResult.get(i).size(); j++) {
                    if (sortResult.get(i).get(j).t != -1 && sortResult.get(i).get(j).r.get(0) == 1)
                        str += sortResult.get(i).get(j).t + ";";
                    else str += "999999;";
                }

            }
        }
//        if (nrTask == = 8) {
//            if (Finding.wrong != = -1) str += Finding.wrong + ";";
//            else str += "0;";
//            if (Finding.wrongTime != = 0) str += Finding.wrongTime + ";";
//            else str += "999999;";
//        }
//
//
//        if (nrTask == = 9) {
//            var wl = Tracking.wrong.length;
//            for (var i = 0; i < conf.length; i++) {
//                if (i < wl) str += Tracking.wrong[i] + ";";
//                else str += "9;";
//
//            }
//        }
/// nie posrotowane
        // sumy
        if (!onlyOne) {

            for (int i = 0; i < conf.size(); i++) {
                int c = conf.get(i);
                if (i < tl) {
                    sum = 0;
                    for (int j = 0; j < c; j++) {
                        sum += result.get(j);
                    }
                    str += sum + ";";
                } else str += "99;";
            }
        } else {
            if (rl == 0) str += "99;";
            else {
                sum = 0;
                for (int j = 0; j < rl; j++) {
                    if (result.get(j) > 0) sum += result.get(j);
                }
                str += sum + ";";
            }
        }
        sumC = 0;
        for (int i = 0; i < conf.size(); i++) {
            int c = conf.get(i);
            if (onlyOne) {
                if (i < rl && result.get(i) != -1) str += result.get(i) + ";";
                else str += "99;";
            } else {
                if (sumC < rl) {
                    for (int j = 0; j < c; j++) {
                        if (sumC + j < result.size() && result.get(sumC + j) != -1)
                            str += result.get(sumC + j) + ";";
                        else str += "9;";
                    }
                } else {
                    for (int j = 0; j < c; j++) {
                        str += "9;";
                    }
                }
            }
            sumC += c;
        }

		/*	else {
            if(rl===0) 	str += ";";
			else str += result.reduce(function(a,b){if(b!==-1) return a+b; else return a;})+";";
		}
		for(var i=0; i < sortResult.length; i++) {

			for(var j=0;j<sortResult[i].length;j++) {

				for(var k=0;k<sortResult[i][j].r.length;k++) {
					if(sortResult[i][j].r[k]!==-1)
						str += sortResult[i][j].r[k]+";";
					else
						str += ";";
				}

			}

		}
*/
//        if (nrTask ==  2) {
//            var result2 = Global.results[2].resultpam;
//
//            for (var i = 0; i < conf.length; i++) {
//                c = conf[i];
//                if (i < tl) {
//                    var sum = 0;
//                    for (var j = 0; j < c; j++) {
//                        sum += result2[j];
//                    }
//                    str += sum + ";";
//                } else str += "99;";
//            }
//
//            var sumC = 0;
//            for (var i = 0; i < conf.length; i++) {
//                c = conf[i];
//                if (onlyOne) {
//                    str += ";";
//                } else {
//                    if (sumC < rl) {
//                        for (var j = 0; j < c; j++) {
//                            str += result2[sumC + j] + ";";
//                        }
//                    } else {
//                        for (var j = 0; j < c; j++) {
//                            str += "9;";
//                        }
//                    }
//                }
//                sumC += c;
//            }
//
//        }
        for (int i = 0; i < conf.size(); i++) {
            if (i < tl) {
                str += times.get(i) + ";";
            } else str += "999999;";
        }
        if (nrTask == 5 || nrTask == 6) {

            for (int i = 0; i < conf.size(); i++) {
                if (i < tl && result.get(i) == 1) {
                    str += times.get(i) + ";";
                } else str += "999999;";
            }
        }
//        if (nrTask == = 9) {
//            var wl = Tracking.wrong.length;
//            for (var i = 0; i < conf.length; i++) {
//                if (i < wl) str += Tracking.wrong[i] + ";";
//                else str += "999999;";
//
//            }
//        }

        str += (overallTime > 0) ? overallTime + ";" : ";";
        str += (taskIsCompleted) ? "1;" : "0;";

        return str;
    }

    // Global.js makeResultArray
    private ArrayList<ArrayList<ResultsObject>> makeResultArray(ArrayList<Integer> config, boolean onlyOne) {
        ArrayList<ArrayList<ResultsObject>> output = new ArrayList<>();
        int howMany;
        if (config.size() > 0) {
            howMany = config.get(0);
        } else {
            howMany = 0;
        }
        for (int i = 0; i < config.size(); i++) {
            if (howMany < config.get(i)) {
                howMany = config.get(i);
            }
        }

        for (int i = 0; i < howMany; i++) {
            output.add(new ArrayList<>());
        }


        Collections.sort(config);
        ArrayList<Double> arr;
        for (int i = 0; i < config.size(); i++) {
            int c = config.get(i);
            arr = new ArrayList<>();
            if (onlyOne) {
                arr.add(-1d);
            } else {
                for (int j = 0; j < c; j++) {
                    arr.add(-1d);
                }
            }
            if (c != 0) {
                output.get(c - 1).add(new ResultsObject(arr, -1));
            }
        }
        return output;
    }


    private class ResultsObject {
        public ArrayList<Double> r;
        public long t;

        private ResultsObject(ArrayList<Double> arr, long t) {
            this.r = arr;
            this.t = t;
        }
    }
}

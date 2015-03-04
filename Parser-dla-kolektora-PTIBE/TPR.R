# Parser danych z kolektorów PT IBE #

# Jak używać?
# -do napisania-

setwd("D:\\temp\\kolektor\\TPR")
require(XML)
dir(pattern = "^\\d{4}-\\d{2}-\\d{2}.*\\.zip$") -> pliki
# dput(temp[-c(1:3),])
structure(character(0), .Dim = c(0L, 14L), 
          .Dimnames = list(NULL, c("n_pliku", "tester", "id", "badany[1]", "badany[2]",
                                   "badany[3]","data", "godzina", "data2", "godzina2", 
                                   "strefa", "area", "mark", "answer"))) -> wyniki
for (n_pliku in pliki) {
  unzip(n_pliku)
  xmlRoot(xmlParse("tasks.xml")) -> podsumowanie
  xmlRoot(xmlParse("user.xml")) -> badacz
  
  # dane z pliku tasks.xml + z nazwy pliku zip
  xmlApply(podsumowanie, xmlAttrs) -> skale
  do.call(rbind, skale) -> skale
  if (dim(skale)[1]<3) {
    match(c("zad1", "zad6", "zad7"), skale[,2]) -> cojestaconie
    if (is.na(cojestaconie[1])) {
      rbind(skale, c("001", "zad1", "1", rep(NA, 5))) -> skale
    }
    if (is.na(cojestaconie[2])) {
      rbind(skale, c("002", "zad6", "2", rep(NA, 5))) -> skale
    }
    if (is.na(cojestaconie[3])) {
      rbind(skale, c("003", "zad7", "3", rep(NA, 5))) -> skale
    }
  }
  skale <- skale[,c(2, 4:5)]
  substr(x = n_pliku, start = 0, stop = 10) -> data
  gsub("-", ":", substr(x = n_pliku, start = 12, stop = 19)) -> godzina
  substr(x = n_pliku, start = 28, stop = 37) -> data2
  gsub("%3A", ":", substr(x = n_pliku, start = 39, stop = 50)) -> godzina2
  substr(x = n_pliku, start = 51, stop = 55) -> strefa
  
  # dane z pliku user.xml
  xmlAttrs(badacz[[1]]) -> tester
  xmlAttrs(badacz[[2]]) -> id
  xmlAttrs(badacz[[3]]) -> badany
  
  # połączenie wszystkich danych
  cbind(n_pliku, tester, id, cbind(badany[1], badany[2], badany[3], deparse.level = 2), data, godzina, data2, godzina2, strefa) -> temp
  cbind(temp[rep(seq_len(nrow(temp)), each=3),], skale) -> temp
  
  # eksport na zewnątrz pętli
  rbind(wyniki, temp) -> wyniki
}
rm(badacz, badany, cojestaconie, id, skale, tester, data, data2, godzina, godzina2, n_pliku, pliki, podsumowanie, strefa, temp)
file.remove(c("summary.xml", "tasks.xml", "user.xml"))

# write.csv2(wyniki, "podsumowanie_long.csv")
as.data.frame(wyniki, stringsAsFactors = FALSE) -> wyniki
wyniki$uID <- as.numeric(as.factor(wyniki$n_pliku))
wyniki_wide <- reshape(wyniki, timevar = "area", 
                       idvar = c("uID", "n_pliku", "tester", "id", "badany[1]", "badany[2]",
                                 "badany[3]", "data", "godzina", "data2", "godzina2", "strefa"),
                       direction = "wide")
# wyjmuję dane o przebiegu testu z celek do wielu kolumn (47)
# dput(detale[-1,])
structure(list(z1_s21 = integer(0), z1_s31 = integer(0), z1_s41 = integer(0), 
               z1_s51 = integer(0), z1_s21_p1 = numeric(0), z1_s21_p2 = numeric(0), 
               z1_s31_p1 = numeric(0), z1_s31_p2 = numeric(0), z1_s31_p3 = numeric(0), 
               z1_s41_p1 = numeric(0), z1_s41_p2 = numeric(0), z1_s41_p3 = numeric(0), 
               z1_s41_p4 = numeric(0), z1_s51_p1 = numeric(0), z1_s51_p2 = numeric(0), 
               z1_s51_p3 = numeric(0), z1_s51_p4 = numeric(0), z1_s51_p5 = numeric(0), 
               z1_t21 = integer(0), z1_t31 = integer(0), z1_t41 = integer(0), 
               z1_t51 = integer(0), z1_s21_k = integer(0), z1_s41_k = integer(0), 
               z1_s51_k = integer(0), z1_s31_k = integer(0), z1_s21_p1_k = numeric(0), 
               z1_s21_p2_k = numeric(0), z1_s41_p1_k = numeric(0), z1_s41_p2_k = numeric(0), 
               z1_s41_p3_k = numeric(0), z1_s41_p4_k = numeric(0), z1_s51_p1_k = numeric(0), 
               z1_s51_p2_k = numeric(0), z1_s51_p3_k = numeric(0), z1_s51_p4_k = numeric(0), 
               z1_s51_p5_k = numeric(0), z1_s31_p1_k = numeric(0), z1_s31_p2_k = numeric(0), 
               z1_s31_p3_k = numeric(0), z1_t21_k = integer(0), z1_t41_k = integer(0), 
               z1_t51_k = integer(0), z1_t31_k = integer(0), tc_zad1 = integer(0), 
               w_zad1 = integer(0), X = logical(0)), .Names = c("z1_s21", 
                                                                "z1_s31", "z1_s41", "z1_s51", "z1_s21_p1", "z1_s21_p2", "z1_s31_p1", 
                                                                "z1_s31_p2", "z1_s31_p3", "z1_s41_p1", "z1_s41_p2", "z1_s41_p3", 
                                                                "z1_s41_p4", "z1_s51_p1", "z1_s51_p2", "z1_s51_p3", "z1_s51_p4", 
                                                                "z1_s51_p5", "z1_t21", "z1_t31", "z1_t41", "z1_t51", "z1_s21_k", 
                                                                "z1_s41_k", "z1_s51_k", "z1_s31_k", "z1_s21_p1_k", "z1_s21_p2_k", 
                                                                "z1_s41_p1_k", "z1_s41_p2_k", "z1_s41_p3_k", "z1_s41_p4_k", "z1_s51_p1_k", 
                                                                "z1_s51_p2_k", "z1_s51_p3_k", "z1_s51_p4_k", "z1_s51_p5_k", "z1_s31_p1_k", 
                                                                "z1_s31_p2_k", "z1_s31_p3_k", "z1_t21_k", "z1_t41_k", "z1_t51_k", 
                                                                "z1_t31_k", "tc_zad1", "w_zad1", "X"), 
          row.names = integer(0), class = "data.frame") -> detale_zad1
for (i in 1:dim(wyniki_wide)[1]) {
  if (!is.na(wyniki_wide$answer.zad1[i])&nchar(wyniki_wide$answer.zad1[i])!=0) {
    read.table(textConnection(wyniki_wide$answer.zad1[i]), sep = ";", header = TRUE) -> historia
    for (j in 1:length(names(detale_zad1))) {
      if (!is.na(match(names(detale_zad1)[j], names(historia)))) {
        detale_zad1[i,j] <- historia[1,match(names(detale_zad1)[j], names(historia))]
      }
    }  
  } else detale_zad1[i,] <- NA
}
structure(list(z6 = integer(0), z6_s11_t = numeric(0), z6_s12_t = numeric(0), 
               z6_s13_t = numeric(0), z6_s14_t = numeric(0), z6_s15_t = numeric(0), 
               z6_s16_t = numeric(0), z6_s16_n = numeric(0), z6_s18_n = numeric(0), 
               z6_s19_n = numeric(0), z6_s110_n = numeric(0), z6_s111_n = numeric(0), 
               z6_s112_n = integer(0), z6_s21_t = numeric(0), z6_s22_t = numeric(0), 
               z6_s23_t = numeric(0), z6_s24_t = numeric(0), z6_s25_n = numeric(0), 
               z6_s26_n = numeric(0), z6_s27_n = numeric(0), z6_s28_n = numeric(0), 
               z6_s29_n = numeric(0), z6_s210_n = numeric(0), z6_s211_n = numeric(0), 
               z6_s212_n = numeric(0), z6_s31_t = numeric(0), z6_s32_t = numeric(0), 
               z6_s33_t = numeric(0), z6_s34_t = numeric(0), z6_s35_t = numeric(0), 
               z6_s36_t = numeric(0), z6_s37_n = numeric(0), z6_s38_n = numeric(0), 
               z6_s39_n = numeric(0), z6_s310_n = numeric(0), z6_s311_n = numeric(0), 
               z6_s312_n = numeric(0), z6_s41_t = numeric(0), z6_s42_t = numeric(0), 
               z6_s43_t = numeric(0), z6_s44_t = numeric(0), z6_s45_t = numeric(0), 
               z6_s46_t = numeric(0), z6_s47_t = numeric(0), z6_s48_t = numeric(0), 
               z6_s49_n = numeric(0), z6_s410_n = numeric(0), z6_s411_n = numeric(0), 
               z6_s412_n = numeric(0), z6_s51_n = numeric(0), z6_t11 = integer(0), 
               z6_t12 = integer(0), z6_t13 = integer(0), z6_t14 = integer(0), 
               z6_t15 = integer(0), z6_t16 = integer(0), z6_t17 = integer(0), 
               z6_t18 = integer(0), z6_t19 = integer(0), z6_t110 = integer(0), 
               z6_t111 = integer(0), z6_t112 = integer(0), z6_t21 = integer(0), 
               z6_t22 = integer(0), z6_t23 = integer(0), z6_t24 = integer(0), 
               z6_t25 = integer(0), z6_t26 = integer(0), z6_t27 = integer(0), 
               z6_t28 = integer(0), z6_t29 = integer(0), z6_t210 = integer(0), 
               z6_t211 = integer(0), z6_t212 = integer(0), z6_t31 = integer(0), 
               z6_t32 = integer(0), z6_t33 = integer(0), z6_t34 = integer(0), 
               z6_t35 = integer(0), z6_t36 = integer(0), z6_t37 = integer(0), 
               z6_t38 = integer(0), z6_t39 = integer(0), z6_t310 = integer(0), 
               z6_t311 = integer(0), z6_t312 = integer(0), z6_t41 = integer(0), 
               z6_t42 = integer(0), z6_t43 = integer(0), z6_t44 = integer(0), 
               z6_t45 = integer(0), z6_t46 = integer(0), z6_t47 = integer(0), 
               z6_t48 = integer(0), z6_t49 = integer(0), z6_t410 = integer(0), 
               z6_t411 = integer(0), z6_t412 = integer(0), z6_t51 = integer(0), 
               z6_c_t11 = integer(0), z6_c_t12 = integer(0), z6_c_t13 = integer(0), 
               z6_c_t14 = integer(0), z6_c_t15 = integer(0), z6_c_t16 = integer(0), 
               z6_c_t17 = integer(0), z6_c_t18 = integer(0), z6_c_t19 = integer(0), 
               z6_c_t110 = integer(0), z6_c_t111 = integer(0), z6_c_t112 = integer(0), 
               z6_c_t21 = integer(0), z6_c_t22 = integer(0), z6_c_t23 = integer(0), 
               z6_c_t24 = integer(0), z6_c_t25 = integer(0), z6_c_t26 = integer(0), 
               z6_c_t27 = integer(0), z6_c_t28 = integer(0), z6_c_t29 = integer(0), 
               z6_c_t210 = integer(0), z6_c_t211 = integer(0), z6_c_t212 = integer(0), 
               z6_c_t31 = integer(0), z6_c_t32 = integer(0), z6_c_t33 = integer(0), 
               z6_c_t34 = integer(0), z6_c_t35 = integer(0), z6_c_t36 = integer(0), 
               z6_c_t37 = integer(0), z6_c_t38 = integer(0), z6_c_t39 = integer(0), 
               z6_c_t310 = integer(0), z6_c_t311 = integer(0), z6_c_t312 = integer(0), 
               z6_c_t41 = integer(0), z6_c_t42 = integer(0), z6_c_t43 = integer(0), 
               z6_c_t44 = integer(0), z6_c_t45 = integer(0), z6_c_t46 = integer(0), 
               z6_c_t47 = integer(0), z6_c_t48 = integer(0), z6_c_t49 = integer(0), 
               z6_c_t410 = integer(0), z6_c_t411 = integer(0), z6_c_t412 = integer(0), 
               z6_c_t51 = integer(0), z6_k = integer(0), z6_s51_k_n = numeric(0), 
               z6_s41_k_t = numeric(0), z6_s11_k_t = numeric(0), z6_s42_k_n = numeric(0), 
               z6_s21_k_t = numeric(0), z6_s31_k_t = numeric(0), z6_s12_k_t = numeric(0), 
               z6_s43_k_n = numeric(0), z6_s13_k_n = numeric(0), z6_s32_k_n = numeric(0), 
               z6_s22_k_t = numeric(0), z6_s33_k_t = numeric(0), z6_s23_k_n = numeric(0), 
               z6_s44_k_t = numeric(0), z6_s14_k_t = numeric(0), z6_s34_k_t = numeric(0), 
               z6_s15_k_t = numeric(0), z6_s35_k_t = numeric(0), z6_s24_k_n = numeric(0), 
               z6_s45_k_t = numeric(0), z6_s25_k_n = numeric(0), z6_s36_k_n = numeric(0), 
               z6_s26_k_t = numeric(0), z6_s46_k_n = numeric(0), z6_s16_k_n = numeric(0), 
               z6_s47_k_t = numeric(0), z6_s27_k_n = numeric(0), z6_s37_k_n = numeric(0), 
               z6_s28_k_t = numeric(0), z6_s38_k_t = numeric(0), z6_s29_k_n = numeric(0), 
               z6_s48_k_t = numeric(0), z6_s210_k_n = numeric(0), z6_s39_k_n = numeric(0), 
               z6_s17_k_n = numeric(0), z6_s49_k_t = numeric(0), z6_s211_k_n = numeric(0), 
               z6_s410_k_t = numeric(0), z6_s212_k_n = numeric(0), z6_s310_k_n = numeric(0), 
               z6_s18_k_n = numeric(0), z6_s311_k_n = numeric(0), z6_s19_k_n = numeric(0), 
               z6_s411_k_t = numeric(0), z6_s110_k_t = numeric(0), z6_s312_k_t = numeric(0), 
               z6_s111_k_t = numeric(0), z6_s412_k_n = numeric(0), z6_s112_k_n = integer(0), 
               z6_t51_k = integer(0), z6_t41_k = integer(0), z6_t11_k = integer(0), 
               z6_t42_k = integer(0), z6_t21_k = integer(0), z6_t31_k = integer(0), 
               z6_t12_k = integer(0), z6_t43_k = integer(0), z6_t13_k = integer(0), 
               z6_t32_k = integer(0), z6_t22_k = integer(0), z6_t33_k = integer(0), 
               z6_t23_k = integer(0), z6_t44_k = integer(0), z6_t14_k = integer(0), 
               z6_t34_k = integer(0), z6_t15_k = integer(0), z6_t35_k = integer(0), 
               z6_t24_k = integer(0), z6_t45_k = integer(0), z6_t25_k = integer(0), 
               z6_t36_k = integer(0), z6_t26_k = integer(0), z6_t46_k = integer(0), 
               z6_t16_k = integer(0), z6_t47_k = integer(0), z6_t27_k = integer(0), 
               z6_t37_k = integer(0), z6_t28_k = integer(0), z6_t38_k = integer(0), 
               z6_t29_k = integer(0), z6_t48_k = integer(0), z6_t210_k = integer(0), 
               z6_t39_k = integer(0), z6_t17_k = integer(0), z6_t49_k = integer(0), 
               z6_t211_k = integer(0), z6_t410_k = integer(0), z6_t212_k = integer(0), 
               z6_t310_k = integer(0), z6_t18_k = integer(0), z6_t311_k = integer(0), 
               z6_t19_k = integer(0), z6_t411_k = integer(0), z6_t110_k = integer(0), 
               z6_t312_k = integer(0), z6_t111_k = integer(0), z6_t412_k = integer(0), 
               z6_t112_k = integer(0), z6_c_t51_k = integer(0), z6_c_t41_k = integer(0), 
               z6_c_t11_k = integer(0), z6_c_t42_k = integer(0), z6_c_t21_k = integer(0), 
               z6_c_t31_k = integer(0), z6_c_t12_k = integer(0), z6_c_t43_k = integer(0), 
               z6_c_t13_k = integer(0), z6_c_t32_k = integer(0), z6_c_t22_k = integer(0), 
               z6_c_t33_k = integer(0), z6_c_t23_k = integer(0), z6_c_t44_k = integer(0), 
               z6_c_t14_k = integer(0), z6_c_t34_k = integer(0), z6_c_t15_k = integer(0), 
               z6_c_t35_k = integer(0), z6_c_t24_k = integer(0), z6_c_t45_k = integer(0), 
               z6_c_t25_k = integer(0), z6_c_t36_k = integer(0), z6_c_t26_k = integer(0), 
               z6_c_t46_k = integer(0), z6_c_t16_k = integer(0), z6_c_t47_k = integer(0), 
               z6_c_t27_k = integer(0), z6_c_t37_k = integer(0), z6_c_t28_k = integer(0), 
               z6_c_t38_k = integer(0), z6_c_t29_k = integer(0), z6_c_t48_k = integer(0), 
               z6_c_t210_k = integer(0), z6_c_t39_k = integer(0), z6_c_t17_k = integer(0), 
               z6_c_t49_k = integer(0), z6_c_t211_k = integer(0), z6_c_t410_k = integer(0), 
               z6_c_t212_k = integer(0), z6_c_t310_k = integer(0), z6_c_t18_k = integer(0), 
               z6_c_t311_k = integer(0), z6_c_t19_k = integer(0), z6_c_t411_k = integer(0), 
               z6_c_t110_k = integer(0), z6_c_t312_k = integer(0), z6_c_t111_k = integer(0), 
               z6_c_t412_k = integer(0), z6_c_t112_k = integer(0), tc_zad6 = integer(0), 
               w_zad6 = integer(0), X = logical(0)), .Names = c("z6", "z6_s11_t", 
                                                                "z6_s12_t", "z6_s13_t", "z6_s14_t", "z6_s15_t", "z6_s16_t", "z6_s17_n", 
                                                                "z6_s18_n", "z6_s19_n", "z6_s110_n", "z6_s111_n", "z6_s112_n", 
                                                                "z6_s21_t", "z6_s22_t", "z6_s23_t", "z6_s24_t", "z6_s25_n", "z6_s26_n", 
                                                                "z6_s27_n", "z6_s28_n", "z6_s29_n", "z6_s210_n", "z6_s211_n", 
                                                                "z6_s212_n", "z6_s31_t", "z6_s32_t", "z6_s33_t", "z6_s34_t", 
                                                                "z6_s35_t", "z6_s36_t", "z6_s37_n", "z6_s38_n", "z6_s39_n", "z6_s310_n", 
                                                                "z6_s311_n", "z6_s312_n", "z6_s41_t", "z6_s42_t", "z6_s43_t", 
                                                                "z6_s44_t", "z6_s45_t", "z6_s46_t", "z6_s47_t", "z6_s48_t", "z6_s49_n", 
                                                                "z6_s410_n", "z6_s411_n", "z6_s412_n", "z6_s51_n", "z6_t11", 
                                                                "z6_t12", "z6_t13", "z6_t14", "z6_t15", "z6_t16", "z6_t17", "z6_t18", 
                                                                "z6_t19", "z6_t110", "z6_t111", "z6_t112", "z6_t21", "z6_t22", 
                                                                "z6_t23", "z6_t24", "z6_t25", "z6_t26", "z6_t27", "z6_t28", "z6_t29", 
                                                                "z6_t210", "z6_t211", "z6_t212", "z6_t31", "z6_t32", "z6_t33", 
                                                                "z6_t34", "z6_t35", "z6_t36", "z6_t37", "z6_t38", "z6_t39", "z6_t310", 
                                                                "z6_t311", "z6_t312", "z6_t41", "z6_t42", "z6_t43", "z6_t44", 
                                                                "z6_t45", "z6_t46", "z6_t47", "z6_t48", "z6_t49", "z6_t410", 
                                                                "z6_t411", "z6_t412", "z6_t51", "z6_c_t11", "z6_c_t12", "z6_c_t13", 
                                                                "z6_c_t14", "z6_c_t15", "z6_c_t16", "z6_c_t17", "z6_c_t18", "z6_c_t19", 
                                                                "z6_c_t110", "z6_c_t111", "z6_c_t112", "z6_c_t21", "z6_c_t22", 
                                                                "z6_c_t23", "z6_c_t24", "z6_c_t25", "z6_c_t26", "z6_c_t27", "z6_c_t28", 
                                                                "z6_c_t29", "z6_c_t210", "z6_c_t211", "z6_c_t212", "z6_c_t31", 
                                                                "z6_c_t32", "z6_c_t33", "z6_c_t34", "z6_c_t35", "z6_c_t36", "z6_c_t37", 
                                                                "z6_c_t38", "z6_c_t39", "z6_c_t310", "z6_c_t311", "z6_c_t312", 
                                                                "z6_c_t41", "z6_c_t42", "z6_c_t43", "z6_c_t44", "z6_c_t45", "z6_c_t46", 
                                                                "z6_c_t47", "z6_c_t48", "z6_c_t49", "z6_c_t410", "z6_c_t411", 
                                                                "z6_c_t412", "z6_c_t51", "z6_k", "z6_s51_k_n", "z6_s41_k_t", 
                                                                "z6_s11_k_t", "z6_s42_k_n", "z6_s21_k_t", "z6_s31_k_t", "z6_s12_k_t", 
                                                                "z6_s43_k_n", "z6_s13_k_n", "z6_s32_k_n", "z6_s22_k_t", "z6_s33_k_t", 
                                                                "z6_s23_k_n", "z6_s44_k_t", "z6_s14_k_t", "z6_s34_k_t", "z6_s15_k_t", 
                                                                "z6_s35_k_t", "z6_s24_k_n", "z6_s45_k_t", "z6_s25_k_n", "z6_s36_k_n", 
                                                                "z6_s26_k_t", "z6_s46_k_n", "z6_s16_k_n", "z6_s47_k_t", "z6_s27_k_n", 
                                                                "z6_s37_k_n", "z6_s28_k_t", "z6_s38_k_t", "z6_s29_k_n", "z6_s48_k_t", 
                                                                "z6_s210_k_n", "z6_s39_k_n", "z6_s17_k_n", "z6_s49_k_t", "z6_s211_k_n", 
                                                                "z6_s410_k_t", "z6_s212_k_n", "z6_s310_k_n", "z6_s18_k_n", "z6_s311_k_n", 
                                                                "z6_s19_k_n", "z6_s411_k_t", "z6_s110_k_t", "z6_s312_k_t", "z6_s111_k_t", 
                                                                "z6_s412_k_n", "z6_s112_k_n", "z6_t51_k", "z6_t41_k", "z6_t11_k", 
                                                                "z6_t42_k", "z6_t21_k", "z6_t31_k", "z6_t12_k", "z6_t43_k", "z6_t13_k", 
                                                                "z6_t32_k", "z6_t22_k", "z6_t33_k", "z6_t23_k", "z6_t44_k", "z6_t14_k", 
                                                                "z6_t34_k", "z6_t15_k", "z6_t35_k", "z6_t24_k", "z6_t45_k", "z6_t25_k", 
                                                                "z6_t36_k", "z6_t26_k", "z6_t46_k", "z6_t16_k", "z6_t47_k", "z6_t27_k", 
                                                                "z6_t37_k", "z6_t28_k", "z6_t38_k", "z6_t29_k", "z6_t48_k", "z6_t210_k", 
                                                                "z6_t39_k", "z6_t17_k", "z6_t49_k", "z6_t211_k", "z6_t410_k", 
                                                                "z6_t212_k", "z6_t310_k", "z6_t18_k", "z6_t311_k", "z6_t19_k", 
                                                                "z6_t411_k", "z6_t110_k", "z6_t312_k", "z6_t111_k", "z6_t412_k", 
                                                                "z6_t112_k", "z6_c_t51_k", "z6_c_t41_k", "z6_c_t11_k", "z6_c_t42_k", 
                                                                "z6_c_t21_k", "z6_c_t31_k", "z6_c_t12_k", "z6_c_t43_k", "z6_c_t13_k", 
                                                                "z6_c_t32_k", "z6_c_t22_k", "z6_c_t33_k", "z6_c_t23_k", "z6_c_t44_k", 
                                                                "z6_c_t14_k", "z6_c_t34_k", "z6_c_t15_k", "z6_c_t35_k", "z6_c_t24_k", 
                                                                "z6_c_t45_k", "z6_c_t25_k", "z6_c_t36_k", "z6_c_t26_k", "z6_c_t46_k", 
                                                                "z6_c_t16_k", "z6_c_t47_k", "z6_c_t27_k", "z6_c_t37_k", "z6_c_t28_k", 
                                                                "z6_c_t38_k", "z6_c_t29_k", "z6_c_t48_k", "z6_c_t210_k", "z6_c_t39_k", 
                                                                "z6_c_t17_k", "z6_c_t49_k", "z6_c_t211_k", "z6_c_t410_k", "z6_c_t212_k", 
                                                                "z6_c_t310_k", "z6_c_t18_k", "z6_c_t311_k", "z6_c_t19_k", "z6_c_t411_k", 
                                                                "z6_c_t110_k", "z6_c_t312_k", "z6_c_t111_k", "z6_c_t412_k", "z6_c_t112_k", 
                                                                "tc_zad6", "w_zad6", "X"), row.names = integer(0), class = "data.frame") -> detale_zad6
for (i in 1:dim(wyniki_wide)[1]) {
  if (!is.na(wyniki_wide$answer.zad6[i])&nchar(wyniki_wide$answer.zad6[i])!=0) {
    read.table(textConnection(wyniki_wide$answer.zad6[i]), sep = ";", header = TRUE) -> historia
    for (j in 1:length(names(detale_zad6))) {
      if (!is.na(match(names(detale_zad6)[j], names(historia)))) {
        detale_zad6[i,j] <- historia[1,match(names(detale_zad6)[j], names(historia))]
      }
    }  
  } else detale_zad6[i,] <- NA
}
structure(list(z7_s21 = integer(0), z7_s22 = integer(0), z7_s31 = integer(0), 
               z7_s32 = integer(0), z7_s41 = integer(0), z7_s42 = integer(0), 
               z7_s51 = integer(0), z7_s52 = integer(0), z7_s21_p1 = numeric(0), 
               z7_s21_p2 = numeric(0), z7_s22_p1 = numeric(0), z7_s22_p2 = numeric(0), 
               z7_s31_p1 = numeric(0), z7_s31_p2 = numeric(0), z7_s31_p3 = numeric(0), 
               z7_s32_p1 = numeric(0), z7_s32_p2 = numeric(0), z7_s32_p3 = numeric(0), 
               z7_s41_p1 = numeric(0), z7_s41_p2 = numeric(0), z7_s41_p3 = numeric(0), 
               z7_s41_p4 = numeric(0), z7_s42_p1 = numeric(0), z7_s42_p2 = numeric(0), 
               z7_s42_p3 = numeric(0), z7_s42_p4 = numeric(0), z7_s51_p1 = numeric(0), 
               z7_s51_p2 = numeric(0), z7_s51_p3 = numeric(0), z7_s51_p4 = numeric(0), 
               z7_s51_p5 = numeric(0), z7_s52_p1 = numeric(0), z7_s52_p2 = numeric(0), 
               z7_s52_p3 = numeric(0), z7_s52_p4 = numeric(0), z7_s52_p5 = numeric(0), 
               z7_t21 = integer(0), z7_t22 = integer(0), z7_t31 = integer(0), 
               z7_t32 = integer(0), z7_t41 = integer(0), z7_t42 = integer(0), 
               z7_t51 = integer(0), z7_t52 = integer(0), z7_s21_k = integer(0), 
               z7_s41_k = integer(0), z7_s51_k = integer(0), z7_s31_k = integer(0), 
               z7_s42_k = integer(0), z7_s52_k = integer(0), z7_s32_k = integer(0), 
               z7_s22_k = integer(0), z7_s21_p1_k = numeric(0), z7_s21_p2_k = numeric(0), 
               z7_s41_p1_k = numeric(0), z7_s41_p2_k = numeric(0), z7_s41_p3_k = numeric(0), 
               z7_s41_p4_k = numeric(0), z7_s51_p1_k = numeric(0), z7_s51_p2_k = numeric(0), 
               z7_s51_p3_k = numeric(0), z7_s51_p4_k = numeric(0), z7_s51_p5_k = numeric(0), 
               z7_s31_p1_k = numeric(0), z7_s31_p2_k = numeric(0), z7_s31_p3_k = numeric(0), 
               z7_s42_p1_k = numeric(0), z7_s42_p2_k = numeric(0), z7_s42_p3_k = numeric(0), 
               z7_s42_p4_k = numeric(0), z7_s52_p1_k = numeric(0), z7_s52_p2_k = numeric(0), 
               z7_s52_p3_k = numeric(0), z7_s52_p4_k = numeric(0), z7_s52_p5_k = numeric(0), 
               z7_s32_p1_k = numeric(0), z7_s32_p2_k = numeric(0), z7_s32_p3_k = numeric(0), 
               z7_s22_p1_k = numeric(0), z7_s22_p2_k = numeric(0), z7_t21_k = integer(0), 
               z7_t41_k = integer(0), z7_t51_k = integer(0), z7_t31_k = integer(0), 
               z7_t42_k = integer(0), z7_t52_k = integer(0), z7_t32_k = integer(0), 
               z7_t22_k = integer(0), tc_zad7 = integer(0), w_zad7 = integer(0), 
               X = logical(0)), .Names = c("z7_s21", "z7_s22", "z7_s31", 
                                           "z7_s32", "z7_s41", "z7_s42", "z7_s51", "z7_s52", "z7_s21_p1", 
                                           "z7_s21_p2", "z7_s22_p1", "z7_s22_p2", "z7_s31_p1", "z7_s31_p2", 
                                           "z7_s31_p3", "z7_s32_p1", "z7_s32_p2", "z7_s32_p3", "z7_s41_p1", 
                                           "z7_s41_p2", "z7_s41_p3", "z7_s41_p4", "z7_s42_p1", "z7_s42_p2", 
                                           "z7_s42_p3", "z7_s42_p4", "z7_s51_p1", "z7_s51_p2", "z7_s51_p3", 
                                           "z7_s51_p4", "z7_s51_p5", "z7_s52_p1", "z7_s52_p2", "z7_s52_p3", 
                                           "z7_s52_p4", "z7_s52_p5", "z7_t21", "z7_t22", "z7_t31", "z7_t32", 
                                           "z7_t41", "z7_t42", "z7_t51", "z7_t52", "z7_s21_k", "z7_s41_k", 
                                           "z7_s51_k", "z7_s31_k", "z7_s42_k", "z7_s52_k", "z7_s32_k", "z7_s22_k", 
                                           "z7_s21_p1_k", "z7_s21_p2_k", "z7_s41_p1_k", "z7_s41_p2_k", "z7_s41_p3_k", 
                                           "z7_s41_p4_k", "z7_s51_p1_k", "z7_s51_p2_k", "z7_s51_p3_k", "z7_s51_p4_k", 
                                           "z7_s51_p5_k", "z7_s31_p1_k", "z7_s31_p2_k", "z7_s31_p3_k", "z7_s42_p1_k", 
                                           "z7_s42_p2_k", "z7_s42_p3_k", "z7_s42_p4_k", "z7_s52_p1_k", "z7_s52_p2_k", 
                                           "z7_s52_p3_k", "z7_s52_p4_k", "z7_s52_p5_k", "z7_s32_p1_k", "z7_s32_p2_k", 
                                           "z7_s32_p3_k", "z7_s22_p1_k", "z7_s22_p2_k", "z7_t21_k", "z7_t41_k", 
                                           "z7_t51_k", "z7_t31_k", "z7_t42_k", "z7_t52_k", "z7_t32_k", "z7_t22_k", 
                                           "tc_zad7", "w_zad7", "X"), row.names = integer(0), class = "data.frame") -> detale_zad7
for (i in 1:dim(wyniki_wide)[1]) {
  if (!is.na(wyniki_wide$answer.zad7[i])&nchar(wyniki_wide$answer.zad7[i])!=0) {
    read.table(textConnection(wyniki_wide$answer.zad7[i]), sep = ";", header = TRUE) -> historia
    for (j in 1:length(names(detale_zad7))) {
      if (!is.na(match(names(detale_zad7)[j], names(historia)))) {
        detale_zad7[i,j] <- historia[1,match(names(detale_zad7)[j], names(historia))]
      }
    }  
  } else detale_zad7[i,] <- NA
}
rm(i, j, historia)
cbind(wyniki_wide[,-c(14, 16, 18)], detale_zad1[,-47], detale_zad6[,-299], detale_zad7[,-91]) -> wyniki_wide_expand
write.csv2(wyniki_wide_expand, "podsumowanie_wide_expand.csv")
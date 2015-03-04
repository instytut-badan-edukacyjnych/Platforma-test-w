# Parser danych z kolektorów PT IBE #

# Jak używać?
# -do napisania-

# library(Rcurl) # żeby zalogować się po SCP i zgrać wyniki - do zrobienia później

setwd("D:\\temp\\kolektor\\TUNSS") # tutaj ścieżka do katalogu w którym będą pliki XML
require(XML)
dir(pattern = "^\\d{4}-\\d{2}-\\d{2}.*") -> pliki
structure(character(0), .Dim = c(0L, 16L),
          .Dimnames = list(NULL, c("n_pliku", "tester", "id", "badany[1]", "badany[2]",
                                   "badany[3]", "data", "godzina", "data2", "godzina2",
                                   "strefa", "area", "status", "length", "theta", "se"))) -> wyniki
for (n_pliku in pliki) {
  unzip(n_pliku)
  xmlRoot(xmlParse("summary.xml")) -> podsumowanie
  xmlRoot(xmlParse("user.xml")) -> badacz
  
  # dane z pliku summary.xml + z nazwy pliku zip
  xmlApply(podsumowanie, xmlAttrs) -> skale
  do.call(rbind, skale) -> skale
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
rm(badacz, badany, id, skale, tester, data, data2, godzina, godzina2, n_pliku, pliki, podsumowanie, strefa, temp)
file.remove(c("summary.xml", "tasks.xml", "user.xml"))

write.csv2(wyniki, "podsumowanie_long.csv") # podsumowanie zapisywane jest w katalogu w którym uruchamia się skrypt
as.data.frame(wyniki) -> wyniki
wyniki$uID <- as.numeric(as.factor(wyniki$n_pliku))
wyniki_wide <- reshape(wyniki, timevar = "area", 
                       idvar = c("uID", "n_pliku", "tester", "id", "badany[1]", "badany[2]",
                                 "badany[3]", "data", "godzina", "data2", "godzina2", "strefa"),
                       direction = "wide")
write.csv2(wyniki_wide, "podsumowanie_wide.csv") # tutaj j.w.
#This file is part of Test Platform.
#
#Test Platform is free software; you can redistribute it and/or modify
#it under the terms of the GNU General Public License as published by
#the Free Software Foundation; either version 2 of the License, or
#(at your option) any later version.
#
#Test Platform is distributed in the hope that it will be useful,
#but WITHOUT ANY WARRANTY; without even the implied warranty of
#MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#GNU General Public License for more details.
#
#You should have received a copy of the GNU General Public License
#along with Test Platform; if not, write to the Free Software
#Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#Ten plik jest częścią Platformy Testów.
#
#Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
#i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
#wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
#Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
#
#Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
#użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
#gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
#ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
#Powszechnej Licencji Publicznej GNU.
#
#Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
#Powszechnej Licencji Publicznej GNU (GNU General Public License);
#jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
#Place, Fifth Floor, Boston, MA  02110-1301  USA

#!/bin/sh
rm server.key server.csr certs.bks config.gen.cfg

(
    C=11 &&
	echo "======== PREPARING CERTIFICATE ========" &&
	echo "1/$C: generate config"
	cat config.cfg > config.gen.cfg
	n=$(cat config.gen.cfg | tail -n 1 | cut -d '.' -f 2 | cut -d ' ' -f 1)
	for((i = 0; i < 256; ++i)); do
		((n++))
		echo "IP.$n = 10.100.30.$i" >> config.gen.cfg
	done &&
	echo "2/$C: genrsa" &&
	openssl genrsa -passout pass:server -des3 -out server.key 1024 &&
	echo "3/$C: req" &&
	openssl req -passin pass:server -new -key server.key -out server.csr -config config.gen.cfg &&
	echo "4/$C: cp" &&
	cp server.key server.key.enc &&
	echo "5/$C: rsa" &&
	openssl rsa -passin pass:server -in server.key.enc -out server.key &&
	echo "6/$C: x509" &&
	openssl x509 -req -days 3650 -in server.csr -signkey server.key -out server.crt -extfile config.gen.cfg -extensions v3_req &&
	echo "======== CERTIFICATE READY, PRINTING DETAILS ========" &&
	echo "7/$C: x509" &&
	openssl x509 -in server.crt -noout -text &&
	echo "======== PREPARING KEYSTORE FOR ANDROID ========" &&
	echo "8/$C: keytool" &&
	keytool -import -noprompt -v -trustcacerts -alias 0 -file server.crt -keystore certs.bks -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-146.jar -storepass "TestServer" &&
	echo "9/$C: mkdir" &&
	mkdir -p ../LoremIpsum/src/androidTest/res/raw &&
	mkdir -p ../LoremIpsum/src/main/res/raw &&
	echo "10/$C: cp" &&
	cp certs.bks ../LoremIpsum/src/androidTest/res/raw/certs.bks &&
	cp certs.bks ../LoremIpsum/src/main/res/raw/certs.bks &&
	echo "======== COPYING CERTIFICATE TO COLLECTOR SERVER ========" &&
	echo "11/$C"
	cp server.key ../CollectorServer &&
	cp server.crt ../CollectorServer &&
	echo "======== DONE ========"
)
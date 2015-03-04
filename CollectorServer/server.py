#!/usr/bin/env python3
# -*- coding: utf-8 -*-
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






import argparse
import hashlib
import json
import logging
import os
import os.path
import signal
import sys
import subprocess
import time
import tornado.httpserver
import tornado.ioloop
from tornado.options import define, options
import tornado.web

define('port', type=int, default=40666)
define('results_dir', type=str, default='results')

test_mode = False

logging.basicConfig(
	level=logging.DEBUG,
	filename='server.log',
	format='%(asctime)s %(levelname)10s %(message)s'
)

class SubmitHandler(tornado.web.RequestHandler):
	def post(self):
		logging.info('Submit handler')
		try :
			ifile = self.request.files
			if len(ifile) == 0 or len(ifile) >= 2:
				self.set_status(400)
				self.finish('Bad Request: expected one (only one!) file')
				return
			ifile = list(ifile.values())[0][0]
			ofile = options.results_dir
			if not os.path.isabs(ofile):
				ofile = os.path.join(os.getcwd(), ofile)
			ofile_base = os.path.join(ofile, time.strftime('%Y-%m-%d_%H-%M-%S')+'-'+ifile['filename'])
			ofile = ofile_base
			n = 1
			while os.path.exists(ofile):
				ofile = '{}.{}'.format(ofile_base, n)
				n += 1
			ifile = ifile['body']
			logging.info('Receiving file: {}'.format(ofile))
			with open(ofile, "wb") as ofile:
				ofile.write(ifile)
			self.set_status(200)
			self.finish('OK')
		except:
			logging.exception('Exception in SubmitHandler')
			self.set_status(500)
			self.finish('Internal Server Error')

class ListHandler(tornado.web.RequestHandler):
	def get(self):
		if not test_mode:
			self.set_status(404)
			self.finish('Not found')
			return
		try:
			logging.info('ListHandler')
			odir = options.results_dir
			if not os.path.isabs(odir):
				odir = os.path.join(os.getcwd(), odir)
			ofiles = os.listdir(odir)
			odata = []
			for i in ofiles:
				f = os.path.join(odir, i)
				m = hashlib.md5()
				with open(f, 'rb') as fh:
					while True:
						data = fh.read(65536)
						if not data:
							break
						m.update(data)
				md5 = m.hexdigest()
				odata.append({"name": i, "md5": md5})
			self.finish(json.dumps(odata))
		except:
			logging.exception('Exception in ListHandler')
			self.set_status(500)
			self.finish('Internal Server Error')


def run(args):
	global test_mode
	try:
		tornado.options.parse_config_file(args.config_file)

		logging.info('=====================================================')
		logging.info('Running LoremIpsum task provider server')
		with open('server.pid', 'w') as f:
			f.write(str(os.getpid()))

		odir = options.results_dir
		if not os.path.isabs(odir):
			odir = os.path.join(os.getcwd(), odir)
		if os.path.exists(odir):
			if not os.path.isdir(odir):
				raise Exception('Output path ({}) should end with directory'.format(odir))
		else:
			os.mkdir(odir)

		handlers = [
			tornado.web.URLSpec(r'/submit', SubmitHandler)
		]
		test_mode = args.test_mode
		if test_mode:
			logging.warning('Server is running in TEST MODE!')
			handlers += [
				tornado.web.URLSpec(r'/list', ListHandler)
			]

		application = tornado.web.Application(handlers)
		server = tornado.httpserver.HTTPServer(application, ssl_options = {
                                                          			'certfile': './server.crt',
                                                          			'keyfile': './server.key'
                                                          		})
		server.listen(options.port)
		tornado.ioloop.IOLoop.instance().start()
	except:
		logging.exception('Server stopped due to exception')

def main():
	parser = argparse.ArgumentParser(description='LoremIpsum reference task server')
	parser.add_argument('--test-mode', action='store_true',
		help='Run in test mode')
	parser.add_argument('--start', action='store_true',
		help='Start new server instance and detach')
	parser.add_argument('--stop', action='store_true',
		help='Stop running server instance instead of starting a new one')
	parser.add_argument('--config-file', action='store', default='server.conf',
		help='Configuration file to be used')
	args = parser.parse_args()

	if args.start:
		argv = [ i for i in sys.argv if i != '--start' ]
		if not os.path.isabs(argv[0]):
			argv[0] = os.path.abspath(argv[0])
		subprocess.Popen(argv)
		time.sleep(1)
		return

	if args.stop:
		with open('server.pid') as f:
			pid = int(f.read())
			os.kill(pid, signal.SIGTERM)
		return

	run(args)

if __name__ == '__main__':
	main()
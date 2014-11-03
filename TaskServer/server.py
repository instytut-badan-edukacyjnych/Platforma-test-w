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

#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import base64
import hashlib
import json
import logging
import os
import os.path
import re
import signal
import sqlite3
import subprocess
import sys
import time
import tornado.httpserver
import tornado.ioloop
from tornado.options import define, options
import tornado.web

define('port', type=int, default=40555)
define(
	'suite_file_regex', type=str,
	default=r'(?P<suite_name>.*)-(?P<suite_version>[^\-]+).zip'
)
define('database', type=str)
define('database_schema', type=str)
define('suites_path', type=str)

define('test_database', type=str)
define('test_database_data', type=str)
define('test_suites_path', type=str)

testmode = False
testmode_transfer_failure = False
testmode_version_level = 0

logging.basicConfig(
	level=logging.DEBUG,
	filename='server.log',
	format='%(asctime)s %(levelname)10s %(message)s'
)

class TestSuiteInvalidName(Exception): pass
class NoTestSuitesException(Exception): pass
class LimitExceededException(Exception): pass

__suite_file_regex = None

def suite_file_regex():
	global __suite_file_regex
	if not __suite_file_regex:
		__suite_file_regex = re.compile(options.suite_file_regex)
	return __suite_file_regex

class Suite:
	@staticmethod
	def parse(file_name):
		name = os.path.split(file_name)[1]
		m = suite_file_regex().match(name)
		if not m:
			raise TestSuiteInvalidName(name)
		s = Suite()
		s.file_name = file_name
		s.name = m.group('suite_name')
		s.version = m.group('suite_version')
		return s

	def read(self):
		return open(self.file_name, "rb").read()

def database(connect=True):
	db = options.test_database if testmode else options.database 
	return sqlite3.connect(db) if connect else db

def get_test_suites_path():
	if not testmode:
		raise Exception('Testmode not activated')
	return os.path.join(options.test_suites_path, str(testmode_version_level))

def get_suites():
	suites = []
	path = (get_test_suites_path() if testmode else options.suites_path)
	for i in os.listdir(path):
		try :
			i = os.path.join(path, i)
			suites.append(Suite.parse(i))
		except TestSuiteInvalidName:
			logging.exception('Invalid suite name')
			pass

	if len(suites) == 0:
		raise NoTestSuitesException('Could not find any test suite in {}'.format(path))
	if len(suites) > 1:
		raise Exception('More than one test suite found in {}'.format(path))

	return suites

class Handler(tornado.web.RequestHandler):
	def set_server_header(self):
		suffix = '-testmode' if testmode else ''
		self.add_header('Server', 'LoremIpsumProvider/0.1'+suffix)

	def prepare(self, server_header=True):
		super(Handler, self).prepare()
		logging.info(self.request.uri)

		if server_header:
			self.set_server_header()

	def check_auth(self):
		auth_header = self.request.headers.get('Authorization')
		if not auth_header:
			self.add_header('WWW-Authenticate', 'Basic realm="Lorem Ipsum suite provider"')
			self.set_status(401)
			self.finish()
			return False
		try:
			username, password = self._process_http_authorization_header(auth_header)
			password = hashlib.sha512(password.encode('utf-8')).hexdigest()
			with database() as d:
				auth = d.execute('SELECT ID FROM users WHERE UserName=? AND Password=?', (username, password))
				auth = auth.fetchall()
				if len(auth) != 1:
					raise Exception('Invalid user or password')
				auth = auth[0]
				auth = auth[0]
				logging.debug('Logged user: {}'.format(username))
				return auth
		except:
			logging.exception('Authorization failed')
			self.set_status(403)
			self.finish('403: Forbidden')
			raise

	def _process_http_authorization_header(self, header):
		header = header.split()
		if len(header) < 2:
			raise Exception('HTTP Authorization header has too less component (should contain scheme and authentication arguments)')

		if header[0] != 'Basic':
			raise Exception('Only "Basic" HTTP authentication scheme is available')

		try:
			auth = base64.b64decode(header[1])
		except Exception:
			logging.exception('Base64 decode failed')
			raise Exception('HTTP Authorization header is not valid base64-encoded string')

		i = auth.find(b':')
		if i == -1:
			raise Exception('HTTP Authorization header is malformed, should contain client identier and secret with ":" delimiter character between them')

		return (auth[:i].decode('utf-8'), auth[i+1:].decode('utf-8'))

class ManifestHandler(Handler):
	def get(self):
		user = self.check_auth()
		if user:
			try:
				suites = get_suites()
			except NoTestSuitesException:
				suites = []

			data = json.dumps({
				'suites':[
					{
						'name': suite.name,
						'version': suite.version,
						'url': self.reverse_url('TestSuite', suite.name, suite.version)
					}
					for suite in suites
				]
			})
			logging.debug('answer: '+repr(data))
			self.finish(data)

class TestSuiteHandler(Handler):
	def add_device(self, device_id, user_id):
		with database() as d:
			count = d.execute('SELECT 1 FROM Devices WHERE UserID = ?', (user_id,))
			count = count.fetchall()
			count = len(count)
			logging.debug('Existing installations: {}'.format(count))

			limit = d.execute('SELECT InstallLimit FROM Users WHERE ID = ?', (user_id,))
			limit = limit.fetchall()
			if len(limit) == 1:
				limit = limit[0][0]
				if limit is not None and count+1 > limit:
					raise LimitExceededException('This user cannot install test on more devices')

			d.execute('INSERT INTO Devices (UserID, DeviceID) VALUES (?, ?)', (user_id, device_id))

	def get(self, name, version):
		user = self.check_auth()
		if user:
			try:
				suites = get_suites()
				logging.info('Downloading test "{}" version "{}"'.format(name, version))
				suite = suites[0]

				device_id = self.get_argument('device_id')

				self.add_device(device_id, user)

				if testmode_transfer_failure:
					logging.error('Test of transfer failure: passing error 500')
					self.set_status(500, 'Internal server error')
					self.finish('500: Internal server error')
					return

				self.set_header('Content-Type', 'application/octet-stream')
				self.write(suite.read())
			except LimitExceededException:
				self.set_status(403, 'Installation limit exceeded')
				self.write('403: Installation limit exceeded')

def start_testmode():
	global testmode
	testmode = True
	db_file = database(connect=False)
	if os.path.exists(db_file):
		os.remove(db_file)
	with database() as d:
		logging.info('Rebuilding database for tests...')
		#d.executescript(open(DATABASE_SCHEMA_DROP).read())
		d.executescript(open(options.database_schema).read())
		d.executescript(open(options.test_database_data).read())
		logging.info('...done')

def stop_testmode():
	global testmode, testmode_transfer_failure, testmode_version_level
	testmode = False
	testmode_transfer_failure = False
	testmode_version_level = 0

class TestModeHandler(Handler):
	def prepare(self):
		super(TestModeHandler, self).prepare(server_header=False)

	def get(self, op):
		global testmode
		testmode = (op == 'on')
		if testmode:
			start_testmode()
		else:
			stop_testmode()
		self.set_server_header()
		self.finish('testmode switched to {}'.format(testmode))

class TestModeTransferFailureHandler(Handler):
	def get(self, op):
		global testmode_transfer_failure
		testmode_transfer_failure = (op == 'on')
		self.finish('testmode switched transfer_failure to {}'.format(testmode_transfer_failure))

class TestModeLevelHandler(Handler):
	def get(self, level):
		global testmode_version_level
		testmode_version_level = level
		self.finish('testmode switched level to {}'.format(testmode_version_level))

def run(args):
	try:
		tornado.options.parse_config_file(args.config_file)

		logging.info('=====================================================')
		logging.info('Running LoremIpsum task provider server')
		with open('server.pid', 'w') as f:
			f.write(str(os.getpid()))
		allow_testmode = args.test_mode

		handlers = [
			tornado.web.URLSpec(r'/', ManifestHandler),
			tornado.web.URLSpec(r'/suite/(.*)/(.*)', TestSuiteHandler, name='TestSuite')
		]
		if args.test_mode:
			handlers += [
				tornado.web.URLSpec(r'/testmode/(on|off)', TestModeHandler),
				tornado.web.URLSpec(r'/testmode/transfer_failure/(on|off)', TestModeTransferFailureHandler),
				tornado.web.URLSpec(r'/testmode/version_level/(\d+)', TestModeLevelHandler)
			]

		if args.start_in_test_mode:
			start_testmode()

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
		help='Enable dynamic switching to test mode')
	parser.add_argument('--start', action='store_true',
		help='Start new server instance and detach')
	parser.add_argument('--stop', action='store_true',
		help='Stop running server instance instead of starting a new one')
	parser.add_argument('--start-in-test-mode', action='store_true',
		help='When starting server be in test mode from the beginning')
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
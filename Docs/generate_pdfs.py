#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import os
import re
import subprocess
import sys

RE_MDFILE = re.compile(r'(.*)\.md')
RE_TEXFILE = re.compile(r'(.*)\.tex')

OPEN = 'xdg-open'

parser = argparse.ArgumentParser(description='Generator of PDF files')
parser.add_argument('inputs', nargs='*', help='files to process')
parser.add_argument('--open', action='store_true',
	help='Open file after generating')
args = parser.parse_args()

infiles = args.inputs
if not infiles:
	directory = os.getcwd()
	for i in os.listdir(directory):
		m = RE_MDFILE.match(i)  or RE_TEXFILE.match(i)
		if m:
			infiles.append(os.path.join(directory, i))

for i in infiles:
	m = RE_MDFILE.match(i)
	if m:
		ofile = '{}.pdf'.format(m.group(1))
		sys.stderr.write('Generating {}...\n'.format(ofile))
		subprocess.check_call(['pandoc', i, '-o', ofile])
	else:
		m = RE_TEXFILE.match(i)
		if m:
			ofile = '{}.pdf'.format(m.group(1))
			sys.stderr.write('Generating {}...\n'.format(ofile))
			subprocess.check_call(['xelatex', i, '-o', ofile])
			subprocess.check_call(['xelatex', i, '-o', ofile])
	if m:
		if args.open:
			subprocess.check_call([OPEN, ofile])		

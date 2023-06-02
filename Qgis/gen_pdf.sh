#! /bin/bash

pandoc 1_processings.md -o 1_processings.pdf -s --pdf-engine=xelatex -V geometry:margin=3cm -V fontsize=11pt -V "mainfont:LiberationSans"
pandoc 2_python.md -o 2_python.pdf -s --pdf-engine=xelatex -V geometry:margin=3cm -V fontsize=11pt -V "mainfont:LiberationSans"
pandoc 3_databases.md -o 3_databases.pdf -s --pdf-engine=xelatex -V geometry:margin=3cm -V fontsize=11pt -V "mainfont:LiberationSans"

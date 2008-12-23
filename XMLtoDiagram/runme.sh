#!/bin/sh
# pfh 12/22/08, script before I rewrite as a makefile

./xmlToDot.py P3_startup.xml > test.dot

dot -Nfontname="/System/Library/Fonts/Times.dfont" test.dot -o test.dotlayout

dot -Tpng -Nfontname="/System/Library/Fonts/Times.dfont" test.dot -o test.png

dot -Tsvg -Nfontname="/System/Library/Fonts/Times.dfont" test.dot -o test.svg

dot2gxl test.dot > test.gxl


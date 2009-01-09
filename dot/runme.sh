#!/bin/sh
# pfh 12/22/08, script before I rewrite as a makefile

dot -Nfontname="/System/Library/Fonts/Times.dfont" p3-third.dot -o p3-third.dotlayout
dot -Tsvg -Nfontname="/System/Library/Fonts/Times.dfont" p3-third.dot -o p3-third.svg
dot -Tpng -Nfontname="/System/Library/Fonts/Times.dfont" p3-third.dot -o p3-third.png


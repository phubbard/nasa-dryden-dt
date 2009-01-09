#!/bin/sh
# pfh 12/22/08, script before I rewrite as a makefile

dot -Nfontname="/System/Library/Fonts/Times.dfont" p4.dot -o p4.dotlayout
dot -Tsvg -Nfontname="/System/Library/Fonts/Times.dfont" p4.dot -o p4.svg
dot -Tpng -Nfontname="/System/Library/Fonts/Times.dfont" p4.dot -o p4.png



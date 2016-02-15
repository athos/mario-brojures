#!/bin/sh

git checkout gh-pages
git merge --no-ff master
lein clean
lein cljsbuild once min
git commit -am 'Update compiled JS file'
git checkout master

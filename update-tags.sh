#!/usr/bin/env zsh

local FILE=.
local FLAGS=""

tagfile() {
  FILE=$1
  echo "========$FILE========"
  rm $FILE > /dev/null 2>&1
}
ctags_append() {
  echo ---$2---
  ctags $FLAGS --recurse -f $1 --append --extra=+q --langmap=java:+.aidl $2
}
ctag_dirs() {
  for dir in $*
  do
    ctags_append $FILE $dir
  done
}
ctags_limited_append() {
  echo ---$2---
  ctags $FLAGS --recurse -f $1 --append --extra=+q --java-kinds=ci --c-kinds=c --c++-kinds=c --langmap=java:+.aidl $2
}
ctag_limited_dirs() {
  for dir in $*
  do
    ctags_limited_append $FILE $dir
  done
}

if [ "x$1" = "x-e" ]; then
  tagfile TAGS
  FLAGS="-e"
else
  tagfile tags
  FLAGS="--sort=yes"
fi
ctag_dirs \
  SpotifyLibrary/jni/wigwamlabs \
  vendor/spotify/include \
#
ctag_limited_dirs \
#

#!/bin/sh

echo "starting"
cd $PREFIX/bin && {
  echo "start"
  curl -O https://raw.githubusercontent.com/rajpar29/termux-launcher/master/open ;
  echo "downloaded"
  chmod 777 open;
  echo "changed mode"
  cd -;
  echo "Changed Dir"
  echo $?
  echo "END"
   }

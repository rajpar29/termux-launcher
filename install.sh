#!/bin/sh

echo "starting"
cd $PREFIX/bin && {
  echo "Fetching script"
  curl -O https://raw.githubusercontent.com/rajpar29/termux-launcher/master/open ;
  echo "Insatlling"
  chmod 777 open;
  cd -;
  echo "Make sure the termux launcher app is installed."
  echo "open <app name>"
   }

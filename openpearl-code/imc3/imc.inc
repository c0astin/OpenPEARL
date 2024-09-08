if [ "$CONFIGELEMENT" = "" ]
then
   java -cp "/usr/local"/bin/imc.jar imc/main/InterModuleChecker -std=OpenPEARL -b $PLATTFORM  -I "/usr/local" -o system.cc $fn
else
   java -cp "/usr/local"/bin/imc.jar imc/main/InterModuleChecker -std=OpenPEARL -b $PLATTFORM  $CONFIGELEMENT -I "/usr/local" -o system.cc $fn
fi

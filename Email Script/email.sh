#!/bin/bash





echo

if [ -n "$1" ]           
then
 VAR1="$1"
 else
 VAR1="DefaultSub"
fi 

if [ -n "$2" ]
then
 VAR2=$2
 else
 VAR2="Default Message Body"
fi 



mail -s $VAR1 adahal3cs441@gmail.com <<< $VAR2


echo

exit 0
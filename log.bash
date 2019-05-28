#!/usr/bin/bash

APP=$1
ENV=$2

if [ -z $APP ] || [ -z $ENV ]; then 
	echo
	echo "Usage: $0 <appname> <env/namespace>"
	echo
	echo "Example: $0 sporingslogg t4"
    if [ -f deploys.txt ]; then
       echo 
       echo "Tidligere deploys/versjoner:"
       cat deploys.txt
    fi
	exit
fi

echo --------------------------------
echo ---------- Listing pods
kubectl get po -n=$ENV | grep $APP
echo
echo -n "Tail log for imageid": 
read IMAGEID
echo
# Finn image med appnavn i id og bare sekunders levetid, hent log for dette
kubectl logs $IMAGEID -n $ENV -f



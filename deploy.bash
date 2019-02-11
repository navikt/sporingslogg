#!/usr/bin/bash

APP=$1
VERSION=$2
ENV=$3
FASITUSER=$4

if [ -z $APP ] || [ -z $VERSION ] || [ -z $ENV ] || [ -z $FASITUSER ]; then 
	echo
	echo "Usage: $0 <appname> <version> <env/namespace> <fasituser>"
	echo
	echo "Example: $0 sporingslogg 1.0.0 t4 k12345"
    if [ -f deploys.txt ]; then
       echo 
       echo "Tidligere deploys/versjoner:"
       cat deploys.txt
    fi
	exit
fi

echo -n Password for $FASITUSER: 
read -s PASSWORD
echo

echo $APP $VERSION $ENV > deploys.txt

# Hardkodet docker-repo, cluster og zone, dette kunne også vært parametre
# Bruker env og namespace på NAIS
REPO=docker.adeo.no:5000
REPOGROUP=integrasjon
CLUSTER=preprod-fss
ZONE=fss
IMAGENAME=${REPO}/${REPOGROUP}/${APP}":"${VERSION}

echo
echo --------------------------------
echo ---------- Building docker image $IMAGENAME
docker build . -t $IMAGENAME
echo
echo --------------------------------
echo ---------- Pushing docker image $IMAGENAME
docker push $IMAGENAME
echo
echo --------------------------------
echo ---------- Uploading nais.yaml for $APP $VERSION
nais upload -a $APP -f nais.yaml -v $VERSION
echo
echo --------------------------------
echo ---------- Deploying $APP to $CLUSTER in zone $ZONE, namespace and env $ENV, fasit-user $FASITUSER
nais deploy -c $CLUSTER -z $ZONE -e $ENV -n $ENV -a $APP -v $VERSION -u $FASITUSER -p $PASSWORD
echo
echo --------------------------------
echo ---------- Listing pods
kubectl get po -n=$ENV | grep $APP
echo
echo -n "Tail log (y/n)": 
read TAILLOG
echo
# Finn image med appnavn i id og bare sekunders levetid, hent log for dette
if [ $TAILLOG = "y" ]; then
	IMAGEID=$(kubectl get po -n=$ENV | grep $APP | egrep "s$" | awk '{ print $1 }' )
	kubectl logs $IMAGEID -n $ENV -f
fi


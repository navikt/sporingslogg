#!/usr/bin/bash

# Dette scriptet utfører en eller begge av kommandoene 
# 'docker': bygger docker-image med IMAGENAME og pusher til docker-repo,
# 'nais': genererer nais.yaml fra ./naisTemplate_<env>.yaml, med injisering av IMAGENAME, og deployer nais.yaml på kubernetes.
# Deploy-historikk ligger i deploys.txt
#
# Hvis arg1 inneholder 'd' utføres docker, inneholdes 'n' utføres nais, både 'd' og 'n': docker etterfulgt av nais.
# Hvis arg2 angis skal denne inneholde versjon, ellers hentes versjon fra pom.xml. Versjon brukes som del av IMAGENAME.
# Hvis arg3 angis skal denne inneholde env/namespace, ellers ber scriptet om denne for 'nais'-deploy. naisTemplate_<env>.yaml må finnes.

COMMAND=$1
VERSION=$2
ENV=$3

# Hardkodede verdier - endre ved behov. Envs må være lowercase for å matche NAIS namespace.
APP=sporingslogg
ENVS=t4/q2/prod
REPO=docker.adeo.no:5000
REPOGROUP=integrasjon

# Vis hjelp hvis ingen parametre angis
if [ -z $COMMAND ]
then 
	echo
	echo "Usage: $0 <docker|nais|dockernais> (version if not from pom) ($ENVS)"
	exit
fi

# Arg2/version ikke angitt, hent fra første linje i pom.xml som inneholder <version>
if [ -z $VERSION ] 
then 
    echo
    VERSION=`grep -m 1 "<version>" pom.xml | sed s-"<version>"--  | sed s-"</version>"-- | tr -d '[:space:]'`
    read -p "Using version from pom.xml: $VERSION (y/n)? " -n 1 -r
    echo 
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
        echo "Exiting, try again with version as arg 2"
        exit 1
    fi
fi

# Image name kan bygges opp, med version
IMAGENAME=${REPO}/${REPOGROUP}/${APP}":"${VERSION}

# Command inneholder 'd': kjør DOCKER-DEPLOY -------------------------------------------
if [[ $COMMAND == "d"* ]] 
then
    echo
    echo --------------------------------
    echo ---------- Building docker image $IMAGENAME
    docker build . -t $IMAGENAME
    echo
    echo --------------------------------
    echo ---------- Pushing docker image $IMAGENAME
    docker push $IMAGENAME
    echo 
else
    echo
    echo --------------------------------
    echo ---------- No docker command - skipping Docker deploy
fi

if [[ $COMMAND != *"n"* ]] 
then
    echo
    echo --------------------------------
    echo ---------- No nais command - skipping NAIS deploy
    exit
fi

# Command inneholder 'n': kjør NAIS-DEPLOY ----------------------------------------------
if [ -z $ENV ] # env ikke angitt, les inn
then 
    echo
    read -p "Deploy to env ($ENVS)? " -r
    echo 
    ENV=$REPLY
fi

# Lowercase for å sikre match med NAIS namespace
ENV=`echo $ENV | tr A-Z a-z`

if [[ $ENVS != *$ENV* ]] 
then
    echo
    echo --------------------------------
    echo ---------- Invalid env $ENV, must be one of $ENVS - exiting
    exit
fi

# template-yaml er identifisert av env
NAISFILE=naisTemplate_${ENV}.yaml

echo 
echo --------------------------------
echo ---------- Building nais.yaml from $NAISFILE with image $IMAGENAME
sed s!_IMAGE_FULLNAME_!$IMAGENAME! $NAISFILE > nais.yaml

if [[ ${ENV} == "prod" ]]
then
    KUBECONTEXT=prod-fss
    NAMESPACE=default
else
    KUBECONTEXT=preprod-fss
    NAMESPACE=$ENV
fi

echo Deployed $IMAGENAME to $ENV >> deploys.txt

echo
echo --------------------------------
echo ---------- Setting kubernetes context to $KUBECONTEXT
kubectl config use-context $KUBECONTEXT
echo
echo --------------------------------
echo ---------- Deploying $APP to kubernetes/nais
kubectl apply -f nais.yaml
echo
echo --------------------------------
echo "---------- Listing pods with kubectl get po -n=$NAMESPACE | grep $APP"
kubectl get po -n=$NAMESPACE | grep $APP
echo
echo To look at log:
echo "   kubectl -n $NAMESPACE logs <image-id> -f"

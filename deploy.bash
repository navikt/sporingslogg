#!/usr/bin/bash

# Dette scriptet bruker ./Dockerfile_ENV som Dockerfile,
# og genererer nais.yaml fra ./naisTemplate.yaml, med ENV og IMAGENAME.
# Image bygges og pushes til docker-repo, og nais.yaml deployes på kubernetes.

APP=$1
VERSION=$2
ENV=$3

if [ -z $APP ] || [ -z $VERSION ] || [ -z $ENV ]; then 
	echo
	echo "Usage: $0 <appname> <version> <env/namespace>"
	echo
	echo "Example: $0 sporingslogg 1.0.0 t4"
    if [ -f deploys.txt ]; then
       echo 
       echo "Tidligere deploys/versjoner:"
       cat deploys.txt
    fi
	exit
fi

# Hardkodede verdier - endre ved behov
KUBECONTEXT=preprod-fss
REPO=docker.adeo.no:5000
REPOGROUP=integrasjon
IMAGENAME=${REPO}/${REPOGROUP}/${APP}":"${VERSION}
DOCKERFILE=Dockerfile_${ENV} 
echo $IMAGENAME

echo Deployed $IMAGENAME to $ENV >> deploys.txt

echo
echo --------------------------------
echo ---------- Using docker file $DOCKERFILE
cp $DOCKERFILE Dockerfile  
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
echo ---------- Building nais.yaml for $ENV and image $IMAGENAME
sed s/_NAIS_ENV_/$ENV/  naisTemplate.yaml | sed s!_IMAGE_FULLNAME_!$IMAGENAME! > nais.yaml
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
echo ---------- Listing pods
kubectl get po -n=$ENV | grep $APP
echo
echo To list pods again:
echo "  kubectl get po -n=$ENV | grep $APP"
echo
echo To look at log:
echo "   kubectl -n $ENV logs <image-id> -f"

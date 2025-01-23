#!/usr/bin/env bash

export DBPASSWORD=$(cat /secrets/oracle_creds/password)
export DBUSERNAME=$(cat /secrets/oracle_creds/username)
export SRVPASSWORD=$(cat /secrets/serviceuser/password)
export SRVUSERNAME=$(cat /secrets/serviceuser/username)
echo "VAULT variables exported to local variables"

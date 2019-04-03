# Quick Start

## Build
   
mkdir /data0/workspace
cd /data0/workspace

git clone https://github.com/weibodip/cosmos.git
git checkout ${tag}

cd cosmos

mvn clean package -pl node -am

## Config

vim conf/cosmos.properties

### c3p0

### quartz

### docker

### server

## Run

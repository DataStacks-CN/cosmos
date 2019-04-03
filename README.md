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

```text
c3p0.driverClass=com.mysql.jdbc.Driver
c3p0.jdbcUrl=jdbc:mysql://${host}:${port}/${db}?&useUnicode=true&characterEncoding=utf-8&useSSL=false
c3p0.user=${user}
c3p0.password=${password}
c3p0.acquireIncrement=3
c3p0.initialPoolSize=3
c3p0.maxPoolSize=30
c3p0.maxIdleTime=60
c3p0.minPoolSize=1
c3p0.preferredTestQuery=select 1
c3p0.testConnectionOnCheckout=true
c3p0.acquireRetryAttempts=3
c3p0.acquireRetryDelay=1000
c3p0.checkoutTimeout=3000

```

### quartz

```text
org.quartz.scheduler.instanceId=AUTO
org.quartz.scheduler.instanceName=${scheduler}
org.quartz.threadPool.threadCount=${threadCount}
org.quartz.jobStore.isClustered=true
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.tablePrefix=QRTZ_
org.quartz.jobStore.dataSource=schedulerds
org.quartz.dataSource.schedulerds.connectionProvider.class=com.weibo.dip.cosmos.node.quartz.QuartzConnectionProvider
```

### docker

* docker.image.pull.timeout

   拉取Docker Image超时时间
   
* docker.container.python

   Docker Container启动时的入口Python脚本
   
* docker.container.log

   Docker Container运行时的日志输出父目录，子目录格式：${docker.container.log}/${queue}/${name}/${yyyyMMddHHmmss}，日志文件：start.log、container.log
   
* docker.container.tmp

   Docker Container运行时的“/tmp”宿主挂载目录

```text
docker.image.pull.timeout=600000
docker.container.python=start.py
docker.container.log=/var/log/cosmos/container
docker.container.tmp=/data0/tmp
```

### server

* server.cores

   可以使用的CPU数目

* server.mems

   可以使用的内存大小，单位：MB
   
* server.port

   调度系统服务端口，所有实例端口必须保持一致

* server.hosts

   调度系统实例地址，多个实例地址以“,”分隔
   
```text
server.cores=${cores}
server.mems=${mems}
server.port=${port}
server.hosts=${hosts}
```

## Run

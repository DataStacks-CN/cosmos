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
c3p0.jdbcUrl=jdbc:mysql://{host}:{port}/{db}?&useUnicode=true&characterEncoding=utf-8&useSSL=false
c3p0.user={user}
c3p0.password={password}
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
org.quartz.scheduler.instanceName=scheduler
org.quartz.threadPool.threadCount=10
org.quartz.jobStore.isClustered=true
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.tablePrefix=QRTZ_
org.quartz.jobStore.dataSource=schedulerds
org.quartz.dataSource.schedulerds.connectionProvider.class=com.weibo.dip.cosmos.node.quartz.QuartzConnectionProvider
```

### docker

```text
docker.image.pull.timeout=600000
docker.container.python=start.py
docker.container.log=/var/log/cosmos/container
docker.container.tmp=/data0/tmp
```

### server

## Run

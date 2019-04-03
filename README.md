# Cosmos

分布式任务调度系统

# 快速开始

## 构建
   
mkdir /data0/workspace
cd /data0/workspace

git clone https://github.com/weibodip/cosmos.git

git checkout ${tag}

cd cosmos

mvn clean package -pl node -am

## 配置

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

## 运行

### 启动

python bin/cosmos.py -start

### 查看

python bin/cosmos.py -status

### 停止

python bin/cosmos.py -stop

## 操作

### start

python bin/client.py -start /tmp/start.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "user": "yurun",
    "priority": 0,
    "cores": 1,
    "mems": 6144,
    "repository": "registry.api.weibo.com/dippub/pinot_import_application",
    "tag": "0.0.1",
    "params": [],
    "cron": "0 45 16 * * ?",
    "timeout": 7200
}
```

开启应用调度

* name
   应用名称
   
* queue
   队列名称

* user
   用户
   
* priority
   调度优先级
   
* cores
   应用运行时使用的CPU数目
   
* mems
   应用运行时使用的内存大小
   
* repository
   应用运行时使用的Docker Image仓库地址
   
* tag
   应用运行时使用的Docker Image版本号
   
* params
   应用运行时接收的参数值，默认数组为空；
   
* cron
   Quartz Cron表达式
   
* timeout
   应用运行超时时间，单位：秒

### stop

python bin/client.py -stop ${name}:${queue}

停止应用调度

### update

python bin/client.py -start /tmp/update.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "user": "yurun",
    "priority": 0,
    "cores": 1,
    "mems": 6144,
    "repository": "registry.api.weibo.com/dippub/pinot_import_application",
    "tag": "0.0.1",
    "params": [],
    "timeout": 7200
}
```

更新应用信息

* name
   应用名称
   
* queue
   队列名称
   
update操作要求应用“name:queue”必须处于“已调度”状态，除“cron”之外，其余字段信息均可被更新

### queues

python bin/client.py -queues

列出调度系统中的所有队列名称

### apps

python bin/client.py -apps ${queue}

列出调度系统中指定队列下的所有应用名称

### app

python bin/client.py -app ${name}:${queue}

描述调度系统中指定名称、指定队列的应用详情

### running

python bin/client.py -running ${queue}

列出调度系统中指定队列下的所有处于"运行"状态的应用信息

### queued

python bin/client.py -queued

列出调度系统"等待队列"中的所有应用信息

### deleteQueued

python bin/client.py -deleteQueued ${mid}

删除调度系统"等待队列"中指定mid的应用

### records

python bin/client.py -records /tmp/records.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "beginTime": "2019-04-02 00:00:00",
    "endTime": "2019-04-02 23:59:59"
}
```

列出调度系统中指定名称、指定队列、指定时间范围的应用运行记录

* name
    应用名称

* queue
    队列名称

* beginTime
    起始时间，闭区间

* endTime
    截止时间，闭区间

### kill

python bin/client.py -kill /tmp/kill.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "scheduleTime": "2019-04-02 16:45:00"
}
```

杀死调度系统中指定名称、指定队列、指定调度时间的应用，要求该应用必须处于"运行"状态

* name
    应用名称

* queue
    队列名称

* scheduleTime
    调度时间

### log

python bin/client.py -log /tmp/log.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "scheduleTime": "2019-04-02 16:45:00"
}
```

查看调度系统中指定名称、指定队列、指定调度时间的应用日志，要求相应的应用记录必须存在

* name
    应用名称

* queue
    队列名称

* scheduleTime
    调度时间

### repair

python bin/client.py -repair /tmp/repair.json

{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "beginTime": "2019-04-02 00:00:00",
    "endTime": "2019-04-02 23:59:59"
}

修复调度系统中指定名称、指定队列、指定时间范围内的应用，要求相应的应用记录必须是"未调度"、"失败"、"杀死"三者之一

### replay

python bin/client.py -replay /tmp/replay.json

{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "beginTime": "2019-04-02 00:00:00",
    "endTime": "2019-04-02 23:59:59"
}

重新执行调度系统中指定名称、指定队列、指定时间范围内的应用
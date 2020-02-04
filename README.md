# Cosmos

Cosmos是一个分布式调度系统，应用以Docker的形式进行发布与运行，支持以下特性：

* 多队列

   不同类型的应用可以运行于不同的队列中，多个队列之间的资源分配使用“公平调度”的策略进行调配；
   队列资源，即为该队列中所有处于运行状态的应用资源总和；
   资源分配时，优先分配给队列资源使用最小的队列，如果该队列中没有满足运行条件的应用，则依此类推下一个“最小”的队列；
   
* 热部署
   
   新增应用使用Docker Image的形式进行发布；调度系统本身无需任何更改或重启操作；
   
* 热更新

   应用运行时参数及Docker Image版本可以根据需求随时更新（更新时应用需要处理“停止调度”状态）；
   
* 资源隔离

   应用运行时可以限制CPU和MEM的使用量；
   
* 超时控制

   应用运行时长超过指定阈值时会执行“kill”；

* 依赖调度（DAG）

  应用A调度运行的前置条件：应用B在时间范围[startTime, endTime]内相应的运行记录状态全部为成功；

# 快速开始

## 构建
   
mkdir /data0/workspace

cd /data0/workspace

git clone https://github.com/weibodip/cosmos.git

cd cosmos

git checkout ${tag}

mvn clean package -pl node -am

## 数据库/表创建（MySQL）

sql/cosmos.sql
sql/quartz.sql

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

### add

新增应用

python bin/client.py -add /tmp/add.json

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
    "params": {},
    "cron": "0 45 16 * * ?",
    "timeout": 7200
}
```

应用信息

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
   应用运行时接收的参数值，Json格式，默认为空；
   
* cron
   Quartz Cron表达式
   
* timeout
   应用运行超时时间，单位：秒
   
### start

启动应用调度

python bin/client.py -start ${name}:${queue}

### stop

停止应用调度

python bin/client.py -stop ${name}:${queue}

### update

更新应用

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

应用信息

* name
   应用名称
   
* queue
   队列名称
   
update操作要求应用“name:queue”必须处于“新增且停止调度”的状态，其余字段信息均可被更新

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

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "beginTime": "2019-04-02 00:00:00",
    "endTime": "2019-04-02 23:59:59"
}
```

修复调度系统中指定名称、指定队列、指定时间范围内的应用，要求相应的应用记录必须是"未调度"、"失败"、"杀死"三者之一

### replay

python bin/client.py -replay /tmp/replay.json

```text
{
    "name": "video_client_upload_metrics",
    "queue": "pinot",
    "beginTime": "2019-04-02 00:00:00",
    "endTime": "2019-04-02 23:59:59"
}
```

重新执行调度系统中指定名称、指定队列、指定时间范围内的应用

### addDepend

python bin/client.py -addDepend /tmp/depend.json

```text
{
    "name": "baishan_bandwidth_staging",
    "queue": "pinot",
    "dependName": "cdn.baishan_bandwidth_staging",
    "dependQueue": "hive",
    "fromSeconds": 86400,
    "toSeconds": 0
}
```

添加应用依赖

* name
    应用名称

* queue
    队列名称

* dependName
    依赖应用名称

* dependQueue
    依赖队列名称

* fromSeconds
    起始偏移秒数

* toSeconds
    截止偏移秒数

### getDepends

python bin/client.py -getDepends ${name}:${queue}

查询调度系统中指定名称、指定队列的应用依赖信息

### removeDepend

python bin/client.py -removeDepend /tmp/depend.json

```text
{
    "name": "baishan_bandwidth_staging",
    "queue": "pinot",
    "dependName": "cdn.baishan_bandwidth_staging",
    "dependQueue": "hive"
}
```

移除应用依赖

* name
    应用名称

* queue
    队列名称

* dependName
    依赖应用名称

* dependQueue
    依赖队列名称

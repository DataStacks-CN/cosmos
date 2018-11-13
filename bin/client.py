#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import commands

user = 'root'

java_home = '/usr/java/default'

classpaths = [
              '/data0/workspace/hubble/scheduler/conf/',
              '/data0/workspace/hubble/scheduler/target/hubble-scheduler-0.0.1-SNAPSHOT.jar',
              '/data0/workspace/hubble/scheduler/target/hubble-scheduler-0.0.1-SNAPSHOT-lib/*']

main_class = 'com.weibo.dip.hubble.scheduler.client.ConsoleSchedulerClient'

if __name__ == '__main__':
    command = 'sudo -u %s %s/bin/java -cp %s %s %s' % (user, java_home, ':'.join(classpaths), main_class, ' '.join(sys.argv[1:]))

    print commands.getoutput(command)
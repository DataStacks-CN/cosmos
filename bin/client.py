#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import commands

user = 'root'

java_home = '/usr/java/default'

classpaths = [
              '/data0/workspace/cosmos/conf/',
              '/data0/workspace/cosmos/node/target/cosmos-node-0.1.jar',
              '/data0/workspace/cosmos/node/target/cosmos-node-0.1-lib/*']

main_class = 'com.weibo.dip.cosmos.node.client.ConsoleClient'

if __name__ == '__main__':
    command = 'sudo -u %s %s/bin/java -cp %s %s %s' % (user, java_home, ':'.join(classpaths), main_class, ' '.join(sys.argv[1:]))

    print commands.getoutput(command)
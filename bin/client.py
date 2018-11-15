#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import commands
import os


def get_jars(dir_path):
    jars = []

    if os.path.exists(dir_path):
        for name in os.listdir(dir_path):
            file_path = os.path.join(dir_path, name)

            if os.path.isfile(file_path):
                if file_path.endswith('.jar'):
                    jars.append(file_path)
            elif os.path.isdir(file_path):
                if file_path.endswith('-lib'):
                    jars.append(file_path + '/*')
                else:
                    jars.extend(get_jars(file_path))

    return jars


user = 'root'

java_home = '/usr/java/default'

project_home = '/data0/workspace/cosmos'

module_home = project_home + '/node'

classpaths = [project_home + '/conf']
classpaths.extend(get_jars(module_home))

main_class = 'com.weibo.dip.cosmos.node.client.ConsoleClient'


if __name__ == '__main__':
    command = 'sudo -u %s %s/bin/java -cp %s %s %s' % (
        user, java_home, ':'.join(classpaths), main_class, ' '.join(sys.argv[1:]))

    print commands.getoutput(command)

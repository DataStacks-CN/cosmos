#!/usr/bin/env python
# -*- coding: utf-8 -*-
import argparse
import os
import commands


def get_jars(dir_path):
    jars = []

    if os.path.exists(dir_path):
        for name in os.listdir(dir_path):
            file_path = os.path.join(dir_path, name)

            if os.path.isfile(file_path):
                if file_path.endswith(".jar"):
                    jars.append(file_path)
            elif os.path.isdir(file_path):
                jars.extend(get_jars(file_path))

    return jars


user = 'root'

java_home = '/usr/java/default'

system_properties = {}

min_heap_size = '256'
max_heap_size = '1024'

project_home = '/data0/workspace/cosmos'

module_home = project_home + '/node'

classpaths = [project_home + '/conf']
classpaths.extend(get_jars(module_home))

main_class = 'com.weibo.dip.cosmos.node.NodeManager'

running = "running"

stoped = "stoped"


def start():
    if status() == running:
        print "app is runing: %s" % getPID()

        return

    dparams = []

    for key, value in system_properties.items():
        dparams.append('-D%s=%s' % (key, value))

    command = 'sudo -u %s %s/bin/java %s -cp %s %s >> /dev/null 2>&1 &' % (
        user, java_home, ' '.join(dparams), ':'.join(classpaths), main_class)

    print command

    os.system(command)


def getPID():
    command = "ps aux | grep %s | grep '%s' | grep -v sudo | grep -v grep | awk '{print $2}'" % (
        user, main_class)

    print command

    return commands.getoutput(command)


def status():
    pid = getPID()

    if pid:
        print pid

        return running
    else:
        return stoped


def stop():
    pid = getPID()

    if not pid:
        print "services stoped"

        return

    command = "sudo -u %s kill %s" % (user, pid)

    print command

    os.system(command)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="data-platform services")

    group = parser.add_mutually_exclusive_group()

    group.add_argument("-start", "--start",
                       action="store_true", required=False, help="services start")

    group.add_argument("-status", "--status",
                       action="store_true", required=False, help="services status")

    group.add_argument("-stop", "--stop", action="store_true",
                       required=False, help="services stop")

    args = parser.parse_args()

    if args.start:
        start()
    elif args.status:
        print status()
    elif args.stop:
        stop()
    else:
        parser.print_help()

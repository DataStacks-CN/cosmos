import sys
import urllib
import logging
import commands

if __name__ == '__main__':
    args = sys.argv

    name = args[1]
    queue = args[2]
    user = args[3]
    priority = args[4]
    cores = args[5]
    mems = args[6]
    repository = args[7]
    tag = args[8]
    params = urllib.unquote_plus(args[9])
    cron = urllib.unquote_plus(args[10])
    timeout = args[11]
    schedule_time = args[12]
    log_dir = args[13]

    log_file = log_dir + '/start.log'

    logging.basicConfig(filename = log_file, level = logging.INFO)

    logging.info('name: %s', name)
    logging.info('queue: %s', queue)
    logging.info('user: %s', user)
    logging.info('priority: %s', priority)
    logging.info('cores: %s', cores)
    logging.info('mems: %s', mems)
    logging.info('repository: %s', repository)
    logging.info('tag: %s', tag)
    logging.info('params: %s', params)
    logging.info('cron: %s', cron)
    logging.info('timeout: %s', timeout)
    logging.info('schedule_time: %s', schedule_time)
    logging.info('log_dir: %s', log_dir)

    cmd = 'date > {log_dir}/container.log 2>&1'.format(log_dir=log_dir)

    logging.info('cmd: %s', cmd)

    (status, output) = commands.getstatusoutput(cmd)

    logging.info('status: %s, output: %s', status, output)

    if status != 0:
        raise RuntimeError("The exit code of executing command is not zero!")
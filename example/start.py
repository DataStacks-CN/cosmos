import sys
import urllib
import logging
import commands

if __name__ == '__main__':
    params = sys.argv

    name = params[1]
    queue = params[2]
    user = params[3]
    priority = params[4]
    cores = params[5]
    mems = params[6]
    repository = params[7]
    tag = params[8]
    params = urllib.unquote(params[9])
    cron = param[10]
    timeout = param[11]
    schedule_time = params[12]
    log_dir = params[13]

    app_name = name

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

    logging.info(cmd)

    (status, output) = commands.getstatusoutput(cmd)

    logging.info('status: %s, output: %s', status, output)

    if status != 0:
        raise RuntimeError("The exit code of executing command is not zero!")
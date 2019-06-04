import sys
import logging
import commands

if __name__ == '__main__':
    params = sys.argv

    name = params[1]
    queue = params[2]
    user = params[3]
    schedule_time = params[4]
    log_dir = params[5]

    app_name = name

    log_file = log_dir + '/start.log'

    logging.basicConfig(filename = log_file, level = logging.INFO)

    cmd = 'date > {log_dir}/container.log 2>&1'.format(log_dir=log_dir)

    logging.info(cmd)

    (status, output) = commands.getstatusoutput(cmd)

    logging.info('status: %s, output: %s', status, output)

    if status != 0:
        raise RuntimeError("The exit code of executing command is not zero!")
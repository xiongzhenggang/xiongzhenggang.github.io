#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import paramiko
import numpy as np
def methond():
    # 创建SSH对象
    ssh = paramiko.SSHClient()
    # 允许连接不在know_hosts文件中的主机，否则可能报错：paramiko.ssh_exception.SSHException: Server '192.168.43.140' not found in known_hosts
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    strh = '''172.21.89.7
    172.21.89.8
    172.21.89.9
    172.21.89.10
    172.21.89.11
    172.21.89.12
    172.21.89.13
    172.21.89.14
    172.21.89.15
    172.21.89.17
    172.21.89.18
    172.21.89.19
    172.21.89.50'''
    hosts= np.array(strh.split('\n'))
    # 连接服务器
    for hostname in hosts:
        hostname = hostname.strip()
        print('地址： %s' %hostname)
        ssh.connect(hostname=hostname, port=22, username='root', password='start123')
        stdin, stdout, stderr = ssh.exec_command('tail -1 /etc/rsyslog.conf')
        result = stdout.read().decode()
        print('执行结果；%s' %result)
        if -1 == result.rfind('112.35.69.28') or result is None:
            stdin, stdout, stderr = ssh.exec_command('echo "*.* @112.35.69.28:1514" >> /etc/rsyslog.conf | systemctl restart rsyslog')
            if stderr is None:
                print('%s 执行成功！' %hostname)
        ssh.close()
if __name__ == "__main__":
    methond()


#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import paramiko
import numpy as np
def methond():
    # 创建SSH对象
    ssh = paramiko.SSHClient()
    # 允许连接不在know_hosts文件中的主机，否则可能报错：paramiko.ssh_exception.SSHException: Server '192.168.43.140' not found in known_hosts
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    strh = '''172.21.69.1
172.21.69.2
172.21.69.3
172.21.69.4
172.21.69.5
172.21.69.6
172.21.69.7
172.21.69.8
172.21.69.9
172.21.69.10
172.21.69.11
172.21.69.12
172.21.69.13
172.21.69.14
172.21.69.15
172.21.69.17
172.21.69.18
172.21.69.19
172.21.69.50'''
    hosts= np.array(strh.split('\n'))
    # 连接服务器
    for hostname in hosts:
        hostname = hostname.strip()
        print('地址： %s' %hostname)
        ssh.connect(hostname=hostname, port=22, username='root', password='start123')
        stdin, stdout, stderr = ssh.exec_command('tail -1 /etc/rsyslog.conf')
        result = stdout.read().decode()
        print('查询：%s' %result)
        # echo "authpriv.info    @112.35.69.28:1514" >> /etc/rsyslog.conf | systemctl restart rsyslog
        # sed -i '$s/*.* @112.35.69.28:1514/authpriv.info    @112.35.69.28:1514/' /etc/rsyslog.conf| systemctl restart rsyslog
        if -1 == result.rfind('authpriv.info    @112.35.69.28:1514'):
            if 0<= result.rfind('*.* @112.35.69.28:1514'):
                stdin, stdout, stderr = ssh.exec_command("sed -i '$s/*.* @112.35.69.28:1514/authpriv.info    @112.35.69.28:1514/' /etc/rsyslog.conf | systemctl restart rsyslog |tail -1 /etc/rsyslog.conf")
                if stdout:
                    print('替换后结果===%s' %stdout.read().decode())
            else:
                stdin, stdout, stderr = ssh.exec_command('echo "authpriv.info    @112.35.69.28:1514" >> /etc/rsyslog.conf | systemctl restart rsyslog |tail -1 /etc/rsyslog.conf')
                if stdout:
                    print('插入新的结果集===%s' %stdout.read().decode())
        ssh.close()
if __name__ == "__main__":
    methond()


## ubuntu操作记录

### 如何取消ubuntu桌面解决方案

1. 编辑 /etc/default/grub
```conf

sudo nano /etc/default/grub

```
找到下面这一行
```

GRUB_CMDLINE_LINUX_DEFAULT="quiet splash"

```
把它改为：
```

GRUB_CMDLINE_LINUX_DEFAULT="text"

```
更新 Grub:
```sh
sudo update-grub
```

No need to remove / disable lightdm upstart conf, it already does that for you.

lightdm.conf

# Check kernel command-line for inhibitors, unless we are being called

        # manually

        for ARG in $(cat /proc/cmdline); do

            if [ "$ARG" = "text" ]; then

                plymouth quit || :

                stop

                exit 0

            fi

        done

You will still be able to use X by typing startx after you logged in.

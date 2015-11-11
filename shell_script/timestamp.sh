#!/bin/bash
now="$(date --date='9 hour' +'%Y-%m-%d %H:%M:%S')"
printf "$now" > /home/ubuntu/shell/timestamp.out

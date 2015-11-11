#!/bin/bash -f
var=$(cat /var/www/html/shell_script/image_flag.out)
if [ $var -eq 0 ]
then
sudo rm -rf /var/www/html/images/*
sudo cp -R /home/ubuntu/hipi/templateMatchingHadoop/car_num/* /var/www/html/images/
sudo chmod -R 777 /var/www/html/images/*
$(echo "1" > /var/www/html/shell_script/image_flag.out)
fi
ls /var/www/html/images/

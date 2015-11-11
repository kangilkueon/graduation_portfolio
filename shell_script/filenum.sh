#!bin/sh
FILECOUNT=0

FILECOUNT=$(ls /home/ubuntu/hipi/templateMatchingHadoop/car_num | wc -l)
echo $FILECOUNT

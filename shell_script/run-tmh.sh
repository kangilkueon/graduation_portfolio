#!/bin/bash
$(echo "1" > /var/www/html/shell_script/hadoop_launching.out)
$(/home/ubuntu/hadoop/bin/hadoop jar /home/ubuntu/hipi/tool/hibimport.jar /home/ubuntu/hipi/templateMatchingHadoop/car_num /user/ubuntu/project/input.hib)
$(/home/ubuntu/hadoop/bin/hadoop fs -rm -R -skipTrash /user/ubuntu/project/output)
$(/home/ubuntu/hadoop/bin/hadoop jar /home/ubuntu/hipi/templateMatchingHadoop/templateMatchingHadoop.jar project/input.hib /user/ubuntu/project/output)
$(/home/ubuntu/hadoop/bin/hdfs dfs -cat /user/ubuntu/project/output/part-r-00000)
$(echo "0" > /var/www/html/shell_script/hadoop_launching.out)
$(echo "0" > /var/www/html/shell_script/hadooptest.out)

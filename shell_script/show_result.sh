#/home/ubuntu/hadoop/bin/hdfs dfs -cat project/output/part-r-00000 > /home/ubuntu/result.out
#cat /home/ubuntu/result.out
#!/bin/bash
var=$(cat /var/www/html/shell_script/hadooptest.out)
if [ $var -eq 0 ]
then
  #cat /home/ubuntu/result.out
    $(/home/ubuntu/hadoop/bin/hadoop fs -rm -R -skipTrash /user/ubuntu/project/final)
    $(/home/ubuntu/hadoop/bin/hadoop jar /home/ubuntu/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.0.jar wordcount project/output/part-r-00000 project/final)
    $(/home/ubuntu/hadoop/bin/hdfs dfs -cat project/final/part-r-00000 > /home/ubuntu/result.out)
    $(echo "1" > /var/www/html/shell_script/hadooptest.out)
fi
#/home/ubuntu/hadoop/bin/hadoop fs -rm -R -skipTrash /user/ubuntu/project/final
#/home/ubuntu/hadoop/bin/hadoop jar /home/ubuntu/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.0.jar wordcount project/output/part-r-00000 project/final
#/home/ubuntu/hadoop/bin/hdfs dfs -cat project/final/part-r-00000 > /home/ubuntu/result.out
cat /home/ubuntu/result.out

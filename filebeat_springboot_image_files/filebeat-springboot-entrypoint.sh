echo "start filebeat now ..."
nohup /opt/filebeat-6.2.2-linux-x86_64/filebeat -e -c /opt/filebeat-6.2.2-linux-x86_64/filebeat.yml >/dev/null 2>&1 &
echo "start springboot jar now ..."
java -jar $1

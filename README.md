### Usage

    java -jar mqtt-tester-<ver>.jar -h localhost -p 1883


### Zabbix configuration

#### User parameter config file

1. create /etc/zabbix/zabbix_agentd.d/userparameter_mqtt.conf
with this content:


    # mqtt tester user parameter
    UserParameter=mqtt.tester[*],java -jar /opt/smart-platform/mqtt-tester-1.0-SNAPSHOT.jar -h localhost -p $1


2. restart zabbix agent


3. test with zabbix_get


    zabbix_get -s 127.0.0.1 -p 10050 -k mqtt.tester[1883]


4. finalize the configuration from zabbix web console


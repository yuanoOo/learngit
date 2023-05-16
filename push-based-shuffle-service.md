- Spark-default.yaml
```yaml
# Spark ESS: with push-based shuffle service
#spark.shuffle.useOldFetchProtocol=true
spark.shuffle.service.name=spark3_shuffle
spark.shuffle.service.port=7773
spark.shuffle.push.enabled=true
spark.shuffle.push.mergersMinStaticThreshold=5
```

- yarn-site.xml

```xml
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle,spark_shuffle,spark3_shuffle</value>
    </property>


    <property>
        <name>yarn.nodemanager.aux-services.spark_shuffle.class</name>
        <value>org.apache.spark.network.yarn.YarnShuffleService</value>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services.spark3_shuffle.class</name>
        <value>org.apache.spark.network.yarn.YarnShuffleService</value>
    </property>

    <property>
        <name>yarn.nodemanager.aux-services.spark_shuffle.classpath</name>
        <value>/opt/spark/yarn/*:/opt/hadoop/etc/hadoop/ess/spark-2-config</value>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services.spark3_shuffle.classpath</name>
        <value>/opt/spark3/yarn/*:/opt/hadoop/etc/hadoop/ess/spark-3-config</value>
    </property>

    <property>
        <name>spark.shuffle.push.server.mergedShuffleFileManagerImpl</name>
        <value>org.apache.spark.network.shuffle.RemoteBlockPushResolver</value>
    </property>
```

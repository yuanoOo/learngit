## Data Engine

- Kyuubi:为Kyuubi开发了Kylin JDBC-Engine模块，为Kylin添加了Hive协议。公司的BI系统PowerBI、Congons不支持连接Kylin，
  现在BI系统可以直接通过Hive协议连接Kylin。


### use case
- paimon
基于Paimon加速数据仓库：由于制造业公司海量的数据都来自Oracle数据库。并且这些海量数据一般是不断变化的，即不断的更新。
这与互联网海量不可变的log数据所带来的挑战截然不同。 
主要挑战：传统的数据仓库Hive不支持数据的更新，想要在Hive数据仓库上加速ODS层，几乎是不可能的。
因此我们选择了Apache Paimon数据湖表格式来加速数据仓库。
而在此前我们还调研了数据湖Hudi、Iceberg，在我们的测试中，都不尽如意。Hudi系统设计复杂，代码结构混乱，就是由于其
设计问题，导致Hudi的Bug满天飞，不符合要求。而Iceberg并不擅长实时更新的场景，特别在
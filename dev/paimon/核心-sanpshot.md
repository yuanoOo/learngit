## snapshot快照是paimon写入的核心概念
- paimon一次commit最多产生两个snapshot，必定有一个Data的快照，或许有一个compactor压缩的快照。
- 在Flink中一次ck，进行一次commit，commit后数据可见。

- 快照和manifest清单好像是有紧密的关系：TODO：需要弄清两者的关系

org.apache.paimon.Snapshot
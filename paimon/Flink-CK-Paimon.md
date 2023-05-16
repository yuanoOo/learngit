## Paimon Flink CK流程
我们知道Flink paimon写入主要涉及两个算子：
- 1、org.apache.paimon.flink.sink.RowDataStoreWriteOperator
这个算子实现了org.apache.flink.streaming.api.operators.StreamOperator#prepareSnapshotPreBarrier方法，这个方法会在算子接受到   
driver的CK协调后，进行调用，而paimon-flink-sink会在这个方法中先后面的commit算子发送committable信息。   
调用过程中会调用prepareCommit方法，而在这个方法的实现中，最终会调用org.apache.paimon.mergetree.MergeTreeWriter#prepareCommit
最终会在Flink CK的时候，进行数据的落盘的操作，即Flink每次CK的时候，都会保证数据写入到文件系统。

2、CommitterOperator
这个算子实现了org.apache.flink.api.common.state.CheckpointListener#notifyCheckpointComplete接口中的方法，
会在FLink CK完成后进行调用，paimon-flink-sink在这个方法中会进行snapshot文件的提交，包括snapshot、manifest文件。

```scala
org.apache.paimon.flink.sink.PrepareCommitOperator#prepareSnapshotPreBarrier

@Override
public void prepareSnapshotPreBarrier(long checkpointId) throws Exception {
  if (!endOfInput) {
    emitCommittables(false, checkpointId);
  }
  // no records are expected to emit after endOfInput
}

@Override
public void endInput() throws Exception {
  endOfInput = true;
  emitCommittables(true, Long.MAX_VALUE);
}

private void emitCommittables(boolean doCompaction, long checkpointId) throws IOException {
  prepareCommit(doCompaction, checkpointId)
    .forEach(committable -> output.collect(new StreamRecord<>(committable)));
}

protected abstract List<Committable> prepareCommit(boolean doCompaction, long checkpointId) throws IOException;
```

## Paimon-Flink-Sink

### 构建Paimon Sink Flink DAG源码流程

入口类以及对应的入口方法为：org.apache.paimon.flink.sink.FlinkSinkBuilder#build，
进入这个方法中看看，发现会先对DataStream进行，按照分桶进行分区转换

```java
org.apache.paimon.flink.sink.FlinkSinkBuilder#build

public DataStreamSink<?> build() {
    BucketingStreamPartitioner<RowData> partitioner =
            new BucketingStreamPartitioner<>(
                    new RowDataChannelComputer(table.schema(), logSinkFunction != null));
    PartitionTransformation<RowData> partitioned =
            new PartitionTransformation<>(input.getTransformation(), partitioner);
    if (parallelism != null) {
        partitioned.setParallelism(parallelism);
    }

    StreamExecutionEnvironment env = input.getExecutionEnvironment();
    // 构建Flink paimon sink DAG类
    FileStoreSink sink =
            new FileStoreSink(table, lockFactory, overwritePartition, logSinkFunction);
    return commitUser != null && sinkProvider != null
            ? sink.sinkFrom(new DataStream<>(env, partitioned), commitUser, sinkProvider)
            : sink.sinkFrom(new DataStream<>(env, partitioned));
}
```
 
- 1、createWriteOperator：实际进行写入Record的算子, org.apache.paimon.flink.sink.RowDataStoreWriteOperator
  org.apache.paimon.flink.AbstractFlinkTableFactory#buildPaimonTable

- 2、CommitterOperator：CK时进行snapshot commit的地方，保证数据可见性。

```java
org.apache.paimon.flink.sink.FlinkSink#sinkFrom(org.apache.flink.streaming.api.datastream.DataStream<T>, java.lang.String, org.apache.paimon.flink.sink.StoreSinkWrite.Provider);

public DataStreamSink<?> sinkFrom(
  DataStream<T> input, String commitUser, StoreSinkWrite.Provider sinkProvider) {
  StreamExecutionEnvironment env = input.getExecutionEnvironment();
  ReadableConfig conf = StreamExecutionEnvironmentUtils.getConfiguration(env);
  CheckpointConfig checkpointConfig = env.getCheckpointConfig();

  boolean isStreaming =
    conf.get(ExecutionOptions.RUNTIME_MODE) == RuntimeExecutionMode.STREAMING;
  boolean streamingCheckpointEnabled =
    isStreaming && checkpointConfig.isCheckpointingEnabled();
  if (streamingCheckpointEnabled) {
    assertCheckpointConfiguration(env);
  }

  CommittableTypeInfo typeInfo = new CommittableTypeInfo();
  SingleOutputStreamOperator<Committable> written =
    input.transform(
      WRITER_NAME + " -> " + table.name(),
      typeInfo,
      createWriteOperator(sinkProvider, isStreaming, commitUser))
      .setParallelism(input.getParallelism());

  SingleOutputStreamOperator<?> committed =
    written.transform(
      GLOBAL_COMMITTER_NAME + " -> " + table.name(),
      typeInfo,
      new CommitterOperator(
        streamingCheckpointEnabled,
        commitUser,
        createCommitterFactory(streamingCheckpointEnabled),
        createCommittableStateManager()))
      .setParallelism(1)
      .setMaxParallelism(1);
  return committed.addSink(new DiscardingSink<>()).name("end").setParallelism(1);
}
```


### 1、createWriteOperator
```scala
private StoreSinkWrite.Provider createWriteProvider(CheckpointConfig checkpointConfig) {
    boolean waitCompaction;

    if (table.coreOptions().writeOnly()) {
        // 如果配置为writeOnly()，则不进行在线压缩
        waitCompaction = false;
    } else {
        Options options = table.coreOptions().toConfiguration();
        ChangelogProducer changelogProducer = table.coreOptions().changelogProducer();
        // 当ChangelogProducer为LOOKUP时，则等待压缩
        waitCompaction =
                changelogProducer == ChangelogProducer.LOOKUP
                        && options.get(CHANGELOG_PRODUCER_LOOKUP_WAIT);
        
        // 决定FULL_COMPACTION的压缩间隔
        int deltaCommits = -1;
        if (options.contains(FULL_COMPACTION_DELTA_COMMITS)) {
            deltaCommits = options.get(FULL_COMPACTION_DELTA_COMMITS);
        } else if (options.contains(CHANGELOG_PRODUCER_FULL_COMPACTION_TRIGGER_INTERVAL)) {
            long fullCompactionThresholdMs =
                    options.get(CHANGELOG_PRODUCER_FULL_COMPACTION_TRIGGER_INTERVAL).toMillis();
            deltaCommits =
                    (int)
                            (fullCompactionThresholdMs
                                    / checkpointConfig.getCheckpointInterval());
        }
        
        // Generate changelog files with each full compaction
        // 当进行FULL_COMPACTION的时候，需要生成changelog files
        if (changelogProducer == ChangelogProducer.FULL_COMPACTION || deltaCommits >= 0) {
            int finalDeltaCommits = Math.max(deltaCommits, 1);
            return (table, commitUser, state, ioManager) ->
                    new GlobalFullCompactionSinkWrite(
                            table,
                            commitUser,
                            state,
                            ioManager,
                            isOverwrite,
                            waitCompaction,
                            finalDeltaCommits);
        }
    }

    return (table, commitUser, state, ioManager) ->
            new StoreSinkWriteImpl(
                    table, commitUser, state, ioManager, isOverwrite, waitCompaction);
}
```



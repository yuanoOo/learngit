## Paimon dynamic bucket设计与实现

- 实现集中在paimon-core/src/main/java/org/apache/paimon/index包中：

- 入口类
org.apache.paimon.index.HashBucketAssigner#HashBucketAssigner是实现的入口类，被
org.apache.paimon.flink.sink.HashBucketAssignerOperator#initializeState方法调用。

找到了入口类，接下来就是一步步阅读源码，理清逻辑了

```java
    @Override
    public void initializeState(StateInitializationContext context) throws Exception {
        super.initializeState(context);

        // Each job can only have one user name and this name must be consistent across restarts.
        // We cannot use job id as commit user name here because user may change job id by creating
        // a savepoint, stop the job and then resume from savepoint.
        String commitUser =
                StateUtils.getSingleValueFromState(
                        context, "commit_user_state", String.class, initialCommitUser);
        
        // 初始化bucket分配器，因此HashBucketAssigner是入口类
        this.assigner =
                new HashBucketAssigner(
                        table.snapshotManager(),
                        commitUser,
                        table.store().newIndexFileHandler(),
                        getRuntimeContext().getNumberOfParallelSubtasks(),
                        getRuntimeContext().getIndexOfThisSubtask(),
                        table.coreOptions().dynamicBucketTargetRowNum());
        this.extractor = extractorFunction.apply(table.schema());
    }
    
        @Override
    public void processElement(StreamRecord<T> streamRecord) throws Exception {
        T value = streamRecord.getValue();
        
        // 通过调用assign方法，获取每一个record对应的bucket
        int bucket =
                assigner.assign(
                        extractor.partition(value), extractor.trimmedPrimaryKey(value).hashCode());
        output.collect(new StreamRecord<>(new Tuple2<>(value, bucket)));
    }
```

- HashBucketAssigner
```java
    public HashBucketAssigner(
            SnapshotManager snapshotManager,
            String commitUser,
            IndexFileHandler indexFileHandler,
            int numAssigners,
            int assignId,
            long targetBucketRowNumber) {
        this.snapshotManager = snapshotManager;
        this.commitUser = commitUser;
        this.indexFileHandler = indexFileHandler;
        this.numAssigners = numAssigners;
        this.assignId = assignId; // getRuntimeContext().getIndexOfThisSubtask()
        this.targetBucketRowNumber = targetBucketRowNumber;
        this.partitionIndex = new HashMap<>();
    }

    /** Assign a bucket for key hash of a record. */
    public int assign(BinaryRow partition, int hash) {
        // hash: Record主键的hashcode，唯一确认一个Record
        int recordAssignId = computeAssignId(hash);
        // 可能是因为，Flink DAG前面已经通过主键的hashcode % channels了，所以一定相等
        checkArgument(
                recordAssignId == assignId,
                "This is a bug, record assign id %s should equal to assign id %s.",
                recordAssignId,
                assignId);
        
        // PartitionIndex: Bucket Index Per Partition.
        // 为每一个partition计算对应的Bucket Index
        PartitionIndex index = partitionIndex.computeIfAbsent(partition, this::loadIndex);
        return index.assign(hash, (bucket) -> computeAssignId(bucket) == assignId);
    }

    private int computeAssignId(int hash) {
        // numAssigners: getRuntimeContext().getNumberOfParallelSubtasks()
        return Math.abs(hash % numAssigners);
    }
```

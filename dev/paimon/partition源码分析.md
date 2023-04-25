
## 删除过期的分区路径
```java
org.apache.paimon.operation.FileStoreExpireImpl#tryDeleteDirectories

// Map<BinaryRow, Set<Integer>> changedBuckets: partition -> buckets
private void tryDeleteDirectories(Map<BinaryRow, Set<Integer>> changedBuckets) {
        // All directory paths are deduplicated and sorted by hierarchy level
        Map<Integer, Set<Path>> deduplicate = new HashMap<>();
        for (Map.Entry<BinaryRow, Set<Integer>> entry : changedBuckets.entrySet()) {
            // try to delete bucket directories
            for (Integer bucket : entry.getValue()) {
                tryDeleteEmptyDirectory(pathFactory.bucketPath(entry.getKey(), bucket));
            }

            List<Path> hierarchicalPaths = pathFactory.getHierarchicalPartitionPath(entry.getKey());
            int hierarchies = hierarchicalPaths.size();
                if (hierarchies == 0) {
                continue;
            }

            if (tryDeleteEmptyDirectory(hierarchicalPaths.get(hierarchies - 1))) {
            // deduplicate high level partition directories
                for (int hierarchy = 0; hierarchy < hierarchies - 1; hierarchy++) {
                    Path path = hierarchicalPaths.get(hierarchy);
                    deduplicate.computeIfAbsent(hierarchy, i -> new HashSet<>()).add(path);
                }
            }
        }

        // from deepest to shallowest
        for (int hierarchy = deduplicate.size() - 1; hierarchy >= 0; hierarchy--) {
            deduplicate.get(hierarchy).forEach(this::tryDeleteEmptyDirectory);
        }
}
```
```java
org.apache.paimon.utils.FileStorePathFactory#getHierarchicalPartitionPath

public List<Path> getHierarchicalPartitionPath(BinaryRow partition) {
        return PartitionPathUtils.generateHierarchicalPartitionPaths(
            partitionComputer.generatePartValues(
                Preconditions.checkNotNull(partition,"Partition binary row is null. This is unexpected.")))
        .stream()
        .map(p -> new Path(root + "/" + p))
        .collect(Collectors.toList());
}
```


```java
org.apache.paimon.utils.PartitionPathUtils#generateHierarchicalPartitionPaths
/**
 * Generate all hierarchical paths from partition spec.
 *
 * <p>For example, if the partition spec is (pt1: '0601', pt2: '12', pt3: '30'), this method
 * will return a list (start from index 0):
 *
 * <ul>
 *   <li>pt1=0601
 *   <li>pt1=0601/pt2=12
 *   <li>pt1=0601/pt2=12/pt3=30
 * </ul>
 */
public static List<String> generateHierarchicalPartitionPaths(
        LinkedHashMap<String, String> partitionSpec) {
        List<String> paths = new ArrayList<>();
        if (partitionSpec.isEmpty()) {
            return paths;
        }
        StringBuilder suffixBuf = new StringBuilder();
        for (Map.Entry<String, String> e : partitionSpec.entrySet()) {
            suffixBuf.append(escapePathName(e.getKey()));
            suffixBuf.append('=');
            suffixBuf.append(escapePathName(e.getValue()));
            suffixBuf.append(Path.SEPARATOR);
            paths.add(suffixBuf.toString());
        }
        return paths;
}
```

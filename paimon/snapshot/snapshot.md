## Snapshot文件夹

### snapshot file
- baseManifestList
a manifest list recording all changes from the previous snapshots
一个记录了以前快照中所有变化的清单

- deltaManifestList
a manifest list recording all new changes occurred in this snapshot for faster expire and streaming reads
记录该快照中发生的所有新变化的清单，以便更快地过期和流式读取

- changelogManifestList
a manifest list recording all changelog produced in this snapshot null if no changelog is produced, or for paimon <= 0.2
记录该快照中产生的所有更新日志的清单，如果没有产生更新日志则为空，如果paimon<=0.2则为空。

- commitIdentifier
主要用于快照去重。
如果多个快照有相同的commitIdentifier，从任何一个快照中读取都必须产生相同的表。   
如果快照A的commitIdentifier比快照B小，那么快照A必须在快照B之前提交，因此快照A必须包含比快照B更早的记录。

```json
{
  "version" : 3,
  "id" : 3540,
  "schemaId" : 0,
  "baseManifestList" : "manifest-c924909a-d786-4c5f-8587-11f8c6233b41-6962",
  "deltaManifestList" : "manifest-c924909a-d786-4c5f-8587-11f8c6233b41-6963",
  "changelogManifestList" : null,
  "commitUser" : "cbda2750-6553-4e10-8952-7024246b166f",
  "commitIdentifier" : 2863,
  "commitKind" : "COMPACT",
  "timeMillis" : 1683687697960,
  "logOffsets" : { },
  "totalRecordCount" : 6782639004,
  "deltaRecordCount" : 11830118,
  "changelogRecordCount" : 0,
  "watermark" : -9223372036854775808
}
```
### 记得
snapshot文件中的manifest是ManifestList:由一条条ManifestFileMeta组成，代表当前snapshot状态下，该表所有的data。   
而在ManifestList文件中的一条条ManifestFileMeta里，又指向一个个ManifestFile文件。    
而ManifestFile指向一个个变化的文件。

snapshot -> ManifestList -> ManifestFile -> file-change

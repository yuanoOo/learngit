## manifest文件夹

### org.apache.paimon.manifest.ManifestFile
This file includes several ManifestEntrys, representing the additional changes since last snapshot.

- org.apache.paimon.manifest.ManifestEntry
Entry of a manifest file, representing an addition / deletion of a data file.

### org.apache.paimon.manifest.ManifestList
This file includes several ManifestFileMeta, representing all data of the whole table at the corresponding snapshot.

- org.apache.paimon.manifest.ManifestFileMeta
Metadata of a manifest file.

## 后续
在manifest文件夹中，存在两种类型的manifest文件：
- ManifestFile：由一条条ManifestEntry组成，代表自last snapshot后增量变化.
```json
{
  "org.apache.paimon.avro.generated.record": {
    "_VERSION": 2,
    "_KIND": 0,
    "_PARTITION": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\u0010\u0000\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
    "_BUCKET": 0,
    "_TOTAL_BUCKETS": 2,
    "_FILE": {
      "org.apache.paimon.avro.generated.record__FILE": {
        "_FILE_NAME": "data-edcde986-bd39-4935-a2e8-07c5d2bb6cd4-2.parquet",
        "_FILE_SIZE": 592826,
        "_ROW_COUNT": 14650,
        "_MIN_KEY": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000OÓ>\u0001\u0000\u0000\u0000",
        "_MAX_KEY": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000O«\f?\u0001\u0000\u0000\u0000",
        "_KEY_STATS": {
          "org.apache.paimon.avro.generated.record__FILE__KEY_STATS": {
            "_MIN_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000OÓ>\u0001\u0000\u0000\u0000",
            "_MAX_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000O«\f?\u0001\u0000\u0000\u0000",
            "_NULL_COUNTS": {
              "array": [
                {
                  "long": 0
                }
              ]
            }
          }
        },
        "_VALUE_STATS": {
          "org.apache.paimon.avro.generated.record__FILE__VALUE_STATS": {
            "_MIN_VALUES": "\u0000\u0000\u0000-\u0000\u0000\u0000\u0000\u0000ø\u0003\u0000OÓ>\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000p\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000UNKNOWND74K\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u000b\u0000\u0000\u0000\u0001\u0000\u0000A-OTA\u0000\u00001\u0000\u0000\u0000\u0000\u0000\u00000.0.16\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 \u0001\u0000\u0000AUTO\u0000\u0000\u0000-0.079\u0000-0.05\u0000\u0000-0.075\u0000ÿÿÿÿÿÿÿÿ\u0000\u0000\u0000\u0000¨\u0001\u0000\u0000ÿÿÿÿÿÿÿÿ\u0000\u0000\u0000\u0000°\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000¸\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000À\u0001\u0000\u0000C00329HM4V\u0000\u0000\u0000\u0000\u0000\u0000\blßÿ\u0001\u0000\u0000\blßÿ\u0001\u0000\u0000B03-1FT-09E\u0000\u0000\u0000\u0000\u0000\blßÿ\u0001\u0000\u0000àê¬\u0003\u0001\u0000\u0000pTM\u0005\u0001\u0000\u0000*½ÿ\u0001\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
            "_MAX_VALUES": "\u0000\u0000\u0000-\u0000\u0000\u0000\u0000\u0000ø\u0003\u0000O«\f?\u0001\u0000\u0000\u00006¦_{\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000p\u0001\u0000\u0000ó®_\u0000\u0000\u0000\u0000\u0000\u0010\u0000\u0000\u0000\u0001\u0000\u0000\n\u0000\u0000\u0000\u0001\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 \u0001\u0000\u0000\u0000\u0000\u0000\u0000¨\u0001\u0000\u0000\n\u0000\u0000\u0000°\u0001\u0000\u0000\u000b\u0000\u0000\u0000À\u0001\u0000\u000092\u0000\u0000\u0000\u0000\u0000\u0016\u0000\u0000\u0000Ð\u0001\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000è\u0001\u0000\u0000MANUAL\u0000999\u0000\u0000\u0000\u0000NA\u0000\u0000\u0000\u0000\u0000NA\u0000\u0000\u0000\u0000\u0000ÿÿÿÿÿÿÿÿ\u0000\u0000\u0000\u0000ð\u0001\u0000\u0000ÿÿÿÿÿÿÿÿ\u0000\u0000\u0000\u0000ø\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0002\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\b\u0002\u0000\u0000YVVF7W70QM\u0000\u0000\u0000\u0000\u0000\u0000WQC-002133002413D74_STROBE\u0000\u0000\u0000\u0000\u0000\u0000Ø7H\u0005\u0001\u0000\u0000Ø7H\u0005\u0001\u0000\u0000L03-3FT-20\u0000\u0000\u0000\u0000\u0000\u0000WIFI-BT-OTA\u0000\u0000\u0000\u0000\u0000com.apple.PurpleRabbit\u0000\u0000Ø7H\u0005\u0001\u0000\u0000øäM\u0005\u0001\u0000\u0000øäM\u0005\u0001\u0000\u0000\u0000u\u001a\u0005\u0001\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
            "_NULL_COUNTS": {
              "array": [
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 14535
                },
                {
                  "long": 14535
                },
                {
                  "long": 14535
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 14650
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                },
                {
                  "long": 0
                }
              ]
            }
          }
        },
        "_MIN_SEQUENCE_NUMBER": 2998674,
        "_MAX_SEQUENCE_NUMBER": 3013482,
        "_SCHEMA_ID": 0,
        "_LEVEL": 0,
        "_EXTRA_FILES": [],
        "_CREATION_TIME": {
          "long": 1683687516800
        }
      }
    }
  }
}
```
- ManifestList:由一条条ManifestFileMeta组成，代表当前snapshot状态下，该表所有的data。
```json
{
  "org.apache.paimon.avro.generated.record": {
    "_VERSION": 2,
    "_FILE_NAME": "manifest-aebb0d5b-8300-4494-a350-fe4b4479b0c1-3121",
    "_FILE_SIZE": 29011,
    "_NUM_ADDED_FILES": 82,
    "_NUM_DELETED_FILES": 0,
    "_PARTITION_STATS": {
      "org.apache.paimon.avro.generated.record__PARTITION_STATS": {
        "_MIN_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\u0010\u0000\u0000\u00002022-05-27\u0000\u0000\u0000\u0000\u0000\u0000",
        "_MAX_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\u0010\u0000\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
        "_NULL_COUNTS": {
          "array": [
            {
              "long": 0
            }
          ]
        }
      }
    },
    "_SCHEMA_ID": 0
  }
}
{
  "org.apache.paimon.avro.generated.record": {
    "_VERSION": 2,
    "_FILE_NAME": "manifest-aebb0d5b-8300-4494-a350-fe4b4479b0c1-3122",
    "_FILE_SIZE": 2505,
    "_NUM_ADDED_FILES": 2,
    "_NUM_DELETED_FILES": 0,
    "_PARTITION_STATS": {
      "org.apache.paimon.avro.generated.record__PARTITION_STATS": {
        "_MIN_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\u0010\u0000\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
        "_MAX_VALUES": "\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n\u0000\u0000\u0000\u0010\u0000\u0000\u00002023-05-10\u0000\u0000\u0000\u0000\u0000\u0000",
        "_NULL_COUNTS": {
          "array": [
            {
              "long": 0
            }
          ]
        }
      }
    },
    "_SCHEMA_ID": 0
  }
}
```

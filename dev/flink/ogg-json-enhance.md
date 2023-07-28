## Enhance  Flink ogg-json format
- for case:"before":{}
```json
{
    "table": "ZBCDMP2_DMPDB2.R_PDCA_FFD_D37",
    "op_type": "U",
    "op_ts": "2023-07-20 21:45:34.860817",
    "current_ts": "2023-07-21T05:45:36.615000",
    "pos": "00002564940142073691",
    "before": {},
    "after": {
        "ID": 1461242,
        "WIP_ID": 836087110,
        "WIP_NO": "MCQ57V267J",
        "WO_ID": 6284576,
        "WO_NO": "WKP-002103049879",
        "CATEGORY_KEY": "D37K",
        "IS_MSR": 0,
        "REPAIR_TIMES": 0,
        "TEST_TIMES": 1,
        "TEST_RESULT": 1,
        "TEST_START_TIME": "2023-07-21 03:15:12",
        "TEST_STOP_TIME": "2023-07-21 03:19:46",
        "TEST_LINE": "K06-2FT-02",
        "TEST_STATION_CODE": "A-OTA",
        "TEST_STATION_NUM": "5",
        "SW_VERSION": "1HDwEC436_MP077_K_2306190607L1K_R_2307110942",
        "SYMPTOM_CODE": "tcRL tNRbN75c301464tpMXstcAVG_PRM om2cdDISP1d1aAdl-74mLMg-1p2p97.06ur1-12dr24-0bw10sb-1dpMscs30; ; ; ",
        "SYMPTOM_DESC": "Measurement exceed diagnositic limits.; ; ; ",
        "RETEST_FLAG": 0,
        "TIME_OUT_FLAG": 0,
        "OVERRIDE_FLAG": 0,
        "APPLE_PASS_FLAG": 0,
        "REPORT_TYPE": "AUTO",
        "TEST_VALUE": "-78.4",
        "LOWER_LIMIT": "-77",
        "UPPER_LIMIT": "-71",
        "PROPERTY_01": "tcRL tNRbN75c301464tpMXstcAVG_PRM om2cdDISP1d1aAdl-74mLMg-1p2p97.06ur1-12dr24-0bw10sb-1dpMscs30",
        "PROPERTY_02": null,
        "PROPERTY_03": null,
        "PROPERTY_04": "K06-2FT-03",
        "PROPERTY_05": "5",
        "PROPERTY_06": null,
        "PROPERTY_07": null,
        "PROPERTY_08": null,
        "PROPERTY_09": null,
        "PROPERTY_10": null,
        "ADD_BY": -1,
        "ADD_DATE": "2023-07-21 05:30:42",
        "EDIT_BY": -1,
        "EDIT_DATE": "2023-07-21 05:45:35",
        "DEL_FLAG": 0,
        "WORK_DATE": "2023-07-21 03:00:00",
        "LAST_TEST_START_TIME": null,
        "LAST_SYMPTOM_CODE": null,
        "LAST_SYMPTOM_DESC": null,
        "LAST_SYMTOM": null
    }
}
```

- 增加对for case:"before":{}的校验
org.apache.flink.formats.json.ogg.OggJsonDeserializationSchema#deserialize(byte[], org.apache.flink.util.Collector<org.apache.flink.table.data.RowData>)
``` java
@Override
public void deserialize(byte[] message, Collector<RowData> out) throws IOException {
    if (message == null || message.length == 0) {
        // skip tombstone messages
        return;
    }
    try {
        final JsonNode root = jsonDeserializer.deserializeToJsonNode(message);
        GenericRowData row = (GenericRowData) jsonDeserializer.convertToRowData(root);

        GenericRowData before = (GenericRowData) row.getField(0);
        GenericRowData after = (GenericRowData) row.getField(1);
        String op = row.getField(2).toString();
        if (OP_CREATE.equals(op)) {
            after.setRowKind(RowKind.INSERT);
            emitRow(row, after, out);
        } else if (OP_UPDATE.equals(op)) {
            // for case:"before":{}
            if (!root.get("before").isEmpty()) {
                before.setRowKind(RowKind.UPDATE_BEFORE);
                emitRow(row, before, out);
            }

            after.setRowKind(RowKind.UPDATE_AFTER);
            emitRow(row, after, out);
        } else if (OP_DELETE.equals(op)) {
            if (before == null) {
                throw new IllegalStateException(
                        String.format(REPLICA_IDENTITY_EXCEPTION, "DELETE"));
            }
            before.setRowKind(RowKind.DELETE);
            emitRow(row, before, out);
        } else {
            if (!ignoreParseErrors) {
                throw new IOException(
                        format(
                                "Unknown \"op_type\" value \"%s\". The Ogg JSON message is '%s'",
                                op, new String(message)));
            }
        }
    } catch (Throwable t) {
        // a big try catch to protect the processing.
        if (!ignoreParseErrors) {
            throw new IOException(
                    format("Corrupt Ogg JSON message '%s'.", new String(message)), t);
        }
    }
}

```

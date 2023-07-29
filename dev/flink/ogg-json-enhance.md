## Enhance  Flink ogg-json format
- for case:"before":{}
The BEFORE field in ogg-json can be configured to contain no table fields, as in the ogg-json example below.
For this case, ogg-json format should not send a Record of type UPDATE_BEFORE downstream.
```json
{
  "table": "ZBZZZ",
  "op_type": "U",
  "op_ts": "2023-07-20 21:45:34.860817",
  "current_ts": "2023-07-21T05:45:36.615000",
  "pos": "00002564940142073691",
  "before": {},
  "after": {
      "ID": 1461242,
      "PROPERTY_01": "tc",
      "PROPERTY_02": null,
      "PROPERTY_03": null,
      "PROPERTY_04": "K",
      "PROPERTY_05": "5",
      "PROPERTY_06": null,
      "PROPERTY_07": null,
      "PROPERTY_08": null,
      "PROPERTY_09": null,
      "PROPERTY_10": null
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

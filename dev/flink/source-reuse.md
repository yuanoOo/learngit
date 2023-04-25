set 'table.optimizer.source.report-statistics-enabled' = 'true';
set 'table.optimizer.reuse-source-enabled' = 'true';

EXECUTE STATEMENT SET
BEGIN
    insert into paimon_table_1 select name,age,city from kafka_source_1;
    insert into paimon_table_2 select name,age,city from kafka_source_1;
END;
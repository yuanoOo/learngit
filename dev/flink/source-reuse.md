set able.optimizer.source.report-statistics-enabled = true;
set table.optimizer.reuse-source-enabled = true;

EXECUTE STATEMENT SET
BEGIN
    insert into print_table_1 select name,age,city from PERSON;
    insert into print_table_2 select name,age,city from PERSON;
END;
SELECT t1.TABLE_NAME,
       t2.TABLE_COMMENT,
       t1.COLUMN_NAME,
       t1.IS_NULLABLE,
       t1.DATA_TYPE,
       t1.COLUMN_TYPE,
       t1.COLUMN_COMMENT,
       t1.CHARACTER_MAXIMUM_LENGTH,
       t1.COLUMN_KEY
FROM information_schema.COLUMNS t1,
     information_schema.TABLES t2
WHERE t1.TABLE_SCHEMA = ?
  AND t2.TABLE_NAME = t1.TABLE_NAME
GROUP BY t1.TABLE_NAME, t1.ORDINAL_POSITION
ORDER BY t1.TABLE_NAME, t1.ORDINAL_POSITION;
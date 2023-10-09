# Predicate Functions

[TOC]

## Predicate Functions

### ! expr

Logical not.

### expr1 < expr2

Returns true if `expr1` is less than `expr2`.

### expr1 <= expr2

Returns true if `expr1` is less than or equal to `expr2`.

### expr1 <=> expr2

Returns same result as the EQUAL(=) operator for non-null operands, but returns true if both are null, false if one of the them is null.

### expr1 = expr2

Returns true if `expr1` equals `expr2`, or false otherwise.

### expr1 == expr2

Returns true if `expr1` equals `expr2`, or false otherwise.

### expr1 > expr2

Returns true if `expr1` is greater than `expr2`.

### expr1 >= expr2

Returns true if `expr1` is greater than or equal to `expr2`.

### expr1 and expr2

Logical AND.

### str ilike pattern[ ESCAPE escape]

Returns true if str matches `pattern` with `escape` case-insensitively, null if any arguments are null, false otherwise.

### expr1 in(expr2, expr3, ...)

Returns true if `expr` equals to any valN.

### isnan(expr)

Returns true if `expr` is NaN, or false otherwise.

### isnotnull(expr)

Returns true if `expr` is not null, or false otherwise.

### isnull(expr)

Returns true if `expr` is null, or false otherwise.

### str like pattern[ ESCAPE escape]

Returns true if str matches `pattern` with `escape`, null if any arguments are null, false otherwise.

### not expr

Logical not.

### expr1 or expr2

Logical OR.

### regexp(str, regexp)

Returns true if `str` matches `regexp`, or false otherwise.

### regexp_like(str, regexp)

Returns true if `str` matches `regexp`, or false otherwise.

### rlike(str, regexp)

Returns true if `str` matches `regexp`, or false otherwise.

## Examples

```sql
-- !
SELECT ! true;
+----------+
|(NOT true)|
+----------+
|     false|
+----------+

SELECT ! false;
+-----------+
|(NOT false)|
+-----------+
|       true|
+-----------+

SELECT ! NULL;
+----------+
|(NOT NULL)|
+----------+
|      null|
+----------+

-- <
SELECT 1 < 2;
+-------+
|(1 < 2)|
+-------+
|   true|
+-------+

SELECT 1.1 < '1';
+---------+
|(1.1 < 1)|
+---------+
|    false|
+---------+

SELECT to_date('2009-07-30 04:17:52') < to_date('2009-07-30 04:17:52');
+-------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) < to_date(2009-07-30 04:17:52))|
+-------------------------------------------------------------+
|                                                        false|
+-------------------------------------------------------------+

SELECT to_date('2009-07-30 04:17:52') < to_date('2009-08-01 04:17:52');
+-------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) < to_date(2009-08-01 04:17:52))|
+-------------------------------------------------------------+
|                                                         true|
+-------------------------------------------------------------+

SELECT 1 < NULL;
+----------+
|(1 < NULL)|
+----------+
|      null|
+----------+

-- <=
SELECT 2 <= 2;
+--------+
|(2 <= 2)|
+--------+
|    true|
+--------+

SELECT 1.0 <= '1';
+----------+
|(1.0 <= 1)|
+----------+
|      true|
+----------+

SELECT to_date('2009-07-30 04:17:52') <= to_date('2009-07-30 04:17:52');
+--------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) <= to_date(2009-07-30 04:17:52))|
+--------------------------------------------------------------+
|                                                          true|
+--------------------------------------------------------------+

SELECT to_date('2009-07-30 04:17:52') <= to_date('2009-08-01 04:17:52');
+--------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) <= to_date(2009-08-01 04:17:52))|
+--------------------------------------------------------------+
|                                                          true|
+--------------------------------------------------------------+

SELECT 1 <= NULL;
+-----------+
|(1 <= NULL)|
+-----------+
|       null|
+-----------+

-- <=>
SELECT 2 <=> 2;
+---------+
|(2 <=> 2)|
+---------+
|     true|
+---------+

SELECT 1 <=> '1';
+---------+
|(1 <=> 1)|
+---------+
|     true|
+---------+

SELECT true <=> NULL;
+---------------+
|(true <=> NULL)|
+---------------+
|          false|
+---------------+

SELECT NULL <=> NULL;
+---------------+
|(NULL <=> NULL)|
+---------------+
|           true|
+---------------+

-- =
SELECT 2 = 2;
+-------+
|(2 = 2)|
+-------+
|   true|
+-------+

SELECT 1 = '1';
+-------+
|(1 = 1)|
+-------+
|   true|
+-------+

SELECT true = NULL;
+-------------+
|(true = NULL)|
+-------------+
|         null|
+-------------+

SELECT NULL = NULL;
+-------------+
|(NULL = NULL)|
+-------------+
|         null|
+-------------+

-- ==
SELECT 2 == 2;
+-------+
|(2 = 2)|
+-------+
|   true|
+-------+

SELECT 1 == '1';
+-------+
|(1 = 1)|
+-------+
|   true|
+-------+

SELECT true == NULL;
+-------------+
|(true = NULL)|
+-------------+
|         null|
+-------------+

SELECT NULL == NULL;
+-------------+
|(NULL = NULL)|
+-------------+
|         null|
+-------------+

-- >
SELECT 2 > 1;
+-------+
|(2 > 1)|
+-------+
|   true|
+-------+

SELECT 2 > 1.1;
+-------+
|(2 > 1)|
+-------+
|   true|
+-------+

SELECT to_date('2009-07-30 04:17:52') > to_date('2009-07-30 04:17:52');
+-------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) > to_date(2009-07-30 04:17:52))|
+-------------------------------------------------------------+
|                                                        false|
+-------------------------------------------------------------+

SELECT to_date('2009-07-30 04:17:52') > to_date('2009-08-01 04:17:52');
+-------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) > to_date(2009-08-01 04:17:52))|
+-------------------------------------------------------------+
|                                                        false|
+-------------------------------------------------------------+

SELECT 1 > NULL;
+----------+
|(1 > NULL)|
+----------+
|      null|
+----------+

-- >=
SELECT 2 >= 1;
+--------+
|(2 >= 1)|
+--------+
|    true|
+--------+

SELECT 2.0 >= '2.1';
+------------+
|(2.0 >= 2.1)|
+------------+
|       false|
+------------+

SELECT to_date('2009-07-30 04:17:52') >= to_date('2009-07-30 04:17:52');
+--------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) >= to_date(2009-07-30 04:17:52))|
+--------------------------------------------------------------+
|                                                          true|
+--------------------------------------------------------------+

SELECT to_date('2009-07-30 04:17:52') >= to_date('2009-08-01 04:17:52');
+--------------------------------------------------------------+
|(to_date(2009-07-30 04:17:52) >= to_date(2009-08-01 04:17:52))|
+--------------------------------------------------------------+
|                                                         false|
+--------------------------------------------------------------+

SELECT 1 >= NULL;
+-----------+
|(1 >= NULL)|
+-----------+
|       null|
+-----------+

-- and
SELECT true and true;
+---------------+
|(true AND true)|
+---------------+
|           true|
+---------------+

SELECT true and false;
+----------------+
|(true AND false)|
+----------------+
|           false|
+----------------+

SELECT true and NULL;
+---------------+
|(true AND NULL)|
+---------------+
|           null|
+---------------+

SELECT false and NULL;
+----------------+
|(false AND NULL)|
+----------------+
|           false|
+----------------+

-- ilike
SELECT ilike('Spark', '_Park');
+-------------------+
|ilike(Spark, _Park)|
+-------------------+
|               true|
+-------------------+

SET spark.sql.parser.escapedStringLiterals=true;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....| true|
+--------------------+-----+

SELECT '%SystemDrive%\Users\John' ilike '\%SystemDrive\%\\users%';
+--------------------------------------------------------+
|ilike(%SystemDrive%\Users\John, \%SystemDrive\%\\users%)|
+--------------------------------------------------------+
|                                                    true|
+--------------------------------------------------------+

SET spark.sql.parser.escapedStringLiterals=false;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....|false|
+--------------------+-----+

SELECT '%SystemDrive%\\USERS\\John' ilike '\%SystemDrive\%\\\\Users%';
+--------------------------------------------------------+
|ilike(%SystemDrive%\USERS\John, \%SystemDrive\%\\Users%)|
+--------------------------------------------------------+
|                                                    true|
+--------------------------------------------------------+

SELECT '%SystemDrive%/Users/John' ilike '/%SYSTEMDrive/%//Users%' ESCAPE '/';
+--------------------------------------------------------+
|ilike(%SystemDrive%/Users/John, /%SYSTEMDrive/%//Users%)|
+--------------------------------------------------------+
|                                                    true|
+--------------------------------------------------------+

-- in
SELECT 1 in(1, 2, 3);
+----------------+
|(1 IN (1, 2, 3))|
+----------------+
|            true|
+----------------+

SELECT 1 in(2, 3, 4);
+----------------+
|(1 IN (2, 3, 4))|
+----------------+
|           false|
+----------------+

SELECT named_struct('a', 1, 'b', 2) in(named_struct('a', 1, 'b', 1), named_struct('a', 1, 'b', 3));
+----------------------------------------------------------------------------------+
|(named_struct(a, 1, b, 2) IN (named_struct(a, 1, b, 1), named_struct(a, 1, b, 3)))|
+----------------------------------------------------------------------------------+
|                                                                             false|
+----------------------------------------------------------------------------------+

SELECT named_struct('a', 1, 'b', 2) in(named_struct('a', 1, 'b', 2), named_struct('a', 1, 'b', 3));
+----------------------------------------------------------------------------------+
|(named_struct(a, 1, b, 2) IN (named_struct(a, 1, b, 2), named_struct(a, 1, b, 3)))|
+----------------------------------------------------------------------------------+
|                                                                              true|
+----------------------------------------------------------------------------------+

-- isnan
SELECT isnan(cast('NaN' as double));
+--------------------------+
|isnan(CAST(NaN AS DOUBLE))|
+--------------------------+
|                      true|
+--------------------------+

-- isnotnull
SELECT isnotnull(1);
+---------------+
|(1 IS NOT NULL)|
+---------------+
|           true|
+---------------+

-- isnull
SELECT isnull(1);
+-----------+
|(1 IS NULL)|
+-----------+
|      false|
+-----------+

-- like
SELECT like('Spark', '_park');
+----------------+
|Spark LIKE _park|
+----------------+
|            true|
+----------------+

SET spark.sql.parser.escapedStringLiterals=true;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....| true|
+--------------------+-----+

SELECT '%SystemDrive%\Users\John' like '\%SystemDrive\%\\Users%';
+-----------------------------------------------------+
|%SystemDrive%\Users\John LIKE \%SystemDrive\%\\Users%|
+-----------------------------------------------------+
|                                                 true|
+-----------------------------------------------------+

SET spark.sql.parser.escapedStringLiterals=false;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....|false|
+--------------------+-----+

SELECT '%SystemDrive%\\Users\\John' like '\%SystemDrive\%\\\\Users%';
+-----------------------------------------------------+
|%SystemDrive%\Users\John LIKE \%SystemDrive\%\\Users%|
+-----------------------------------------------------+
|                                                 true|
+-----------------------------------------------------+

SELECT '%SystemDrive%/Users/John' like '/%SystemDrive/%//Users%' ESCAPE '/';
+-----------------------------------------------------+
|%SystemDrive%/Users/John LIKE /%SystemDrive/%//Users%|
+-----------------------------------------------------+
|                                                 true|
+-----------------------------------------------------+

-- not
SELECT not true;
+----------+
|(NOT true)|
+----------+
|     false|
+----------+

SELECT not false;
+-----------+
|(NOT false)|
+-----------+
|       true|
+-----------+

SELECT not NULL;
+----------+
|(NOT NULL)|
+----------+
|      null|
+----------+

-- or
SELECT true or false;
+---------------+
|(true OR false)|
+---------------+
|           true|
+---------------+

SELECT false or false;
+----------------+
|(false OR false)|
+----------------+
|           false|
+----------------+

SELECT true or NULL;
+--------------+
|(true OR NULL)|
+--------------+
|          true|
+--------------+

SELECT false or NULL;
+---------------+
|(false OR NULL)|
+---------------+
|           null|
+---------------+

-- regexp
SET spark.sql.parser.escapedStringLiterals=true;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....| true|
+--------------------+-----+

SELECT regexp('%SystemDrive%\Users\John', '%SystemDrive%\\Users.*');
+--------------------------------------------------------+
|REGEXP(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+--------------------------------------------------------+
|                                                    true|
+--------------------------------------------------------+

SET spark.sql.parser.escapedStringLiterals=false;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....|false|
+--------------------+-----+

SELECT regexp('%SystemDrive%\\Users\\John', '%SystemDrive%\\\\Users.*');
+--------------------------------------------------------+
|REGEXP(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+--------------------------------------------------------+
|                                                    true|
+--------------------------------------------------------+

-- regexp_like
SET spark.sql.parser.escapedStringLiterals=true;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....| true|
+--------------------+-----+

SELECT regexp_like('%SystemDrive%\Users\John', '%SystemDrive%\\Users.*');
+-------------------------------------------------------------+
|REGEXP_LIKE(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+-------------------------------------------------------------+
|                                                         true|
+-------------------------------------------------------------+

SET spark.sql.parser.escapedStringLiterals=false;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....|false|
+--------------------+-----+

SELECT regexp_like('%SystemDrive%\\Users\\John', '%SystemDrive%\\\\Users.*');
+-------------------------------------------------------------+
|REGEXP_LIKE(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+-------------------------------------------------------------+
|                                                         true|
+-------------------------------------------------------------+

-- rlike
SET spark.sql.parser.escapedStringLiterals=true;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....| true|
+--------------------+-----+

SELECT rlike('%SystemDrive%\Users\John', '%SystemDrive%\\Users.*');
+-------------------------------------------------------+
|RLIKE(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+-------------------------------------------------------+
|                                                   true|
+-------------------------------------------------------+

SET spark.sql.parser.escapedStringLiterals=false;
+--------------------+-----+
|                 key|value|
+--------------------+-----+
|spark.sql.parser....|false|
+--------------------+-----+

SELECT rlike('%SystemDrive%\\Users\\John', '%SystemDrive%\\\\Users.*');
+-------------------------------------------------------+
|RLIKE(%SystemDrive%\Users\John, %SystemDrive%\\Users.*)|
+-------------------------------------------------------+
|                                                   true|
+-------------------------------------------------------+
```
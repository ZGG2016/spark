# Literals

> A literal (also known as a constant) represents a fixed data value. Spark SQL supports the following literals:

一个字面量（或者说是一个常量）表示一个固定的数据值。Spark SQL 支持如下字面量：

[TOC]

## String Literal

> A string literal is used to specify a character string value.

### Syntax

```
[ r ] { 'char [ ... ]' | "char [ ... ]" }
```

### Parameters

- char

	> One character from the character set. Use `\` to escape special characters (e.g., `'` or `\`). To represent unicode characters, use 16-bit or 32-bit unicode escape of the form `\uxxxx` or `\Uxxxxxxxx`, where `xxxx` and `xxxxxxxx` are 16-bit and 32-bit code points in hexadecimal respectively (e.g., `\u3042` for `あ` and `\U0001F44D` for 👍).

	来自字符集的一个字符。使用 `\` 来转义特殊字符（例如 `'` 或 `\`）。为表示 unicode 字符，使用 `\uxxxx` 或 `\Uxxxxxxxx` 形式的16位或32位的 unicode 转义，其中 `xxxx` 和 `xxxxxxxx` 分别是16位和32位的十六进制码位。

- r

	> Case insensitive, indicates `RAW`. If a string literal starts with `r` prefix, neither special characters nor unicode characters are escaped by `\`.
	
	大小写不敏感，表示 `RAW`. 如果一个字符串字面量以 `r` 前缀开始，特殊字符或 unicode 字符都不会被 `\` 转义。

### Examples

```sql
SELECT 'Hello, World!' AS col;
+-------------+
|          col|
+-------------+
|Hello, World!|
+-------------+

SELECT "SPARK SQL" AS col;
+---------+
|      col|
+---------+
|Spark SQL|
+---------+

SELECT 'it\'s $10.' AS col;
+---------+
|      col|
+---------+
|It's $10.|
+---------+

SELECT r"'\n' represents newline character." AS col;
+----------------------------------+
|                               col|
+----------------------------------+
|'\n' represents newline character.|
+----------------------------------+
```

## Binary Literal

> A binary literal is used to specify a byte sequence value.

一个二进制字面量用来指定字节序列值。

### Syntax

```
X { 'num [ ... ]' | "num [ ... ]" }
```

### Parameters

- num

	Any hexadecimal number from 0 to F.

### Examples

```sql
SELECT X'123456' AS col;
+----------+
|       col|
+----------+
|[12 34 56]|
+----------+
```

## Null Literal

> A null literal is used to specify a null value.

null 字面量用来指定 null 值。

### Syntax

```
NULL
```

### Examples

```sql
SELECT NULL AS col;
+----+
| col|
+----+
|NULL|
+----+
```

## Boolean Literal

> A boolean literal is used to specify a boolean value.

布尔字面量用来指定布尔值。

### Syntax

```
TRUE | FALSE
```

### Examples

```sql
SELECT TRUE AS col;
+----+
| col|
+----+
|true|
+----+
```

## Numeric Literal

> A numeric literal is used to specify a fixed or floating-point number. There are two kinds of numeric literals: integral literal and fractional literal.

数值字面量用来指定一个固定的或浮点数值。有两种数值字面量：整型字面量和小数字面量。

### Integral Literal Syntax

```
[ + | - ] digit [ ... ] [ L | S | Y ]
```

### Integral Literal Parameters

- digit

	Any numeral from 0 to 9.

- L

	Case insensitive, indicates BIGINT, which is an 8-byte signed integer number.

- S

	Case insensitive, indicates SMALLINT, which is a 2-byte signed integer number.

- Y

	Case insensitive, indicates TINYINT, which is a 1-byte signed integer number.

- default (no postfix)

	Indicates a 4-byte signed integer number.

### Integral Literal Examples

```sql
SELECT -2147483648 AS col;
+-----------+
|        col|
+-----------+
|-2147483648|
+-----------+

SELECT 9223372036854775807l AS col;
+-------------------+
|                col|
+-------------------+
|9223372036854775807|
+-------------------+

SELECT -32Y AS col;
+---+
|col|
+---+
|-32|
+---+

SELECT 482S AS col;
+---+
|col|
+---+
|482|
+---+
```

### Fractional Literals Syntax

decimal literals:

	decimal_digits { [ BD ] | [ exponent BD ] } | digit [ ... ] [ exponent ] BD

double literals:

	decimal_digits  { D | exponent [ D ] }  | digit [ ... ] { exponent [ D ] | [ exponent ] D }

float literals:

	decimal_digits  { F | exponent [ F ] }  | digit [ ... ] { exponent [ F ] | [ exponent ] F }

While decimal_digits is defined as

	[ + | - ] { digit [ ... ] . [ digit [ ... ] ] | . digit [ ... ] }

and exponent is defined as

	E [ + | - ] digit [ ... ]

### Fractional Literals Parameters

- digit

	Any numeral from 0 to 9.

- D

	Case insensitive, indicates DOUBLE, which is an 8-byte double-precision floating point number.

- F

	Case insensitive, indicates FLOAT, which is a 4-byte single-precision floating point number.

- BD

	Case insensitive, indicates DECIMAL, with the total number of digits as precision and the number of digits to right of decimal point as scale.

### Fractional Literals Examples

```sql
SELECT 12.578 AS col;
+------+
|   col|
+------+
|12.578|
+------+

SELECT -0.1234567 AS col;
+----------+
|       col|
+----------+
|-0.1234567|
+----------+

SELECT -.1234567 AS col;
+----------+
|       col|
+----------+
|-0.1234567|
+----------+

SELECT 123. AS col;
+---+
|col|
+---+
|123|
+---+

SELECT 123.BD AS col;
+---+
|col|
+---+
|123|
+---+

SELECT 5E2 AS col;
+-----+
|  col|
+-----+
|500.0|
+-----+

SELECT 5D AS col;
+---+
|col|
+---+
|5.0|
+---+

SELECT -5BD AS col;
+---+
|col|
+---+
| -5|
+---+

SELECT 12.578e-2d AS col;
+-------+
|    col|
+-------+
|0.12578|
+-------+

SELECT -.1234567E+2BD AS col;
+---------+
|      col|
+---------+
|-12.34567|
+---------+

SELECT +3.e+3 AS col;
+------+
|   col|
+------+
|3000.0|
+------+

SELECT -3.E-3D AS col;
+------+
|   col|
+------+
|-0.003|
+------+
```

## Datetime Literal

> A datetime literal is used to specify a date or timestamp value.

datetime 字面量用来指定一个日期或时间戳值。

### Date Syntax

```
DATE { 'yyyy' |
       'yyyy-[m]m' |
       'yyyy-[m]m-[d]d' |
       'yyyy-[m]m-[d]d[T]' }
```

> Note: defaults to 01 if month or day is not specified.

### Date Examples

```sql
SELECT DATE '1997' AS col;
+----------+
|       col|
+----------+
|1997-01-01|
+----------+

SELECT DATE '1997-01' AS col;
+----------+
|       col|
+----------+
|1997-01-01|
+----------+

SELECT DATE '2011-11-11' AS col;
+----------+
|       col|
+----------+
|2011-11-11|
+----------+
```

### Timestamp Syntax

```
TIMESTAMP { 'yyyy' |
            'yyyy-[m]m' |
            'yyyy-[m]m-[d]d' |
            'yyyy-[m]m-[d]d ' |
            'yyyy-[m]m-[d]d[T][h]h[:]' |
            'yyyy-[m]m-[d]d[T][h]h:[m]m[:]' |
            'yyyy-[m]m-[d]d[T][h]h:[m]m:[s]s[.]' |
            'yyyy-[m]m-[d]d[T][h]h:[m]m:[s]s.[ms][ms][ms][us][us][us][zone_id]'}
```

> Note: defaults to 00 if hour, minute or second is not specified. `zone_id` should have one of the forms:

注意：如果没有指定小时、分组或秒，默认是00。 `zone_id` 应该是如下形式：


- Z - Zulu time zone UTC+0
- `+|-[h]h:[m]m`
- An id with one of the prefixes UTC+, UTC-, GMT+, GMT-, UT+ or UT-, and a suffix in the formats:
	- `+|-h[h]`
	- `+|-hh[:]mm`
	- `+|-hh:mm:ss`
	- `+|-hhmmss`

- Region-based zone IDs in the form `area/city`, such as `Europe/Paris`

> Note: defaults to the session local timezone (set via `spark.sql.session.timeZone`) if `zone_id` is not specified.

### Timestamp Examples

```sql
SELECT TIMESTAMP '1997-01-31 09:26:56.123' AS col;
+-----------------------+
|                    col|
+-----------------------+
|1997-01-31 09:26:56.123|
+-----------------------+

SELECT TIMESTAMP '1997-01-31 09:26:56.66666666UTC+08:00' AS col;
+--------------------------+
|                      col |
+--------------------------+
|1997-01-30 17:26:56.666666|
+--------------------------+

SELECT TIMESTAMP '1997-01' AS col;
+-------------------+
|                col|
+-------------------+
|1997-01-01 00:00:00|
+-------------------+
```

## Interval Literal

> An interval literal is used to specify a fixed period of time. The interval literal supports two syntaxes: ANSI syntax and multi-units syntax.

interval字面量用来指定时间的固定周期。支持两种语法: ANSI 和 multi-units 语法

### ANSI Syntax

The ANSI SQL standard defines interval literals in the form:

```
INTERVAL [ <sign> ] <interval string> <interval qualifier>
```

where `<interval qualifier>` can be a single field or in the field-to-field form:

```
<interval qualifier> ::= <start field> TO <end field> | <single field>
```
T
he field name is case-insensitive, and can be one of `YEAR`, `MONTH`, `DAY`, `HOUR`, `MINUTE` and `SECOND`.

An interval literal can have either `year-month` or `day-time` interval type. The interval sub-type defines format of `<interval string>`:

```
<interval string> ::= <quote> [ <sign> ] { <year-month literal> | <day-time literal> } <quote>
<year-month literal> ::= <years value> [ <minus sign> <months value> ] | <months value>
<day-time literal> ::= <day-time interval> | <time interval>
<day-time interval> ::= <days value> [ <space> <hours value> [ <colon> <minutes value> [ <colon> <seconds value> ] ] ]
<time interval> ::= <hours value> [ <colon> <minutes value> [ <colon> <seconds value> ] ]
  | <minutes value> [ <colon> <seconds value> ]
  | <seconds value>
```

Supported `year-month` interval literals and theirs formats:

`<interval qualifier>` | Interval string pattern | An instance of the literal
---|:---|:---
YEAR | `[+|-]'[+|-]y'`	| INTERVAL -'2021' YEAR
YEAR TO MONTH	| `[+|-]'[+|-]y-m'`	 | INTERVAL '-2021-07' YEAR TO MONTH
MONTH | `[+|-]'[+|-]m'`	| interval '10' month

Formats of supported day-time interval literals:

`<interval qualifier>` | Interval string pattern |  An instance of the literal
---|:---|:---
DAY	| `[+|-]'[+|-]d'`	| INTERVAL -'100' DAY
DAY TO HOUR	 | `[+|-]'[+|-]d h'`	| INTERVAL '-100 10' DAY TO HOUR
DAY TO MINUTE | `[+|-]'[+|-]d h:m'`	| INTERVAL '100 10:30' DAY TO MINUTE
DAY TO SECOND | `[+|-]'[+|-]d h:m:s.n'`	| INTERVAL '100 10:30:40.999999' DAY TO SECOND
HOUR | `[+|-]'[+|-]h'` | INTERVAL '123' HOUR
HOUR TO MINUTE | `[+|-]'[+|-]h:m'` | INTERVAL -'-123:10' HOUR TO MINUTE
HOUR TO SECOND | `[+|-]'[+|-]h:m:s.n'` | INTERVAL '123:10:59' HOUR TO SECOND
MINUTE | `[+|-]'[+|-]m'`	| interval '1000' minute
MINUTE TO SECOND | `[+|-]'[+|-]m:s.n'` | INTERVAL '1000:01.001' MINUTE TO SECOND
SECOND | `[+|-]'[+|-]s.n'` | INTERVAL '1000.000001' SECOND

### ANSI Examples

```sql
SELECT INTERVAL '2-3' YEAR TO MONTH AS col;
+----------------------------+
|col                         |
+----------------------------+
|INTERVAL '2-3' YEAR TO MONTH|
+----------------------------+

SELECT INTERVAL -'20 15:40:32.99899999' DAY TO SECOND AS col;
+--------------------------------------------+
|col                                         |
+--------------------------------------------+
|INTERVAL '-20 15:40:32.998999' DAY TO SECOND|
+--------------------------------------------+
```

### Multi-units Syntax

```
INTERVAL interval_value interval_unit [ interval_value interval_unit ... ] |
INTERVAL 'interval_value interval_unit [ interval_value interval_unit ... ]' |
```

### Multi-units Parameters

- interval_value

	Syntax:

	```
	[ + | - ] number_value | '[ + | - ] number_value'
	```

- interval_unit

	Syntax:
	
	```
	YEAR[S] | MONTH[S] | WEEK[S] | DAY[S] | HOUR[S] | MINUTE[S] | SECOND[S] |
	MILLISECOND[S] | MICROSECOND[S]
	
	Mix of the YEAR[S] or MONTH[S] interval units with other units is not allowed.
	```

### Multi-units Examples

```sql
SELECT INTERVAL 3 YEAR AS col;
+-------+
|    col|
+-------+
|3 years|
+-------+

SELECT INTERVAL -2 HOUR '3' MINUTE AS col;
+--------------------+
|                 col|
+--------------------+
|-1 hours -57 minutes|
+--------------------+

SELECT INTERVAL '1 YEAR 2 DAYS 3 HOURS';
+----------------------+
|                   col|
+----------------------+
|1 years 2 days 3 hours|
+----------------------+

SELECT INTERVAL 1 YEARS 2 MONTH 3 WEEK 4 DAYS 5 HOUR 6 MINUTES 7 SECOND 8
    MILLISECOND 9 MICROSECONDS AS col;
+-----------------------------------------------------------+
|                                                        col|
+-----------------------------------------------------------+
|1 years 2 months 25 days 5 hours 6 minutes 7.008009 seconds|
+-----------------------------------------------------------+
```
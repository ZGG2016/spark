# Date and Timestamp Functions

[TOC]

## Date and Timestamp Functions

### add_months(start_date, num_months)

Returns the date that is `num_months` after `start_date`.

返回 `num_months` 之后 `start_date` 月的日期。

### current_date()

Returns the current date at the start of query evaluation. All calls of current_date within the same query return the same value.

返回查询评估开始的当前日期。

在相同查询中，对 current_date 的任意调用，返回相同的值。

### current_date

Returns the current date at the start of query evaluation.

返回查询评估开始的当前日期。

### current_timestamp()

Returns the current timestamp at the start of query evaluation. All calls of current_timestamp within the same query return the same value.

返回查询评估开始的当前时间戳。

在相同查询中，对 current_timestamp 的任意调用，返回相同的值。

### current_timestamp

Returns the current timestamp at the start of query evaluation.

返回查询评估开始的当前日期。

### current_timezone()

Returns the current session local timezone.

返回当前会话的本地时区

### date_add(start_date, num_days)

Returns the date that is `num_days` after `start_date`.

返回 `num_days` 之后 `start_date` 天的日期。

### date_format(timestamp, fmt)

Converts `timestamp` to a value of string in the format specified by the date format `fmt`.

把 `timestamp` 转换成一个字符串值，其格式由 `fmt` 指定。

### date_from_unix_date(days)

Create date from the number of days since 1970-01-01.

创建一个日期，该日期是从 1970-01-01 开始之后到现在的天数。

### date_part(field, source)

Extracts a part of the date/timestamp or interval source.

抽取 date/timestamp 或 interval 的一部分

### date_sub(start_date, num_days)

Returns the date that is `num_days` before `start_date`.

返回 `num_days` 之前 `start_date` 天的日期 

### date_trunc(fmt, ts)

Returns timestamp `ts` truncated to the unit specified by the format model `fmt`.

返回被截断的时间戳 `ts`，截断的单元由 `fmt` 指定。

### datediff(endDate, startDate)

Returns the number of days from `startDate` to `endDate`.

返回从 `startDate` 到 `endDate` 相差的天数

### day(date)

Returns the day of month of the date/timestamp.

返回 date/timestamp 的天数（一个月中）

### dayofmonth(date)

Returns the day of month of the date/timestamp.

返回 date/timestamp 的天数（一个月中）

### dayofweek(date)

Returns the day of the week for date/timestamp (1 = Sunday, 2 = Monday, ..., 7 = Saturday).

返回 date/timestamp 的天数（一个周中）

### dayofyear(date)

Returns the day of year of the date/timestamp.

返回 date/timestamp 的天数（一个年中）

### extract(field FROM source)

Extracts a part of the date/timestamp or interval source.

抽取 date/timestamp 或 interval 的一部分

### from_unixtime(unix_time[, fmt])

Returns `unix_time` in the specified `fmt`.

返回 `fmt` 格式下的 `unix_time`

### from_utc_timestamp(timestamp, timezone)

Given a timestamp like '2017-07-14 02:40:00.0', interprets it as a time in UTC, and renders that time as a timestamp in the given time zone. For example, 'GMT+1' would yield '2017-07-14 03:40:00.0'.

给定一个像 '2017-07-14 02:40:00.0' 的时间戳，将其解释成 UTC 下的时间，并渲染成给定时区下的时间戳。

### hour(timestamp)

Returns the hour component of the string/timestamp.

返回 string/timestamp 的小时部分

### last_day(date)

Returns the last day of the month which the date belongs to.

返回日期所在的月份的最后一天

### make_date(year, month, day)

Create date from year, month and day fields. If the configuration `spark.sql.ansi.enabled` is false, the function returns NULL on invalid inputs. Otherwise, it will throw an error instead.

根据 year, month 和 day 字段，创建一个日期。

如果配置 `spark.sql.ansi.enabled` 为 false，对于无效输入，函数返回 NULL。否则，将抛一个错误。

### make_dt_interval([days[, hours[, mins[, secs]]]])

Make DayTimeIntervalType duration from days, hours, mins and secs.

从 days, hours, mins 和 secs 字段创建 DayTimeIntervalType 间隔。

### make_interval([years[, months[, weeks[, days[, hours[, mins[, secs]]]]]]])

Make interval from years, months, weeks, days, hours, mins and secs.

从 years, months, weeks, days, hours, mins 和 secs 字段中，创建间隔。

### make_timestamp(year, month, day, hour, min, sec[, timezone])

Create timestamp from year, month, day, hour, min, sec and timezone fields. The result data type is consistent with the value of configuration `spark.sql.timestampType`. If the configuration `spark.sql.ansi.enabled` is false, the function returns NULL on invalid inputs. Otherwise, it will throw an error instead.

从 years, months, weeks, days, hours, mins 和 secs 字段中，创建时间戳。

结果数据类型和 `spark.sql.timestampType` 配置的数据类型相同。

如果配置 `spark.sql.timestampType` 设为 false，对于无效输入，函数返回 NULL。否则，将抛一个错误。

### make_ym_interval([years[, months]])

Make year-month interval from years, months.

从 years, months 中，创建 year-month 间隔。

### minute(timestamp)

Returns the minute component of the string/timestamp.

返回 date/timestamp 的分钟部分

### month(date)

Returns the month component of the date/timestamp.

返回 date/timestamp 的月份部分

### months_between(timestamp1, timestamp2[, roundOff])

If `timestamp1` is later than `timestamp2`, then the result is positive. If `timestamp1` and `timestamp2` are on the same day of month, or both are the last day of month, time of day will be ignored. Otherwise, the difference is calculated based on 31 days per month, and rounded to 8 digits unless roundOff=false.

如果 `timestamp1` 晚于 `timestamp2`，那么结果就是正数。

如果 `timestamp1` 和 `timestamp2` 在一月的同一天，或者都是一月的最后一天，一天的时间部分将被忽略。

如果roundOff=true, 基于每月31天计算差值，保留8位数字。

### next_day(start_date, day_of_week)

Returns the first date which is later than `start_date` and named as indicated. The function returns NULL if at least one of the input parameters is NULL. When both of the input parameters are not NULL and day_of_week is an invalid input, the function throws IllegalArgumentException if `spark.sql.ansi.enabled` is set to true, otherwise NULL.

返回 `start_date` 之后，由 day_of_week 指示的第一个日期。

如果其中一个输入参数是 NULL，那么函数返回 NULL. 如果输入参数都不是 NULL, 且 day_of_week 是一个无效输入，且 `spark.sql.ansi.enabled` 设为 true, 函数抛 IllegalArgumentException 异常。否则返回 NULL.

### now()

Returns the current timestamp at the start of query evaluation.

返回查询评估开始的当前时间戳

### quarter(date)

Returns the quarter of the year for date, in the range 1 to 4.

返回日期的季度，从1到4

### second(timestamp)

Returns the second component of the string/timestamp.

返回 string/timestamp 的秒的部分

### session_window(time_column, gap_duration)

Generates session window given a timestamp specifying column and gap duration. See ['Types of time windows'](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#types-of-time-windows) in Structured Streaming guide doc for detailed explanation and examples.

基于给定的时间戳列和间隔，生成会话窗口。

### timestamp_micros(microseconds)

Creates timestamp from the number of microseconds since UTC epoch.

从 UTC epoch 开始，根据 microseconds 创建时间戳

### timestamp_millis(milliseconds)

Creates timestamp from the number of milliseconds since UTC epoch.

从 UTC epoch 开始，根据 microseconds 创建时间戳

### timestamp_seconds(seconds)

Creates timestamp from the number of seconds (can be fractional) since UTC epoch.

从 UTC epoch 开始，根据 seconds 创建时间戳

### to_date(date_str[, fmt])

Parses the `date_str` expression with the `fmt` expression to a date. Returns null with invalid input. By default, it follows casting rules to a date if the `fmt` is omitted.

使用 `fmt` 表达式，将 `date_str` 表达式解析成一个日期。

对于无效输入，返回null.

默认，如果忽略了 `fmt`，它遵循转换成日期的规则。 

### to_timestamp(timestamp_str[, fmt])

Parses the `timestamp_str` expression with the `fmt` expression to a timestamp. Returns null with invalid input. By default, it follows casting rules to a timestamp if the `fmt` is omitted. The result data type is consistent with the value of configuration `spark.sql.timestampType`.

使用 `fmt` 表达式，将 `date_str` 表达式解析成一个时间戳。

对于无效输入，返回null.

默认，如果忽略了 `fmt`，它遵循转换成时间戳的规则。 

结果的数据类型和 `spark.sql.timestampType` 配置的值一致。

### to_unix_timestamp(timeExp[, fmt])

Returns the UNIX timestamp of the given time.

返回给定时间的 UNIX 时间戳

### to_utc_timestamp(timestamp, timezone)

Given a timestamp like '2017-07-14 02:40:00.0', interprets it as a time in the given time zone, and renders that time as a timestamp in UTC. For example, 'GMT+1' would yield '2017-07-14 01:40:00.0'.

给定一个时间戳，将其解释成给定时区下的一个时间，将该时间渲染成一个 UTC 下的时间戳。

### trunc(date, fmt)

Returns `date` with the time portion of the day truncated to the unit specified by the format model `fmt`.

返回日期，其时间部分被截断成由 `fmt` 指定的单元

### unix_date(date)

Returns the number of days since 1970-01-01.

返回从 1970-01-01 到现在的天数

### unix_micros(timestamp)

Returns the number of microseconds since 1970-01-01 00:00:00 UTC.

返回从 1970-01-01 00:00:00 UTC 到现在的微秒数

### unix_millis(timestamp)

Returns the number of milliseconds since 1970-01-01 00:00:00 UTC. Truncates higher levels of precision.

返回从 1970-01-01 00:00:00 UTC 到现在的毫秒数

### unix_seconds(timestamp)

Returns the number of seconds since 1970-01-01 00:00:00 UTC. Truncates higher levels of precision.

返回从 1970-01-01 00:00:00 UTC 到现在的秒数

### unix_timestamp([timeExp[, fmt]])

Returns the UNIX timestamp of current or specified time.

返回当前或指定时间的 UNIX 时间戳

### weekday(date)

Returns the day of the week for date/timestamp (0 = Monday, 1 = Tuesday, ..., 6 = Sunday).

返回 date/timestamp 的周几

### weekofyear(date)

Returns the week of the year of the given date. A week is considered to start on a Monday and week 1 is the first week with >3 days.

返回给定日期的年中的第几个周。

一周从周一开始。week 1 是第一个周，大于3天。

### window(time_column, window_duration[, slide_duration[, start_time]])

Bucketize rows into one or more time windows given a timestamp specifying column. Window starts are inclusive but the window ends are exclusive, e.g. 12:05 will be in the window [12:05,12:10) but not in [12:00,12:05). Windows can support microsecond precision. Windows in the order of months are not supported. See ['Window Operations on Event Time'](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#window-operations-on-event-time) in Structured Streaming guide doc for detailed explanation and examples.

给与给定的时间戳列，将行分桶到一个或多个时间窗口。

包含窗口开始，不包含窗口结束，例如 12:05 在窗口 [12:05,12:10)，但不在窗口 [12:00,12:05).

窗口支持微秒精度。不支持以月为单位的窗口。

### year(date)

Returns the year component of the date/timestamp.

返回 date/timestamp 的年的部分

## Examples

```sql
-- add_months
SELECT add_months('2016-08-31', 1);
+-------------------------+
|add_months(2016-08-31, 1)|
+-------------------------+
|               2016-09-30|
+-------------------------+

-- current_date
SELECT current_date();
+--------------+
|current_date()|
+--------------+
|    2023-02-16|
+--------------+

SELECT current_date;
+--------------+
|current_date()|
+--------------+
|    2023-02-16|
+--------------+

-- current_timestamp
SELECT current_timestamp();
+--------------------+
| current_timestamp()|
+--------------------+
|2023-02-16 09:54:...|
+--------------------+

SELECT current_timestamp;
+--------------------+
| current_timestamp()|
+--------------------+
|2023-02-16 09:54:...|
+--------------------+

-- current_timezone
SELECT current_timezone();
+------------------+
|current_timezone()|
+------------------+
|           Etc/UTC|
+------------------+

-- date_add
SELECT date_add('2016-07-30', 1);
+-----------------------+
|date_add(2016-07-30, 1)|
+-----------------------+
|             2016-07-31|
+-----------------------+

-- date_format
SELECT date_format('2016-04-08', 'y');
+--------------------------+
|date_format(2016-04-08, y)|
+--------------------------+
|                      2016|
+--------------------------+

-- date_from_unix_date
SELECT date_from_unix_date(1);
+----------------------+
|date_from_unix_date(1)|
+----------------------+
|            1970-01-02|
+----------------------+

-- date_part
SELECT date_part('YEAR', TIMESTAMP '2019-08-12 01:00:00.123456');
+-------------------------------------------------------+
|date_part(YEAR, TIMESTAMP '2019-08-12 01:00:00.123456')|
+-------------------------------------------------------+
|                                                   2019|
+-------------------------------------------------------+

SELECT date_part('week', timestamp'2019-08-12 01:00:00.123456');
+-------------------------------------------------------+
|date_part(week, TIMESTAMP '2019-08-12 01:00:00.123456')|
+-------------------------------------------------------+
|                                                     33|
+-------------------------------------------------------+

SELECT date_part('doy', DATE'2019-08-12');
+---------------------------------+
|date_part(doy, DATE '2019-08-12')|
+---------------------------------+
|                              224|
+---------------------------------+

SELECT date_part('SECONDS', timestamp'2019-10-01 00:00:01.000001');
+----------------------------------------------------------+
|date_part(SECONDS, TIMESTAMP '2019-10-01 00:00:01.000001')|
+----------------------------------------------------------+
|                                                  1.000001|
+----------------------------------------------------------+

SELECT date_part('days', interval 5 days 3 hours 7 minutes);
+-------------------------------------------------+
|date_part(days, INTERVAL '5 03:07' DAY TO MINUTE)|
+-------------------------------------------------+
|                                                5|
+-------------------------------------------------+

SELECT date_part('seconds', interval 5 hours 30 seconds 1 milliseconds 1 microseconds);
+-------------------------------------------------------------+
|date_part(seconds, INTERVAL '05:00:30.001001' HOUR TO SECOND)|
+-------------------------------------------------------------+
|                                                    30.001001|
+-------------------------------------------------------------+

SELECT date_part('MONTH', INTERVAL '2021-11' YEAR TO MONTH);
+--------------------------------------------------+
|date_part(MONTH, INTERVAL '2021-11' YEAR TO MONTH)|
+--------------------------------------------------+
|                                                11|
+--------------------------------------------------+

SELECT date_part('MINUTE', INTERVAL '123 23:55:59.002001' DAY TO SECOND);
+---------------------------------------------------------------+
|date_part(MINUTE, INTERVAL '123 23:55:59.002001' DAY TO SECOND)|
+---------------------------------------------------------------+
|                                                             55|
+---------------------------------------------------------------+

-- date_sub
SELECT date_sub('2016-07-30', 1);
+-----------------------+
|date_sub(2016-07-30, 1)|
+-----------------------+
|             2016-07-29|
+-----------------------+

-- date_trunc
SELECT date_trunc('YEAR', '2015-03-05T09:32:05.359');
+-----------------------------------------+
|date_trunc(YEAR, 2015-03-05T09:32:05.359)|
+-----------------------------------------+
|                      2015-01-01 00:00:00|
+-----------------------------------------+

SELECT date_trunc('MM', '2015-03-05T09:32:05.359');
+---------------------------------------+
|date_trunc(MM, 2015-03-05T09:32:05.359)|
+---------------------------------------+
|                    2015-03-01 00:00:00|
+---------------------------------------+

SELECT date_trunc('DD', '2015-03-05T09:32:05.359');
+---------------------------------------+
|date_trunc(DD, 2015-03-05T09:32:05.359)|
+---------------------------------------+
|                    2015-03-05 00:00:00|
+---------------------------------------+

SELECT date_trunc('HOUR', '2015-03-05T09:32:05.359');
+-----------------------------------------+
|date_trunc(HOUR, 2015-03-05T09:32:05.359)|
+-----------------------------------------+
|                      2015-03-05 09:00:00|
+-----------------------------------------+

SELECT date_trunc('MILLISECOND', '2015-03-05T09:32:05.123456');
+---------------------------------------------------+
|date_trunc(MILLISECOND, 2015-03-05T09:32:05.123456)|
+---------------------------------------------------+
|                               2015-03-05 09:32:...|
+---------------------------------------------------+

-- datediff
SELECT datediff('2009-07-31', '2009-07-30');
+--------------------------------+
|datediff(2009-07-31, 2009-07-30)|
+--------------------------------+
|                               1|
+--------------------------------+

SELECT datediff('2009-07-30', '2009-07-31');
+--------------------------------+
|datediff(2009-07-30, 2009-07-31)|
+--------------------------------+
|                              -1|
+--------------------------------+

-- day
SELECT day('2009-07-30');
+---------------+
|day(2009-07-30)|
+---------------+
|             30|
+---------------+

-- dayofmonth
SELECT dayofmonth('2009-07-30');
+----------------------+
|dayofmonth(2009-07-30)|
+----------------------+
|                    30|
+----------------------+

-- dayofweek
SELECT dayofweek('2009-07-30');
+---------------------+
|dayofweek(2009-07-30)|
+---------------------+
|                    5|
+---------------------+

-- dayofyear
SELECT dayofyear('2016-04-09');
+---------------------+
|dayofyear(2016-04-09)|
+---------------------+
|                  100|
+---------------------+

-- extract
SELECT extract(YEAR FROM TIMESTAMP '2019-08-12 01:00:00.123456');
+---------------------------------------------------------+
|extract(YEAR FROM TIMESTAMP '2019-08-12 01:00:00.123456')|
+---------------------------------------------------------+
|                                                     2019|
+---------------------------------------------------------+

SELECT extract(week FROM timestamp'2019-08-12 01:00:00.123456');
+---------------------------------------------------------+
|extract(week FROM TIMESTAMP '2019-08-12 01:00:00.123456')|
+---------------------------------------------------------+
|                                                       33|
+---------------------------------------------------------+

SELECT extract(doy FROM DATE'2019-08-12');
+-----------------------------------+
|extract(doy FROM DATE '2019-08-12')|
+-----------------------------------+
|                                224|
+-----------------------------------+

SELECT extract(SECONDS FROM timestamp'2019-10-01 00:00:01.000001');
+------------------------------------------------------------+
|extract(SECONDS FROM TIMESTAMP '2019-10-01 00:00:01.000001')|
+------------------------------------------------------------+
|                                                    1.000001|
+------------------------------------------------------------+

SELECT extract(days FROM interval 5 days 3 hours 7 minutes);
+---------------------------------------------------+
|extract(days FROM INTERVAL '5 03:07' DAY TO MINUTE)|
+---------------------------------------------------+
|                                                  5|
+---------------------------------------------------+

SELECT extract(seconds FROM interval 5 hours 30 seconds 1 milliseconds 1 microseconds);
+---------------------------------------------------------------+
|extract(seconds FROM INTERVAL '05:00:30.001001' HOUR TO SECOND)|
+---------------------------------------------------------------+
|                                                      30.001001|
+---------------------------------------------------------------+

SELECT extract(MONTH FROM INTERVAL '2021-11' YEAR TO MONTH);
+----------------------------------------------------+
|extract(MONTH FROM INTERVAL '2021-11' YEAR TO MONTH)|
+----------------------------------------------------+
|                                                  11|
+----------------------------------------------------+

SELECT extract(MINUTE FROM INTERVAL '123 23:55:59.002001' DAY TO SECOND);
+-----------------------------------------------------------------+
|extract(MINUTE FROM INTERVAL '123 23:55:59.002001' DAY TO SECOND)|
+-----------------------------------------------------------------+
|                                                               55|
+-----------------------------------------------------------------+

-- from_unixtime
SELECT from_unixtime(0, 'yyyy-MM-dd HH:mm:ss');
+-------------------------------------+
|from_unixtime(0, yyyy-MM-dd HH:mm:ss)|
+-------------------------------------+
|                  1970-01-01 00:00:00|
+-------------------------------------+

SELECT from_unixtime(0);
+-------------------------------------+
|from_unixtime(0, yyyy-MM-dd HH:mm:ss)|
+-------------------------------------+
|                  1970-01-01 00:00:00|
+-------------------------------------+

-- from_utc_timestamp
SELECT from_utc_timestamp('2016-08-31', 'Asia/Seoul');
+------------------------------------------+
|from_utc_timestamp(2016-08-31, Asia/Seoul)|
+------------------------------------------+
|                       2016-08-31 09:00:00|
+------------------------------------------+

-- hour
SELECT hour('2009-07-30 12:58:59');
+-------------------------+
|hour(2009-07-30 12:58:59)|
+-------------------------+
|                       12|
+-------------------------+

-- last_day
SELECT last_day('2009-01-12');
+--------------------+
|last_day(2009-01-12)|
+--------------------+
|          2009-01-31|
+--------------------+

-- make_date
SELECT make_date(2013, 7, 15);
+----------------------+
|make_date(2013, 7, 15)|
+----------------------+
|            2013-07-15|
+----------------------+

SELECT make_date(2019, 7, NULL);
+------------------------+
|make_date(2019, 7, NULL)|
+------------------------+
|                    null|
+------------------------+

-- make_dt_interval
SELECT make_dt_interval(1, 12, 30, 01.001001);
+-------------------------------------+
|make_dt_interval(1, 12, 30, 1.001001)|
+-------------------------------------+
|                 INTERVAL '1 12:30...|
+-------------------------------------+

SELECT make_dt_interval(2);
+-----------------------------------+
|make_dt_interval(2, 0, 0, 0.000000)|
+-----------------------------------+
|               INTERVAL '2 00:00...|
+-----------------------------------+

SELECT make_dt_interval(100, null, 3);
+----------------------------------------+
|make_dt_interval(100, NULL, 3, 0.000000)|
+----------------------------------------+
|                                    null|
+----------------------------------------+

-- make_interval
SELECT make_interval(100, 11, 1, 1, 12, 30, 01.001001);
+----------------------------------------------+
|make_interval(100, 11, 1, 1, 12, 30, 1.001001)|
+----------------------------------------------+
|                          100 years 11 mont...|
+----------------------------------------------+

SELECT make_interval(100, null, 3);
+----------------------------------------------+
|make_interval(100, NULL, 3, 0, 0, 0, 0.000000)|
+----------------------------------------------+
|                                          null|
+----------------------------------------------+

SELECT make_interval(0, 1, 0, 1, 0, 0, 100.000001);
+-------------------------------------------+
|make_interval(0, 1, 0, 1, 0, 0, 100.000001)|
+-------------------------------------------+
|                       1 months 1 days 1...|
+-------------------------------------------+

-- make_timestamp
SELECT make_timestamp(2014, 12, 28, 6, 30, 45.887);
+-------------------------------------------+
|make_timestamp(2014, 12, 28, 6, 30, 45.887)|
+-------------------------------------------+
|                       2014-12-28 06:30:...|
+-------------------------------------------+

SELECT make_timestamp(2014, 12, 28, 6, 30, 45.887, 'CET');
+------------------------------------------------+
|make_timestamp(2014, 12, 28, 6, 30, 45.887, CET)|
+------------------------------------------------+
|                            2014-12-28 05:30:...|
+------------------------------------------------+

SELECT make_timestamp(2019, 6, 30, 23, 59, 60);
+---------------------------------------+
|make_timestamp(2019, 6, 30, 23, 59, 60)|
+---------------------------------------+
|                    2019-07-01 00:00:00|
+---------------------------------------+

SELECT make_timestamp(2019, 6, 30, 23, 59, 1);
+--------------------------------------+
|make_timestamp(2019, 6, 30, 23, 59, 1)|
+--------------------------------------+
|                   2019-06-30 23:59:01|
+--------------------------------------+

SELECT make_timestamp(null, 7, 22, 15, 30, 0);
+--------------------------------------+
|make_timestamp(NULL, 7, 22, 15, 30, 0)|
+--------------------------------------+
|                                  null|
+--------------------------------------+

-- make_ym_interval
SELECT make_ym_interval(1, 2);
+----------------------+
|make_ym_interval(1, 2)|
+----------------------+
|  INTERVAL '1-2' YE...|
+----------------------+

SELECT make_ym_interval(1, 0);
+----------------------+
|make_ym_interval(1, 0)|
+----------------------+
|  INTERVAL '1-0' YE...|
+----------------------+

SELECT make_ym_interval(-1, 1);
+-----------------------+
|make_ym_interval(-1, 1)|
+-----------------------+
|   INTERVAL '-0-11' ...|
+-----------------------+

SELECT make_ym_interval(2);
+----------------------+
|make_ym_interval(2, 0)|
+----------------------+
|  INTERVAL '2-0' YE...|
+----------------------+

-- minute
SELECT minute('2009-07-30 12:58:59');
+---------------------------+
|minute(2009-07-30 12:58:59)|
+---------------------------+
|                         58|
+---------------------------+

-- month
SELECT month('2016-07-30');
+-----------------+
|month(2016-07-30)|
+-----------------+
|                7|
+-----------------+

-- months_between
SELECT months_between('1997-02-28 10:30:00', '1996-10-30');
+-----------------------------------------------------+
|months_between(1997-02-28 10:30:00, 1996-10-30, true)|
+-----------------------------------------------------+
|                                           3.94959677|
+-----------------------------------------------------+

SELECT months_between('1997-02-28 10:30:00', '1996-10-30', false);
+------------------------------------------------------+
|months_between(1997-02-28 10:30:00, 1996-10-30, false)|
+------------------------------------------------------+
|                                    3.9495967741935485|
+------------------------------------------------------+

-- next_day
SELECT next_day('2015-01-14', 'TU');
+------------------------+
|next_day(2015-01-14, TU)|
+------------------------+
|              2015-01-20|
+------------------------+

-- now
SELECT now();
+--------------------+
|               now()|
+--------------------+
|2023-02-16 09:54:...|
+--------------------+

-- quarter
SELECT quarter('2016-08-31');
+-------------------+
|quarter(2016-08-31)|
+-------------------+
|                  3|
+-------------------+

-- second
SELECT second('2009-07-30 12:58:59');
+---------------------------+
|second(2009-07-30 12:58:59)|
+---------------------------+
|                         59|
+---------------------------+

-- session_window
SELECT a, session_window.start, session_window.end, count(*) as cnt FROM VALUES ('A1', '2021-01-01 00:00:00'), ('A1', '2021-01-01 00:04:30'), ('A1', '2021-01-01 00:10:00'), ('A2', '2021-01-01 00:01:00') AS tab(a, b) GROUP by a, session_window(b, '5 minutes') ORDER BY a, start;
+---+-------------------+-------------------+---+
|  a|              start|                end|cnt|
+---+-------------------+-------------------+---+
| A1|2021-01-01 00:00:00|2021-01-01 00:09:30|  2|
| A1|2021-01-01 00:10:00|2021-01-01 00:15:00|  1|
| A2|2021-01-01 00:01:00|2021-01-01 00:06:00|  1|
+---+-------------------+-------------------+---+

SELECT a, session_window.start, session_window.end, count(*) as cnt FROM VALUES ('A1', '2021-01-01 00:00:00'), ('A1', '2021-01-01 00:04:30'), ('A1', '2021-01-01 00:10:00'), ('A2', '2021-01-01 00:01:00'), ('A2', '2021-01-01 00:04:30') AS tab(a, b) GROUP by a, session_window(b, CASE WHEN a = 'A1' THEN '5 minutes' WHEN a = 'A2' THEN '1 minute' ELSE '10 minutes' END) ORDER BY a, start;
+---+-------------------+-------------------+---+
|  a|              start|                end|cnt|
+---+-------------------+-------------------+---+
| A1|2021-01-01 00:00:00|2021-01-01 00:09:30|  2|
| A1|2021-01-01 00:10:00|2021-01-01 00:15:00|  1|
| A2|2021-01-01 00:01:00|2021-01-01 00:02:00|  1|
| A2|2021-01-01 00:04:30|2021-01-01 00:05:30|  1|
+---+-------------------+-------------------+---+

-- timestamp_micros
SELECT timestamp_micros(1230219000123123);
+----------------------------------+
|timestamp_micros(1230219000123123)|
+----------------------------------+
|              2008-12-25 15:30:...|
+----------------------------------+

-- timestamp_millis
SELECT timestamp_millis(1230219000123);
+-------------------------------+
|timestamp_millis(1230219000123)|
+-------------------------------+
|           2008-12-25 15:30:...|
+-------------------------------+

-- timestamp_seconds
SELECT timestamp_seconds(1230219000);
+-----------------------------+
|timestamp_seconds(1230219000)|
+-----------------------------+
|          2008-12-25 15:30:00|
+-----------------------------+

SELECT timestamp_seconds(1230219000.123);
+---------------------------------+
|timestamp_seconds(1230219000.123)|
+---------------------------------+
|             2008-12-25 15:30:...|
+---------------------------------+

-- to_date
SELECT to_date('2009-07-30 04:17:52');
+----------------------------+
|to_date(2009-07-30 04:17:52)|
+----------------------------+
|                  2009-07-30|
+----------------------------+

SELECT to_date('2016-12-31', 'yyyy-MM-dd');
+-------------------------------+
|to_date(2016-12-31, yyyy-MM-dd)|
+-------------------------------+
|                     2016-12-31|
+-------------------------------+

-- to_timestamp
SELECT to_timestamp('2016-12-31 00:12:00');
+---------------------------------+
|to_timestamp(2016-12-31 00:12:00)|
+---------------------------------+
|              2016-12-31 00:12:00|
+---------------------------------+

SELECT to_timestamp('2016-12-31', 'yyyy-MM-dd');
+------------------------------------+
|to_timestamp(2016-12-31, yyyy-MM-dd)|
+------------------------------------+
|                 2016-12-31 00:00:00|
+------------------------------------+

-- to_unix_timestamp
SELECT to_unix_timestamp('2016-04-08', 'yyyy-MM-dd');
+-----------------------------------------+
|to_unix_timestamp(2016-04-08, yyyy-MM-dd)|
+-----------------------------------------+
|                               1460073600|
+-----------------------------------------+

-- to_utc_timestamp
SELECT to_utc_timestamp('2016-08-31', 'Asia/Seoul');
+----------------------------------------+
|to_utc_timestamp(2016-08-31, Asia/Seoul)|
+----------------------------------------+
|                     2016-08-30 15:00:00|
+----------------------------------------+

-- trunc
SELECT trunc('2019-08-04', 'week');
+-----------------------+
|trunc(2019-08-04, week)|
+-----------------------+
|             2019-07-29|
+-----------------------+

SELECT trunc('2019-08-04', 'quarter');
+--------------------------+
|trunc(2019-08-04, quarter)|
+--------------------------+
|                2019-07-01|
+--------------------------+

SELECT trunc('2009-02-12', 'MM');
+---------------------+
|trunc(2009-02-12, MM)|
+---------------------+
|           2009-02-01|
+---------------------+

SELECT trunc('2015-10-27', 'YEAR');
+-----------------------+
|trunc(2015-10-27, YEAR)|
+-----------------------+
|             2015-01-01|
+-----------------------+

-- unix_date
SELECT unix_date(DATE("1970-01-02"));
+---------------------+
|unix_date(1970-01-02)|
+---------------------+
|                    1|
+---------------------+

-- unix_micros
SELECT unix_micros(TIMESTAMP('1970-01-01 00:00:01Z'));
+---------------------------------+
|unix_micros(1970-01-01 00:00:01Z)|
+---------------------------------+
|                          1000000|
+---------------------------------+

-- unix_millis
SELECT unix_millis(TIMESTAMP('1970-01-01 00:00:01Z'));
+---------------------------------+
|unix_millis(1970-01-01 00:00:01Z)|
+---------------------------------+
|                             1000|
+---------------------------------+

-- unix_seconds
SELECT unix_seconds(TIMESTAMP('1970-01-01 00:00:01Z'));
+----------------------------------+
|unix_seconds(1970-01-01 00:00:01Z)|
+----------------------------------+
|                                 1|
+----------------------------------+

-- unix_timestamp
SELECT unix_timestamp();
+--------------------------------------------------------+
|unix_timestamp(current_timestamp(), yyyy-MM-dd HH:mm:ss)|
+--------------------------------------------------------+
|                                              1676541248|
+--------------------------------------------------------+

SELECT unix_timestamp('2016-04-08', 'yyyy-MM-dd');
+--------------------------------------+
|unix_timestamp(2016-04-08, yyyy-MM-dd)|
+--------------------------------------+
|                            1460073600|
+--------------------------------------+

-- weekday
SELECT weekday('2009-07-30');
+-------------------+
|weekday(2009-07-30)|
+-------------------+
|                  3|
+-------------------+

-- weekofyear
SELECT weekofyear('2008-02-20');
+----------------------+
|weekofyear(2008-02-20)|
+----------------------+
|                     8|
+----------------------+

-- window
SELECT a, window.start, window.end, count(*) as cnt FROM VALUES ('A1', '2021-01-01 00:00:00'), ('A1', '2021-01-01 00:04:30'), ('A1', '2021-01-01 00:06:00'), ('A2', '2021-01-01 00:01:00') AS tab(a, b) GROUP by a, window(b, '5 minutes') ORDER BY a, start;
+---+-------------------+-------------------+---+
|  a|              start|                end|cnt|
+---+-------------------+-------------------+---+
| A1|2021-01-01 00:00:00|2021-01-01 00:05:00|  2|
| A1|2021-01-01 00:05:00|2021-01-01 00:10:00|  1|
| A2|2021-01-01 00:00:00|2021-01-01 00:05:00|  1|
+---+-------------------+-------------------+---+

SELECT a, window.start, window.end, count(*) as cnt FROM VALUES ('A1', '2021-01-01 00:00:00'), ('A1', '2021-01-01 00:04:30'), ('A1', '2021-01-01 00:06:00'), ('A2', '2021-01-01 00:01:00') AS tab(a, b) GROUP by a, window(b, '10 minutes', '5 minutes') ORDER BY a, start;
+---+-------------------+-------------------+---+
|  a|              start|                end|cnt|
+---+-------------------+-------------------+---+
| A1|2020-12-31 23:55:00|2021-01-01 00:05:00|  2|
| A1|2021-01-01 00:00:00|2021-01-01 00:10:00|  3|
| A1|2021-01-01 00:05:00|2021-01-01 00:15:00|  1|
| A2|2020-12-31 23:55:00|2021-01-01 00:05:00|  1|
| A2|2021-01-01 00:00:00|2021-01-01 00:10:00|  1|
+---+-------------------+-------------------+---+

-- year
SELECT year('2016-07-30');
+----------------+
|year(2016-07-30)|
+----------------+
|            2016|
+----------------+
```
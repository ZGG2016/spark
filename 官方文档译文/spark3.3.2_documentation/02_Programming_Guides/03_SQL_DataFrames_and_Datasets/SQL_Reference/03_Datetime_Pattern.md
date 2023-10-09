# Datetime Patterns for Formatting and Parsing

> There are several common scenarios for datetime usage in Spark:

在 spark 中，对于 datetime 的用法有一些通用场景：

- CSV/JSON 数据源使用模板字符串解析和格式化 datetime 内容
- `StringType` 与 `DateType`和`TimestampType` 相关转换相关的 datetime 函数

> CSV/JSON datasources use the pattern string for parsing and formatting datetime content.
> Datetime functions related to convert `StringType` to/from `DateType` or `TimestampType`. For example, `unix_timestamp`, `date_format`, `to_unix_timestamp`, `from_unixtime`, `to_date`, `to_timestamp`, `from_utc_timestamp`, `to_utc_timestamp`, etc.

> Spark uses pattern letters in the following table for date and timestamp parsing and formatting:

在下面的表中，Spark 使用模板字母解析和格式化日期和时间戳：

表内容见原文: [https://spark.apache.org/docs/3.3.2/sql-ref-datetime-pattern.html](https://spark.apache.org/docs/3.3.2/sql-ref-datetime-pattern.html)

> The count of pattern letters determines the format.

模板字母的数量决定了格式：

> Text: The text style is determined based on the number of pattern letters used. Less than 4 pattern letters will use the short text form, typically an abbreviation, e.g. day-of-week Monday might output “Mon”. Exactly 4 pattern letters will use the full text form, typically the full description, e.g, day-of-week Monday might output “Monday”. 5 or more letters will fail.

- Text: 使用的模板字母的数量决定了文本的风格。小于4个将使用短文本形式，通常是缩写，例如，day-of-week Monday 会输出 Mon. 正好是4个时使用完整的文本形式，通常是完整描述，例如，day-of-week Monday 会输出 Monday. 如果是5个或更多就会失败。

> Number(n): The n here represents the maximum count of letters this type of datetime pattern can be used. If the count of letters is one, then the value is output using the minimum number of digits and without padding. Otherwise, the count of digits is used as the width of the output field, with the value zero-padded as necessary.

- Number(n): 这里的 n 表示这个 datetime 模板能使用的字母的最大计数。如果是1，那么输出使用数字的最小数量，没有填充。否则，数字的计数用作输出字段的宽度，必要时使用0填充。

> Number/Text: If the count of pattern letters is 3 or greater, use the Text rules above. Otherwise use the Number rules above.

- Number/Text: 如果计数大于等于3，规则就和 Text 一样。否则规则就和 Number 一样。

> Fraction: Use one or more (up to 9) contiguous 'S' characters, e,g SSSSSS, to parse and format fraction of second. For parsing, the acceptable fraction length can be `[1, the number of contiguous ‘S’]`. For formatting, the fraction length would be padded to the number of contiguous ‘S’ with zeros. Spark supports datetime of micro-of-second precision, which has up to 6 significant digits, but can parse nano-of-second with exceeded part truncated.

- Fraction: 使用一个或更多（最多9个）连续的 S 字符，例如 SSSSSS，解析和格式化秒的小数部分。对于解析，可接受的小数部分的长度可以是 `[1，连续S的数量]`。对于格式化，小数部分的长度将使用0填充，满足连续的 S 的数量。Spark 支持微秒精度的 datetime，最多有6位有效数字，但可以将超过部分解析为纳秒。

> Year: The count of letters determines the minimum field width below which padding is used. If the count of letters is two, then a reduced two digit form is used. For printing, this outputs the rightmost two digits. For parsing, this will parse using the base value of 2000, resulting in a year within the range 2000 to 2099 inclusive. If the count of letters is less than four (but not two), then the sign is only output for negative years. Otherwise, the sign is output if the pad width is exceeded when ‘G’ is not present. 7 or more letters will fail.

- Year: 字母的计数决定了使用填充的最小字段宽度。如果是2，就会减少到两位数字形式。对于打印，这输出最右边的两个数字。对于解析，使用2000这个基础值，这将解析出一个在 [2000,2099) 范围内的年份。如果计数小于4（但不是2），那么对于负的年份，会输出符号。否则，如果不存在 G，且超过了填充宽度，才会输出符号。如果计数大于等于7，就会失败。 

> Month: It follows the rule of Number/Text. The text form is depend on letters - ‘M’ denotes the ‘standard’ form, and ‘L’ is for ‘stand-alone’ form. These two forms are different only in some certain languages. For example, in Russian, ‘Июль’ is the stand-alone form of July, and ‘Июля’ is the standard form. Here are examples for all supported pattern letters:

- Month: 遵循 Number/Text 规则。文本形式取决于字母： M表示标准形式，L表示独立形式。这两种格式仅在特定语言下才不同。以下是所有支持的模板字母：

	> `'M'` or `'L'`: Month number in a year starting from 1. There is no difference between ‘M’ and ‘L’. Month from 1 to 9 are printed without padding.
	
	- `'M'` or `'L'`: 一年中月份的数值，从1开始。 M 还是 L 并没有区别。打印1月到9月不会有填充。

	```sql
	spark-sql> select date_format(date '1970-01-01', "M");
	1
	spark-sql> select date_format(date '1970-12-01', "L");
	12
	```

	> `'MM'` or `'LL'`: Month number in a year starting from 1. Zero padding is added for month 1-9.
  
  	- `'MM'` or `'LL'`: 一年中月份的数值，从1开始。打印1月到9月时使用0填充。

  	```sql
  	spark-sql> select date_format(date '1970-1-01', "LL");
	01
	spark-sql> select date_format(date '1970-09-01', "MM");
	09
	```

	> `'MMM'`: Short textual representation in the standard form. The month pattern should be a part of a date pattern not just a stand-alone month except locales where there is no difference between stand and stand-alone forms like in English.
	
	- `'MMM'`: 标准形式的短文本表示。月份模板应该是日期模板的一部分，而不是单独的月份。

	```sql
	spark-sql> select date_format(date '1970-01-01', "d MMM");
	1 Jan
	spark-sql> select to_csv(named_struct('date', date '1970-01-01'), map('dateFormat', 'dd MMM', 'locale', 'RU'));
	01 янв.
	```

	> `'LLL'`: Short textual representation in the stand-alone form. It should be used to format/parse only months without any other date fields.

	- `'LLL'`: 单独形式的短文本表示。它应该用于不需要其他日期字段，仅需要月份的格式化或解析场景。

	```sql
	spark-sql> select date_format(date '1970-01-01', "LLL");
	Jan
	spark-sql> select to_csv(named_struct('date', date '1970-01-01'), map('dateFormat', 'LLL', 'locale', 'RU'));
	янв.
	```

	> `'MMMM'`: full textual month representation in the standard form. It is used for parsing/formatting months as a part of dates/timestamps.

	- `'MMMM'`: 标准形式的完整文本表示。用于解析或格式化作为日期或时间戳一部分的月份。

	```sql
	spark-sql> select date_format(date '1970-01-01', "d MMMM");
	1 January
	spark-sql> select to_csv(named_struct('date', date '1970-01-01'), map('dateFormat', 'd MMMM', 'locale', 'RU'));
	1 января
	```

	> `'LLLL'`: full textual month representation in the stand-alone form. The pattern can be used to format/parse only months.

	- `'LLLL'`: 标准形式的单独文本表示。用于仅解析或格式化月份。

	```sql
	spark-sql> select date_format(date '1970-01-01', "LLLL");
	January
	spark-sql> select to_csv(named_struct('date', date '1970-01-01'), map('dateFormat', 'LLLL', 'locale', 'RU'));
	январь
	```

> am-pm: This outputs the am-pm-of-day. Pattern letter count must be 1.

- am-pm: 这输出 am-pm-of-day. 模板字母计数必须是1.

> Zone ID(V): This outputs the display the time-zone ID. Pattern letter count must be 2.

- Zone ID(V): 这输出展示 time-zone ID. 模板字母计数必须是2.

> Zone names(z): This outputs the display textual name of the time-zone ID. If the count of letters is one, two or three, then the short name is output. If the count of letters is four, then the full name is output. Five or more letters will fail.

- Zone names(z): 这输出展示 time-zone ID 的文本名字。
	+ 如果字母计数是1、2或3，那么输出短名字
	+ 如果计数是4，输出完整名字。如果计数大于等于5，就会失败

> Offset X and x: This formats the offset based on the number of pattern letters. One letter outputs just the hour, such as ‘+01’, unless the minute is non-zero in which case the minute is also output, such as ‘+0130’. Two letters outputs the hour and minute, without a colon, such as ‘+0130’. Three letters outputs the hour and minute, with a colon, such as ‘+01:30’. Four letters outputs the hour and minute and optional second, without a colon, such as ‘+013015’. Five letters outputs the hour and minute and optional second, with a colon, such as ‘+01:30:15’. Six or more letters will fail. Pattern letter ‘X’ (upper case) will output ‘Z’ when the offset to be output would be zero, whereas pattern letter ‘x’ (lower case) will output ‘+00’, ‘+0000’, or ‘+00:00’.

- Offset X and x: 基于模板字母的数量，格式化偏移量。
	+ 一个字母输出小数，例如‘+01’, 除非分钟是非0值，在这种情况下，也要输出分钟，例如‘+0130’
	+ 3个字母输出小时和分钟，及冒号，例如‘+01:30’
	+ 4个字母输出小时、分钟和可选地秒，无需冒号，例如‘+013015’
	+ 5个字母输出小时、分钟和可选地秒，及冒号，例如‘+01:30:15’
	+ 6个及更多字母将导致失败。当偏移量输出为0时，大写字母 X 将输出 Z，小写字母 x 输出为 ‘+00’, ‘+0000’, or ‘+00:00’

> Offset O: This formats the localized offset based on the number of pattern letters. One letter outputs the short form of the localized offset, which is localized offset text, such as ‘GMT’, with hour without leading zero, optional 2-digit minute and second if non-zero, and colon, for example ‘GMT+8’. Four letters outputs the full form, which is localized offset text, such as ‘GMT, with 2-digit hour and minute field, optional second field if non-zero, and colon, for example ‘GMT+08:00’. Any other count of letters will fail.

- Offset O: 基于模板字母的数量，格式化本地偏移量。
	+ 一个字母输出本地偏移量的短形式，也就是本地偏移量文本，例如‘GMT’，小时不用以0开头 ，如果分钟和秒不为0，那么还可以添加可选的两位数字的分钟和秒，及逗号，例如‘GMT+8’
	+ 4个字母输出完整形式，也就是本地偏移量文本，例如‘GMT’，包括两位数字的小时和分钟字段，如果秒不为0，那么还可以添加可选的秒，及逗号，例如‘GMT+08:00’
	+ 其他数量的字母将导致失败

> Offset Z: This formats the offset based on the number of pattern letters. One, two or three letters outputs the hour and minute, without a colon, such as ‘+0130’. The output will be ‘+0000’ when the offset is zero. Four letters outputs the full form of localized offset, equivalent to four letters of Offset-O. The output will be the corresponding localized offset text if the offset is zero. Five letters outputs the hour, minute, with optional second if non-zero, with colon. It outputs ‘Z’ if the offset is zero. Six or more letters will fail.

- Offset Z: 基于模板字母的数量，格式化偏移量。
	+ 1、2或3个字母输出小时和分钟，无需冒号，例如‘+0130’. 如果偏移量为0，输出就会是‘+0000’
	+ 4个字母输出本地化偏移量的完整形式，等价于Offset-O的4个字母版本。如果偏移量为0，输出将会是对应的本地化偏移量文本
	+ 5个字母输出小时、分钟，如果秒不为0，再包含可选的秒，及冒号。如果偏移量为0，就输出Z
	+ 6个及更多字母将失败

> Optional section start and end: Use `[]` to define an optional section and maybe nested. During formatting, all valid data will be output even it is in the optional section. During parsing, the whole section may be missing from the parsed string. An optional section is started by `[` and ended using `]` (or at the end of the pattern).

- 可选的中括号开始和结束：使用 `[]` 来定义一个可选的中括号及其可能的嵌套。在格式化期间，所有有效数据将被输出，即使是在可选的中括号里。在解析期间，整个中括号可能从待解析的字符串中缺失。一个可选的中括号以 `[` 开始，以 `]` 结束（或者在模板的结尾）

> Symbols of ‘E’, ‘F’, ‘q’ and ‘Q’ can only be used for datetime formatting, e.g. `date_format`. They are not allowed used for datetime parsing, e.g. `to_timestamp`.

- 符号 ‘E’, ‘F’, ‘q’ and ‘Q’ 仅用来格式化 datetime，例如`date_format`。但不能用来解析datetime，例如`to_timestamp`.
# String Functions

[TOC]

## String Functions

### ascii(str)

Returns the numeric value of the first character of `str`.

返回 `str` 的首个字符的数字值

### base64(bin)

Converts the argument from a binary `bin` to a base 64 string.

将参数 `bin` 从二进制转换成 base 64 字符串。

### bit_length(expr)

Returns the bit length of string data or number of bits of binary data.

返回字符串数据的位的长度，或二进制数据的位的数量

### btrim(str)

Removes the leading and trailing space characters from `str`.

移除字符串 `str` 的开头和结尾的空字符。

### btrim(str, trimStr)

Remove the leading and trailing `trimStr` characters from `str`.

移除字符串 `str` 的开头和结尾的 `trimStr` 字符。

### char(expr)

Returns the ASCII character having the binary equivalent to `expr`. If n is larger than 256 the result is equivalent to chr(n % 256)

返回 `expr` 等价的 ASCII 字符。如果 n 大于 256，那么结果等于 `chr(n % 256)`

### char_length(expr)

Returns the character length of string data or number of bytes of binary data. The length of string data includes the trailing spaces. The length of binary data includes binary zeros.

返回字符串数据的字符长度，或二进制数据的字节数量。

字符串数据的长度包含了末尾的空格。二进制数据的长度包含了二进制0

### character_length(expr)

Returns the character length of string data or number of bytes of binary data. The length of string data includes the trailing spaces. The length of binary data includes binary zeros.

返回字符串数据的字符长度，或二进制数据的字节数量。

字符串数据的长度包含了末尾的空格。二进制数据的长度包含了二进制0

### chr(expr)

Returns the ASCII character having the binary equivalent to `expr`. If n is larger than 256 the result is equivalent to chr(n % 256)

返回 `expr` 等价的 ASCII 字符。如果 n 大于 256，那么结果等于 `chr(n % 256)`

### concat_ws(sep[, str | array(str)]+)

Returns the concatenation of the strings separated by `sep`.

返回以 `sep` 分隔的字符串的拼接。

### contains(left, right)

Returns a boolean. The value is True if right is found inside left. Returns NULL if either input expression is NULL. Otherwise, returns False. Both left or right must be of STRING or BINARY type.

返回一个布尔值。

如果 right 在 left 里面，返回 True. 如果输入表达式是 NULL, 返回 NULL. 否则，返回 False. 

left 和 right 必须是 STRING 或 BINARY 类型。

### decode(bin, charset)

Decodes the first argument using the second argument character set.

使用第二个参数字符集，解码第一个参数。

### decode(expr, search, result [, search, result ] ... [, default])

Compares expr to each search value in order. If expr is equal to a search value, decode returns the corresponding result. If no match is found, then it returns default. If default is omitted, it returns null.

将 expr 按顺序和每个 search 值比较。

如果 expr 等于 search 值，返回对应的结果。如果没有匹配到，就返回默认值。

如果忽略了默认值，返回 null.

### elt(n, input1, input2, ...)

Returns the `n`-th input, e.g., returns `input2` when `n` is 2. The function returns NULL if the index exceeds the length of the array and `spark.sql.ansi.enabled` is set to false. If `spark.sql.ansi.enabled` is set to true, it throws ArrayIndexOutOfBoundsException for invalid indices.

返回第 `n` 个输入，例如当 `n` 等于2时，返回 `input2`。

如果索引超过了数组长度，且 `spark.sql.ansi.enabled` 设为 false, 函数返回 NULL.

如果 `spark.sql.ansi.enabled` 设为 true, 对于无效索引，就会抛出 ArrayIndexOutOfBoundsException 异常。

### encode(str, charset)

Encodes the first argument using the second argument character set.

使用第二个参数字符集编码第一个参数。

### endswith(left, right)

Returns a boolean. The value is True if left ends with right. Returns NULL if either input expression is NULL. Otherwise, returns False. Both left or right must be of STRING or BINARY type.

返回一个布尔值。

如果 left 以 right 结尾，返回 True. 如果输入表达式都是 NULL，则返回 NULL. 否则返回 False.

left 和 right 必须是 STRING 或 BINARY 类型。

### find_in_set(str, str_array)

Returns the index (1-based) of the given string (`str`) in the comma-delimited list (`str_array`). Returns 0, if the string was not found or if the given string (`str`) contains a comma.

返回给定字符串在逗号分隔的列表中的索引（索引从1开始）

如果字符串在列表中没有找到该字符串，或该字符串包含一个逗号，就返回0

### format_number(expr1, expr2)

Formats the number `expr1` like '#,###,###.##', rounded to `expr2` decimal places. If `expr2` is 0, the result has no decimal point or fractional part. `expr2` also accept a user specified format. This is supposed to function like MySQL's FORMAT.

格式化数字 `expr1`，近似到 `expr2` 小数位。

如果 `expr2` 是0，结果就没有小数点或小数部分。

`expr2` 也接受用户指定的格式。

### format_string(strfmt, obj, ...)

Returns a formatted string from printf-style format strings.

使用 printf 风格格式化字符串

### initcap(str)

Returns `str` with the first letter of each word in uppercase. All other letters are in lowercase. Words are delimited by white space.

以大写形式，返回每个单词的首个字母。

所有其他字母都是小写。单词以空格分隔

### instr(str, substr)

Returns the (1-based) index of the first occurrence of `substr` in `str`.

返回 `str` 中，`substr` 首次出现的索引（索引从1开始）

### lcase(str)

Returns `str` with all characters changed to lowercase.

把 `str` 的所有字符转成小写

### left(str, len)

Returns the leftmost `len`(`len` can be string type) characters from the string `str`,if `len` is less or equal than 0 the result is an empty string.

取字符串 `str` 最左边 `len` 多个字符。

如果 `len` 小于等于0，结果就是一个空字符串。

### length(expr)

Returns the character length of string data or number of bytes of binary data. The length of string data includes the trailing spaces. The length of binary data includes binary zeros.

返回字符串数据的字符长度，或二进制数据的字节数量。

字符串数据的长度包括末尾空格。二进制数据长度包含二进制0.

### levenshtein(str1, str2)

Returns the Levenshtein distance between the two given strings.

返回给定的两个字符串之间的 Levenshtein 距离。

### locate(substr, str[, pos])

Returns the position of the first occurrence of `substr` in `str` after position `pos`. The given `pos` and return value are 1-based.

返回 `pos` 位置之后，在 `str` 中首次出现 `substr` 的位置。

给定的 `pos` 和返回值从1开始。

### lower(str)

Returns `str` with all characters changed to lowercase.

将 `str` 中的所有字符转换成小写。

### lpad(str, len[, pad])

Returns `str`, left-padded with `pad` to a length of `len`. If `str` is longer than `len`, the return value is shortened to `len` characters or bytes. If `pad` is not specified, `str` will be padded to the left with space characters if it is a character string, and with zeros if it is a byte sequence.

返回 `str`, 左侧使用 `pad` 填充 `len` 长度。

如果 `str` 长度大于 `len`，返回值被截断到 `len` 字符或字节。

不指定 `pad`，如果是一个字符字符串，将使用空字符从左边开始填充 `str`，如果是一个字节序列，使用0填充。

### ltrim(str)

Removes the leading space characters from `str`.

移除 `str` 中开头的空字符。

### octet_length(expr)

Returns the byte length of string data or number of bytes of binary data.

返回字符串数据的字节长度，或二进制数据的字节数量。

### overlay(input, replace, pos[, len])

Replace `input` with `replace` that starts at `pos` and is of length `len`.

使用 `replace` 代替 `input`，从 `pos` 位置开始，长度是 `len`

### parse_url(url, partToExtract[, key])

Extracts a part from a URL.

从 URL 中抽取一部分。

### position(substr, str[, pos])

Returns the position of the first occurrence of `substr` in `str` after position `pos`. The given `pos` and return value are 1-based.

返回 `pos` 位置之后，在 `str` 中首次出现 `substr` 的位置。

给定的 `pos` 和返回值从1开始。

### printf(strfmt, obj, ...)

Returns a formatted string from printf-style format strings.

返回使用 printf 风格格式化的字符串

### regexp_extract(str, regexp[, idx])

Extract the first string in the `str` that match the `regexp` expression and corresponding to the regex group index.

抽取 `str` 中匹配 `regexp` 表达式的首个字符串对应的正则组索引

### regexp_extract_all(str, regexp[, idx])

Extract all strings in the `str` that match the `regexp` expression and corresponding to the regex group index.

抽取 `str` 中匹配 `regexp` 表达式的所有字符串对应的正则组索引

### regexp_replace(str, regexp, rep[, position])

Replaces all substrings of `str` that match `regexp` with `rep`.

使用 `rep` 替代能匹配 `regexp` 的 `str` 的所有子字符串。

### repeat(str, n)

Returns the string which repeats the given string value n times.

重复给定字符串 n 次

### replace(str, search[, replace])

Replaces all occurrences of `search` with `replace`.

使用 `replace` 取代所有的 `search`

### right(str, len)

Returns the rightmost `len`(`len` can be string type) characters from the string `str`,if `len` is less or equal than 0 the result is an empty string.

返回字符串 `str` 的最右侧 `len` 长度的字符。

如果 `len` 小于等于0，那么结果是一个空字符串。

### rpad(str, len[, pad])

Returns `str`, right-padded with `pad` to a length of `len`. If `str` is longer than `len`, the return value is shortened to `len` characters. If `pad` is not specified, `str` will be padded to the right with space characters if it is a character string, and with zeros if it is a binary string.

返回 `str`, 右侧使用 `pad` 填充 `len` 长度。

如果 `str` 长度大于 `len`，返回值被截断到 `len` 字符或字节。

不指定 `pad`，如果是一个字符字符串，将使用空字符从右边开始填充 `str`，如果是一个字节序列，使用0填充。

### rtrim(str)

Removes the trailing space characters from `str`.

移除 `str` 中末尾的空字符。

### sentences(str[, lang, country])

Splits `str` into an array of array of words.

把 `str` 划分成单词数组的数组

### soundex(str)

Returns Soundex code of the string.

返回字符串的 Soundex 编码

### space(n)

Returns a string consisting of `n` spaces.

返回由 n 个空格组成的字符串

### split(str, regex, limit)

Splits `str` around occurrences that match `regex` and returns an array with a length of at most `limit`

使用 `regex` 中的字符划分字符串 `str`，返回长度最大为 `limit` 的数组。

### split_part(str, delimiter, partNum)

Splits `str` by delimiter and return requested part of the split (1-based). If any input is null, returns null. if `partNum` is out of range of split parts, returns empty string. If `partNum` is 0, throws an error. If `partNum` is negative, the parts are counted backward from the end of the string. If the `delimiter` is an empty string, the `str` is not split.

使用 delimiter 划分 `str`，并返回第 partNum 个元素。

如果任意输入是 null, 则返回 null.

如果 `partNum` 超过了划分部分的范围，就返回空字符串。

如果 `partNum` 为0，就抛出一个错误。

如果 `partNum` 是负数，从字符串的末尾向前统计。

如果 `delimiter` 是一个空字符串，`str` 就不会被划分。

### startswith(left, right)

Returns a boolean. The value is True if left starts with right. Returns NULL if either input expression is NULL. Otherwise, returns False. Both left or right must be of STRING or BINARY type.

返回一个布尔值。

如果 left 以 right 开始，返回 True。如果任意一个输入表达式是 NULL，返回 NULL。否则返回 False.

left 和 right 必须是 STRING 或 BINARY类型

### substr(str, pos[, len])

Returns the substring of `str` that starts at `pos` and is of length `len`, or the slice of byte array that starts at `pos` and is of length `len`.

返回从 `pos` 位置开始，长度为 `len` 的 `str` 的子字符串，或从 `pos` 位置开始，长度为 `len` 的字节数组的切片。

### substr(str FROM pos[ FOR len]])

Returns the substring of `str` that starts at `pos` and is of length `len`, or the slice of byte array that starts at `pos` and is of length `len`.

返回从 `pos` 位置开始，长度为 `len` 的 `str` 的子字符串，或从 `pos` 位置开始，长度为 `len` 的字节数组的切片。

### substring(str, pos[, len])

Returns the substring of `str` that starts at `pos` and is of length `len`, or the slice of byte array that starts at `pos` and is of length `len`.

返回从 `pos` 位置开始，长度为 `len` 的 `str` 的子字符串，或从 `pos` 位置开始，长度为 `len` 的字节数组的切片。

### substring(str FROM pos[ FOR len]])

Returns the substring of `str` that starts at `pos` and is of length `len`, or the slice of byte array that starts at `pos` and is of length `len`.

返回从 `pos` 位置开始，长度为 `len` 的 `str` 的子字符串，或从 `pos` 位置开始，长度为 `len` 的字节数组的切片。

### substring_index(str, delim, count)

Returns the substring from `str` before `count` occurrences of the delimiter `delim`. If `count` is positive, everything to the left of the final delimiter (counting from the left) is returned. If `count` is negative, everything to the right of the final delimiter (counting from the right) is returned. The function substring_index performs a case-sensitive match when searching for `delim`.

在分隔符 `delim` 出现 `count` 之前，返回字符串 `str` 的子串。

如果 `count` 是正数，返回从开始到最后分隔符的左侧的内容。

如果 `count` 是负数，返回从末尾到最后分隔符的右侧的内容。

在搜索 `delim` 时，函数是大小写敏感的。

### to_binary(str[, fmt])

Converts the input `str` to a binary value based on the supplied `fmt`. `fmt` can be a case-insensitive string literal of "hex", "utf-8", or "base64". By default, the binary format for conversion is "hex" if `fmt` is omitted. The function returns NULL if at least one of the input parameters is NULL.

基于 `fmt`，把输入 `str` 转换成二进制值。

`fmt` 是大小写不敏感的 "hex"、"utf-8"或"base64" 字符串字面量。

默认，如果忽略了 `fmt`，转换使用的二进制格式是"hex"。

如果其中之一的输入参数是 NULL，那么函数返回 NULL

### to_number(expr, fmt)

Convert string 'expr' to a number based on the string format 'fmt'. Throws an exception if the conversion fails. The format can consist of the following characters, case insensitive: '0' or '9': Specifies an expected digit between 0 and 9. A sequence of 0 or 9 in the format string matches a sequence of digits in the input string. If the 0/9 sequence starts with 0 and is before the decimal point, it can only match a digit sequence of the same size. Otherwise, if the sequence starts with 9 or is after the decimal poin, it can match a digit sequence that has the same or smaller size. '.' or 'D': Specifies the position of the decimal point (optional, only allowed once). ',' or 'G': Specifies the position of the grouping (thousands) separator (,). There must be one or more 0 or 9 to the left of the rightmost grouping separator. 'expr' must match the grouping separator relevant for the size of the number. '′:Specifies the location of the currency sign. This character may only be specified once. 'S' or 'MI': Specifies the position of a '-' or '+' sign (optional, only allowed once at the beginning or end of the format string). Note that 'S' allows '-' but 'MI' does not. 'PR': Only allowed at the end of the format string; specifies that 'expr' indicates a negative number with wrapping angled brackets. ('<1>').

基于 `fmt`，把字符串 `expr` 转换成数值。如果转换失败，抛出一个异常。格式可以由下列字符组成：

- 大小写不敏感的 '0' 或 '9'

	指定一个0和9值之间的数字。格式字符串中的一个0或9的序列匹配输入字符串中的数字序列。
	
	如果0/9序列从0开始，在小数点之前，它仅能匹配相同大小的数字序列。否则，如果序列从9开始，在小数点之前，它能匹配相同大小或更小的数字序列。

- '.' or 'D'

	指定小数点的位置（可选的，仅允许一次）

- ',' or 'G'
	
	指定分组分隔符(,)的位置。最右侧的分组分隔符的左边必须存在一个或多个0或9。'expr'必须匹配与数字大小相关的分组分隔符。

- '′

	指定货币符号的位置。这个字符仅被指定一次。

- 'S' or 'MI'
	
	指定 '-' 或 '+' 的位置（可选的，仅允许在格式字符串的开头或结尾）。注意 'S' 允许 '-'，但是 'MI' 不允许

- 'PR'
 
 	仅允许出现在格式字符串的末尾。指定 expr 表示带尖括号的负数<1>。

### translate(input, from, to)

Translates the `input` string by replacing the characters present in the `from` string with the corresponding characters in the `to` string.

使用 `to` 字符串中的字符代替 `from` 字符串中对应的字符

### trim(str)

Removes the leading and trailing space characters from `str`.

移除 `str` 中开头和结尾的空字符

### trim(BOTH FROM str)

Removes the leading and trailing space characters from `str`.

移除 `str` 中开头和结尾的空字符

### trim(LEADING FROM str)

Removes the leading space characters from `str`.

移除 `str` 中开头的空字符

### trim(TRAILING FROM str)

Removes the trailing space characters from `str`.

移除 `str` 中结尾的空字符

### trim(trimStr FROM str)

Remove the leading and trailing `trimStr` characters from `str`.

移除 `str` 中开头和结尾的 `trimStr` 字符

### trim(BOTH trimStr FROM str)

Remove the leading and trailing `trimStr` characters from `str`.

移除 `str` 中开头和结尾的 `trimStr` 字符

### trim(LEADING trimStr FROM str)

Remove the leading `trimStr` characters from `str`.

移除 `str` 中开头的 `trimStr` 字符

### trim(TRAILING trimStr FROM str)

Remove the trailing `trimStr` characters from `str`.

移除 `str` 中结尾的 `trimStr` 字符

### try_to_binary(str[, fmt])

This is a special version of `to_binary` that performs the same operation, but returns a NULL value instead of raising an error if the conversion cannot be performed.

`to_binary` 函数的特殊版本，执行相同操作。但是如果转换不能执行时，就会返回 NULL 值，而不是抛一个错误

### try_to_number(expr, fmt)

Convert string 'expr' to a number based on the string format `fmt`. Returns NULL if the string 'expr' does not match the expected format. The format follows the same semantics as the to_number function.

基于字符串格式 `fmt`，把字符串 expr 转换成一个数字。

如果字符串 expr 没有匹配带期望的格式，就返回 NULL.

格式遵循和 to_number 函数相同的语义。

### ucase(str)

Returns `str` with all characters changed to uppercase.

将 `str` 中的所有字符转成大写。

### unbase64(str)

Converts the argument from a base 64 string `str` to a binary.

将参数从 base 64 字符串转成二进制。

### upper(str)

Returns `str` with all characters changed to uppercase.

将 `str` 中的所有字符转成大写。

## Examples

```sql
-- ascii
SELECT ascii('222');
+----------+
|ascii(222)|
+----------+
|        50|
+----------+

SELECT ascii(2);
+--------+
|ascii(2)|
+--------+
|      50|
+--------+

-- base64
SELECT base64('Spark SQL');
+-----------------+
|base64(Spark SQL)|
+-----------------+
|     U3BhcmsgU1FM|
+-----------------+

-- bit_length
SELECT bit_length('Spark SQL');
+---------------------+
|bit_length(Spark SQL)|
+---------------------+
|                   72|
+---------------------+

-- btrim
SELECT btrim('    SparkSQL   ');
+----------------------+
|btrim(    SparkSQL   )|
+----------------------+
|              SparkSQL|
+----------------------+

SELECT btrim(encode('    SparkSQL   ', 'utf-8'));
+-------------------------------------+
|btrim(encode(    SparkSQL   , utf-8))|
+-------------------------------------+
|                             SparkSQL|
+-------------------------------------+

SELECT btrim('SSparkSQLS', 'SL');
+---------------------+
|btrim(SSparkSQLS, SL)|
+---------------------+
|               parkSQ|
+---------------------+

SELECT btrim(encode('SSparkSQLS', 'utf-8'), encode('SL', 'utf-8'));
+---------------------------------------------------+
|btrim(encode(SSparkSQLS, utf-8), encode(SL, utf-8))|
+---------------------------------------------------+
|                                             parkSQ|
+---------------------------------------------------+

-- char
SELECT char(65);
+--------+
|char(65)|
+--------+
|       A|
+--------+

-- char_length
SELECT char_length('Spark SQL ');
+-----------------------+
|char_length(Spark SQL )|
+-----------------------+
|                     10|
+-----------------------+

SELECT CHAR_LENGTH('Spark SQL ');
+-----------------------+
|char_length(Spark SQL )|
+-----------------------+
|                     10|
+-----------------------+

SELECT CHARACTER_LENGTH('Spark SQL ');
+----------------------------+
|character_length(Spark SQL )|
+----------------------------+
|                          10|
+----------------------------+

-- character_length
SELECT character_length('Spark SQL ');
+----------------------------+
|character_length(Spark SQL )|
+----------------------------+
|                          10|
+----------------------------+

SELECT CHAR_LENGTH('Spark SQL ');
+-----------------------+
|char_length(Spark SQL )|
+-----------------------+
|                     10|
+-----------------------+

SELECT CHARACTER_LENGTH('Spark SQL ');
+----------------------------+
|character_length(Spark SQL )|
+----------------------------+
|                          10|
+----------------------------+

-- chr
SELECT chr(65);
+-------+
|chr(65)|
+-------+
|      A|
+-------+

-- concat_ws
SELECT concat_ws(' ', 'Spark', 'SQL');
+------------------------+
|concat_ws( , Spark, SQL)|
+------------------------+
|               Spark SQL|
+------------------------+

SELECT concat_ws('s');
+------------+
|concat_ws(s)|
+------------+
|            |
+------------+

-- contains
SELECT contains('Spark SQL', 'Spark');
+--------------------------+
|contains(Spark SQL, Spark)|
+--------------------------+
|                      true|
+--------------------------+

SELECT contains('Spark SQL', 'SPARK');
+--------------------------+
|contains(Spark SQL, SPARK)|
+--------------------------+
|                     false|
+--------------------------+

SELECT contains('Spark SQL', null);
+-------------------------+
|contains(Spark SQL, NULL)|
+-------------------------+
|                     null|
+-------------------------+

SELECT contains(x'537061726b2053514c', x'537061726b');
+----------------------------------------------+
|contains(X'537061726B2053514C', X'537061726B')|
+----------------------------------------------+
|                                          true|
+----------------------------------------------+

-- decode
SELECT decode(encode('abc', 'utf-8'), 'utf-8');
+---------------------------------+
|decode(encode(abc, utf-8), utf-8)|
+---------------------------------+
|                              abc|
+---------------------------------+

SELECT decode(2, 1, 'Southlake', 2, 'San Francisco', 3, 'New Jersey', 4, 'Seattle', 'Non domestic');
+----------------------------------------------------------------------------------+
|decode(2, 1, Southlake, 2, San Francisco, 3, New Jersey, 4, Seattle, Non domestic)|
+----------------------------------------------------------------------------------+
|                                                                     San Francisco|
+----------------------------------------------------------------------------------+

SELECT decode(6, 1, 'Southlake', 2, 'San Francisco', 3, 'New Jersey', 4, 'Seattle', 'Non domestic');
+----------------------------------------------------------------------------------+
|decode(6, 1, Southlake, 2, San Francisco, 3, New Jersey, 4, Seattle, Non domestic)|
+----------------------------------------------------------------------------------+
|                                                                      Non domestic|
+----------------------------------------------------------------------------------+

SELECT decode(6, 1, 'Southlake', 2, 'San Francisco', 3, 'New Jersey', 4, 'Seattle');
+--------------------------------------------------------------------+
|decode(6, 1, Southlake, 2, San Francisco, 3, New Jersey, 4, Seattle)|
+--------------------------------------------------------------------+
|                                                                null|
+--------------------------------------------------------------------+

SELECT decode(null, 6, 'Spark', NULL, 'SQL', 4, 'rocks');
+-------------------------------------------+
|decode(NULL, 6, Spark, NULL, SQL, 4, rocks)|
+-------------------------------------------+
|                                        SQL|
+-------------------------------------------+

-- elt
SELECT elt(1, 'scala', 'java');
+-------------------+
|elt(1, scala, java)|
+-------------------+
|              scala|
+-------------------+

-- encode
SELECT encode('abc', 'utf-8');
+------------------+
|encode(abc, utf-8)|
+------------------+
|        [61 62 63]|
+------------------+

-- endswith
SELECT endswith('Spark SQL', 'SQL');
+------------------------+
|endswith(Spark SQL, SQL)|
+------------------------+
|                    true|
+------------------------+

SELECT endswith('Spark SQL', 'Spark');
+--------------------------+
|endswith(Spark SQL, Spark)|
+--------------------------+
|                     false|
+--------------------------+

SELECT endswith('Spark SQL', null);
+-------------------------+
|endswith(Spark SQL, NULL)|
+-------------------------+
|                     null|
+-------------------------+

SELECT endswith(x'537061726b2053514c', x'537061726b');
+----------------------------------------------+
|endswith(X'537061726B2053514C', X'537061726B')|
+----------------------------------------------+
|                                         false|
+----------------------------------------------+

SELECT endswith(x'537061726b2053514c', x'53514c');
+------------------------------------------+
|endswith(X'537061726B2053514C', X'53514C')|
+------------------------------------------+
|                                      true|
+------------------------------------------+

-- find_in_set
SELECT find_in_set('ab','abc,b,ab,c,def');
+-------------------------------+
|find_in_set(ab, abc,b,ab,c,def)|
+-------------------------------+
|                              3|
+-------------------------------+

-- format_number
SELECT format_number(12332.123456, 4);
+------------------------------+
|format_number(12332.123456, 4)|
+------------------------------+
|                   12,332.1235|
+------------------------------+

SELECT format_number(12332.123456, '##################.###');
+---------------------------------------------------+
|format_number(12332.123456, ##################.###)|
+---------------------------------------------------+
|                                          12332.123|
+---------------------------------------------------+

-- format_string
SELECT format_string("Hello World %d %s", 100, "days");
+-------------------------------------------+
|format_string(Hello World %d %s, 100, days)|
+-------------------------------------------+
|                       Hello World 100 days|
+-------------------------------------------+

-- initcap
SELECT initcap('sPark sql');
+------------------+
|initcap(sPark sql)|
+------------------+
|         Spark Sql|
+------------------+

-- instr
SELECT instr('SparkSQL', 'SQL');
+--------------------+
|instr(SparkSQL, SQL)|
+--------------------+
|                   6|
+--------------------+

-- lcase
SELECT lcase('SparkSql');
+---------------+
|lcase(SparkSql)|
+---------------+
|       sparksql|
+---------------+

-- left
SELECT left('Spark SQL', 3);
+------------------+
|left(Spark SQL, 3)|
+------------------+
|               Spa|
+------------------+

-- length
SELECT length('Spark SQL ');
+------------------+
|length(Spark SQL )|
+------------------+
|                10|
+------------------+

SELECT CHAR_LENGTH('Spark SQL ');
+-----------------------+
|char_length(Spark SQL )|
+-----------------------+
|                     10|
+-----------------------+

SELECT CHARACTER_LENGTH('Spark SQL ');
+----------------------------+
|character_length(Spark SQL )|
+----------------------------+
|                          10|
+----------------------------+

-- levenshtein
SELECT levenshtein('kitten', 'sitting');
+----------------------------+
|levenshtein(kitten, sitting)|
+----------------------------+
|                           3|
+----------------------------+

-- locate
SELECT locate('bar', 'foobarbar');
+-------------------------+
|locate(bar, foobarbar, 1)|
+-------------------------+
|                        4|
+-------------------------+

SELECT locate('bar', 'foobarbar', 5);
+-------------------------+
|locate(bar, foobarbar, 5)|
+-------------------------+
|                        7|
+-------------------------+

SELECT POSITION('bar' IN 'foobarbar');
+-------------------------+
|locate(bar, foobarbar, 1)|
+-------------------------+
|                        4|
+-------------------------+

-- lower
SELECT lower('SparkSql');
+---------------+
|lower(SparkSql)|
+---------------+
|       sparksql|
+---------------+

-- lpad
SELECT lpad('hi', 5, '??');
+---------------+
|lpad(hi, 5, ??)|
+---------------+
|          ???hi|
+---------------+

SELECT lpad('hi', 1, '??');
+---------------+
|lpad(hi, 1, ??)|
+---------------+
|              h|
+---------------+

SELECT lpad('hi', 5);
+--------------+
|lpad(hi, 5,  )|
+--------------+
|            hi|
+--------------+

SELECT hex(lpad(unhex('aabb'), 5));
+--------------------------------+
|hex(lpad(unhex(aabb), 5, X'00'))|
+--------------------------------+
|                      000000AABB|
+--------------------------------+

SELECT hex(lpad(unhex('aabb'), 5, unhex('1122')));
+--------------------------------------+
|hex(lpad(unhex(aabb), 5, unhex(1122)))|
+--------------------------------------+
|                            112211AABB|
+--------------------------------------+

-- ltrim
SELECT ltrim('    SparkSQL   ');
+----------------------+
|ltrim(    SparkSQL   )|
+----------------------+
|           SparkSQL   |
+----------------------+

-- octet_length
SELECT octet_length('Spark SQL');
+-----------------------+
|octet_length(Spark SQL)|
+-----------------------+
|                      9|
+-----------------------+

-- overlay
SELECT overlay('Spark SQL' PLACING '_' FROM 6);
+----------------------------+
|overlay(Spark SQL, _, 6, -1)|
+----------------------------+
|                   Spark_SQL|
+----------------------------+

SELECT overlay('Spark SQL' PLACING 'CORE' FROM 7);
+-------------------------------+
|overlay(Spark SQL, CORE, 7, -1)|
+-------------------------------+
|                     Spark CORE|
+-------------------------------+

SELECT overlay('Spark SQL' PLACING 'ANSI ' FROM 7 FOR 0);
+-------------------------------+
|overlay(Spark SQL, ANSI , 7, 0)|
+-------------------------------+
|                 Spark ANSI SQL|
+-------------------------------+

SELECT overlay('Spark SQL' PLACING 'tructured' FROM 2 FOR 4);
+-----------------------------------+
|overlay(Spark SQL, tructured, 2, 4)|
+-----------------------------------+
|                     Structured SQL|
+-----------------------------------+

SELECT overlay(encode('Spark SQL', 'utf-8') PLACING encode('_', 'utf-8') FROM 6);
+----------------------------------------------------------+
|overlay(encode(Spark SQL, utf-8), encode(_, utf-8), 6, -1)|
+----------------------------------------------------------+
|                                      [53 70 61 72 6B 5...|
+----------------------------------------------------------+

SELECT overlay(encode('Spark SQL', 'utf-8') PLACING encode('CORE', 'utf-8') FROM 7);
+-------------------------------------------------------------+
|overlay(encode(Spark SQL, utf-8), encode(CORE, utf-8), 7, -1)|
+-------------------------------------------------------------+
|                                         [53 70 61 72 6B 2...|
+-------------------------------------------------------------+

SELECT overlay(encode('Spark SQL', 'utf-8') PLACING encode('ANSI ', 'utf-8') FROM 7 FOR 0);
+-------------------------------------------------------------+
|overlay(encode(Spark SQL, utf-8), encode(ANSI , utf-8), 7, 0)|
+-------------------------------------------------------------+
|                                         [53 70 61 72 6B 2...|
+-------------------------------------------------------------+

SELECT overlay(encode('Spark SQL', 'utf-8') PLACING encode('tructured', 'utf-8') FROM 2 FOR 4);
+-----------------------------------------------------------------+
|overlay(encode(Spark SQL, utf-8), encode(tructured, utf-8), 2, 4)|
+-----------------------------------------------------------------+
|                                             [53 74 72 75 63 7...|
+-----------------------------------------------------------------+

-- parse_url
SELECT parse_url('http://spark.apache.org/path?query=1', 'HOST');
+-----------------------------------------------------+
|parse_url(http://spark.apache.org/path?query=1, HOST)|
+-----------------------------------------------------+
|                                     spark.apache.org|
+-----------------------------------------------------+

SELECT parse_url('http://spark.apache.org/path?query=1', 'QUERY');
+------------------------------------------------------+
|parse_url(http://spark.apache.org/path?query=1, QUERY)|
+------------------------------------------------------+
|                                               query=1|
+------------------------------------------------------+

SELECT parse_url('http://spark.apache.org/path?query=1', 'QUERY', 'query');
+-------------------------------------------------------------+
|parse_url(http://spark.apache.org/path?query=1, QUERY, query)|
+-------------------------------------------------------------+
|                                                            1|
+-------------------------------------------------------------+

-- position
SELECT position('bar', 'foobarbar');
+---------------------------+
|position(bar, foobarbar, 1)|
+---------------------------+
|                          4|
+---------------------------+

SELECT position('bar', 'foobarbar', 5);
+---------------------------+
|position(bar, foobarbar, 5)|
+---------------------------+
|                          7|
+---------------------------+

SELECT POSITION('bar' IN 'foobarbar');
+-------------------------+
|locate(bar, foobarbar, 1)|
+-------------------------+
|                        4|
+-------------------------+

-- printf
SELECT printf("Hello World %d %s", 100, "days");
+------------------------------------+
|printf(Hello World %d %s, 100, days)|
+------------------------------------+
|                Hello World 100 days|
+------------------------------------+

-- regexp_extract
SELECT regexp_extract('100-200', '(\\d+)-(\\d+)', 1);
+---------------------------------------+
|regexp_extract(100-200, (\d+)-(\d+), 1)|
+---------------------------------------+
|                                    100|
+---------------------------------------+

-- regexp_extract_all
SELECT regexp_extract_all('100-200, 300-400', '(\\d+)-(\\d+)', 1);
+----------------------------------------------------+
|regexp_extract_all(100-200, 300-400, (\d+)-(\d+), 1)|
+----------------------------------------------------+
|                                          [100, 300]|
+----------------------------------------------------+

-- regexp_replace
SELECT regexp_replace('100-200', '(\\d+)', 'num');
+--------------------------------------+
|regexp_replace(100-200, (\d+), num, 1)|
+--------------------------------------+
|                               num-num|
+--------------------------------------+

-- repeat
SELECT repeat('123', 2);
+--------------+
|repeat(123, 2)|
+--------------+
|        123123|
+--------------+

-- replace
SELECT replace('ABCabc', 'abc', 'DEF');
+-------------------------+
|replace(ABCabc, abc, DEF)|
+-------------------------+
|                   ABCDEF|
+-------------------------+

-- right
SELECT right('Spark SQL', 3);
+-------------------+
|right(Spark SQL, 3)|
+-------------------+
|                SQL|
+-------------------+

-- rpad
SELECT rpad('hi', 5, '??');
+---------------+
|rpad(hi, 5, ??)|
+---------------+
|          hi???|
+---------------+

SELECT rpad('hi', 1, '??');
+---------------+
|rpad(hi, 1, ??)|
+---------------+
|              h|
+---------------+

SELECT rpad('hi', 5);
+--------------+
|rpad(hi, 5,  )|
+--------------+
|         hi   |
+--------------+

SELECT hex(rpad(unhex('aabb'), 5));
+--------------------------------+
|hex(rpad(unhex(aabb), 5, X'00'))|
+--------------------------------+
|                      AABB000000|
+--------------------------------+

SELECT hex(rpad(unhex('aabb'), 5, unhex('1122')));
+--------------------------------------+
|hex(rpad(unhex(aabb), 5, unhex(1122)))|
+--------------------------------------+
|                            AABB112211|
+--------------------------------------+

-- rtrim
SELECT rtrim('    SparkSQL   ');
+----------------------+
|rtrim(    SparkSQL   )|
+----------------------+
|              SparkSQL|
+----------------------+

-- sentences
SELECT sentences('Hi there! Good morning.');
+--------------------------------------+
|sentences(Hi there! Good morning., , )|
+--------------------------------------+
|                  [[Hi, there], [Go...|
+--------------------------------------+

-- soundex
SELECT soundex('Miller');
+---------------+
|soundex(Miller)|
+---------------+
|           M460|
+---------------+

-- space
SELECT concat(space(2), '1');
+-------------------+
|concat(space(2), 1)|
+-------------------+
|                  1|
+-------------------+

-- split
SELECT split('oneAtwoBthreeC', '[ABC]');
+--------------------------------+
|split(oneAtwoBthreeC, [ABC], -1)|
+--------------------------------+
|             [one, two, three, ]|
+--------------------------------+

SELECT split('oneAtwoBthreeC', '[ABC]', -1);
+--------------------------------+
|split(oneAtwoBthreeC, [ABC], -1)|
+--------------------------------+
|             [one, two, three, ]|
+--------------------------------+

SELECT split('oneAtwoBthreeC', '[ABC]', 2);
+-------------------------------+
|split(oneAtwoBthreeC, [ABC], 2)|
+-------------------------------+
|              [one, twoBthreeC]|
+-------------------------------+

-- split_part
SELECT split_part('11.12.13', '.', 3);
+--------------------------+
|split_part(11.12.13, ., 3)|
+--------------------------+
|                        13|
+--------------------------+

-- startswith
SELECT startswith('Spark SQL', 'Spark');
+----------------------------+
|startswith(Spark SQL, Spark)|
+----------------------------+
|                        true|
+----------------------------+

SELECT startswith('Spark SQL', 'SQL');
+--------------------------+
|startswith(Spark SQL, SQL)|
+--------------------------+
|                     false|
+--------------------------+

SELECT startswith('Spark SQL', null);
+---------------------------+
|startswith(Spark SQL, NULL)|
+---------------------------+
|                       null|
+---------------------------+

SELECT startswith(x'537061726b2053514c', x'537061726b');
+------------------------------------------------+
|startswith(X'537061726B2053514C', X'537061726B')|
+------------------------------------------------+
|                                            true|
+------------------------------------------------+

SELECT startswith(x'537061726b2053514c', x'53514c');
+--------------------------------------------+
|startswith(X'537061726B2053514C', X'53514C')|
+--------------------------------------------+
|                                       false|
+--------------------------------------------+

-- substr
SELECT substr('Spark SQL', 5);
+--------------------------------+
|substr(Spark SQL, 5, 2147483647)|
+--------------------------------+
|                           k SQL|
+--------------------------------+

SELECT substr('Spark SQL', -3);
+---------------------------------+
|substr(Spark SQL, -3, 2147483647)|
+---------------------------------+
|                              SQL|
+---------------------------------+

SELECT substr('Spark SQL', 5, 1);
+-----------------------+
|substr(Spark SQL, 5, 1)|
+-----------------------+
|                      k|
+-----------------------+

SELECT substr('Spark SQL' FROM 5);
+-----------------------------------+
|substring(Spark SQL, 5, 2147483647)|
+-----------------------------------+
|                              k SQL|
+-----------------------------------+

SELECT substr('Spark SQL' FROM -3);
+------------------------------------+
|substring(Spark SQL, -3, 2147483647)|
+------------------------------------+
|                                 SQL|
+------------------------------------+

SELECT substr('Spark SQL' FROM 5 FOR 1);
+--------------------------+
|substring(Spark SQL, 5, 1)|
+--------------------------+
|                         k|
+--------------------------+

-- substring
SELECT substring('Spark SQL', 5);
+-----------------------------------+
|substring(Spark SQL, 5, 2147483647)|
+-----------------------------------+
|                              k SQL|
+-----------------------------------+

SELECT substring('Spark SQL', -3);
+------------------------------------+
|substring(Spark SQL, -3, 2147483647)|
+------------------------------------+
|                                 SQL|
+------------------------------------+

SELECT substring('Spark SQL', 5, 1);
+--------------------------+
|substring(Spark SQL, 5, 1)|
+--------------------------+
|                         k|
+--------------------------+

SELECT substring('Spark SQL' FROM 5);
+-----------------------------------+
|substring(Spark SQL, 5, 2147483647)|
+-----------------------------------+
|                              k SQL|
+-----------------------------------+

SELECT substring('Spark SQL' FROM -3);
+------------------------------------+
|substring(Spark SQL, -3, 2147483647)|
+------------------------------------+
|                                 SQL|
+------------------------------------+

SELECT substring('Spark SQL' FROM 5 FOR 1);
+--------------------------+
|substring(Spark SQL, 5, 1)|
+--------------------------+
|                         k|
+--------------------------+

-- substring_index
SELECT substring_index('www.apache.org', '.', 2);
+-------------------------------------+
|substring_index(www.apache.org, ., 2)|
+-------------------------------------+
|                           www.apache|
+-------------------------------------+

-- to_binary
SELECT to_binary('abc', 'utf-8');
+---------------------+
|to_binary(abc, utf-8)|
+---------------------+
|           [61 62 63]|
+---------------------+

-- to_number
SELECT to_number('454', '999');
+-------------------+
|to_number(454, 999)|
+-------------------+
|                454|
+-------------------+

SELECT to_number('454.00', '000.00');
+-------------------------+
|to_number(454.00, 000.00)|
+-------------------------+
|                   454.00|
+-------------------------+

SELECT to_number('12,454', '99,999');
+-------------------------+
|to_number(12,454, 99,999)|
+-------------------------+
|                    12454|
+-------------------------+

SELECT to_number('$78.12', '$99.99');
+-------------------------+
|to_number($78.12, $99.99)|
+-------------------------+
|                    78.12|
+-------------------------+

SELECT to_number('12,454.8-', '99,999.9S');
+-------------------------------+
|to_number(12,454.8-, 99,999.9S)|
+-------------------------------+
|                       -12454.8|
+-------------------------------+

-- translate
SELECT translate('AaBbCc', 'abc', '123');
+---------------------------+
|translate(AaBbCc, abc, 123)|
+---------------------------+
|                     A1B2C3|
+---------------------------+

-- trim
SELECT trim('    SparkSQL   ');
+---------------------+
|trim(    SparkSQL   )|
+---------------------+
|             SparkSQL|
+---------------------+

SELECT trim(BOTH FROM '    SparkSQL   ');
+---------------------+
|trim(    SparkSQL   )|
+---------------------+
|             SparkSQL|
+---------------------+

SELECT trim(LEADING FROM '    SparkSQL   ');
+----------------------+
|ltrim(    SparkSQL   )|
+----------------------+
|           SparkSQL   |
+----------------------+

SELECT trim(TRAILING FROM '    SparkSQL   ');
+----------------------+
|rtrim(    SparkSQL   )|
+----------------------+
|              SparkSQL|
+----------------------+

SELECT trim('SL' FROM 'SSparkSQLS');
+-----------------------------+
|TRIM(BOTH SL FROM SSparkSQLS)|
+-----------------------------+
|                       parkSQ|
+-----------------------------+

SELECT trim(BOTH 'SL' FROM 'SSparkSQLS');
+-----------------------------+
|TRIM(BOTH SL FROM SSparkSQLS)|
+-----------------------------+
|                       parkSQ|
+-----------------------------+

SELECT trim(LEADING 'SL' FROM 'SSparkSQLS');
+--------------------------------+
|TRIM(LEADING SL FROM SSparkSQLS)|
+--------------------------------+
|                        parkSQLS|
+--------------------------------+

SELECT trim(TRAILING 'SL' FROM 'SSparkSQLS');
+---------------------------------+
|TRIM(TRAILING SL FROM SSparkSQLS)|
+---------------------------------+
|                         SSparkSQ|
+---------------------------------+

-- try_to_binary
SELECT try_to_binary('abc', 'utf-8');
+-------------------------+
|try_to_binary(abc, utf-8)|
+-------------------------+
|               [61 62 63]|
+-------------------------+

select try_to_binary('a!', 'base64');
+-------------------------+
|try_to_binary(a!, base64)|
+-------------------------+
|                     null|
+-------------------------+

select try_to_binary('abc', 'invalidFormat');
+---------------------------------+
|try_to_binary(abc, invalidFormat)|
+---------------------------------+
|                             null|
+---------------------------------+

-- try_to_number
SELECT try_to_number('454', '999');
+-----------------------+
|try_to_number(454, 999)|
+-----------------------+
|                    454|
+-----------------------+

SELECT try_to_number('454.00', '000.00');
+-----------------------------+
|try_to_number(454.00, 000.00)|
+-----------------------------+
|                       454.00|
+-----------------------------+

SELECT try_to_number('12,454', '99,999');
+-----------------------------+
|try_to_number(12,454, 99,999)|
+-----------------------------+
|                        12454|
+-----------------------------+

SELECT try_to_number('$78.12', '$99.99');
+-----------------------------+
|try_to_number($78.12, $99.99)|
+-----------------------------+
|                        78.12|
+-----------------------------+

SELECT try_to_number('12,454.8-', '99,999.9S');
+-----------------------------------+
|try_to_number(12,454.8-, 99,999.9S)|
+-----------------------------------+
|                           -12454.8|
+-----------------------------------+

-- ucase
SELECT ucase('SparkSql');
+---------------+
|ucase(SparkSql)|
+---------------+
|       SPARKSQL|
+---------------+

-- unbase64
SELECT unbase64('U3BhcmsgU1FM');
+----------------------+
|unbase64(U3BhcmsgU1FM)|
+----------------------+
|  [53 70 61 72 6B 2...|
+----------------------+

-- upper
SELECT upper('SparkSql');
+---------------+
|upper(SparkSql)|
+---------------+
|       SPARKSQL|
+---------------+
```
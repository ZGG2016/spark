# Number Patterns for Formatting and Parsing

[TOC]

## Description

> Functions such as `to_number` and `to_char` support converting between values of string and Decimal type. Such functions accept format strings indicating how to map between these types.

像 `to_number` 和 `to_char` 这种函数支持字符串和 Decimal 类型的互相转换。这种函数接收如何映射这些类型的格式字符串。

## Syntax

> Number format strings support the following syntax:

数值格式字符串支持如下语法：

```
  { ' [ MI | S ] [ $ ] 
      [ 0 | 9 | G | , ] [...] 
      [ . | D ] 
      [ 0 | 9 ] [...] 
      [ $ ] [ PR | MI | S ] ' }
```

## Elements

> Each number format string can contain the following elements (case insensitive):

- 0 or 9

	> Specifies an expected digit between 0 and 9.

	指定一个0到9的数字

	> A sequence of 0 or 9 in the format string matches a sequence of digits with the same or smaller size. If the 0/9 sequence starts with 0 and is before the decimal point, it requires matching the number of digits: when parsing, it matches only a digit sequence of the same size; when formatting, the result string adds left-padding with zeros to the digit sequence to reach the same size. Otherwise, the 0/9 sequence matches any digit sequence with the same or smaller size when parsing, and pads the digit sequence with spaces in the result string when formatting. Note that the digit sequence will become a ‘#’ sequence if the size is larger than the 0/9 sequence.
	
	在格式字符串中，0或9的一个序列匹配具有相同或更小大小的数字序列。

	如果0/9序列从0开始，在小数点之前，那么就要求匹配数字的数量：当解析时，它仅匹配具有相同大小的数字序列；当格式化时，结果字符串使用0在数字序列的左侧填充，以达到相同的大小。

	否则在解析时，0/9序列匹配具有相同或更小大小的任意数字序列，在格式化时，使用空格填充结果字符串的数字序列。

	注意，如果大小大于0/9序列，数字序列将成为 ‘#’ 序列。

- `.` or D

	> Specifies the position of the decimal point. This character may only be specified once.
	
	指定小数点的位置。这个字符仅被指定一次。

	> When parsing, the input string does not need to include a decimal point.

	在解析时，输入字符串不需要包含小数点。

- `,` or G

	> Specifies the position of the `,` grouping (thousands) separator.
	
	指定分组分隔符逗号的位置。

	> There must be a 0 or 9 to the left and right of each grouping separator. When parsing, the input string must match the grouping separator relevant for the size of the number.

	每个分组分隔符的左侧和右侧必须是0或9。在解析时，输入字符串必须匹配和数值大小相关联的分组分隔符。

- $

	> Specifies the location of the $ currency sign. This character may only be specified once.

	指定货币符号 $ 的位置。这个字符仅被指定一次。

- S

	> Specifies the position of an optional ‘+’ or ‘-‘ sign. This character may only be specified once.

	指定可选的 ‘+’ 或 ‘-‘ 符号的位置。这个字符仅被指定一次。

- MI

	> Specifies the position of an optional ‘-‘ sign (no ‘+’). This character may only be specified once.

	指定可选的 ‘-‘ 符号（没有‘+’）的位置。这个字符仅被指定一次。

	> When formatting, it prints a space for positive values.

	在格式化时，对于正值，会打印空格。

- PR

	> Maps negative input values to wrapping angle brackets (<1>) in the corresponding string.

	将负输入值映射到用尖括号括起来的字符串。

	> Positive input values do not receive wrapping angle brackets.
	
	正输入值不接受尖括号。

## Function types and error handling

> The `to_number` function accepts an input string and a format string argument. It requires that the input string matches the provided format and raises an error otherwise. The function then returns the corresponding Decimal value.

`to_number` 函数接收一个输入字符串和一个格式化字符串参数。要求输入字符串匹配提供的格式，否则就抛出错误。然后函数返回对应的 Decimal 值。

> The `try_to_number` function accepts an input string and a format string argument. It works the same as the `to_number` function except that it returns NULL instead of raising an error if the input string does not match the given number format.

`try_to_number` 函数接收一个输入字符串和一个格式化字符串参数。工作原理和 `to_number` 函数相同，除了如果输入字符串不匹配给定的数值格式，它会返回 NULL，而不是抛出一个错误。

> The `to_char` function accepts an input decimal and a format string argument. It requires that the input decimal matches the provided format and raises an error otherwise. The function then returns the corresponding string value.

`to_char` 函数接收一个输入 decimal 和一个格式化字符串参数。要求输入 decimal 匹配提供的格式，否则就抛出错误。然后函数返回对应的字符串值。

> All functions will fail if the given format string is invalid.

如果给定的格式字符串无效，那么所有函数将失败。

## Examples

The following examples use the `to_number`, `try_to_number`, `to_char`, and `try_to_char` SQL functions.

Note that the format string used in most of these examples expects:

- an optional sign at the beginning,
- followed by a dollar sign,
- followed by a number between 3 and 6 digits long,
- thousands separators,
- up to two digits beyond the decimal point.

### The to_number function

```sql
-- The negative number with currency symbol maps to characters in the format string.
> SELECT to_number('-$12,345.67', 'S$999,099.99');
  -12345.67
 
-- The '$' sign is not optional.
> SELECT to_number('5', '$9');
  Error: the input string does not match the given number format
 
-- The plus sign is optional, and so are fractional digits.
> SELECT to_number('$345', 'S$999,099.99');
  345.00
 
-- The format requires at least three digits.
> SELECT to_number('$45', 'S$999,099.99');
  Error: the input string does not match the given number format
 
-- The format requires at least three digits.
> SELECT to_number('$045', 'S$999,099.99');
  45.00
 
-- MI indicates an optional minus sign at the beginning or end of the input string.
> SELECT to_number('1234-', '999999MI');
  -1234
 
-- PR indicates optional wrapping angel brakets.
> SELECT to_number('9', '999PR')
  9
```

### The try_to_number function:

```
-- The '$' sign is not optional.
> SELECT try_to_number('5', '$9');
  NULL
 
-- The format requires at least three digits.
> SELECT try_to_number('$45', 'S$999,099.99');
  NULL
```

### The to_char function:

```
> SELECT to_char(decimal(454), '999');
  "454"

-- '99' can format digit sequence with a smaller size.
> SELECT to_char(decimal(1), '99.9');
  " 1.0"

-- '000' left-pads 0 for digit sequence with a smaller size.
> SELECT to_char(decimal(45.00), '000.00');
  "045.00"

> SELECT to_char(decimal(12454), '99,999');
  "12,454"

-- digit sequence with a larger size leads to '#' sequence.
> SELECT to_char(decimal(78.12), '$9.99');
  "$#.##"

-- 'S' can be at the end.
> SELECT try_to_char(decimal(-12454.8), '99,999.9S');
  "12,454.8-"

> SELECT try_to_char(decimal(12454.8), 'L99,999.9');
  Error: cannot resolve 'try_to_char(Decimal(12454.8), 'L99,999.9')' due to data type mismatch:
  Unexpected character 'L' found in the format string 'L99,999.9'; the structure of the format
  string must match: [MI|S] [$] [0|9|G|,]* [.|D] [0|9]* [$] [PR|MI|S]; line 1 pos 25
```
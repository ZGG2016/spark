# LIST FILE

[TOC]

## Description

`LIST FILE` lists the resources added by `ADD FILE`.

### Syntax

	LIST { FILE | FILES } file_name [ ... ]

## Examples

```sql
ADD FILE /tmp/test;
ADD FILE /tmp/test_2;
LIST FILE;
-- output for LIST FILE
file:/private/tmp/test
file:/private/tmp/test_2

LIST FILE /tmp/test /some/random/file /another/random/file
--output
file:/private/tmp/test
```
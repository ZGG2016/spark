# ADD FILE

[TOC]

## Description

> `ADD FILE` can be used to add a single file as well as a directory to the list of resources. The added resource can be listed using `LIST FILE`.

`ADD FILE` 用来将单个文件或目录添加到资源列表。

添加的资源可以使用 `LIST FILE` 列出来。

### Syntax

	ADD { FILE | FILES } resource_name [ ... ]

### Parameters

- resource_name

	The name of the file or directory to be added.

## Examples

```sql
ADD FILE /tmp/test;
ADD FILE "/path/to/file/abc.txt";
ADD FILE '/another/test.txt';
ADD FILE "/path with space/abc.txt";
ADD FILE "/path/to/some/directory";
ADD FILES "/path with space/cde.txt" '/path with space/fgh.txt';
```
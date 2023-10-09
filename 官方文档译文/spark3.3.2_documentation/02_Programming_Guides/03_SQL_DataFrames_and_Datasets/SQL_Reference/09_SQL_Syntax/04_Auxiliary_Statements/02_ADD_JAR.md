# ADD JAR

[TOC]

## Description

> `ADD JAR` adds a JAR file to the list of resources. The added JAR file can be listed using `LIST JAR`.

`ADD JAR` 将 JAR 文件添加到资源列表中。

添加的 JAR 文件可以使用 `LIST JAR` 列出来。

### Syntax

	ADD { JAR | JARS } file_name [ ... ]

### Parameters

- file_name

	The name of the JAR file to be added. It could be either on a local file system or a distributed file system or an Ivy URI. 

	Apache Ivy is a popular dependency manager focusing on flexibility and simplicity. Now we support two parameter in URI query string:

	- transitive: whether to download dependent jars related to your ivy URL. The parameter name is case-sensitive, and the parameter value is case-insensitive. If multiple transitive parameters are specified, the last one wins.

	- exclude: exclusion list during downloading Ivy URI jar and dependent jars.

	User can write Ivy URI such as:

	```
	ivy://group:module:version
	ivy://group:module:version?transitive=[true|false]
	ivy://group:module:version?transitive=[true|false]&exclude=group:module,group:module
	```

## Examples

```sql
ADD JAR /tmp/test.jar;
ADD JAR "/path/to/some.jar";
ADD JAR '/some/other.jar';
ADD JAR "/path with space/abc.jar";
ADD JARS "/path with space/def.jar" '/path with space/ghi.jar';
ADD JAR "ivy://group:module:version";
ADD JAR "ivy://group:module:version?transitive=false"
ADD JAR "ivy://group:module:version?transitive=true"
ADD JAR "ivy://group:module:version?exclude=group:module&transitive=true"
```
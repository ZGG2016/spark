# SET

[TOC]

## Description

> The `SET` command sets a property, returns the value of an existing property or returns all SQLConf properties with value and meaning.

`SET` 命令设置一个属性，返回一个已存在属性的值，或者返回所有 SQLConf 属性，包括值和含义。

### Syntax

	SET
	SET [ -v ]
	SET property_key[ = property_value ]

### Parameters

- -v

	Outputs the key, value and meaning of existing SQLConf properties.

- property_key

	Returns the value of specified property key.

- property_key=property_value

	Sets the value for a given property key. If an old value exists for a given property key, then it gets overridden by the new value.

## Examples

```SQL
-- Set a property.
SET spark.sql.variable.substitute=false;

-- List all SQLConf properties with value and meaning.
SET -v;

-- List all SQLConf properties with value for current session.
SET;

-- List the value of specified property key.
SET spark.sql.variable.substitute;
+-----------------------------+-----+
|                          key|value|
+-----------------------------+-----+
|spark.sql.variable.substitute|false|
+-----------------------------+-----+
```
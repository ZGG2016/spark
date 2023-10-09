# RESET

[TOC]

## Description

> The `RESET` command resets runtime configurations specific to the current session which were set via the `SET` command to their default values.

`RESET` 命令会重置特定于当前会话的运行时配置，此配置通过 `SET` 命令设置默认值。

### Syntax

	RESET;

	RESET configuration_key;

### Parameters

- (none)

	Reset any runtime configurations specific to the current session which were set via the `SET` command to their default values.

- configuration_key

	Restore the value of the `configuration_key` to the default value. If the default value is undefined, the `configuration_key` will be removed.

## Examples

```sql
-- Reset any runtime configurations specific to the current session which were set via the SET command to their default values.
RESET;

-- If you start your application with --conf spark.foo=bar and set spark.foo=foobar in runtime, the example below will restore it to 'bar'. If spark.foo is not specified during starting, the example bellow will remove this config from the SQLConf. It will ignore nonexistent keys.
RESET spark.abc;
```
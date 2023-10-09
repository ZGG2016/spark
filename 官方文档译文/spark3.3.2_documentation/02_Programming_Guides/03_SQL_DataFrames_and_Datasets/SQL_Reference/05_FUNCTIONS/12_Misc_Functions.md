# Misc Functions

[TOC]

## Misc Functions

### aes_decrypt(expr, key[, mode[, padding]])

Returns a decrypted value of `expr` using AES in `mode` with `padding`. Key lengths of 16, 24 and 32 bits are supported. Supported combinations of (`mode`, `padding`) are ('ECB', 'PKCS') and ('GCM', 'NONE'). The default mode is GCM.

对 `expr` 进行解密

### aes_encrypt(expr, key[, mode[, padding]])

Returns an encrypted value of `expr` using AES in given `mode` with the specified `padding`. Key lengths of 16, 24 and 32 bits are supported. Supported combinations of (`mode`, `padding`) are ('ECB', 'PKCS') and ('GCM', 'NONE'). The default mode is GCM.

对 `expr` 进行加密

### assert_true(expr)

Throws an exception if `expr` is not true.

如果 `expr` 不是 true, 就抛一个异常。

### current_catalog()

Returns the current catalog.

返回当前的 catalog

### current_database()

Returns the current database.

返回当前的 database

### current_user()

user name of current execution context.

当前执行上下文的用户名称

### input_file_block_length()

Returns the length of the block being read, or -1 if not available.

返回正在被读取的块的长度。如果不可用，就返回-1

### input_file_block_start()

Returns the start offset of the block being read, or -1 if not available.

返回正在被读取的块的开始偏移量。如果不可用，就返回-1

### input_file_name()

Returns the name of the file being read, or empty string if not available.

返回正在被读取的文件的名称。如果不可用，就返回空字符串

### java_method(class, method[, arg1[, arg2 ..]])

Calls a method with reflection.

使用反射调用一个方法

### monotonically_increasing_id()

Returns monotonically increasing 64-bit integers. The generated ID is guaranteed to be monotonically increasing and unique, but not consecutive. The current implementation puts the partition ID in the upper 31 bits, and the lower 33 bits represent the record number within each partition. The assumption is that the data frame has less than 1 billion partitions, and each partition has less than 8 billion records. The function is non-deterministic because its result depends on partition IDs.

返回单调递增的64位的整型。生成的 ID 是单调递增的且是唯一的，但不是连续的。

当前的实现将分区 ID 放入高31位，低33位表示在每个分区中的记录数量。做出的假设就是数据帧小于100万个分区，且每个分区小于800万条记录。

这个函数的结果不是确定的，因为它的结果依赖于分区 ID

### reflect(class, method[, arg1[, arg2 ..]])

Calls a method with reflection.

使用反射调用一个方法

### spark_partition_id()

Returns the current partition id.

返回当前的分区 ID

### typeof(expr)

Return DDL-formatted type string for the data type of the input.

对于输入数据类型，返回 DDL 格式化的类型字符串。

### uuid()

Returns an universally unique identifier (UUID) string. The value is returned as a canonical UUID 36-character string.

返回一个在各种情况下都唯一的标识字符串。值作为一个标准的 UUID 36字符字符串返回。

### version()

Returns the Spark version. The string contains 2 fields, the first being a release version and the second being a git revision.

返回 spark 版本。字符串包含两个字段，第一个字段是发行版本，第二个字段是 git 版本。

## Examples

```sql
-- aes_decrypt
SELECT aes_decrypt(unhex('83F16B2AA704794132802D248E6BFD4E380078182D1544813898AC97E709B28A94'), '0000111122223333');
+----------------------------------------------------------------------------------------------------------------------+
|aes_decrypt(unhex(83F16B2AA704794132802D248E6BFD4E380078182D1544813898AC97E709B28A94), 0000111122223333, GCM, DEFAULT)|
+----------------------------------------------------------------------------------------------------------------------+
|                                                                                                      [53 70 61 72 6B]|
+----------------------------------------------------------------------------------------------------------------------+

SELECT aes_decrypt(unhex('6E7CA17BBB468D3084B5744BCA729FB7B2B7BCB8E4472847D02670489D95FA97DBBA7D3210'), '0000111122223333', 'GCM');
+------------------------------------------------------------------------------------------------------------------------------+
|aes_decrypt(unhex(6E7CA17BBB468D3084B5744BCA729FB7B2B7BCB8E4472847D02670489D95FA97DBBA7D3210), 0000111122223333, GCM, DEFAULT)|
+------------------------------------------------------------------------------------------------------------------------------+
|                                                                                                          [53 70 61 72 6B 2...|
+------------------------------------------------------------------------------------------------------------------------------+

SELECT aes_decrypt(unbase64('3lmwu+Mw0H3fi5NDvcu9lg=='), '1234567890abcdef', 'ECB', 'PKCS');
+----------------------------------------------------------------------------+
|aes_decrypt(unbase64(3lmwu+Mw0H3fi5NDvcu9lg==), 1234567890abcdef, ECB, PKCS)|
+----------------------------------------------------------------------------+
|                                                        [53 70 61 72 6B 2...|
+----------------------------------------------------------------------------+

-- aes_encrypt
SELECT hex(aes_encrypt('Spark', '0000111122223333'));
+-------------------------------------------------------+
|hex(aes_encrypt(Spark, 0000111122223333, GCM, DEFAULT))|
+-------------------------------------------------------+
|                                   89C9013876D99EA19...|
+-------------------------------------------------------+

SELECT hex(aes_encrypt('Spark SQL', '0000111122223333', 'GCM'));
+-----------------------------------------------------------+
|hex(aes_encrypt(Spark SQL, 0000111122223333, GCM, DEFAULT))|
+-----------------------------------------------------------+
|                                       696FAAA46679AA847...|
+-----------------------------------------------------------+

SELECT base64(aes_encrypt('Spark SQL', '1234567890abcdef', 'ECB', 'PKCS'));
+-----------------------------------------------------------+
|base64(aes_encrypt(Spark SQL, 1234567890abcdef, ECB, PKCS))|
+-----------------------------------------------------------+
|                                       3lmwu+Mw0H3fi5NDv...|
+-----------------------------------------------------------+

-- assert_true
SELECT assert_true(0 < 1);
+--------------------------------------------+
|assert_true((0 < 1), '(0 < 1)' is not true!)|
+--------------------------------------------+
|                                        null|
+--------------------------------------------+

-- current_catalog
SELECT current_catalog();
+-----------------+
|current_catalog()|
+-----------------+
|    spark_catalog|
+-----------------+

-- current_database
SELECT current_database();
+------------------+
|current_database()|
+------------------+
|           default|
+------------------+

-- current_user
SELECT current_user();
+--------------+
|current_user()|
+--------------+
|      spark-rm|
+--------------+

-- input_file_block_length
SELECT input_file_block_length();
+-------------------------+
|input_file_block_length()|
+-------------------------+
|                       -1|
+-------------------------+

-- input_file_block_start
SELECT input_file_block_start();
+------------------------+
|input_file_block_start()|
+------------------------+
|                      -1|
+------------------------+

-- input_file_name
SELECT input_file_name();
+-----------------+
|input_file_name()|
+-----------------+
|                 |
+-----------------+

-- java_method
SELECT java_method('java.util.UUID', 'randomUUID');
+---------------------------------------+
|java_method(java.util.UUID, randomUUID)|
+---------------------------------------+
|                   144baaff-2982-463...|
+---------------------------------------+

SELECT java_method('java.util.UUID', 'fromString', 'a5cf6c42-0c85-418f-af6c-3e4e5b1328f2');
+-----------------------------------------------------------------------------+
|java_method(java.util.UUID, fromString, a5cf6c42-0c85-418f-af6c-3e4e5b1328f2)|
+-----------------------------------------------------------------------------+
|                                                         a5cf6c42-0c85-418...|
+-----------------------------------------------------------------------------+

-- monotonically_increasing_id
SELECT monotonically_increasing_id();
+-----------------------------+
|monotonically_increasing_id()|
+-----------------------------+
|                            0|
+-----------------------------+

-- reflect
SELECT reflect('java.util.UUID', 'randomUUID');
+-----------------------------------+
|reflect(java.util.UUID, randomUUID)|
+-----------------------------------+
|               74e3b558-ffe6-4c0...|
+-----------------------------------+

SELECT reflect('java.util.UUID', 'fromString', 'a5cf6c42-0c85-418f-af6c-3e4e5b1328f2');
+-------------------------------------------------------------------------+
|reflect(java.util.UUID, fromString, a5cf6c42-0c85-418f-af6c-3e4e5b1328f2)|
+-------------------------------------------------------------------------+
|                                                     a5cf6c42-0c85-418...|
+-------------------------------------------------------------------------+

-- spark_partition_id
SELECT spark_partition_id();
+--------------------+
|SPARK_PARTITION_ID()|
+--------------------+
|                   0|
+--------------------+

-- typeof
SELECT typeof(1);
+---------+
|typeof(1)|
+---------+
|      int|
+---------+

SELECT typeof(array(1));
+----------------+
|typeof(array(1))|
+----------------+
|      array<int>|
+----------------+

-- uuid
SELECT uuid();
+--------------------+
|              uuid()|
+--------------------+
|52b80907-c540-4ff...|
+--------------------+

-- version
SELECT version();
+--------------------+
|           version()|
+--------------------+
|3.3.2 5103e00c4ce...|
+--------------------+
```
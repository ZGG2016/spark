# Functions

> Spark SQL provides two function features to meet a wide range of user needs: built-in functions and user-defined functions (UDFs). Built-in functions are commonly used routines that Spark SQL predefines and a complete list of the functions can be found in the [Built-in Functions](https://spark.apache.org/docs/3.3.2/api/sql/) API document. UDFs allow users to define their own functions when the system’s built-in functions are not enough to perform the desired task.

Spark SQL 提供了两种函数特征来满足更多的用户需求：内建函数和用户自定义函数。

## Built-in Functions

> Spark SQL has some categories of frequently-used built-in functions for aggregation, arrays/maps, date/timestamp, and JSON data. This subsection presents the usages and descriptions of these functions.

### Scalar Functions

- [Array Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#array-functions)
- [Map Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#map-functions)
- [Date and Timestamp Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#date-and-timestamp-functions)
- [JSON Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#json-functions)
- [Mathematical Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#mathematical-functions)
- [String Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#string-functions)
- [Bitwise Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#bitwise-functions)
- [Conversion Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#conversion-functions)
- [Conditional Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#conditional-functions)
- [Predicate Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#predicate-functions)
- [Csv Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#csv-functions)
- [Misc Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#misc-functions)

------------------------------

- [Array Functions]()
- [Map Functions]()
- [Date and Timestamp Functions]()
- [JSON Functions]()
- [Mathematical Functions]()
- [String Functions]()
- [Bitwise Functions]()
- [Conversion Functions]()
- [Conditional Functions]()
- [Predicate Functions]()
- [Csv Functions]()
- [Misc Functions]()

### Aggregate-like Functions

- [Aggregate Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#aggregate-functions)
- [Window Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#window-functions)

------------------------------

- [Aggregate Functions]()
- [Window Functions]()

### Generator Functions

- [Generator Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#generator-functions)

------------------------------

- [Generator Functions]()

## UDFs (User-Defined Functions)

> User-Defined Functions (UDFs) are a feature of Spark SQL that allows users to define their own functions when the system’s built-in functions are not enough to perform the desired task. To use UDFs in Spark SQL, users must first define the function, then register the function with Spark, and finally call the registered function. The User-Defined Functions can act on a single row or act on multiple rows at once. Spark SQL also supports integration of existing Hive implementations of UDFs, UDAFs and UDTFs.

对于用户自定义函数，首先要定义函数，然后要注册函数，最后再调用注册的函数。

用户自定义函数可以在一行上操作，也可以在多行上操作。

spark也支持 hive 的 UDFs, UDAFs 和 UDTFs 的嵌入。

- [Scalar User-Defined Functions (UDFs)](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-scalar.html)
- [User-Defined Aggregate Functions (UDAFs)](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-aggregate.html)
- [Integration with Hive UDFs/UDAFs/UDTFs](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-hive.html)

------------------------------

- [Scalar User-Defined Functions (UDFs)]()
- [User-Defined Aggregate Functions (UDAFs)]()
- [Integration with Hive UDFs/UDAFs/UDTFs]()
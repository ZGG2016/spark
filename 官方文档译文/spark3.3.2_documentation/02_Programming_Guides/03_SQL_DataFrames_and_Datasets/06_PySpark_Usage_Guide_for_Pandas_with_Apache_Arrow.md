# PySpark Usage Guide for Pandas with Apache Arrow

The Arrow usage guide is now archived on [this page](https://spark.apache.org/docs/latest/api/python/user_guide/sql/arrow_pandas.html).

---------------------------------------------------------------------

# Apache Arrow in PySpark

[TOC]

> Apache Arrow is an in-memory columnar data format that is used in Spark to efficiently transfer data between JVM and Python processes. This currently is most beneficial to Python users that work with Pandas/NumPy data. Its usage is not automatic and might require some minor changes to configuration or code to take full advantage and ensure compatibility. This guide will give a high-level description of how to use Arrow in Spark and highlight any differences when working with Arrow-enabled data.

Apache Arrow 是一个内存柱状数据格式，在 spark 中，用于高效地在 JVM 和 Python 进程间传输数据。

当前，这是对 Python 用户使用 Pandas/NumPy 数据最有用的部分。它的用法并不是自动的，可能需要对配置或代码做一些微调，以充分利用和确保兼容性。

本指南将提供如何在 Spark 中使用 Arrow 的高级描述，并强调在使用启用 Arrow 的数据时的任何差异。

## Ensure PyArrow Installed

> To use Apache Arrow in PySpark, [the recommended version of PyArrow](https://spark.apache.org/docs/latest/api/python/user_guide/sql/arrow_pandas.html#recommended-pandas-and-pyarrow-versions) should be installed. If you install PySpark using pip, then PyArrow can be brought in as an extra dependency of the SQL module with the command `pip install pyspark[sql]`. Otherwise, you must ensure that PyArrow is installed and available on all cluster nodes. You can install it using pip or conda from the conda-forge channel. See PyArrow [installation](https://arrow.apache.org/docs/python/install.html) for details.

要在 PySpark 中使用 Apache Arrow, 应该安装推荐的 PyArrow 版本。

如果使用 pip 安装 PySpark, 那么 PyArrow 可以作为 SQL 模板的额外依赖被添加。

否则，你必须确认已安装了 PyArrow, 且在所有集群节点上可用。你可以使用 pip 或 conda 安装 PyArrow.

## Enabling for Conversion to/from Pandas

> Arrow is available as an optimization when converting a Spark DataFrame to a Pandas DataFrame using the call [DataFrame.toPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.toPandas.html#pyspark.sql.DataFrame.toPandas) and when creating a Spark DataFrame from a Pandas DataFrame with [SparkSession.createDataFrame()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.SparkSession.createDataFrame.html#pyspark.sql.SparkSession.createDataFrame). To use Arrow when executing these calls, users need to first set the Spark configuration `spark.sql.execution.arrow.pyspark.enabled` to `true`. This is disabled by default.

当调用 `DataFrame.toPandas()` 把一个 Spark DataFrame 转成 Pandas DataFrame 时，和使用 `SparkSession.createDataFrame()` 根据 Pandas DataFrame 创建 Spark DataFrame 时， Arrow 可作为一种优化手段使用.

当执行这些调用时，为了使用 Arrow, 用户需要首先将 `spark.sql.execution.arrow.pyspark.enabled` 配置为 `true`. 这默认是禁用的。  

> In addition, optimizations enabled by `spark.sql.execution.arrow.pyspark.enabled` could fallback automatically to non-Arrow optimization implementation if an error occurs before the actual computation within Spark. This can be controlled by `spark.sql.execution.arrow.pyspark.fallback.enabled`.

如果在真正的计算开始之前出现了错误，那么通过启用 `spark.sql.execution.arrow.pyspark.enabled` 的优化会自动回调非 Arrow 优化实现。

这可以通过 `spark.sql.execution.arrow.pyspark.fallback.enabled` 控制。

```python
import numpy as np
import pandas as pd

# Enable Arrow-based columnar data transfers
spark.conf.set("spark.sql.execution.arrow.pyspark.enabled", "true")

# Generate a Pandas DataFrame
pdf = pd.DataFrame(np.random.rand(100, 3))

# Create a Spark DataFrame from a Pandas DataFrame using Arrow
df = spark.createDataFrame(pdf)

# Convert the Spark DataFrame back to a Pandas DataFrame using Arrow
result_pdf = df.select("*").toPandas()

print("Pandas DataFrame result statistics:\n%s\n" % str(result_pdf.describe()))
```

> Using the above optimizations with Arrow will produce the same results as when Arrow is not enabled.

使用 Arrow 的上述优化将产生和不启用 Arrow 相同的结果。

> Note that even with Arrow, [DataFrame.toPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.toPandas.html#pyspark.sql.DataFrame.toPandas) results in the collection of all records in the DataFrame to the driver program and should be done on a small subset of the data. Not all Spark data types are currently supported and an error can be raised if a column has an unsupported type. If an error occurs during [SparkSession.createDataFrame()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.SparkSession.createDataFrame.html#pyspark.sql.SparkSession.createDataFrame), Spark will fall back to create the DataFrame without Arrow.

即使是使用 Arrow, DataFrame.toPandas() 会把 DataFrame 中的所有的记录拉到驱动程序中，应该在数据的少量子集上完成。

当前并不支持所有的 spark 数据类型，如果一列是不支持的类型，就会报错。如果错误在 SparkSession.createDataFrame() 期间出现，那么 spark 将回退到不使用 Arrow 创建 DataFrame.

## Pandas UDFs (a.k.a. Vectorized UDFs)

> Pandas UDFs are user defined functions that are executed by Spark using Arrow to transfer data and Pandas to work with the data, which allows vectorized operations. A Pandas UDF is defined using the [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf) as a decorator or to wrap the function, and no additional configuration is required. A Pandas UDF behaves as a regular PySpark function API in general.

Pandas UDFs 是由 spark 执行的用户定义函数，它使用 Arrow 传输数据，使用 Pandas 处理数据，允许向量化操作。

将 pandas_udf() 作为装饰器或封装函数来定义 Pandas UDF, 无需额外配置。

通常，一个 Pandas UDF 行为和普通 PySpark 函数 API 相同。

> Before Spark 3.0, Pandas UDFs used to be defined with `pyspark.sql.functions.PandasUDFType`. From Spark 3.0 with Python 3.6+, you can also use Python type hints. Using [Python type hints](https://www.python.org/dev/peps/pep-0484) is preferred and using `pyspark.sql.functions.PandasUDFType` will be deprecated in the future release.

在 Spark 3.0 之前，使用 `pyspark.sql.functions.PandasUDFType` 定义 Pandas UDFs.

从 Spark 3.0 和 Python 3.6 开始，也可以使用 Python 类型提示。

最后使用 Python 类型提示，使用 `pyspark.sql.functions.PandasUDFType` 的方式将被弃用。

> Note that the type hint should use `pandas.Series` in all cases but there is one variant that `pandas.DataFrame` should be used for its input or output type hint instead when the input or output column is of [StructType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.StructType.html#pyspark.sql.types.StructType). The following example shows a Pandas UDF which takes long column, string column and struct column, and outputs a struct column. It requires the function to specify the type hints of `pandas.Series` and `pandas.DataFrame` as below:

在所有情况下，类型提示应该使用 `pandas.Series`, 但是存在一种情况，就是当输入或输出列是 StructType 时，应该使用 `pandas.DataFrame`.

```python
import pandas as pd

from pyspark.sql.functions import pandas_udf

@pandas_udf("col1 string, col2 long")  # type: ignore[call-overload]
def func(s1: pd.Series, s2: pd.Series, s3: pd.DataFrame) -> pd.DataFrame:
    s3['col2'] = s1 + s2.str.len()
    return s3

# Create a Spark DataFrame that has three columns including a struct column.
df = spark.createDataFrame(
    [[1, "a string", ("a nested string",)]],
    "long_col long, string_col string, struct_col struct<col1:string>")

df.printSchema()
# root
# |-- long_column: long (nullable = true)
# |-- string_column: string (nullable = true)
# |-- struct_column: struct (nullable = true)
# |    |-- col1: string (nullable = true)

df.select(func("long_col", "string_col", "struct_col")).printSchema()
# |-- func(long_col, string_col, struct_col): struct (nullable = true)
# |    |-- col1: string (nullable = true)
# |    |-- col2: long (nullable = true)
```

> In the following sections, it describes the combinations of the supported type hints. For simplicity, `pandas.DataFrame` variant is omitted.

下面的部分描述了支持的类型提示的组合。为了简洁，忽略了 `pandas.DataFrame`.


### Series to Series

> The type hint can be expressed as `pandas.Series, … -> pandas.Series`.

类型提示可以这样表达: `pandas.Series, … -> pandas.Series`

> By using [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf) with the function having such type hints above, it creates a Pandas UDF where the given function takes one or more `pandas.Series` and outputs one `pandas.Series`. The output of the function should always be of the same length as the input. Internally, PySpark will execute a Pandas UDF by splitting columns into batches and calling the function for each batch as a subset of the data, then concatenating the results together.

通过使用具有上述类型提示的 pandas_udf() 修改的函数，创建一个 Pandas UDF, 函数会接收一个或多个 `pandas.Series`，输出一个 `pandas.Series`.

函数的输出的长度总是和输入相同。

在内部，PySpark 执行 Pandas UDF 的逻辑是：将列划分成多个批次（每个批次就是数据的子集），并在每个批次上调用函数，然后将结果连接起来。 

The following example shows how to create this Pandas UDF that computes the product of 2 columns.

```python
import pandas as pd

from pyspark.sql.functions import col, pandas_udf
from pyspark.sql.types import LongType

# Declare the function and create the UDF
def multiply_func(a: pd.Series, b: pd.Series) -> pd.Series:
    return a * b

multiply = pandas_udf(multiply_func, returnType=LongType())  # type: ignore[call-overload]

# The function for a pandas_udf should be able to execute with local Pandas data
x = pd.Series([1, 2, 3])
print(multiply_func(x, x))
# 0    1
# 1    4
# 2    9
# dtype: int64

# Create a Spark DataFrame, 'spark' is an existing SparkSession
df = spark.createDataFrame(pd.DataFrame(x, columns=["x"]))

# Execute function as a Spark vectorized UDF
df.select(multiply(col("x"), col("x"))).show()
# +-------------------+
# |multiply_func(x, x)|
# +-------------------+
# |                  1|
# |                  4|
# |                  9|
# +-------------------+
```

For detailed usage, please see [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf).

### Iterator of Series to Iterator of Series

> The type hint can be expressed as `Iterator[pandas.Series] -> Iterator[pandas.Series]`.

类型提示可以这样表达: `Iterator[pandas.Series] -> Iterator[pandas.Series]`

> By using [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf) with the function having such type hints above, it creates a Pandas UDF where the given function takes an iterator of `pandas.Series` and outputs an iterator of `pandas.Series`. The length of the entire output from the function should be the same length of the entire input; therefore, it can prefetch the data from the input iterator as long as the lengths are the same. In this case, the created Pandas UDF requires one input column when the Pandas UDF is called. To use multiple input columns, a different type hint is required. See Iterator of Multiple Series to Iterator of Series.

通过使用具有上述类型提示的 pandas_udf() 修改的函数，创建一个 Pandas UDF, 函数会接收一个 pandas.Series 迭代器，输出一个 pandas.Series 迭代器。

函数的整个输出的长度应该和整个输入相同。因此，只有长度相同，它就可以从输入迭代器中预先获取数据。

在这种情况下，当 Pandas UDF 被调用时，创建的 Pandas UDF 要求一个输入列。为了使用多个输入列，要求不同的类型提示，见下面部分。

> It is also useful when the UDF execution requires initializing some states although internally it works identically as Series to Series case. The pseudocode below illustrates the example.

尽管内部工作原理和 Series to Series 情况相同，但是在 UDF 执行时提供一些初始状态是非常有用的。

```python
@pandas_udf("long")
def calculate(iterator: Iterator[pd.Series]) -> Iterator[pd.Series]:
    # Do some expensive initialization with a state
    state = very_expensive_initialization()
    for x in iterator:
        # Use that state for the whole iterator.
        yield calculate_with_state(x, state)

df.select(calculate("value")).show()
```

The following example shows how to create this Pandas UDF:

```python
from typing import Iterator

import pandas as pd

from pyspark.sql.functions import pandas_udf

pdf = pd.DataFrame([1, 2, 3], columns=["x"])
df = spark.createDataFrame(pdf)

# Declare the function and create the UDF
@pandas_udf("long")  # type: ignore[call-overload]
def plus_one(iterator: Iterator[pd.Series]) -> Iterator[pd.Series]:
    for x in iterator:
        yield x + 1

df.select(plus_one("x")).show()
# +-----------+
# |plus_one(x)|
# +-----------+
# |          2|
# |          3|
# |          4|
# +-----------+
```

For detailed usage, please see [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf).

### Iterator of Multiple Series to Iterator of Series

> The type hint can be expressed as `Iterator[Tuple[pandas.Series, ...]] -> Iterator[pandas.Series]`.

类型提示可以这样表达: `Iterator[Tuple[pandas.Series, ...]] -> Iterator[pandas.Series]`

> By using `pandas_udf()` with the function having such type hints above, it creates a Pandas UDF where the given function takes an iterator of a tuple of multiple `pandas.Series` and outputs an iterator of `pandas.Series`. In this case, the created pandas UDF requires multiple input columns as many as the series in the tuple when the Pandas UDF is called. Otherwise, it has the same characteristics and restrictions as the Iterator of Series to Iterator of Series case.

通过使用具有上述类型提示的 pandas_udf() 修改的函数，创建一个 Pandas UDF, 函数会接收多个 pandas.Series 组成的元组迭代器，输出一个 pandas.Series 迭代器。

在这种情况下，当 Pandas UDF 被调用时，创建的 Pandas UDF 要求多个输入列，和元组中的序列一样多。否则，它具有与 Iterator of Series to Iterator of Series 相同的特征和限制。

The following example shows how to create this Pandas UDF:

```python
from typing import Iterator, Tuple

import pandas as pd

from pyspark.sql.functions import pandas_udf

pdf = pd.DataFrame([1, 2, 3], columns=["x"])
df = spark.createDataFrame(pdf)

# Declare the function and create the UDF
@pandas_udf("long")  # type: ignore[call-overload]
def multiply_two_cols(
        iterator: Iterator[Tuple[pd.Series, pd.Series]]) -> Iterator[pd.Series]:
    for a, b in iterator:
        yield a * b

df.select(multiply_two_cols("x", "x")).show()
# +-----------------------+
# |multiply_two_cols(x, x)|
# +-----------------------+
# |                      1|
# |                      4|
# |                      9|
# +-----------------------+
```

For detailed usage, please see [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf).

### Series to Scalar

> The type hint can be expressed as `pandas.Series, … -> Any`.

类型提示可以这样表达: `pandas.Series, … -> Any`

> By using `pandas_udf()` with the function having such type hints above, it creates a Pandas UDF similar to PySpark’s aggregate functions. The given function takes `pandas.Series` and returns a scalar value. The return type should be a primitive data type, and the returned scalar can be either a python primitive type, e.g., `int` or `float` or a numpy data type, e.g., `numpy.int64` or `numpy.float64`. `Any` should ideally be a specific scalar type accordingly.

通过使用具有上述类型提示的 pandas_udf() 修改的函数，创建一个 Pandas UDF, 它类似 PySpark 的聚合函数。函数会接收 pandas.Series, 返回一个标量值。

返回的类型应该是基础数据类型，返回的标量值要么是 python 基础数据类型，要么是 numpy 数据类型。

理想情况下，Any 应该是一个特定的标量类型。

> This UDF can be also used with [GroupedData.agg()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.GroupedData.agg.html#pyspark.sql.GroupedData.agg) and `Window`. It defines an aggregation from one or more `pandas.Series` to a scalar value, where each `pandas.Series` represents a column within the group or window.

这个 UDF 也可以和 GroupedData.agg() 与 Window 一起使用。它定义了从一个或多个 pandas.Series 到一个标量值的聚合操作，每个 pandas.Series 表示组内或窗口内的一列。

> Note that this type of UDF does not support partial aggregation and all data for a group or window will be loaded into memory. Also, only unbounded window is supported with Grouped aggregate Pandas UDFs currently. The following example shows how to use this type of UDF to compute mean with a group-by and window operations:

这种 UDF 类型不支持部分聚合，组内或窗口内的所有数据都被载入到内存中。而且当前，仅无界窗口支持分组聚合 Pandas UDFs.

```python
import pandas as pd

from pyspark.sql.functions import pandas_udf
from pyspark.sql import Window

df = spark.createDataFrame(
    [(1, 1.0), (1, 2.0), (2, 3.0), (2, 5.0), (2, 10.0)],
    ("id", "v"))

# Declare the function and create the UDF
@pandas_udf("double")  # type: ignore[call-overload]
def mean_udf(v: pd.Series) -> float:
    return v.mean()

df.select(mean_udf(df['v'])).show()
# +-----------+
# |mean_udf(v)|
# +-----------+
# |        4.2|
# +-----------+

df.groupby("id").agg(mean_udf(df['v'])).show()
# +---+-----------+
# | id|mean_udf(v)|
# +---+-----------+
# |  1|        1.5|
# |  2|        6.0|
# +---+-----------+

w = Window \
    .partitionBy('id') \
    .rowsBetween(Window.unboundedPreceding, Window.unboundedFollowing)
df.withColumn('mean_v', mean_udf(df['v']).over(w)).show()
# +---+----+------+
# | id|   v|mean_v|
# +---+----+------+
# |  1| 1.0|   1.5|
# |  1| 2.0|   1.5|
# |  2| 3.0|   6.0|
# |  2| 5.0|   6.0|
# |  2|10.0|   6.0|
# +---+----+------+
```

For detailed usage, please see [pandas_udf()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.functions.pandas_udf.html#pyspark.sql.functions.pandas_udf).

## Pandas Function APIs

> Pandas Function APIs can directly apply a Python native function against the whole [DataFrame](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.html#pyspark.sql.DataFrame) by using Pandas instances. Internally it works similarly with Pandas UDFs by using Arrow to transfer data and Pandas to work with the data, which allows vectorized operations. However, a Pandas Function API behaves as a regular API under PySpark DataFrame instead of [Column](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.Column.html#pyspark.sql.Column), and Python type hints in Pandas Functions APIs are optional and do not affect how it works internally at this moment although they might be required in the future.

Pandas 函数 APIs 能直接在整个 DataFrame 上应用 Python 原生函数。内部原理和 Pandas UDFs 类似，使用 Arrow 传输数据，使用 Pandas 处理数据，允许向量化操作。

然而，一个 Pandas 函数 API 对于 PySpark DataFrame ，而不是 Column ，的作用和普通 API 一样。Pandas 函数 APIs 中的 Python 类型提示是可选的，并不会影响它内部工作原理，尽管可能在未来是必须的。

> From Spark 3.0, grouped map pandas UDF is now categorized as a separate Pandas Function API, `DataFrame.groupby().applyInPandas()`. It is still possible to use it with `pyspark.sql.functions.PandasUDFType` and `DataFrame.groupby().apply()` as it was; however, it is preferred to use `DataFrame.groupby().applyInPandas()` directly. Using `pyspark.sql.functions.PandasUDFType` will be deprecated in the future.

从 Spark 3.0, 分组映射 pandas UDF 被划分到一个独立的 Pandas 函数 API, 即 DataFrame.groupby().applyInPandas().

使用 pyspark.sql.functions.PandasUDFType 和 DataFrame.groupby().apply() 仍然可以使用，但推荐使用 DataFrame.groupby().applyInPandas(). 在未来，将弃用 pyspark.sql.functions.PandasUDFType.

### Grouped Map

> Grouped map operations with Pandas instances are supported by `DataFrame.groupby().applyInPandas()` which requires a Python function that takes a `pandas.DataFrame` and return another `pandas.DataFrame`. It maps each group to each `pandas.DataFrame` in the Python function.

DataFrame.groupby().applyInPandas() 实现了对 Pandas 实例分组映射操作，它需要一个接收 pandas.DataFrame，并返回另一个 pandas.DataFrame 的 Python 函数。它将每组映射成每个 pandas.DataFrame.

> This API implements the “split-apply-combine” pattern which consists of three steps:

这个 API 实现了“划分-应用-合并”模式：

- 使用 DataFrame.groupBy() 将数据划分到组内

- 在每组上应用一个函数。函数的输入和输出都是 pandas.DataFrame，输入数据包含了每组的所有行和列。

- 将结果合并成一个新的 PySpark DataFrame.

> Split the data into groups by using [DataFrame.groupBy()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.groupBy.html#pyspark.sql.DataFrame.groupBy).
> Apply a function on each group. The input and output of the function are both `pandas.DataFrame`. The input data contains all the rows and columns for each group.
> Combine the results into a new PySpark DataFrame.

> To use `DataFrame.groupBy().applyInPandas()`, the user needs to define the following:

要使用 DataFrame.groupBy().applyInPandas(), 用户需要定义如下：

- 定义了每组计算逻辑的 Python 函数

- 定义了输出 PySpark DataFrame 结构的 StructType 对象或字符串

> A Python function that defines the computation for each group.
> A `StructType` object or a string that defines the schema of the output PySpark [DataFrame](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.html#pyspark.sql.DataFrame).

> The column labels of the returned `pandas.DataFrame` must either match the field names in the defined output schema if specified as strings, or match the field data types by position if not strings, e.g. integer indices. See [pandas.DataFrame](https://pandas.pydata.org/pandas-docs/stable/generated/pandas.DataFrame.html#pandas.DataFrame) on how to label columns when constructing a `pandas.DataFrame`.

返回的 pandas.DataFrame 的列标签，如果指定的字段名是字符串，那么必须要匹配定义的输出结构的字段名；如果指定的字段名不是字符串，那么必须要按位置匹配字段数据类型，例如整型索引。

> Note that all data for a group will be loaded into memory before the function is applied. This can lead to out of memory exceptions, especially if the group sizes are skewed. The configuration for [maxRecordsPerBatch](https://spark.apache.org/docs/latest/api/python/user_guide/sql/arrow_pandas.html#setting-arrow-batch-size) is not applied on groups and it is up to the user to ensure that the grouped data will fit into the available memory.

注意，在应用函数前，一组的所有数据将被载入到内存中。这可能导致内存异常，特别是当组的大小是倾斜的时候。

配置 maxRecordsPerBatch 并没有应用在组上，它取决于用户确保分组的数据将装入可用内存中。

> The following example shows how to use `DataFrame.groupby().applyInPandas()` to subtract the mean from each value in the group.

下面的例子展示了如何使用 DataFrame.groupby().applyInPandas()，使组内的每个值减去均值。

```python
df = spark.createDataFrame(
    [(1, 1.0), (1, 2.0), (2, 3.0), (2, 5.0), (2, 10.0)],
    ("id", "v"))

def subtract_mean(pdf: pd.DataFrame) -> pd.DataFrame:
    # pdf is a pandas.DataFrame
    v = pdf.v
    return pdf.assign(v=v - v.mean())

df.groupby("id").applyInPandas(subtract_mean, schema="id long, v double").show()
# +---+----+
# | id|   v|
# +---+----+
# |  1|-0.5|
# |  1| 0.5|
# |  2|-3.0|
# |  2|-1.0|
# |  2| 4.0|
# +---+----+
```

> For detailed usage, please see please see [GroupedData.applyInPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.GroupedData.applyInPandas.html#pyspark.sql.GroupedData.applyInPandas)

### Map

> Map operations with Pandas instances are supported by [DataFrame.mapInPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.mapInPandas.html#pyspark.sql.DataFrame.mapInPandas) which maps an iterator of `pandas.DataFrames` to another iterator of `pandas.DataFrames` that represents the current PySpark DataFrame and returns the result as a PySpark DataFrame. The function takes and outputs an iterator of `pandas.DataFrame`. It can return the output of arbitrary length in contrast to some Pandas UDFs although internally it works similarly with Series to Series Pandas UDF.

DataFrame.mapInPandas() 实现了对 Pandas 实例映射操作，它将 pandas.DataFrames 迭代器映射成另一个 pandas.DataFrames 迭代器，表示当前的 PySpark DataFrame，并作为一个 PySpark DataFrame 返回。

函数接收并输出 pandas.DataFrames 迭代器。与一些 Pandas UDFs 相反，它能返回任意长度的输出，尽管内部原理和 Series to Series Pandas UDF 类似。

The following example shows how to use [DataFrame.mapInPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.mapInPandas.html#pyspark.sql.DataFrame.mapInPandas):

```python
df = spark.createDataFrame([(1, 21), (2, 30)], ("id", "age"))

def filter_func(iterator: Iterable[pd.DataFrame]) -> Iterable[pd.DataFrame]:
    for pdf in iterator:
        yield pdf[pdf.id == 1]

df.mapInPandas(filter_func, schema=df.schema).show()
# +---+---+
# | id|age|
# +---+---+
# |  1| 21|
# +---+---+
```

> For detailed usage, please see [DataFrame.mapInPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.mapInPandas.html#pyspark.sql.DataFrame.mapInPandas).

### Co-grouped Map

> Co-grouped map operations with Pandas instances are supported by `DataFrame.groupby().cogroup().applyInPandas()` which allows two PySpark DataFrames to be cogrouped by a common key and then a Python function applied to each cogroup. It consists of the following steps:

DataFrame.groupby().cogroup().applyInPandas() 实现了对 Pandas 实例组合映射操作，允许通过一个共同的键组合两个 PySpark DataFrames，然后在每个组合上应用一个 Python 函数。

它包括下面的步骤：

- 对数据进行洗牌，使共享键的每个 dataframe 的组组合在一起

- 在每个组合上应用一个函数。函数的输入是两个 pandas.DataFrame(用一个可选的元组表示键)。函数的返回是一个 pandas.DataFrame

- 将所有组里的 pandas.DataFrames 合并成一个新的 PySpark DataFrame

> Shuffle the data such that the groups of each dataframe which share a key are cogrouped together.
> Apply a function to each cogroup. The input of the function is two `pandas.DataFrame` (with an optional tuple representing the key). The output of the function is a `pandas.DataFrame`.
> Combine the `pandas.DataFrames` from all groups into a new PySpark [DataFrame](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.html#pyspark.sql.DataFrame).

> To use `groupBy().cogroup().applyInPandas()`, the user needs to define the following:

要使用 groupBy().cogroup().applyInPandas(), 用户需要定义如下：

- 定义了每个组合计算逻辑的 Python 函数

- 定义了输出 PySpark DataFrame 结构的 StructType 对象或字符串

> A Python function that defines the computation for each cogroup.
> A `StructType` object or a string that defines the schema of the output PySpark [DataFrame](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.html#pyspark.sql.DataFrame).

> The column labels of the returned `pandas.DataFrame` must either match the field names in the defined output schema if specified as strings, or match the field data types by position if not strings, e.g. integer indices. See [pandas.DataFrame](https://pandas.pydata.org/pandas-docs/stable/generated/pandas.DataFrame.html#pandas.DataFrame). on how to label columns when constructing a `pandas.DataFrame`.

返回的 pandas.DataFrame 的列标签，如果指定的字段名是字符串，那么必须要匹配定义的输出结构的字段名；如果指定的字段名不是字符串，那么必须要按位置匹配字段数据类型，例如整型索引。

> Note that all data for a cogroup will be loaded into memory before the function is applied. This can lead to out of memory exceptions, especially if the group sizes are skewed. The configuration for [maxRecordsPerBatch](https://spark.apache.org/docs/latest/api/python/user_guide/sql/arrow_pandas.html#setting-arrow-batch-size) is not applied and it is up to the user to ensure that the cogrouped data will fit into the available memory.

注意，在应用函数前，一个组合的所有数据将被载入到内存中。这可能导致内存异常，特别是当组的大小是倾斜的时候。

配置 maxRecordsPerBatch 并没有应用在组上，它取决于用户确保组合的数据将装入可用内存中。

> The following example shows how to use `DataFrame.groupby().cogroup().applyInPandas()` to perform an asof join between two datasets.

```python
import pandas as pd

df1 = spark.createDataFrame(
    [(20000101, 1, 1.0), (20000101, 2, 2.0), (20000102, 1, 3.0), (20000102, 2, 4.0)],
    ("time", "id", "v1"))

df2 = spark.createDataFrame(
    [(20000101, 1, "x"), (20000101, 2, "y")],
    ("time", "id", "v2"))

def merge_ordered(left: pd.DataFrame, right: pd.DataFrame) -> pd.DataFrame:
    return pd.merge_ordered(left, right)

df1.groupby("id").cogroup(df2.groupby("id")).applyInPandas(
    merge_ordered, schema="time int, id int, v1 double, v2 string").show()
# +--------+---+---+----+
# |    time| id| v1|  v2|
# +--------+---+---+----+
# |20000101|  1|1.0|   x|
# |20000102|  1|3.0|null|
# |20000101|  2|2.0|   y|
# |20000102|  2|4.0|null|
# +--------+---+---+----+
```

> For detailed usage, please see [PandasCogroupedOps.applyInPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.PandasCogroupedOps.applyInPandas.html#pyspark.sql.PandasCogroupedOps.applyInPandas)

## Usage Notes

### Supported SQL Types

> Currently, all Spark SQL data types are supported by Arrow-based conversion except [ArrayType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.ArrayType.html#pyspark.sql.types.ArrayType) of [TimestampType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.TimestampType.html#pyspark.sql.types.TimestampType). [MapType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.MapType.html#pyspark.sql.types.MapType) and [ArrayType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.ArrayType.html#pyspark.sql.types.ArrayType) of nested [StructType](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.types.StructType.html#pyspark.sql.types.StructType) are only supported when using PyArrow 2.0.0 and above.

目前，除了 TimestampType 的 ArrayType 外，所有 Spark SQL 数据类型都支持基于 Arrow 的转换。

只有在使用 PyArrow 2.0.0 及以上版本时，才支持嵌套 StructType 的 MapType 和 ArrayType

### Setting Arrow Batch Size

> Data partitions in Spark are converted into Arrow record batches, which can temporarily lead to high memory usage in the JVM. To avoid possible out of memory exceptions, the size of the Arrow record batches can be adjusted by setting the conf `spark.sql.execution.arrow.maxRecordsPerBatch` to an integer that will determine the maximum number of rows for each batch. The default value is 10,000 records per batch. If the number of columns is large, the value should be adjusted accordingly. Using this limit, each data partition will be made into 1 or more record batches for processing.

Spark 中的数据分区被转换成 Arrow 记录批次，能导致 JVM 的内存使用率很高。为了避免可能的内存异常，Arrow 记录批次的大小可以通过将 spark.sql.execution.arrow.maxRecordsPerBatch 设置成一个整型进行调整，这个参数决定了每个批次中的行的最大数量。每个批次的默认值是10000条记录。

如果列的数量很大，值应该据此调整。使用这个限制，每个数据分区将被分成1个或多个记录批次进行处理。

### Timestamp with Time Zone Semantics

> Spark internally stores timestamps as UTC values, and timestamp data that is brought in without a specified time zone is converted as local time to UTC with microsecond resolution. When timestamp data is exported or displayed in Spark, the session time zone is used to localize the timestamp values. The session time zone is set with the configuration `spark.sql.session.timeZone` and will default to the JVM system local time zone if not set. Pandas uses a `datetime64` type with nanosecond resolution, `datetime64[ns]`, with optional time zone on a per-column basis.

在内部，Spark 将时间戳存储为 UTC 值，没有指定时区的时间戳数据被转换成到具有微秒的 UTC 的本地时间。当导出时间戳数据，或在 Spark 中展示时，会话时区用来本地化时间戳值。会话时区通过 spark.sql.session.timeZone 配置设置，如果不设置的话，默认是 JVM 系统本地时区。对纳秒，Pandas 使用 datetime64 类型，对于可选的时区，使用 datetime64[ns] 类型。

> When timestamp data is transferred from Spark to Pandas it will be converted to nanoseconds and each column will be converted to the Spark session time zone then localized to that time zone, which removes the time zone and displays values as local time. This will occur when calling [DataFrame.toPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.toPandas.html#pyspark.sql.DataFrame.toPandas) or `pandas_udf` with timestamp columns.

当时间戳数据从 Spark 传输到 Pandas 时，它将被转成纳秒，每列将被转成 Spark 会话时区，然后再本地化成那个时区，也就是移除时区，并以本地时间展示值。

当在时间戳列上调用 DataFrame.toPandas() 或 pandas_udf 时，将出现这种情况。

> When timestamp data is transferred from Pandas to Spark, it will be converted to UTC microseconds. This occurs when calling [SparkSession.createDataFrame()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.SparkSession.createDataFrame.html#pyspark.sql.SparkSession.createDataFrame) with a Pandas DataFrame or when returning a timestamp from a `pandas_udf`. These conversions are done automatically to ensure Spark will have data in the expected format, so it is not necessary to do any of these conversions yourself. Any nanosecond values will be truncated.

当时间戳数据从 Pandas 传输到 Spark 时，它将被转成 UTC 微秒。当在 Pandas DataFrame 上调用 SparkSession.createDataFrame() 时，或从 pandas_udf 返回一个时间戳时，将出现这种情况。

这些转换会自动完成，以确保 Spark 将有期待格式的数据，所以，自己做这些转换没有必要。

任意纳秒值将被截断。

> Note that a standard UDF (non-Pandas) will load timestamp data as Python datetime objects, which is different from a Pandas timestamp. It is recommended to use Pandas time series functionality when working with timestamps in `pandas_udfs` to get the best performance, see [here](https://pandas.pydata.org/pandas-docs/stable/timeseries.html) for details.

注意：一个标准的 UDF(非Pandas) 将时间戳数据载入成 Python datetime 对象，这和 Pandas 时间戳不一样。在 pandas_udfs 中和时间戳一起使用时，建议使用 Pandas 时间序列功能，以获得最佳性能。

### Recommended Pandas and PyArrow Versions

> For usage with pyspark.sql, the minimum supported versions of Pandas is 1.0.5 and PyArrow is 1.0.0. Higher versions may be used, however, compatibility and data correctness can not be guaranteed and should be verified by the user.

对于 pyspark.sql 的使用，支持的最小 Pandas 版本是 1.0.5, PyArrow 是 1.0.0. 可以使用更高的版本，然而不能保证兼容性和数据正确性，用户应该作出验证。

### Compatibility Setting for PyArrow >= 0.15.0 and Spark 2.3.x, 2.4.x

> Since Arrow 0.15.0, a change in the binary IPC format requires an environment variable to be compatible with previous versions of Arrow <= 0.14.1. This is only necessary to do for PySpark users with versions 2.3.x and 2.4.x that have manually upgraded PyArrow to 0.15.0. The following can be added to `conf/spark-env.sh` to use the legacy Arrow IPC format:

从 Arrow 0.15.0 开始，二进制 IPC 格式的更改需要一个环境变量与 Arrow <= 0.14.1 的先前版本兼容。只有使用 2.3.x 和 2.4.x 版本的 PySpark 用户才需要这样做，手动将 PyArrow 升级到 0.15.0。

可以在 conf/spark-env.sh 中添加以下内容，以使用旧的 Arrow IPC 格式:

    ARROW_PRE_0_15_IPC_FORMAT=1

> This will instruct PyArrow >= 0.15.0 to use the legacy IPC format with the older Arrow Java that is in Spark 2.3.x and 2.4.x. Not setting this environment variable will lead to a similar error as described in [SPARK-29367](https://issues.apache.org/jira/browse/SPARK-29367) when running `pandas_udfs` or [DataFrame.toPandas()](https://spark.apache.org/docs/latest/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.toPandas.html#pyspark.sql.DataFrame.toPandas) with Arrow enabled. More information about the Arrow IPC change can be read on the Arrow 0.15.0 release [blog](https://arrow.apache.org/blog/2019/10/06/0.15.0-release/#columnar-streaming-protocol-change-since-0140).

这将指示 PyArrow >= 0.15.0 在 Spark 2.3.x 和 2.4.x 中的旧 Arrow Java 中使用旧的 IPC 格式。

当运行 pandas_udfs 或 DataFrame.toPandas() 时启用了 Arrow，但没有设置这个环境变量，将导致类似 SPARK-29367 中描述的错误。

### Setting Arrow self_destruct for memory savings

> Since Spark 3.2, the Spark configuration `spark.sql.execution.arrow.pyspark.selfDestruct.enabled` can be used to enable PyArrow’s `self_destruct` feature, which can save memory when creating a Pandas DataFrame via `toPandas` by freeing Arrow-allocated memory while building the Pandas DataFrame. This option is experimental, and some operations may fail on the resulting Pandas DataFrame due to immutable backing arrays. Typically, you would see the error `ValueError: buffer source array is read-only`. Newer versions of Pandas may fix these errors by improving support for such cases. You can work around this error by copying the column(s) beforehand. Additionally, this conversion may be slower because it is single-threaded.

从 Spark 3.2 开始，Spark 配置 spark.sql.execution.arrow.pyspark.selfDestruct.enabled 可以用来启用 PyArrow 的 self_destruct 功能，该功能可以在通过 toPandas 创建 Pandas DataFrame 时通过释放分配给 Arrow 的内存来节省内存。

这个选项是实验性的，由于不可变的支持数组，一些操作可能会在生成的 Pandas DataFrame 上失败。通常，你会看到 `ValueError: buffer source array is read-only` 错误。新版本的 Pandas 可以通过改进对此类情况的支持来修复这些错误。你可以通过事先复制列来解决此错误。此外，这种转换可能会比较慢，因为它是单线程的。
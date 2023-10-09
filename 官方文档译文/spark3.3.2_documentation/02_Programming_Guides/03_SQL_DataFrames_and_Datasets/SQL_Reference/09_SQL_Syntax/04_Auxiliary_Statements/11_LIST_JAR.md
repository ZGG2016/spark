# LIST JAR

## Description

`LIST JAR` lists the JARs added by `ADD JAR`.

### Syntax

	LIST { JAR | JARS } file_name [ ... ]

## Examples

```sql
ADD JAR /tmp/test.jar;
ADD JAR /tmp/test_2.jar;
LIST JAR;
-- output for LIST JAR
spark://192.168.1.112:62859/jars/test.jar
spark://192.168.1.112:62859/jars/test_2.jar

LIST JAR /tmp/test.jar /some/random.jar /another/random.jar;
-- output
spark://192.168.1.112:62859/jars/test.jar
```
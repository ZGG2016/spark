
进入 `localhost:4040` 查看

数据是否倾斜可以从下面两处判断

`Shuffle Read Size/Record` `Shuffle Write Size/Record` 字段的数据分布是否均匀，图1就是均匀的

> 图1
![single-table-01](./images/single-table-01.png)

> 图2
![single-table-03](./images/single-table-03.png)

如果有的任务执行很快（很短的一部分）；有的任务执行很慢（很长的一部分），就是倾斜的。

图3、5就是均匀的，图4就是倾斜的

> 图3
![single-table-02](./images/single-table-02.png)

> 图4
![single-table-04](./images/single-table-04.png)

> 图5
![single-table-05](./images/single-table-05.png)
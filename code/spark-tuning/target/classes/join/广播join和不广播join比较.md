
[TOC]

## 1 不启用广播join

打开 `localhost:4040`, 查看执行计划

![bc-no-enable](./images/bc-no-enable-02.png)

![bc-no-enable](./images/bc-no-enable-01.png)

![bc-no-enable](./images/bc-no-enable-03.png)

## 2 启用广播join

可以发现使用了广播，并且广播后的join，仅用了40ms，而不使用广播，则用了3s

![bc-enable](./images/bc-enable-01.png)

![bc-enable](./images/bc-enable-02.png)

![bc-enable](./images/bc-enable-03.png)

也可以从 explain 打印出的执行计划分析
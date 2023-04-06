
RDD.scala

```scala
  /**
   * Return an RDD created by piping elements to a forked external process.
   * 把元素用管道输送到一个外部进程，由此创建一个rdd  
   */
  def pipe(command: String): RDD[String] = withScope {
    // Similar to Runtime.exec(), if we are given a single string, split it into words
    // using a standard StringTokenizer (i.e. by spaces)
    // tokenize：划分字符串成一个个单词。
    // command 作用在 PipedRDD 上
    pipe(PipedRDD.tokenize(command))
  }

  /**
   * Return an RDD created by piping elements to a forked external process.
   */
  def pipe(command: String, env: Map[String, String]): RDD[String] = withScope {
    // Similar to Runtime.exec(), if we are given a single string, split it into words
    // using a standard StringTokenizer (i.e. by spaces)
    pipe(PipedRDD.tokenize(command), env)
  }

    /**
     * Return an RDD created by piping elements to a forked external process. The resulting RDD
     * is computed by executing the given process once per partition. All elements
     * of each input partition are written to a process's stdin as lines of input separated
     * by a newline. The resulting partition consists of the process's stdout output, with
     * each line of stdout resulting in one element of the output partition. A process is invoked
     * even for empty partitions.
     * 返回一个RDD，该RDD是将元素输送到一个外部进程创建的。
     *
     * 在每个分区中，通过执行一个给定进程，计算出结果RDD。
     * 每个输入分区的所有元素都 作为由换行符分隔的输入行 写入到进程的标准输入中。
     * 
     * 即使对于空分区，也会调用进程。
     * 
     * The resulting partition consists of the process's stdout output, 
     * with each line of stdout resulting in one element of the output partition. 
     * 结果分区是由进程的stdout输出组成的，每一行 stdout 导致输出分区的一个元素
     * 
     * The print behavior can be customized by providing two functions.
     * 可以通过提供两个函数来自定义打印行为
     * 
     * @param command command to run in forked process.
     *                运行在forked进程中的命令
     * @param env environment variables to set.
     *            设置的环境变量
     * @param printPipeContext Before piping elements, this function is called as an opportunity
     *                         to pipe context data. Print line function (like out.println) will be
     *                         passed as printPipeContext's parameter.
     *                         在输出元素前，打印context data。打印行函数(如out.println)作为此函数的参数
     * @param printRDDElement Use this function to customize how to pipe elements. This function
     *                        will be called with each RDD element as the 1st parameter, and the
     *                        print line function (like out.println()) as the 2nd parameter.
     *                        An example of pipe the RDD data of groupBy() in a streaming way,
     *                        instead of constructing a huge String to concat all the elements:
     *                        {{{
     *                        def printRDDElement(record:(String, Seq[String]), f:String=>Unit) =
     *                          for (e <- record._2) {f(e)}
     *                        }}}
     *                        定义如何输出元素的函数。
     *                        每个RDD元素作为第一个参数，打印行函数(如out.println)作为第二个参数
     * @param separateWorkingDir Use separate working directories for each task.
     *                           是否为每个任务使用单独的工作目录
     * @param bufferSize Buffer size for the stdin writer for the piped process.
     *                   stdin写入的缓冲区大小
     * @param encoding Char encoding used for interacting (via stdin, stdout and stderr) with
     *                 the piped process
     * @return the result RDD
     */
    def pipe(
              command: Seq[String],
              env: Map[String, String] = Map(),
              printPipeContext: (String => Unit) => Unit = null,
              printRDDElement: (T, String => Unit) => Unit = null,
              separateWorkingDir: Boolean = false,
              bufferSize: Int = 8192,
              encoding: String = Codec.defaultCharsetCodec.name): RDD[String] = withScope {
      // PipedRDD：通过外部进程打印出的每个父分区中的内容(一行行打印)，
      // returns the output as a collection of strings.
      new PipedRDD(this, command, env,
        if (printPipeContext ne null) sc.clean(printPipeContext) else null,
        if (printRDDElement ne null) sc.clean(printRDDElement) else null,
        separateWorkingDir,
        bufferSize,
        encoding)
    }
```
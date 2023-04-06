# Spark Streaming Custom Receivers

[TOC]

> Spark Streaming can receive streaming data from any arbitrary data source beyond the ones for which it has built-in support (that is, beyond Kafka, Kinesis, files, sockets, etc.). This requires the developer to implement a receiver that is customized for receiving data from the concerned data source. This guide walks through the process of implementing a custom receiver and using it in a Spark Streaming application. Note that custom receivers can be implemented in Scala or Java.

**Spark Streaming 可以从任意数据源接收流数据，不仅仅是内建支持的数据源，如Kafka、 Kinesis、files、sockets。此时，就需要开发者自己实现一个 receiver，来接收数据**。

注意：自定义的 receivers 只能使用 Scala 和 Java 实现。

## 1、Implementing a Custom Receiver

> This starts with implementing a Receiver ([Scala doc](https://spark.apache.org/docs/3.0.1/api/scala/org/apache/spark/streaming/receiver/Receiver.html), [Java doc](https://spark.apache.org/docs/3.0.1/api/java/org/apache/spark/streaming/receiver/Receiver.html)). A custom receiver must extend this abstract class by implementing two methods

首先要**实现 Receiver 抽象类，并实现如下两个方法：**

- onStart(): 开始接收数据
- onStop(): 停止接收数据

> onStart(): Things to do to start receiving data.

> onStop(): Things to do to stop receiving data.

> Both onStart() and onStop() must not block indefinitely. Typically, onStart() would start the threads that are responsible for receiving the data, and onStop() would ensure that these threads receiving the data are stopped. The receiving threads can also use isStopped(), a Receiver method, to check whether they should stop receiving data.

onStart() 和 onStop() 不能无限期地阻塞。

通常，onStart() 将启动负责接收数据的线程，而 onStop() 负责停止这些接收数据的线程。

接收的线程还可以使用 isStopped() 方法，来检查它们是否应该停止接收数据。

> Once the data is received, that data can be stored inside Spark by calling store(data), which is a method provided by the Receiver class. There are a number of flavors of store() which allow one to store the received data record-at-a-time or as whole collection of objects / serialized bytes. Note that the flavor of store() used to implement a receiver affects its reliability and fault-tolerance semantics. This is discussed [later](https://spark.apache.org/docs/3.0.1/streaming-custom-receivers.html#receiver-reliability) in more detail.

一旦接收了数据，那么通过调用 Receiver 类中的 `store(data)` 方法将数据存储在 Spark 中。

`store()` 有很多种形式，允许一次存储接收到的数据记录，或者存储为对象/序列化字节的整个集合。

注意，用于实现 receiver 的 `store()` 形式会影响其可靠性和容错语义。

> Any exception in the receiving threads should be caught and handled properly to avoid silent failures of the receiver. restart(<exception>) will restart the receiver by asynchronously calling onStop() and then calling onStart() after a delay. stop(<exception>) will call onStop() and terminate the receiver. Also, reportError(<error>) reports an error message to the driver (visible in the logs and UI) without stopping / restarting the receiver.

接收线程中的任意异常都应该被捕获、处理，以避免 receiver 的沉默错误。

`restart(<exception>)` 将异步地重启 receiver，通过先调用 `onStop()`，再延迟一会调用 `onStart()`实现。

`stop(<exception>)` 将会调用 `onStop()`，停止 receiver。

`reportError(<error>)` 向 driver 报告错误信息(在logs和UI可见)，不用停止或重启 receiver。

> The following is a custom receiver that receives a stream of text over a socket. It treats ‘\n’ delimited lines in the text stream as records and stores them with Spark. If the receiving thread has any error connecting or receiving, the receiver is restarted to make another attempt to connect.

下面就是一个自定义的 receiver，通过 socket 来接收文本流。在这个文本流中使用 `\n` 作为记录分隔符，并将其存储在 Spark 中。如果接收线程任意的连接或接收错误，就会重启 receiver ，以尝试重新连接。

**A:scala**

```scala
class CustomReceiver(host: String, port: Int)
  extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with Logging {

  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Socket Receiver") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself if isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {
    var socket: Socket = null
    var userInput: String = null
    try {
      // Connect to host:port
      socket = new Socket(host, port)

      // Until stopped or connection broken continue reading
      val reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
      userInput = reader.readLine()
      while(!isStopped && userInput != null) {
        store(userInput)
        userInput = reader.readLine()
      }
      reader.close()
      socket.close()

      // Restart in an attempt to connect again when server is active again
      restart("Trying to connect again")
    } catch {
      case e: java.net.ConnectException =>
        // restart if could not connect to server
        restart("Error connecting to " + host + ":" + port, e)
      case t: Throwable =>
        // restart if there is any other error
        restart("Error receiving data", t)
    }
  }
}

```

**B:java**

```java
public class JavaCustomReceiver extends Receiver<String> {

  String host = null;
  int port = -1;

  public JavaCustomReceiver(String host_ , int port_) {
    super(StorageLevel.MEMORY_AND_DISK_2());
    host = host_;
    port = port_;
  }

  @Override
  public void onStart() {
    // Start the thread that receives data over a connection
    new Thread(this::receive).start();
  }

  @Override
  public void onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself if isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private void receive() {
    Socket socket = null;
    String userInput = null;

    try {
      // connect to the server
      socket = new Socket(host, port);

      BufferedReader reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

      // Until stopped or connection broken continue reading
      while (!isStopped() && (userInput = reader.readLine()) != null) {
        System.out.println("Received data '" + userInput + "'");
        store(userInput);
      }
      reader.close();
      socket.close();

      // Restart in an attempt to connect again when server is active again
      restart("Trying to connect again");
    } catch(ConnectException ce) {
      // restart if could not connect to server
      restart("Could not connect", ce);
    } catch(Throwable t) {
      // restart if there is any other error
      restart("Error receiving data", t);
    }
  }
}

```

## 2、Using the custom receiver in a Spark Streaming application

> The custom receiver can be used in a Spark Streaming application by using streamingContext.receiverStream(<instance of custom receiver>). This will create an input DStream using data received by the instance of custom receiver, as shown below:

自定义的 receiver 可以通过使用 `streamingContext.receiverStream(<instance of custom receiver>)` 用在 Spark Streaming 应用程序中。浙江使用自定义 receiver 接收到的数据创建一个输入 DStream：

**A:scala**

```scala
// Assuming ssc is the StreamingContext
val customReceiverStream = ssc.receiverStream(new CustomReceiver(host, port))
val words = customReceiverStream.flatMap(_.split(" "))
...
```

> The full source code is in the example [CustomReceiver.scala](https://github.com/apache/spark/blob/v3.0.1/examples/src/main/scala/org/apache/spark/examples/streaming/CustomReceiver.scala).

**B:java**

```java
// Assuming ssc is the JavaStreamingContext
JavaDStream<String> customReceiverStream = ssc.receiverStream(new JavaCustomReceiver(host, port));
JavaDStream<String> words = customReceiverStream.flatMap(s -> ...);
...
```

> The full source code is in the example [JavaCustomReceiver.java](https://github.com/apache/spark/blob/v3.0.1/examples/src/main/java/org/apache/spark/examples/streaming/JavaCustomReceiver.java).

## 3、Receiver Reliability

> As discussed in brief in the [Spark Streaming Programming Guide](https://spark.apache.org/docs/3.0.1/streaming-programming-guide.html), there are two kinds of receivers based on their reliability and fault-tolerance semantics.

根据它们的可靠性和容错语义，分为两类主要的 receivers：

- 可靠 Receiver：对于一个可以确认发送的数据的可靠源，一个可靠的 receiver 会正确地 **确认数据已被接收，并可靠地存储在 Spark 中**(也就是说，复制成功了)。通常，实现这个 receiver 需要小心地考虑源确认语义。

- 不可靠 Receiver：一个不可靠 receiver **不会向源发送确认**。可以用在不支持确认的源，或不想或不需要进行确认的源。

> Reliable Receiver - For reliable sources that allow sent data to be acknowledged, a reliable receiver correctly acknowledges to the source that the data has been received and stored in Spark reliably (that is, replicated successfully). Usually, implementing this receiver involves careful consideration of the semantics of source acknowledgements.

> Unreliable Receiver - An unreliable receiver does not send acknowledgement to a source. This can be used for sources that do not support acknowledgement, or even for reliable sources when one does not want or need to go into the complexity of acknowledgement.

> To implement a reliable receiver, you have to use store(multiple-records) to store data. This flavor of store is a blocking call which returns only after all the given records have been stored inside Spark. If the receiver’s configured storage level uses replication (enabled by default), then this call returns after replication has completed. Thus it ensures that the data is reliably stored, and the receiver can now acknowledge the source appropriately. This ensures that no data is lost when the receiver fails in the middle of replicating data – the buffered data will not be acknowledged and hence will be later resent by the source.

**为了实现一个可靠的 receiver，你必须使用 `store(multiple-records)` 存储数据**。这种形式的 store 是一种阻塞的调用，仅会在所有给的数据被存储在 Spark 后，才会返回。

如果 receiver 配置的存储等级使用复制(默认启用)，那么在复制完成后，会返回这次调用。因此，**确保数据被可靠地存储之后， receiver 再向源确认**。这就确保了，在复制数据期间， receiver 故障的情况下，没有数据丢失 -- 缓存的数据不会被确认，因此源会稍后再次发送。

> An unreliable receiver does not have to implement any of this logic. It can simply receive records from the source and insert them one-at-a-time using store(single-record). While it does not get the reliability guarantees of store(multiple-records), it has the following advantages:

一个不可靠的 receiver 不需要实现这个逻辑。它只需要从源接收记录，使用 `store(single-record)` 一次插入一个的形式插入。虽然它没有像 `store(multiple-records)` 的可靠性保证，但有如下优势：

- 系统会把数据分成合适的大小的块。
- 如果设置了接收速率，系统可以控制接收的速率。
- 不可靠的 receivers 的实现比可靠的简单。

> The system takes care of chunking that data into appropriate sized blocks (look for block interval in the [Spark Streaming Programming Guide](https://spark.apache.org/docs/3.0.1/streaming-programming-guide.html)).

> The system takes care of controlling the receiving rates if the rate limits have been specified.

> Because of these two, unreliable receivers are simpler to implement than reliable receivers.

> The following table summarizes the characteristics of both types of receivers

下面的表总结了这**两个类型的特点：**

Receiver Type | Characteristics
---|:---
Unreliable Receivers | Simple to implement.System takes care of block generation and rate control. No fault-tolerance guarantees, can lose data on receiver failure.【实现简单。系统控制块生成和接受速率。没有容错语义保证，当receiver故障的时候，可能会丢失数据。】
Reliable Receivers | Strong fault-tolerance guarantees, can ensure zero data loss.Block generation and rate control to be handled by the receiver implementation.Implementation complexity depends on the acknowledgement mechanisms of the source.【强容错语义保证，能确保零数据丢失。块生成和速率控制的receiver控制。实现的复杂性取决于源的确认机制。】
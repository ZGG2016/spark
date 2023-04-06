# DummySerializerInstance

```java
package org.apache.spark.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import scala.reflect.ClassTag;

import org.apache.spark.annotation.Private;
import org.apache.spark.unsafe.Platform;

/**
 * Unfortunately, we need a serializer instance in order to construct a DiskBlockObjectWriter.
 * Our shuffle write path doesn't actually use this serializer (since we end up calling the
 * `write() OutputStream methods), but DiskBlockObjectWriter still calls some methods on it. To work
 * around this, we pass a dummy no-op serializer.
 */
    //为了构造一个DiskBlockObjectWriter，需要一个序列化器实例。
    //我们的写路径实际并没有使用这个序列化器(因为我们通过调用`write()`输出流方法来结束)，
    //但DiskBlockObjectWriter仍会在序列化器上调用溢写方法

    //DummySerializerInstance：虚拟的，无操作的序列化器
@Private
public final class DummySerializerInstance extends SerializerInstance {

  public static final DummySerializerInstance INSTANCE = new DummySerializerInstance();

  private DummySerializerInstance() { }

  @Override
  public SerializationStream serializeStream(final OutputStream s) {
    return new SerializationStream() {
      @Override
      public void flush() {
        // Need to implement this because DiskObjectWriter uses it to flush the compression stream
        try {
          s.flush();
        } catch (IOException e) {
          Platform.throwException(e);
        }
      }

      @Override
      public <T> SerializationStream writeObject(T t, ClassTag<T> ev1) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void close() {
        // Need to implement this because DiskObjectWriter uses it to close the compression stream
        try {
          s.close();
        } catch (IOException e) {
          Platform.throwException(e);
        }
      }
    };
  }

  @Override
  public <T> ByteBuffer serialize(T t, ClassTag<T> ev1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeserializationStream deserializeStream(InputStream s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T deserialize(ByteBuffer bytes, ClassLoader loader, ClassTag<T> ev1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T deserialize(ByteBuffer bytes, ClassTag<T> ev1) {
    throw new UnsupportedOperationException();
  }
}

```
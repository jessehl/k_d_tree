import java.io._


object Main  extends App {


  type Point = Array[Double]
  type Shape = Array[Point]
  type Pointer = Long


  class Node(val left: Pointer, val right: Pointer, val split: Double, val start: Pointer, val end: Pointer) {
    val length = 5 * 8  // in bytes
    def serialize: Array[Byte] = {
      val stream: ByteArrayOutputStream = new ByteArrayOutputStream(length)
      val oos = new ObjectOutputStream(stream)
      oos.writeLong(left);
      oos.writeLong(right);
      oos.writeDouble(split);
      oos.writeLong(start);
      oos.writeLong(end)
      oos.close()
      stream.toByteArray()
    }
    override def toString = List(left, right, split, start, end).mkString(", ")
  }

  object Node{
    def deserialize(bytes: Array[Byte]): Node = {
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val node = new Node(ois.readLong, ois.readLong, ois.readDouble, ois.readLong, ois.readLong)
      ois.close()
      node
    }


  }


  val original = new Node(Long.MinValue,Long.MinValue,Double.MinValue,Long.MinValue,Long.MinValue)

  val bytes = original.serialize
  val inferred = Node.deserialize(bytes)
  println(inferred)

  println(bytes.length)

  val s = bytes.map(_.toBinaryString).mkString(", ")
  println(s)


  val file = new FileOutputStream("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin.bin")
  file.write(bytes)



}


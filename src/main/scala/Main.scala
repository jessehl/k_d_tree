import java.io._
import java.nio.file.{Paths, Files}
import java.nio.ByteBuffer

object Main  extends App {

  type Split = Double
  type Pointer = Long

  class Node(val left: Pointer, val leftSize: Short, val right: Pointer, val rightSize: Short, val split: Split, val record: String) {
    def serialize: Array[Byte] = {
      val recordBytes = record.getBytes("UTF-8")
      val requiredLength = recordBytes.length + Node.baseLength
      val stream = ByteBuffer.allocate(requiredLength)
      stream.putLong(left)
      stream.putShort(leftSize)
      stream.putLong(right)
      stream.putShort(rightSize)
      stream.putDouble(split)
      stream.put(recordBytes)
      stream.array
    }


    override def toString: String = {
      "Node(" +
      this
        .getClass
        .getDeclaredFields
        .map(name => name.getName + ": " + name.get(this).toString).mkString(", ") +
      ")"
    }

  }


  object Node{
    val baseLength = 28.toShort // 2 * 8 (Long) + 1 * 8 (Double) + 2 * 2 (Short)
    def deserialize(bytes: Array[Byte]): Node = {
      val stream = ByteBuffer.wrap(bytes)
      new Node(
        left      = stream.getLong,
        leftSize  = stream.getShort,
        right     = stream.getLong,
        rightSize = stream.getShort,
        split     = stream.getDouble,
        record    = new String((Node.baseLength until Node.baseLength + stream.remaining).map(stream.get(_)).toArray, "UTF-8")
      )

    }
  }


  val nodes = (0 until 10).map(v => new Node(v*v,v.toShort,v, v.toShort, (Node.baseLength + 2).toShort, "32"))
  val outFile = new FileOutputStream("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  nodes.foreach(node => outFile.write(node.serialize))
  outFile.close()


  val path = Paths.get("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  val bytes = Files.readAllBytes(path)
  val inferred = (1 to 10).map(v => Node.deserialize(bytes.slice((v-1) * 30, v * 30)))


  println(nodes.length)
  println(inferred.length)

  println(nodes.last.left)
  println(inferred.last.left)


  println(inferred(7))

}
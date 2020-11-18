import java.io._
import java.nio.file.{Paths, Files}
import java.nio.ByteBuffer

object Main  extends App {

  val K = 2; // nr of dimensions

  type Coordinate = Double
  type Point = Array[Coordinate]
  type Pointer = Long

  class Node(val left: Pointer, val leftSize: Short, val right: Pointer, val rightSize: Short, val point: Point, record: String) {
    def serialize: Array[Byte] = {
      val recordBytes = record.getBytes("UTF-8")
      val requiredLength = recordBytes.length + Node.baseLength + 8 * K
      val stream = ByteBuffer.allocate(requiredLength)
      stream.putLong(left)
      stream.putShort(leftSize)
      stream.putLong(right)
      stream.putShort(rightSize)
      for (p <- point) stream.putDouble(p)
      stream.put(recordBytes)
      stream.array
    }


    def biggerThan(other: Node, k: Int): Boolean = this.point(k)  > other.point(k)
    def equals(other: Node, k: Int): Boolean = this.point(k) == other.point(k)


    def store(): Pointer = this.serialize.size.toLong


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
    val baseLength: Short = 20 // 2 * 8 (Long) + 2 * 2 (Short)
    def deserialize(bytes: Array[Byte]): Node = {
      val stream = ByteBuffer.wrap(bytes)
      new Node(
        left      = stream.getLong,
        leftSize  = stream.getShort,
        right     = stream.getLong,
        rightSize = stream.getShort,
        point     = (for (_ <- 0 until K) yield stream.getDouble).toArray,
        record    = new String(
          (for (_ <- 0 until stream.remaining()) yield stream.get()).toArray, "UTF-8"
        )
      )
    }

  }




  val nodes = (0 until 10).map(v => new Node(v, v.toShort, v, v.toShort, Array(545, 34454), "LL"))
  val outFile = new FileOutputStream("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin", false)
  nodes.foreach(node => outFile.write(node.serialize))
  outFile.close()

  val path = Paths.get("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  val bytes = Files.readAllBytes(path)
  val inferred = (1 to 10).map(v => Node.deserialize(bytes.slice((v-1) * (Node.baseLength + 8 * K + 2), v * (Node.baseLength + 8 * K + 2))))


  println(nodes.length)
  println(inferred.length)

  println(nodes.last.left)
  println(inferred.last.left)


  println(inferred(7))
  println(inferred(7).point.mkString(",  "))

}


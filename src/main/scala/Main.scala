import java.io._
import java.nio.file.{Paths, Files}
import java.nio.ByteBuffer

object Main  extends App {


  type Point = Array[Double]
  type Shape = Array[Point]
  type Pointer = Long


  class Node(val left: Pointer, val right: Pointer, val split: Double, val size: Short) {
    def serialize: Array[Byte] = {
      val stream = ByteBuffer.allocate(Node.length)
      stream.putLong(left)
      stream.putLong(right)
      stream.putDouble(split)
      stream.putShort(size)
      stream.array
    }

    override def toString = List(left, right, split, size).mkString(", ")

  }


  object Node{
    val length = 5*8
    def deserialize(bytes: Array[Byte]): Node = {
      val stream = ByteBuffer.wrap(bytes)
      val node = new Node(stream.getLong, stream.getLong, stream.getDouble, stream.getShort)
      node
    }
  }


  val nodes = (0 until 10000).map(v => new Node(v*v, v, v,0))
  val outFile = new FileOutputStream("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  nodes.foreach(node => outFile.write(node.serialize))
  outFile.close()


  val path = Paths.get("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  val bytes = Files.readAllBytes(path)
  val inferred = (1 to 10000).map(v => Node.deserialize(bytes.slice((v-1) * Node.length, v * Node.length)))


  println(nodes.length)
  println(inferred.length)

  println(nodes.last.left)
  println(inferred.last.left)


  println(bytes.length)
  println(Node.length * nodes.length)

}


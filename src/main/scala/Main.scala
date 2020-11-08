import java.io._
import java.nio.file.{Paths, Files}
import java.nio.ByteBuffer

object Main  extends App {


  type Point = Array[Double]
  type Shape = Array[Point]
  type Pointer = Long


  class Node(val left: Pointer, val right: Pointer, val split: Double, val start: Pointer, val end: Pointer) {
    def serialize: Array[Byte] = {
      val stream = ByteBuffer.allocate(Node.length)
      stream.putLong(left)
      stream.putLong(right)
      stream.putDouble(split)
      stream.putLong(start)
      stream.putLong(end)
      stream.array
    }

    override def toString = List(left, right, split, start, end).mkString(", ")

  }


  object Node{
    val length = 5*8
    def deserialize(bytes: Array[Byte]): Node = {
      val stream = ByteBuffer.wrap(bytes)
      val node = new Node(stream.getLong, stream.getLong, stream.getDouble, stream.getLong, stream.getLong)
      node
    }
  }


  val nodes = (0 until 1000000).map(v => new Node(v*v, v, v, v^v, v))
  val outFile = new FileOutputStream("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  nodes.foreach(node => outFile.write(node.serialize))
  outFile.close()


  val path = Paths.get("C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin")
  val bytes = Files.readAllBytes(path)
  val inferred = (1 to 1000000).map(v => Node.deserialize(bytes.slice((v-1) * Node.length, v * Node.length)))


  println(nodes.length)
  println(inferred.length)

  println(nodes.last.left)
  println(inferred.last.left)


  println(bytes.length)
  println(Node.length * nodes.length)

}


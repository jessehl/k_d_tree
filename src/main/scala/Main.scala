import java.io._
import java.nio.file.{Paths, Files}
import java.nio.ByteBuffer


object Main  extends App {


  val filename = "C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin"
  val K = 2; // nr of dimensions

  type Coordinate = Double
  type Point = Array[Coordinate]
  type Pointer = Long
  type Record = String


  class Node(val left: Pointer, val leftSize: Short, val right: Pointer, val rightSize: Short, val point: Point, record: Record) {
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


    def store(parent: Node, leftOfParent: Boolean): Unit = {

      val outFile = new FileOutputStream(filename, true)
      val bytes = this.serialize
      outFile.write(bytes)

      if (parent != this) {
        val length = bytes.length.toShort;
        val pointer = 1000000 //"beginningOfFileBeforeWriting"
        // update the parent Node with new Pointers and Sizes
        val updatedParent = new Node(
          left = if (leftOfParent) pointer else parent.left,
          leftSize = if (leftOfParent) length else parent.leftSize,
          right = if (leftOfParent) parent.left else pointer,
          rightSize = if (leftOfParent) parent.leftSize else length,
          point = parent.point,
          record = parent.record
        )
        updatedParent.store(updatedParent, leftOfParent = true)
      }

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
    val baseLength: Short = 20 // 2 * 8 (Long) + 2 * 2 (Short)
    def deserialize(bytes: Array[Byte]): Node = {
      val stream = ByteBuffer.wrap(bytes)
      new Node(
        left      = stream.getLong,
        leftSize  = stream.getShort,
        right     = stream.getLong,
        rightSize = stream.getShort,
        point     = (for (_ <- 0 until K) yield stream.getDouble).toArray,
        record    = new Record(
          (for (_ <- 0 until stream.remaining()) yield stream.get()).toArray, "UTF-8"
        )
      )
    }



    def find(point: Point): Array[Node] = {
      val firstNode = Node.deserialize(Array(23,53,234,324,345,345,345,345))

      val k = 0
      def traverse(k: Integer): Array[Node] = {
        if(point(k)
      }
    }


    def insert(point: Point, record: String) = {
      val node = new Node(0, 0, 0, 0, point, record)
      node.store(node, true)
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


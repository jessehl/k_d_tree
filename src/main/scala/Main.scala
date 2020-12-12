import java.io._
import java.nio.ByteBuffer


object Main  extends App {

  val filename = "C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\bin"
  val K = 2 // number of dimensions

  type Coordinate = Double
  type Point = Array[Coordinate]
  type Pointer = Long
  type Record = String


  class Node(val left: Pointer, val right: Pointer, val point: Point, val record: Record) {
    def serialize: Array[Byte] = {
      val recordBytes = record.getBytes("UTF-8")
      val size = (recordBytes.length + Node.baseSize).toShort

      // but the content of the Node in a byte stream
      val stream = ByteBuffer.allocate(size)
      stream.putShort(size)
      stream.putLong(left)
      stream.putLong(right)
      for (p <- point) stream.putDouble(p)
      stream.put(recordBytes)

      // return the bytes
      stream.array
    }


    def store(childIsLeft: Boolean = true, parentPointer: Pointer = 0, parentNode: Option[Node] = None): Unit = {
      val bytes = this.serialize
      val outFile = new FileOutputStream(filename, true)

      // get pointer to this node and store on disk
      val pointer = outFile.getChannel.size()
      outFile.write(bytes)
      outFile.close()

      parentNode.foreach {parent =>
        // update the parent Node with the new Pointer and Size to this node
        val updatedParent = new Node(
          left = if (childIsLeft) pointer else parent.left,
          right = if (!childIsLeft) pointer else parent.right,
          point = parent.point,
          record = parent.record
        )
        val file = new RandomAccessFile(filename, "rw")
        file.seek(parentPointer)
        file.write(updatedParent.serialize)
        file.close()
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
    val baseSize: Short = (
      2 * 8 + // 2 pointers
      2 + // size
      K * 8 // Point
      ).toShort



    def get(pointer: Pointer, estimatedSize: Int = 100): Option[Node] = {
      val file = new RandomAccessFile(filename, "rw")
      val fileSize = file.length()

      // if the file is empty, return None
      if (fileSize == 0) {
        None

        // return None if the file doesn't contain enough bytes to read the size of the Node
      } else if (fileSize < 2) {
        None

        // read the node from disk, and return it
      } else {
        file.seek(pointer)
        val array = (0 until estimatedSize).iterator.map(_.toByte).toArray
        file.read(array)
        val node = Node.deserialize(array)
        node.orElse {
          println("need to get more bytes to read entire Node")
          get(pointer, estimatedSize + 100)
        }
      }
    }


    def deserialize(bytes: Array[Byte]): Option[Node] = {
      val stream = ByteBuffer.wrap(bytes)
      val bytesRead = stream.remaining()

      // return None if the entire Node is not contained in the stream
      val size = stream.getShort
      if (bytesRead < size) None

      // deserialize and return the Node
      Some(
        new Node(
          left      = stream.getLong,
          right     = stream.getLong,
          point     = (for (_ <- 0 until K) yield stream.getDouble).toArray,
          record    = new Record(
            (for (_ <- 0 until size - baseSize) yield stream.get()).toArray, "UTF-8"
          )
        )
      )
    }


    def findParent(point: Point, k: Int = 0, pointer: Pointer = 0, childIsLeft: Boolean = true): (Boolean, Pointer, Option[Node]) = {
      val node = Node.get(pointer)
      if (node.isEmpty) {
        (childIsLeft, pointer, None)
      }
      else if (node.get.point(k) < point(k) && node.get.left != -1) {
        findParent(point, (k + 1) % K, node.get.left, true)
      }
      else if (node.get.point(k) >= point(k) && node.get.right != -1) {
        findParent(point, (k + 1) % K, node.get.right, false)
      }
      else {
        (childIsLeft, pointer, node)
      }
    }



    def insert(point: Point, record: String) = {
      val node = new Node(-1, -1, point, record)
      val (childIsLeft, parentPointer, parentNode) = findParent(point)
      parentNode.foreach{(node: Node) => println("found parent node " + node)}
      node.store(childIsLeft, parentPointer, parentNode)
    }

  }


  Node.insert(Array(0.toDouble,0.toDouble),"1")
  Node.insert(Array(0.toDouble,0.toDouble),"2")
  Node.insert(Array(0.toDouble,0.toDouble),"3")
  Node.insert(Array(0.toDouble,0.toDouble),"4")

}


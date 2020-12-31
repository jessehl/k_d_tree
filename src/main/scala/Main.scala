import java.io._
import java.nio.ByteBuffer


object Main  extends App {

  val filename = "C:\\Users\\Jesse.Loor\\Desktop\\k_d_tree\\tree.bin"
  val K = 3 // number of dimensions (e.g. number of Coordinates that make up a single Point)

  type Coordinate = Double
  type Point = Array[Coordinate]
  type Pointer = Long
  type Record = String


  class Node(val left: Pointer, val right: Pointer, val point: Point, val record: Record) {
    def serialize: Array[Byte] = {
      val recordBytes = record.getBytes("UTF-8")
      val size = (Node.baseSize + recordBytes.length).toShort

      // put the content of the Node in a byte stream
      val stream = ByteBuffer.allocate(size)
      stream.putShort(size)
      stream.putLong(left)
      stream.putLong(right)
      for (p <- point) stream.putDouble(p)
      stream.put(recordBytes)

      // return the bytes
      stream.array
    }



    def store(nodeToOverwrite: Pointer = -1): Pointer = {
      val file = new RandomAccessFile(filename, "rw")

      if (nodeToOverwrite != -1) file.seek(nodeToOverwrite) else file.seek(file.length())
      val pointer = file.getFilePointer()
      file.write(this.serialize)
      file.close()
      pointer
    }



    override def toString: String = {
      "Node(" +
        this
          .getClass
          .getDeclaredFields
          .filterNot(_.getName == "point")
          .map(name => name.getName + ": " + name.get(this).toString).mkString(", ") +
        ", point: (" + this.point.mkString(", ") + ") " +
        ")"
    }

  }



  object Node{
    val baseSize: Short = (
      2 * 8 + // 2 pointers
      2 + // size
      K * 8 // Point
      ).toShort


    def read(pointer: Pointer, chunkSize: Int = 100): Option[Node] = {
      val file = new RandomAccessFile(filename, "rw")

      // return None if file doesn't contain enough bytes to contain a Node
      var node: Option[Node] = None

      // read bytes from the file until the entire Node is deserializable
      if (file.length() - baseSize >= pointer) {
        file.seek(pointer)
        val array = (0 until chunkSize).iterator.map(_.toByte).toArray
        file.read(array)
        node = Node.deserialize(array).orElse{
          println("need to get more bytes to read entire Node")
          // double the number of bytes read with each try
          read(pointer, chunkSize * chunkSize)
        }
      }

      file.close()
      node
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

    class parentNode(val childIsLeft: Boolean, val pointer: Pointer, val node: Option[Node])

    def findParent(pointer: Pointer = 0, point: Point, k: Int = 0): parentNode = {
      // the ancestor - which is potentially(!) the parent
      // simply the first node (pointer = 0) when invoked for the first time
      val ancestor = Node.read(pointer)
      print("*") // show iteration
      // traverse down to more 'direct' ancestors
      if (ancestor.get.point(k) < point(k) && ancestor.get.left != -1) {
        findParent(ancestor.get.left, point, (k + 1) % K)
      }
      else if (ancestor.get.point(k) >= point(k) && ancestor.get.right != -1) {
        findParent(ancestor.get.right, point, (k + 1) % K)
      }

      // if the ancestor is the direct parent (e.g. left/right == -1)
      else if (ancestor.get.point(k) < point(k)) {
        new parentNode(true, pointer, ancestor)
      }
      else if (ancestor.get.point(k) >= point(k)) {
        new parentNode(false, pointer, ancestor)
      }


      else {  //(ancestor.isEmpty) {
        throw new Exception("Pointer doesn't point to an actual Node on-disk. This should not happen, actually - because this is always called after the first node is there.")
      }

    }


    def insert(point: Point, record: String): Unit = {
      // left and right node are both -1
      val node = new Node(-1, -1, point, record)
      val pointer = node.store()

      // if there is a parent Node, update it
      val parent = findParent(point = point)
      parent.node.foreach {parentNode =>
        // ignore if the found parent is actually the node itself (this)
        if(pointer != parent.pointer) {
          println("updating parent: " + parentNode + ", node: " + node)
          val updatedParent = new Node(
            left  = if(parent.childIsLeft) pointer else parentNode.left,
            right = if(!parent.childIsLeft) pointer else parentNode.right,
            point = parentNode.point,
            record = parentNode.record
          )
          updatedParent.store(parent.pointer)
        }

      }

    }


  }
//
//  // worst-case-scenario (always insert at edges of the tree)
//  (0 until 1000).foreach { nr =>
//    println("nr :" + nr)
//    if(nr % 2 == 0) Node.insert(Array(nr.toDouble, nr.toDouble, nr.toDouble), nr.toString)
//    else  Node.insert(Array((-nr).toDouble, (-nr).toDouble, (-nr).toDouble), (-nr).toString)
//  }


  // worst-case-scenario (always insert at edges of the tree)
  (0 until 100).foreach { it =>
    val nr = math.random()
    println("iteration :" + it)
    Node.insert((0 until K).map(b => nr).toArray, nr.toString)
  }


}


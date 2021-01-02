package KDTree

import java.io._
import java.nio.ByteBuffer


/**
 * A class to store string records on-disk using K-D-Tree indexing.
 * Records are indexed based on <K> dimensions (coordinates).
 * The database is stored in a file called <K>_<databaseName>
 * There is one supported operation: inserting nodes. Inserting nodes is NOT thread-safe.
 * @param K: the number of dimensions
 * @param databaseName: the 'database' name
 */
class KDTree(val K: Int, val databaseName: String){

  val filename = this.K.toString + "_" + databaseName + ".kd"

  // a Coordinate is represented by a Double, a point by a set of Coordinates
  type Coordinate = Double
  type Point = Seq[Coordinate]

  // the Pointer to a node - e.g. a byte offset
  type Pointer = Long

  // the record is just a string (e.g. a single value, or some JSON-decoded message)
  type Record = String

  assert(K > 0, "number of dimensions must be > 0")
  assert(databaseName.length > 0, "database name cannot be empty")

  // for worst-case scenario (insertion only at edges of the Tree)
  var coordinate: Double = 0.0000000000
  var coordinateIncrement: Double = 0.0000000001


  /**
   * Inserts a <record> in the database, located/indexed at <point>.
   * Returns the number of iterations required to the tree in the node (e.g. how many nodes
   * had to be read in order to find the node's parent).
   */
  def insert(point: Point, record: Record): Integer = {
    assert(point.length == K, "point must contain exactly K coordinates.")

    // Pointer to left and right node are both -1 as inserted nodes have no children
    val node = new Node(-1, -1, point, record)
    val pointer = node.store()

    // if there is a parent Node, update it
    val parent = Node.findParent(point = point)
    parent.node.foreach { parentNode =>
      // only proceed if the found parent is NOT the node itself (this)
      if (pointer != parent.pointer) {
        val updatedParent = new Node(
          left = if (parent.childIsLeft) pointer else parentNode.left,
          right = if (!parent.childIsLeft) pointer else parentNode.right,
          point = parentNode.point,
          record = parentNode.record
        )
        updatedParent.store(parent.pointer)
      }

    }
    parent.iterations
  }

  /**
   * Inserts a random record, at a random point.
   */
  def insertRandom(): Integer = {
    val iterations = insert((0 until K).map(k => math.random()), randomRecord())
    iterations
  }

  /**
   * Inserts a random record, at a point that is slightly 'higher' than the previous node.
   */
  def insertWorstCase(): Integer = {
    val iterations = insert((0 until K).map(k => coordinate), randomRecord())
    coordinate = coordinate + coordinateIncrement
    iterations
  }


  /**
   * Returns a random integer wrapped inside a JSON-object.
   */
  def randomRecord(): String = {
    val random = scala.util.Random
    "{\"record\":" + random.nextInt(10000).toString + "}"
  }


  class Node(val left: Pointer, val right: Pointer, val point: Point, val record: Record) {
    def serialize: Array[Byte] = {
      val recordBytes = record.getBytes("UTF-8")
      println(recordBytes.size)
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


    /**
     * Stores <this> node on-disk. Appends the node to the end of the file, or overwrites
     * the node that starts at pointer <nodeToOverwrite> (if that argument is supplied).
     * Note: the operation to overwriting an existing node exists because we need to update its left/right child.
     * Updating the <record> of a node
     * @param nodeToOverwrite: Pointer to the node to overwrite.
     * @return
     */
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


    def read(pointer: Pointer, chunkSize: Int = 50, file: RandomAccessFile = new RandomAccessFile(filename, "r")): Option[Node] = {
      // return None if file doesn't contain enough bytes to contain a Node
      var node: Option[Node] = None

      // read bytes from the file until the entire Node is deserializable,
      // but only if there are bytes left
      if (file.length() - baseSize >= pointer) {
        file.seek(pointer)
        val array = (0 until chunkSize).iterator.map(_.toByte).toArray
        file.read(array)
        node = Node.deserialize(array).orElse{
          println("need to get more bytes to read entire Node")
          // double the number of bytes read with each try
          read(pointer, chunkSize * 2, file)
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
      if (bytesRead < size) {
        println("size:" + size + " read: " + bytesRead)
        None
      } else {
        // deserialize and return the Node
        Some(
          new Node(
            left = stream.getLong,
            right = stream.getLong,
            point = (for (_ <- 0 until K) yield stream.getDouble).toArray,
            record = new Record(
              (for (_ <- 0 until size - baseSize) yield stream.get()).toArray, "UTF-8"
            )
          )
        )
      }
    }


    class parentNode(val childIsLeft: Boolean, val pointer: Pointer, val node: Option[Node], val iterations: Integer)
    def findParent(pointer: Pointer = 0, point: Point, k: Int = 0, iterations: Integer = 1): parentNode = {
      // the ancestor - which is potentially(!) the parent
      // simply the first node (pointer = 0) when invoked for the first time
      val ancestor = Node.read(pointer)

      // traverse down to more 'direct' ancestors
      if (ancestor.get.point(k) < point(k) && ancestor.get.left != -1) {
        findParent(ancestor.get.left, point, (k + 1) % K, iterations + 1)
      }
      else if (ancestor.get.point(k) >= point(k) && ancestor.get.right != -1) {
        findParent(ancestor.get.right, point, (k + 1) % K, iterations + 1)
      }

      // if the ancestor is the direct parent (e.g. left/right == -1)
      else if (ancestor.get.point(k) < point(k)) {
        new parentNode(true, pointer, ancestor, iterations)
      }
      else if (ancestor.get.point(k) >= point(k)) {
        new parentNode(false, pointer, ancestor, iterations)
      }


      else { //(ancestor.isEmpty) {
        throw new Exception("Pointer doesn't point to an actual Node on-disk. This should not happen, because this is always called after the first node is there.")
      }

    }

  }


}




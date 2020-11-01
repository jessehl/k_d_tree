class Main {


  class Box(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)


  class Node(sep: Int, leaf: Boolean, values: List[String], smaller: Node, bigger: Node) {
    def findNodes(box: Box): List[Node] = {
      verticalSearch(box)
    }

    def verticalSearch(box: Box): List[Node] = {
      if(this.leaf) List(this)
      else if (box.minY <= sep && box.maxY <= sep) smaller.horizontalSearch(box)
      else if (box.minY <= sep && box.maxY >= sep) smaller.horizontalSearch(box) ::: bigger.horizontalSearch(box)
      else bigger.horizontalSearch(box)
    }

    def horizontalSearch(box: Box): List[Node] = {
      if(this.leaf) List(this)
      else if (box.minX <= sep && box.maxX <= sep) smaller.verticalSearch(box)
      else if (box.minX <= sep && box.maxX >= sep) smaller.verticalSearch(box) ::: bigger.verticalSearch(box)
      else bigger.verticalSearch(box)
    }

  }
}

class Main {


type Point = Vector[Vector[Float]]
type Data = Vector[String]


abstract class Tree{
  def include(tree: Tree): Tree
}

class FilledTree(val point: Point, left: Tree, right: Tree, data: Data){
  def include = new FilledTree(point, left, right, data)
}



class VoidTree extends Tree {
  def include(tree: Tree) = tree
}





}

package KDTree

import java.io.RandomAccessFile


object Main extends App {


  val numberOfRecords = 100
  val maxNumberOfDimensions = 3

  val results = Array("worst_case", "best_case").map{ scenario =>
    (1 to maxNumberOfDimensions).map { nrOfDimensions =>
      val k_d_tree = new KDTree(K = nrOfDimensions, databaseName = scenario)
      (1 to numberOfRecords).map(nr => Array(scenario, nrOfDimensions.toString, nr.toString, if(scenario == "worst_case") k_d_tree.insertWorstCase().toString else k_d_tree.insertRandom().toString))
    }
  }.flatten.flatten

  val file = new RandomAccessFile("output.csv", "rw")
  // write csv file
  //file.writeUTF("scenario,number_of_dimensions,number_of_records,number_of_iterations\n")
  file.writeUTF(results.map(_.mkString(",")).mkString("\n"))
  file.close()



}

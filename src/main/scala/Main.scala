package KDTree

import java.io.{PrintWriter}
import java.time.LocalDateTime
import java.time.temporal._

object Main extends App {


  val numberOfRecords = 10000000
  val maxNumberOfDimensions = 3

  val results = Array("worst_case", "best_case").map { scenario =>
    (1 to maxNumberOfDimensions).map { nrOfDimensions =>
      val k_d_tree = new KDTree(K = nrOfDimensions, databaseName = scenario)
      println("computing: " + k_d_tree.filename)
      (1 to numberOfRecords).map { nr =>
        val startTime = LocalDateTime.now()
        Array(
          scenario,
          nrOfDimensions.toString,
          nr.toString,
          if (scenario == "worst_case") k_d_tree.insertWorstCase().toString else k_d_tree.insertRandom().toString,
          startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS).toString
        )
      }
    }
  }.flatten.flatten

  // write csv file
  new PrintWriter("output.csv") {
    write("scenario,number_of_dimensions,number_of_records,number_of_iterations,milliseconds\n")
    write(results.map(_.mkString(",")).mkString("\n"))
    close()
  }



}

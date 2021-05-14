package KDTree

import java.io.{PrintWriter}
import java.time.LocalDateTime
import java.time.temporal._

object Main extends App {


  val numberOfRecords = 2500
  val numberOfDimensions = 5
  val scenarios = Array("worst_case", "average_case")

  val results = scenarios.map { scenario =>
      val kdTree = new KDTree(K = numberOfDimensions, databaseName = scenario)
      println("computing: " + kdTree.filename)
      (1 to numberOfRecords).map { nr =>
        val startTime = LocalDateTime.now()
        Array(
          scenario,
          numberOfDimensions.toString,
          nr.toString,
          if (scenario == "worst_case") kdTree.insertWorstCase().toString else kdTree.insertRandom().toString,
          startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS).toString
        )
      }
  }.flatten

  // write csv file
  new PrintWriter("output.csv") {
    write("scenario,number_of_dimensions,number_of_records,number_of_iterations,milliseconds\n")
    write(results.map(_.mkString(",")).mkString("\n"))
    close()
  }



}

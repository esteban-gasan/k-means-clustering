package scala

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.math.sqrt
import scala.io.Source
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    runClass()
    // runFunctions()
  }

  def runClass(): Unit = {
    println("oop")
    var kmeans = new KMeans(3, "data")
    kmeans.predict(10)
  }

  def runFunctions(): Unit = {
    println("funcs")
    // 1. Generate a random k
    // val k = randomNumber(10)
    val K = 3 // TODO change back to random
    val filename = "data"

    // Store data in an array of arrays
    val data = loadData(filename)
    val dataSize = data.length
    var clusters = initClusters(K)
    var centroids = initCentroids(K, data)
    var error = 0.0
    val Epsilon = 0

    var iterations = 10

    if (K > dataSize) {
      println("K must be less than the size of the dataset")
      return
    }
    var i = 0
    var end = false
    var previous_sse = 0.0
    while (!end) {
      emptyClusters(clusters)

      // 2. & 3. Assign each point to their closest centroid
      for (point <- data) assignPoint(point, centroids, clusters)

      // 4. Update each cluster's centroid
      updateCentroids(centroids, clusters)

      // printResults(centroids, clusters)

      // Calculate the squared sum of errors
      val sse = calculateSse(centroids, clusters)
      error = (previous_sse - sse).abs
      if (error <= Epsilon || i > iterations) end = true

      // if (i % 10 == 0) println(s"i: $i, e: $error")
      println(s"i: $i, e: $error")

      previous_sse = sse
      i += 1
      // 5. Repeat
    }
  }

  def randomNumber(range: Int): Int = {
    /* Choose a random number from a given range */
    val number = Random.nextInt(range)
    number
  }

  def loadData(filename: String): Array[Array[Double]] = {
    /* Load data into a 2d-array */
    Source
      .fromFile(s"src/main/resources/$filename.csv")
      .getLines()
      .map(_.split(",").map(_.trim.toDouble))
      .toArray
  }

  def initClusters(K: Int): Map[Int, ArrayBuffer[Array[Double]]] = {
    /* Create a map of k-ArrayBuffers */
    var clusters = Map.empty[Int, ArrayBuffer[Array[Double]]]
    for (i <- 0 to K - 1)
      clusters += (i -> ArrayBuffer.empty[Array[Double]])
    // HashMap(0 -> ArrayBuffer(), 1 -> ArrayBuffer(), 2 -> ArrayBuffer(), ...)

    clusters
  }

  def initCentroids(
      K: Int,
      data: Array[Array[Double]]
  ): Array[Array[Double]] = {
    /* Create an array of k-arrays, without specifying their dimension */
    var centroids = Array.ofDim[Double](K, 0)
    // Array(Array(), Array(), Array(), ...)

    // Fill the centroids array with the first items from data
    for (i <- 0 to K - 1)
      centroids(i) = data(i) //? Can be changed to random too

    centroids
  }

  def emptyClusters(clusters: Map[Int, ArrayBuffer[Array[Double]]]): Unit = {
    /* Empty each cluster to re-assign the points */
    for ((k, v) <- clusters) clusters(k) = ArrayBuffer.empty[Array[Double]]
  }

  def assignPoint(
      point: Array[Double],
      centroids: Array[Array[Double]],
      clusters: Map[Int, ArrayBuffer[Array[Double]]]
  ): Unit = {
    /* Assign a given point to the cluster of the closest centroid */
    // Get index of the closest centroid
    val centroidId = closestCentroid(point, centroids)
    clusters(centroidId).append(point)
  }

  def closestCentroid(
      point: Array[Double],
      centroids: Array[Array[Double]]
  ): Int = {
    /* Find the closest centroid to a given point and return its index*/
    var distances = new Array[Double](centroids.length)
    // Store the distances into an array for each centroid
    for (i <- 0 to centroids.length - 1)
      distances(i) = euclideanDistance(point, centroids(i))

    // Get the index of the shortest distance
    val centroidId = distances.indexOf(distances.min)
    centroidId
  }

  def euclideanDistance(
      point: Array[Double],
      centroid: Array[Double]
  ): Double = {
    /* Calculate the euclidean distance between a point and a centroid */
    sqrt(squaredDistance(point, centroid))
  }

  def squaredDistance(point: Array[Double], centroid: Array[Double]): Double = {
    var sum = 0.0
    var sub = 0.0
    for (i <- 0 to point.length - 1) {
      sub = point(i) - centroid(i)
      sum += sub * sub
    }
    sum
  }

  def updateCentroids(
      centroids: Array[Array[Double]],
      clusters: Map[Int, ArrayBuffer[Array[Double]]]
  ): Unit = {
    /* Assign each cluster's mean value as their new centroid */
    for ((centroidId, cluster) <- clusters) {
      // if (cluster.isEmpty)
      val newCentroid = mean(cluster)
      centroids(centroidId) = newCentroid
    }
  }

  def mean(cluster: ArrayBuffer[Array[Double]]): Array[Double] = {
    /* Calculate the mean value of a given cluster of points */
    // if (cluster.empty) return //! ret null?

    // Create an array of zeros with the same length as the first point
    var newCentroid = new Array[Double](cluster(0).length)

    // Calculate the element wise sum of all the points
    for (i <- 0 to cluster.length - 1)
      for (j <- 0 to cluster(i).length - 1)
        newCentroid(j) += cluster(i)(j)

    // Divide each element of the new centroid by the number of points
    for (i <- 0 to newCentroid.length - 1)
      newCentroid(i) /= cluster.length

    newCentroid
  }

  def calculateSse(
      centroids: Array[Array[Double]],
      clusters: Map[Int, ArrayBuffer[Array[Double]]]
  ): Double = {
    var totalSum = 0.0
    for ((centroidId, cluster) <- clusters) {
      var sum = 0.0
      for (point <- cluster)
        sum += squaredDistance(point, centroids(centroidId))

      totalSum += sum
    }
    totalSum
  }

  def printResults(
      centroids: Array[Array[Double]],
      clusters: Map[Int, ArrayBuffer[Array[Double]]]
  ): Unit = {
    for ((k, v) <- clusters) {
      println(s"\nCentroid $k:   ${centroids(k).mkString("[", ", ", "]")}")
      print(s"Cluster  $k: { ")
      for (point <- v) print(s"${point.mkString("[", ", ", "]")}, ")
      println("}\n")
    }
  }
}

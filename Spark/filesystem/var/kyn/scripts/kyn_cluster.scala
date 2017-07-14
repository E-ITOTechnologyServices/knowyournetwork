/**
Copyright (C) 2017 e-ito Technology Services GmbH
e-mail: info@e-ito.de

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.json4s.jackson.Serialization.{read,write}
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors


def normalize(s: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector]): org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector] = {
    val mmax = (for (i <- 0 to s.first.size-1) yield (s.map(x => x(i)).max  )).toArray.map(x => if (x == 0) { 1 }else{ x })
      val r = s.map(x => Vectors.dense((for (i <- 0 to x.size-1) yield { x(i)/mmax(i) }).toArray))
        return r
}

var args = sc.getConf.get("spark.driver.args").split("\\s+")
var numClusters = 10

if (args.size < 2) {
    args = Array("/home/hadoopuser/kmeansinput.csv","/home/hadoopuser/kmeansoutput")
}

if (args.size > 2) {
    numClusters = args(2).toInt
}

val nf_rdd = sc.textFile("file://"+args(0))

val parsedDataRaw = nf_rdd.map(s => Vectors.dense(s.split(' ').drop(1).map(_.toDouble))).cache()
val parsedData = normalize(parsedDataRaw).cache()
val ipData = nf_rdd.map(s => s.split(' ')(0))
val ar1 = parsedData.collect()
val ar2 = ipData.collect()
val parsedSeq = for (i <- 0 to ar1.length-1) yield { (ar2(i), ar1(i)) }
val parsedDataSet =  sc.parallelize(parsedSeq)

val numIterations = 100

val clusters = KMeans.train(parsedDataSet.values, numClusters, numIterations)
val results  = parsedDataSet.map(x => x._1.toString.replaceAll("\\[|\\]","")+","+ x._2.toString.replaceAll("\\[|\\]","")+","+x._2.toString.replaceAll("\\[|\\]","")+","+clusters.predict(x._2).toString+","+Vectors.sqdist(x._2,clusters.clusterCenters(clusters.predict(x._2))).toString )

results.saveAsTextFile("file://"+args(1))
sys.exit

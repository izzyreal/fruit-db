package services

import models.Cultivar

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DatabaseService {

  def getDb = { Database.forConfig("slick-postgres") }

  var species = TableQuery[Species];
  val cultivars = TableQuery[Cultivars];

  implicit val duration = Duration.Inf

  /* Class that represents a table of species */
  class Species(tag: Tag) extends Table[(Option[Int], String, String, String)](tag, Some("fruit"), "species") {
    def id = column[Option[Int]]("species_id", O.PrimaryKey, O.AutoInc)
    def genus = column[String]("genus_name")
    def name = column[String]("species_name")
    def commonName = column[String]("common_name")
    def * = (id, genus, name, commonName)
  }

  /* Class that represents a table of cultivars */
  class Cultivars(tag: Tag) extends Table[(Int, String, Int)](tag, Some("fruit"), "cultivars") {
    def id = column[Int]("cultivar_id")
    def name = column[String]("cultivar_name")
    def speciesId = column[Int]("species_id")
    def * = (id, name, speciesId)
  }

  /* List all species in the database */
  def listSpecies(): String = {

    val db = getDb

    var res = ""
    try {
      Await.result(db.run(species.result), duration).foreach {
        case (id, genus, name, commonName) => {
          res += id.get + "\t" + genus.capitalize + " " + name + " (" + commonName + ")" + "\n"
        }

      }
    } finally db.close
    res
  }


  /* List all cultivars in the database, accompanied by their common species name */
  def listCultivars(): String = {

    val db = getDb

    var res = ""
    try {

      val innerJoin = for {(c, s) <- cultivars join species on (_.speciesId === _.id)} yield (c.name, s.commonName)

      Await.result(db.run(innerJoin.result), duration).foreach {
        case (cultivar, speciesCommonName) => {
          val cultivarCap = cultivar.split(' ').map(_.capitalize).mkString(" ")

          res += cultivarCap + " is a " + speciesCommonName + " cultivar\n"
        }
      }
    } finally db.close
    res
  }

  def listCultivarsOfSpeciesCommonName(name: String): ArrayBuffer[Cultivar] = {

    val db = getDb

    val res = ArrayBuffer[Cultivar]()

    try {
      val innerJoin = for {(c, _) <- cultivars join species.filter(_.commonName === name) on (_.speciesId === _.id)} yield c.name
      Await.result(db.run(innerJoin.result), duration).foreach {
        cultivar => {
          val cultivarCap = cultivar.split(' ').map(_.capitalize).mkString(" ")
          res.append(Cultivar(cultivarCap))
        }
      }
    } finally db.close
    res
  }

  def insertNewSpecies(genusName: String, speciesName: String, commonName: String): Unit = {

    val db = getDb

    try {

      val insertAction = DBIO.seq(species += (None, genusName, speciesName, commonName))
      println("genus: " + genusName)
      Await.result(db.run(insertAction), duration)

      println("Insert statement executed.")

    } finally db.close()

  }

}

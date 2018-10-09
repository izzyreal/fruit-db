package services

import models.Cultivar

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DatabaseService {

  def getDb = {
    Database.forConfig("slick-postgres")
  }

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
  class Cultivars(tag: Tag) extends Table[(Option[Int], String, Int)](tag, Some("fruit"), "cultivars") {
    def id = column[Option[Int]]("cultivar_id", O.PrimaryKey, O.AutoInc)

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

  def insertSpecies(genusName: String, speciesName: String, commonName: String): Unit = {

    val db = getDb

    try {

      val insertAction = DBIO.seq(species += (None, genusName, speciesName, commonName))
      Await.result(db.run(insertAction), duration)

    } finally db.close()

  }

  def deleteSpecies(id: Int): Unit = {

    val db = getDb

    try {

      val deleteAction = species.filter(_.id === id).delete
      Await.result(db.run(deleteAction), duration)

    } finally db.close()

  }

  def deleteSpecies(commonName: String): Unit = {

    val db = getDb

    try {

      val deleteAction = species.filter(_.commonName === commonName).delete
      Await.result(db.run(deleteAction), duration)

    } finally db.close()

  }

  def insertCultivar(name: String, speciesId: Int): Unit = {

    val db = getDb

    try {

      val insertAction = DBIO.seq(cultivars += (None, name, speciesId))
      Await.result(db.run(insertAction), duration)

    } finally db.close()

  }

  def deleteCultivar(name: String): Unit = {

    val db = getDb

    try {

      val deleteAction = cultivars.filter(_.name === name).delete
      Await.result(db.run(deleteAction), duration)

    } finally db.close()

  }

  def deleteCultivar(id: Int): Unit = {

    val db = getDb

    try {

      val deleteAction = cultivars.filter(_.id === id).delete
      Await.result(db.run(deleteAction), duration)

    } finally db.close()

  }

}
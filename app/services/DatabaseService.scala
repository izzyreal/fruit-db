package services

import models.Cultivar

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, TableQuery}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DatabaseService {

  implicit val duration = Duration.Inf

  val species = TableQuery[Species];
  val cultivars = TableQuery[Cultivars];

  class Species(tag: Tag) extends Table[(Int, String, String, String)](tag, Some("fruit"), "species") {
    def id = column[Int]("species_id")

    def genus = column[String]("genus_name")

    def name = column[String]("species_name")

    def commonName = column[String]("common_name")

    def * = (id, genus, name, commonName)
  }

  class Cultivars(tag: Tag) extends Table[(Int, String, Int)](tag, Some("fruit"), "cultivars") {
    def id = column[Int]("cultivar_id")

    def name = column[String]("cultivar_name")

    def speciesId = column[Int]("species_id")

    def * = (id, name, speciesId)
  }

  def listSpecies(): String = {
    val db = Database.forConfig("slick-postgres")
    var res = ""
    try {
      Await.result(db.run(species.result), duration).foreach {
        case (id, genus, name, commonName) => {
          res += id + "\t" + genus.capitalize + " " + name + " (" + commonName + ")" + "\n"
        }

      }

    } finally db.close
    return res
  }

  def listCultivars(): String = {
    val db = Database.forConfig("slick-postgres")
    var res = ""
    try {

      val innerJoin = for {(c, s) <- cultivars join species on (_.speciesId === _.id)} yield (c.name, s.commonName)

      Await.result(db.run(innerJoin.result), duration).foreach {
        case (cultivar, speciesCommonName) => {
          val cultivarCap = cultivar.split(' ').map(_.capitalize).mkString(" ")

          res += cultivarCap + " is a " + speciesCommonName + " cultivar\n"
        }
      }
    }
    finally db.close

    return res
  }

  def listCultivarsOfSpeciesCommonName(name: String): ArrayBuffer[Cultivar] = {
    val db = Database.forConfig("slick-postgres")
    val res = ArrayBuffer[Cultivar]()

    try {

      val filteredSpecies = species.filter(_.commonName === name)

      val innerJoin = for {(c, s) <- cultivars join filteredSpecies on (_.speciesId === _.id)} yield (c.name, s.commonName)

      Await.result(db.run(innerJoin.result), duration).foreach {
        case (cultivar, speciesCommonName) => {
          val cultivarCap = cultivar.split(' ').map(_.capitalize).mkString(" ")

          res.append(Cultivar(cultivarCap))
        }
      }
    }
    finally db.close

    return res
  }

}

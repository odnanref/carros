package dal

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent._

/**
  * A repository for Modelos.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class ModeloRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class ModeloTable(tag: Tag) extends Table[Modelo](tag, "marca_modelo") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def descricao = column[String]("descricao")

    def marca = column[Long]("marca_id")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, marca_id , descricao) <>
      ((Modelo.apply _).tupled, Modelo.unapply)

  }

  /**
    * The starting point for all queries on the people table.
    */
  private val NotiRepo = TableQuery[ModeloTable]

  /**
    * Create a car with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def create(descricao:String, marca_id): Future[Modelo] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (NotiRepo.map(p => (p.descricao, p.marca_id))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning NotiRepo.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Modelo(id, nameAge._1, nameAge._2))
      // And finally, insert the car into the database
      ) += (descricao, marca_id)
  }

  /** Insert a new . */
  def insert(Modelo: Modelo): Future[Unit] =
    db.run(NotiRepo += Modelo).map(_ => ())

  /**
    * Create a Modelo
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def edit(id:Long, Modelo:Modelo): Future[Unit] = {
    val notiToUpdate: Modelo = Modelo.copy(Some(id))
    db.run(NotiRepo.filter(_.id === id).update(notiToUpdate)).map(_ => ())
  }
  /**
    * List all the cars in the database.
    */
  def list(): Future[Seq[Modelo]] = db.run {
    NotiRepo.result
  }

  def get(id: Long): Future[Option[Modelo]] = {
    dbConfig.db.run(NotiRepo.filter(_.id === id).result.headOption)
  }

  def getMarcaByModelo(id:Long): Future[Option[Modelo]] = {
    dbConfig.db.run(NotiRepo.filter(_.marca_id === id).result.headOption)
  }

}

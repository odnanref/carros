package dal

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent._

/**
  * A repository for Marcas.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class MarcaRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class MarcaTable(tag: Tag) extends Table[Marca](tag, "marca") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def descricao = column[String]("descricao")


    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, descricao) <>
      ((Marca.apply _).tupled, Marca.unapply)

  }

  /**
    * The starting point for all queries on the people table.
    */
  private val NotiRepo = TableQuery[MarcaTable]

  /**
    * Create a car with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def create(descricao:String): Future[Marca] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (NotiRepo.map(p => (p.descricao))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning NotiRepo.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Marca(id, nameAge._1))
      // And finally, insert the car into the database
      ) += (descricao)
  }

  /** Insert a new . */
  def insert(Marca: Marca): Future[Unit] =
    db.run(NotiRepo += Marca).map(_ => ())

  /**
    * Create a Marca
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def edit(id:Long, Marca:Marca): Future[Unit] = {
    val notiToUpdate: Marca = Marca.copy(Some(id))
    db.run(NotiRepo.filter(_.id === id).update(notiToUpdate)).map(_ => ())
  }
  /**
    * List all the cars in the database.
    */
  def list(): Future[Seq[Marca]] = db.run {
    NotiRepo.result
  }

  def get(id: Long): Future[Option[Marca]] = {
    dbConfig.db.run(NotiRepo.filter(_.id === id).result.headOption)
  }

}

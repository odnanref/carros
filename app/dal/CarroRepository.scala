package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models._

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class CarroRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class CarroTable(tag: Tag) extends Table[Carro](tag, "carro") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The description column */
    def description = column[String]("description")

    /** The img column */
    def img = column[String]("img")

    /** The keywords column */
    def keywords = column[String]("keywords")

    def state = column[String]("state")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */ 
    def * = (id, name, description, img, keywords, state) <> 
      ((Carro.apply _).tupled, Carro.unapply)
    
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val carro = TableQuery[CarroTable]

  /**
   * Create a car with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, description: String, img:String, keywords: String, state: String): Future[Carro] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (carro.map(p => (p.name, p.description, p.img, p.keywords, p.state))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning carro.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Carro(id, nameAge._1, nameAge._2, nameAge._3, nameAge._4, nameAge._5))
    // And finally, insert the car into the database
    ) += (name, description, img, keywords, state)
  }

  /** Insert a new car. */
  def insert(car: Carro): Future[Unit] =
    db.run(carro += car).map(_ => ())

  /**
   * Create a car with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
   * id for that person.
   */
  def edit(id:Long, car:Carro): Future[Unit] = {
    val carToUpdate: Carro = car.copy(Some(id))
    db.run(carro.filter(_.id === id).update(carToUpdate)).map(_ => ())
  }
  /**
   * List all the cars in the database.
   */
  def list(): Future[Seq[Carro]] = db.run {
    carro.result
  }

  /**
   * List all the cars in the database.
   */
  def listActive(): Future[Seq[Carro]] = db.run {
    carro.filter(_.state === "active").result
  }

  def get(id: Long): Future[Option[Carro]] = {
    dbConfig.db.run(carro.filter(_.id === id).result.headOption)
  }

}
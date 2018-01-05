package dal

import java.sql.Timestamp
import java.time.LocalTime
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import models._
import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * A repository for Cars.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class CarroRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  protected class CarroTable(tag: Tag) extends Table[Carro](tag, "carro") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The name column */
    def year = column[Int]("year")

    /** The description column */
    def description = column[String]("description")

    /** The img column */
    def img = column[String]("img")

    /** The keywords column */
    def keywords = column[String]("keywords")

    def state = column[String]("state")

    def model = column[Long]("model")

    def datein = column[DateTime]("datein")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, name, year, description, img, keywords, state, model, datein) <>
      ((Carro.apply _).tupled, Carro.unapply)
  }

  implicit val JodaDateTimeMapper = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis),
    ts => new DateTime(ts.getTime())
  )

  val PAGESIZE: Int = 20

  implicit class QueryExtensions[T, E, S[E]](val q: Query[T, E, S]) {
    def page(no: Int, pageSize: Int): Query[T, E, S] = {
      q.drop((no - 1) * pageSize).take(pageSize)
    }
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
  def create(name: String, description: String, year: Int, img: String, keywords: String, state: String, model: Long):
  Future[Carro] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (carro.map(p => (p.name, p.year, p.description, p.img, p.keywords, p.state, p.model, p.datein))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning carro.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Carro(id, nameAge._1, nameAge._2, nameAge._3, nameAge._4, nameAge._5, nameAge._6, nameAge._7,
      nameAge._8
      ))
      // And finally, insert the car into the database
      ) +=(name, year, description, img, keywords, state, model, new DateTime() )
  }

  //val insertQuery = carro returning carro.map(_.id) into ((Carro, id) => Carro.copy(id = id))
  val insertQuery = carro returning carro.map(_.id.get)

  /** Insert a new car. */
  def insert(car: Carro): Future[Long] = {
    val action = (insertQuery += car)
    db.run(action)
  }

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
  def list(page: Int): Future[Seq[Carro]] = db.run {
    carro.sortBy(_.name).page(page, PAGESIZE).result
  }

  /**
    * Say how many there are
    */
  def count(): Future[Int] = db.run {
    carro.length.result
  }

  /**
   * List all the cars in the database.
   */
  def listActive(page: Int): Future[Seq[Carro]] = db.run {
    carro.filter(_.state === "active").page(page, PAGESIZE).result
  }

  /**
    * Get the specific Car
    *
    * @param id
    * @return Future[Option[Carro]]
    **/
  def get(id: Long): Future[Option[Carro]] = {
    dbConfig.db.run(carro.filter(_.id === id).result.headOption)
  }

  /**
    * Get the main image for the car
    *
    * @param id
    * @return string
    */
  def getImage( id:Long) :String = {
    if (id <= 0) {
      return "logo.png"
    }

    Await.result(this.get(id.toLong), 10 seconds).get.img
  }

  /**
   * Clear the image field to logo.png so there is no default img to show
   */
  def clearImage(id :Long) : Unit = {
    val q = for { c <- carro if c.id === id } yield c.img
    val updateAction = q.update("logo.png")

    // Get the statement without having to specify an updated value:
    val sql = q.updateStatement
    
    db.run(updateAction)
  }

  /**
    * Delete the ID from the database
    *
    * @param id
    */
  def remove(id:Long): Unit = {
    db.run(carro.filter(_.id === id ).delete)
  }

  /**
    * Update only the filename of a specific car id
    *
    * @param filename
    * @param car_id
    * @return
    */
  def updateMainImage(filename:String, car_id: Long) :Future[Int] = {
    val q = for { l <- carro if l.id === car_id } yield l.img
    db.run(q.update(filename))
  }

  def search(model:Long, year:Int) : Future[Seq[Carro]] = {
    db.run(
      carro.filter( x => (x.model === model && x.year === year) ).result
    )
  }
}
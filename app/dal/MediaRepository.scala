package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models._

import scala.concurrent._
import scala.concurrent.duration._

import scala.util.{Success, Failure}

/**
 * A repository for Media.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class MediaRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class MediaTable(tag: Tag) extends Table[Media](tag, "media") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def filename = column[String]("filename")

    /** The description column */
    def path = column[String]("path")

    /** The description column */
    def car_id = column[Long]("car_id")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */ 
    def * = (id, filename, path, car_id) <> 
      ((Media.apply _).tupled, Media.unapply)
    
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val media = TableQuery[MediaTable]

  /**
   * Create a media with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, path:String, car_id:Long): Future[Media] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (media.map(p => (p.filename, p.path, p.car_id))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning media.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Media(id, nameAge._1, nameAge._2, nameAge._3))
    // And finally, insert the car into the database
    ) += (name, path, car_id)
  }

  /** Insert a new media. */
  def insert(med: Media): Future[Unit] = {
    db.run(media += med).map(_ => ())
  }

  /**
   * Create a media with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
   * id for that person.
   */
  def edit(id:Long, med:Media): Future[Unit] = {
    val medToUpdate: Media = med.copy(Some(id))
    db.run(media.filter(_.id === id).update(medToUpdate)).map(_ => ())
  }
  /**
   * List all the cars in the database.
   */
  def list(): Future[Seq[Media]] = db.run {
    media.result
  }

  def get(id: Long): Future[Option[Media]] = {
    dbConfig.db.run(media.filter(_.id === id).result.headOption)
  }

  def getByCarId(id:Long) : Seq[Media] = {
    Await.result(dbConfig.db.run(media.filter(_.car_id === id).result), 10 seconds)
  }

}

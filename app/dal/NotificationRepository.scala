package dal

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent._

/**
  * A repository for Notifications.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class NotificationRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class NotificationTable(tag: Tag) extends Table[Notification](tag, "notification") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def email = column[String]("email")

    def model = column[Long]("model")

    def make = column[Long]("make")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, email, make, model) <>
      ((Notification.apply _).tupled, Notification.unapply)

  }

  val PAGESIZE: Int = 10

  implicit class QueryExtensions[T, E, S[E]](val q: Query[T, E, S]) {
    def page(no: Int, pageSize: Int): Query[T, E, S] = {
      q.drop((no - 1) * pageSize).take(pageSize)
    }
  }

  /**
    * The starting point for all queries on the people table.
    */
  private val NotiRepo = TableQuery[NotificationTable]

  /**
    * Create a car with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def create(email:String, make: Long, model: Long): Future[Notification] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (NotiRepo.map(p => (p.email, p.make, p.model))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning NotiRepo.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Notification(id, nameAge._1, nameAge._2, nameAge._3))
      // And finally, insert the car into the database
      ) += (email, make, model)
  }

  /** Insert a new . */
  def insert(notification: Notification): Future[Unit] =
    db.run(NotiRepo += notification).map(_ => ())

  /**
    * Create a notification
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def edit(id:Long, notification:Notification): Future[Unit] = {
    val notiToUpdate: Notification = notification.copy(Some(id))
    db.run(NotiRepo.filter(_.id === id).update(notiToUpdate)).map(_ => ())
  }
  /**
    * List all the cars in the database.
    */
  def list(page: Int = 1): Future[Seq[Notification]] = db.run {
    NotiRepo.page(page, PAGESIZE).result
  }

  /**
    * Say how many there are
    */
  def count(): Future[Int] = db.run {
    NotiRepo.length.result
  }

  def get(id: Long): Future[Option[Notification]] = {
    dbConfig.db.run(NotiRepo.filter(_.id === id).result.headOption)
  }

}
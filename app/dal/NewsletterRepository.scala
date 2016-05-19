package dal

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent._

/**
  * A repository for Cars.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class NewsletterRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class NewsletterTable(tag: Tag) extends Table[Newsletter](tag, "Newsletter") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def email = column[String]("email")

    def state = column[String]("state")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, email, state) <>
      ((Newsletter.apply _).tupled, Newsletter.unapply)

  }

  /**
    * The starting point for all queries on the people table.
    */
  private val NewsRepo = TableQuery[NewsletterTable]

  /**
    * Create a car with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def create(email:String, state: String): Future[Newsletter] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (NewsRepo.map(p => (p.email, p.state))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning NewsRepo.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => Newsletter(id, nameAge._1, nameAge._2))
      // And finally, insert the car into the database
      ) += (email, state)
  }

  /** Insert a new car. */
  def insert(news: Newsletter): Future[Unit] =
    db.run(NewsRepo += news).map(_ => ())

  /**
    * Create a car with the given name and age.
    *
    * This is an asynchronous operation, it will return a future of the created car, which can be used to obtain the
    * id for that person.
    */
  def edit(id:Long, news:Newsletter): Future[Unit] = {
    val newsToUpdate: Newsletter = news.copy(Some(id))
    db.run(NewsRepo.filter(_.id === id).update(newsToUpdate)).map(_ => ())
  }
  /**
    * List all the cars in the database.
    */
  def list(): Future[Seq[Newsletter]] = db.run {
    NewsRepo.result
  }

  /**
    * List all the cars in the database.
    */
  def listActive(): Future[Seq[Newsletter]] = db.run {
    NewsRepo.filter(_.state === "active").result
  }

  def get(id: Long): Future[Option[Newsletter]] = {
    dbConfig.db.run(NewsRepo.filter(_.id === id).result.headOption)
  }

}
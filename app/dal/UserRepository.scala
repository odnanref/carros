package dal

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import models._
import play.api.mvc.Result

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * A repository for Media.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class UserTable(tag: Tag) extends Table[User](tag, "user") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    /** The user column */
    def username = column[String]("username")

    /** The pass column */
    def password = column[String]("password")

    /** The email column */
    def email = column[String]("email")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */ 
    def * = (id, username, password, email) <> 
      ((User.apply _).tupled, User.unapply)
    
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val user = TableQuery[UserTable]

  /**
   * Create a user
   *
   * This is an asynchronous operation, it will return a future of the created
   * id
   */
  def create(username: String, password:String, email:String): Future[User] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (user.map(p => (p.username, p.password, p.email))
      // Now define it to return the id, because we want to know what id was generated for the car
      returning user.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameAge, id) => User(id, nameAge._1, nameAge._2, nameAge._3))
    // And finally, insert the car into the database
    ) += (username, password, email)
  }

  /** Insert a new User. */
  def insert(med: User): Future[Unit] = {
    db.run(user += med).map(_ => ())
  }

  /**
   * Edit, update the user.
   *
   * This is an asynchronous operation
   */
  def edit(id:Long, med:User): Future[Unit] = {
    val medToUpdate: User = med.copy(Some(id))
    db.run(user.filter(_.id === id).update(medToUpdate)).map(_ => ())
  }
  /**
   * List all the cars in the database.
   */
  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def get(id: Long): Future[Option[User]] = {
    dbConfig.db.run(user.filter(_.id === id).result.headOption)
  }

  def erase(id:Long) : Boolean = {
    dbConfig.db.run(user.filter(_.id === id).delete)
    true
  }

  /**
    * Check for autentication credentials in the database
    *
    * @param email
    * @param pass
    * @return
    */
  def checkAuth(email: String, pass: String): Future[Option[User]] = {
    db.run(user.filter( x => ( x.email === email && x.password === pass) ).result.headOption)
  }

}

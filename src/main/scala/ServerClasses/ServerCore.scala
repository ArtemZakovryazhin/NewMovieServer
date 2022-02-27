package ServerClasses
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError}
import ServerClasses.ServerCore._
import akka.actor.{Actor, ActorLogging}
import SQLClasses.SqlMethods._
import java.sql._
import scala.language.postfixOps
import java.util.UUID


trait MovieJsonProtocol extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID]{
    override def write(obj: UUID): JsValue = JsString(obj.toString) //source says never execute this line

    override def read(json: JsValue): UUID = json match {
      case JsString(obj) => UUID.fromString(obj)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
  implicit val movieFormat = jsonFormat4(Movie)
  implicit val directorFormat = jsonFormat1(Director)
  implicit val actorFormat = jsonFormat1(MovieActor)
  implicit val fullMovieFormat = jsonFormat3(FullMovie)

}

//replace your actual postgres username and password in "user" and "password" field

object ServerCore {
  val dbName = "movies_base_demo"
  val user = "postgres"
  val password = "qwe"

  case class Movie (id: UUID, title: String, year: Int, oscar: String)
  case class Director (name: String)
  case class MovieActor (name: String)
  case class FullMovie(movie: Movie, director: Director, actor: MovieActor)

  object DbQueryHandler{

    case class AddMovie(movie: Movie)
    case class AddDirector(director: Director)
    case class AddActor(actor: MovieActor)
    case class Added(message: String)
    case class FindMovie(title: String)
    case class NoSuggestions(message: String)
    case object FindAllMovies
    case class FindYear(title: String)
    case class DoHaveOscar(title: String)
    case class FindDirector(title: String)
    case class FindActor(title: String)
    case class AddFullMovie(movie: Movie, director: Director, actor: MovieActor)
    case class FindMovieSearchingOption(searchingString: String)
    case class DeleteDirector(name: String)
    case class DeleteActor(actor: String)
    case class DeleteMovie(title: String)

    var connectionOpt: Option[Connection] = None
    val connection = connectionOpt.fold(connect(dbName, user, password)){conn => conn}
  }

  class DbQueryHandler extends Actor with ActorLogging {


    import DbQueryHandler._
    override def receive: Receive = {
      case AddMovie(movie) =>
        log.info(s"Adding ${movie.title} to database...")
        insertRowMoviesTableOscar(connection, movie)
        sender() ! Added (s"$movie.title added to database!")
      case AddDirector(director) =>
        log.info(s"Adding ${director.name} into database...")
        insertRowDirectors(connection, director)
        sender() ! Added(s"${director.name} added to database!")
      case AddActor(actor) =>
        log.info(s"Adding ${actor.name} into database...")
        insertRowActors(connection, actor)
        sender() ! Added(s"${actor.name} added to database!")
      case FindMovie (title) =>
        log.info(s"finding a movie by title : $title...")
        sender () ! getMovieByTitle(connection, title)
      case FindAllMovies =>
        sender() ! getAllMovies(connection)
      case FindYear(title) =>
        log.info(s"the year of $title is...")
        sender() ! getYear(connection, title).toString
      case FindDirector(title) =>
        log.info(s"director of $title is...")
        sender() ! getDirectorByMovie(connection, title)
      case FindActor(title) =>
        log.info(s"actor in $title is...")
        sender() ! getActorByMovie(connection, title)
      case DoHaveOscar(title) =>
        log.info(s"finding out for oscar to $title...")
        doHaveOscar(connection, title) match {
          case true => sender() ! s"$title does have an oscar"
          case false => sender() ! s"$title doesn't have an oscar"
        }

      case AddFullMovie(movie, director, actor) =>
        log.info(s"Adding ${movie.title} to database...")
        log.info(s"Adding ${director.name} into database...")
        log.info(s"Adding ${actor.name} into database...")
        insertRowMoviesTableOscar(connection, movie)
        sender() ! Added(s"$movie.title added to database!")
        insertRowDirectors(connection, director)
        sender() ! Added(s"${director.name} added to database!")
        insertRowActors(connection, actor)
        sender() ! Added(s"${actor.name} added to database!")
        insertIntoMoviesDirectors(connection, movie.title, director.name)
        insertIntoMoviesActors(connection, movie.title, actor.name)
      case FindMovieSearchingOption(searchingString) =>
        log.info("searching movies with request...")
        val moviesList: List[Movie] = getAllMovies(connection)
        val formattedString = movieStringFormat(searchingString)
        sender() ! moviesList.filter(_.title.splitAt(formattedString.length)._1 == formattedString)
      case DeleteDirector(name) =>
        log.info(s"Deleting $name...")
        sender() ! deleteDirector(connection, name)
      case DeleteActor(name) =>
        log.info(s"Deleting $name...")
        sender() ! deleteActor(connection, name)
      case DeleteMovie(title) =>
        log.info(s"Deleting $title")
        sender() ! deleteMovie(connection, title)

    }

  }
}

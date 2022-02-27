package ServerClasses

import ServerClasses.ServerCore.DbQueryHandler._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import ServerCore._
import scala.language.postfixOps
import akka.pattern.ask
import scala.concurrent.duration._
import scala.language.postfixOps

object RequestHandler extends App with MovieJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem("Skeleton")
  import system.dispatcher
  implicit val skeletonTimeout = Timeout(3 seconds)
  val dbQuery = system.actorOf(Props[DbQueryHandler], "dbQuery")
/*
Endpoints:
- GET api/movies/  - returns all movies from db as list of jsons
- GET api/movies/your_title - returns json of movie with requested title "your_title"
- GET api/movies?=your_title - does the same
- GET api/movies?search=your_search_request - returns list of movie jsons where first characters in title matches to your request
(for example "?search=the" returns the terminator, the godfather, the english patient)
- POST api/movies/addFullMovie adds to database a "full movie" json file (with movie, director and actor).
Example - "green elephant" in main/json directory. Separate endpoints for movie, director and actor are not implemented to avoid conflicts in related tables of database for this project
Method called by this endpoint puts movie, director and actor in different tables, "directors" related with "movies" with external table (movie_id - director_id),
"actors" also related with "movies" with external table (movie_id - actor_id)
- DELETE api/movies/your_title deletes movie with given title from database (also deletes director and actor in this project)
 */
  val route1 = {
    pathPrefix("api" / "movies") {
      get {
        parameters("title", "attribute") { (title, attribute) =>
          attribute match {
            case "director" => complete((dbQuery ? FindDirector(title)).mapTo[Director])
            case "actor" => complete((dbQuery ? FindActor(title)).mapTo[MovieActor])
            case "year" => complete((dbQuery ? FindYear(title)).mapTo[String])
            case "oscar" => complete((dbQuery ? DoHaveOscar(title)).mapTo[String])
            case _ => complete(StatusCodes.BadRequest)
          }
          //has no check for invalid movie title!
        }~
          (path(Segment) | parameter ("title")){title =>
            complete((dbQuery ? FindMovie(title)).mapTo[Movie])
          }~
          parameter("search"){ searchString =>
            complete((dbQuery ? FindMovieSearchingOption(searchString)).mapTo[List[Movie]])
          }~
          pathEndOrSingleSlash{
            complete((dbQuery ? FindAllMovies).mapTo[List[Movie]])
          }
      }~
        post{
          path ("addFullMovie") {
            entity(as[FullMovie]) { fullMovie =>
              complete((dbQuery ? AddFullMovie(fullMovie.movie, fullMovie.director, fullMovie.actor)).map(_ => StatusCodes.OK))
            }
          }~
            path ("addMovie"){
              entity(as[Movie]) { movie =>
                complete((dbQuery ? AddMovie(movie)).map(_ => StatusCodes.OK))
              }
            }~
            path ("addDirector"){
              entity(as[Director]) { director =>
                complete((dbQuery ? AddDirector(director)).map(_ => StatusCodes.OK))
              }
            }~
            path ("addActor") {
              entity(as[MovieActor]) { actor =>
                complete((dbQuery ? AddActor(actor)).map(_ => StatusCodes.OK))
              }
            }
        }~
        delete {
          (path(Segment) | parameter ("title")){title =>
            complete((dbQuery ? DeleteMovie(title)).map(_ => StatusCodes.OK))
          }
        }
    }
  }

  Http().newServerAt("localhost", 8088).bind(route1)
  println("server is running...")

}

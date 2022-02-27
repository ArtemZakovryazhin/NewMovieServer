package SQLClasses

import ServerClasses.ServerCore
import SqlMethods._
import SQLClasses.SqlMethods.connect
import ServerClasses.ServerCore._

import java.sql.Connection
import java.util.UUID
object SqlLoader extends App {

  /*
  First - open postgres and create database "movies_base_demo"
  Make sure you replaced "user" and "password" fields in ServerCore class with your actual psql user an password
   */
  var connectionOpt: Option[Connection] = None
  private val connection = connectionOpt.fold(connect(ServerCore.dbName, ServerCore.user, ServerCore.password)){conn => conn}

  val moviesContent = "(id uuid PRIMARY KEY, title character varying NOT NULL, year_of_production smallint NOT NULL, oscar_yes_or_no character varying);"
  val directorsContent = "(id serial NOT NULL PRIMARY KEY, name character varying NOT NULL);"
  val actorsContent = "(id serial NOT NULL PRIMARY KEY, name character varying NOT NULL);"
  val moviesActorsContent = "(movie_id uuid NOT NULL, actor_id integer NOt NULL);"
  val moviesDirectorsContent = "(movie_id uuid NOT NULL, director_id integer NOT NULL);"
  createTable(connection, "movies", moviesContent)
  createTable(connection, "directors", directorsContent)
  createTable(connection, "actors", actorsContent)
  createTable(connection, "movies_actors", moviesActorsContent)
  createTable(connection, "movies_directors", moviesDirectorsContent)


  alterTable(connection, "movies_directors", "ADD CONSTRAINT movies_directors_id_movies_id_directors PRIMARY KEY (movie_id, director_id);")
  alterTable(connection, "movies_actors", "ADD CONSTRAINT movies_actors_id_movies_id_actors PRIMARY KEY (movie_id, actor_id);")
  alterTable(connection, "movies_directors", "ADD FOREIGN KEY (movie_id) REFERENCES movies (id);")
  alterTable(connection, "movies_directors", "ADD FOREIGN KEY (director_id) REFERENCES directors (id);")
  alterTable(connection, "movies_actors", "ADD FOREIGN KEY (movie_id) REFERENCES movies (id);")
  alterTable(connection, "movies_actors", "ADD FOREIGN KEY (actor_id) REFERENCES actors (id);")



  val defaultActorList = List (
    MovieActor ("Arnold Schwarzenegger"),
    MovieActor ("Ralph Fiennes"),
    MovieActor ("Helena Bonham Carter"),
    MovieActor ("Samuel L Jackson"),
    MovieActor ("Marlon Brando")


  )
  val defaultMovieList = List (
    Movie (UUID.randomUUID(), "The Terminator", 1984, "no"),
    Movie (UUID.randomUUID(), "The English Patient", 1996, "yes"),
    Movie (UUID.randomUUID(), "Fight Club", 1999, "no"),
    Movie (UUID.randomUUID(), "Pulp Fiction", 1994, "yes"),
    Movie (UUID.randomUUID(), "The Godfather", 1972, "yes")

  )
  val defaultDirectorList = List (
    Director ("James Cameron"),
    Director ("Anthony Minghella"),
    Director ("David Fincher"),
    Director ("Quentin Tarantino"),
    Director ("Francis Ford Coppola")

  )
  defaultActorList.foreach(actor => insertRowActors(connection, actor))
  defaultDirectorList.foreach(director => insertRowDirectors(connection, director))
  defaultMovieList.foreach(movie => insertRowMoviesTableOscar(connection, movie))
  for (i <- defaultMovieList.indices) {
    insertIntoMoviesDirectors(connection, defaultMovieList(i).title, defaultDirectorList(i).name)
  }
  for (i <- defaultMovieList.indices) {
    insertIntoMoviesActors(connection, defaultMovieList(i).title, defaultActorList(i).name)
  }

}

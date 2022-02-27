package SQLClasses

import java.sql.{Connection, DriverManager}
import java.util.UUID
import ServerClasses.ServerCore._
object SqlMethods {

  def connect (dbName: String, user: String, password: String) = {
    DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbName, user, password)
  }

  def movieStringFormat (string: String) = {
    if (string.length>1) {
      string.split(' ').map{string=>
        if (((string.length>3) && (string.charAt(0) == 'm') && (string.charAt(1)=='c')) || ((string.length>3) && (string.charAt(1) == '\'')))
          string.splitAt(2)._1.capitalize + string.splitAt(2)._2.capitalize
        else string.capitalize
      }.mkString(" ")
    } else string.capitalize
  } // actually doesn't check for punctuation marks in front of letters

  def createTable (connection: Connection, tableName: String, tableContent: String): Unit = {
    val statement = connection.createStatement()
    try{
      val query: String = "CREATE TABLE " + tableName + tableContent
      statement.executeUpdate(query)
      println(s"table $tableName created")
    } catch {
      case e: Exception => println(s"something goes wrong with creating table: ${e}")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def alterTable (connection: Connection, tableName: String, queryBody: String) = {
    val statement = connection.createStatement()
    try{
      val query: String = "ALTER TABLE " + tableName + " " + queryBody
      statement.executeUpdate(query)
      println(s"table $tableName altered")
    } catch {
      case e: Exception => println(s"something goes wrong with altering table: ${e}")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def insertRowMoviesTableOscar (connection: Connection, movie: Movie) = {
    val statement = connection.createStatement()
    try {
      val query: String = "INSERT INTO movies (id, title, year_of_production, oscar_yes_or_no) VALUES " +
        s"('${movie.id}', '${movie.title.toLowerCase()}', '${movie.year}', '${movie.oscar.toLowerCase()}');"
      statement.executeUpdate(query)
      println(s"${movie.title} added to movies table")
    } catch {
      case e: Exception => println(s"Something goes wrong with inserting movies table: $e")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def insertRowDirectors (connection: Connection, director: Director) = {
    val statement = connection.createStatement()
    try {
      val query: String = s"INSERT INTO directors (name) VALUES ('${director.name.toLowerCase()}');"
      statement.executeUpdate(query)
      println(s"${director.name} added to directors")
    } catch {
      case e: Exception => println(s"Something goes wrong with adding the director: $e")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def insertRowActors (connection: Connection, actor: MovieActor) = {
    val statement = connection.createStatement()
    try {
      val query: String = s"INSERT INTO actors (name) VALUES ('${actor.name.toLowerCase()}');"
      statement.executeUpdate(query)
      println(s"${actor.name} added to actors")
    } catch {
      case e: Exception => println(s"Something goes wrong with adding the movie actor: $e")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def insertIntoMoviesDirectors(connection: Connection, title: String, directorName: String) = {
    val statement = connection.createStatement()
    try {
      val query: String = s"INSERT INTO movies_directors (movie_id, director_id) VALUES ((SELECT id FROM movies WHERE title = '${title.toLowerCase()}'), (SELECT id FROM directors WHERE name = '${directorName.toLowerCase()}'));"
      statement.executeUpdate(query)
      println(s"$title is bound with $directorName")
    } catch {
      case e: Exception => println(s"Something wrong with adding a row into movies_directors table: $e")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def insertIntoMoviesActors(connection: Connection, title: String, actorName: String) = {
    val statement = connection.createStatement()
    try {
      val query: String = s"INSERT INTO movies_actors (movie_id, actor_id) VALUES ((SELECT id FROM movies WHERE title = '${title.toLowerCase()}'), (SELECT id FROM actors WHERE name = '${actorName.toLowerCase()}'));"
      statement.executeUpdate(query)
      println(s"$title is bound with $actorName")
    } catch {
      case e: Exception => println(s"Something wrong with adding a row into movies_actors table: $e")
    }
    finally {
      if (statement != null) statement.close()
    }
  }

  def getMovieByTitle (connection: Connection, title: String) = { //это же можно сделать проще жи да?
    var movie: Movie = null
    val statement = connection.createStatement()
    try {
      val queryRs = connection.createStatement().executeQuery(s"select * from movies where title = '${title.toLowerCase}';")
      if (queryRs.next()) {
        movie = Movie(UUID.fromString(queryRs.getString(1)), movieStringFormat(queryRs.getString(2)), queryRs.getInt(3), movieStringFormat(queryRs.getString(4)))
      }
    } catch {
      case e: Exception => println(s"something wrong with movie with title $title: $e")
    } finally {
      if (statement != null) statement.close()
    }
    movie
  }

  def getAllMovies (connection: Connection) = {
    val statement = connection.createStatement()
    var movieList: List[Movie] = List()
    try {
      val queryRs = statement.executeQuery("select * from movies") //TEST FOR ";"
      while (queryRs.next()) {
        movieList = movieList :+ Movie(UUID.fromString(queryRs.getString(1)), movieStringFormat(queryRs.getString(2)), queryRs.getInt(3), movieStringFormat(queryRs.getString(4)))
      }
    } catch {
      case e: Exception => println(s"something wrong with list of all movies: $e")
    } finally {
      if (statement != null) statement.close()
    }
    movieList
  }

  def getYear (connection: Connection, title: String) = {
    val statement = connection.createStatement()
    var year: Int = 0
    val yearRs = statement.executeQuery(s"select * from movies where title = '${title.toLowerCase}';")
    if (yearRs.next()) year = yearRs.getInt("year_of_production")
    else println(s"movie with title $title not found")
    if (statement != null) statement.close()
    year
  }

  def doHaveOscar (connection: Connection, title: String) = {
    val statement = connection.createStatement()
    var oscar: Boolean = false
    val oscarRs = statement.executeQuery(s"select * from movies where title = '${title.toLowerCase()}';") //.toString == "yes"
    if (oscarRs.next()) oscar = oscarRs.getString("oscar_yes_or_no") == "yes"
    else println(s"movie with title $title not found")
    if (statement != null) statement.close()
    oscar
  }

  def getDirectorByMovie (connection: Connection, title: String) = {
    val statement = connection.createStatement()
    var director: Director = null
    try {
      val movieIdQuery = statement.executeQuery(s"select id from movies where title = '${title.toLowerCase}';")
      if (movieIdQuery.next()) {
        val directorIdRs = statement.executeQuery("select * from movies_directors where movie_id = '" + movieIdQuery.getString(1) + "';")
        if (directorIdRs.next()) {
          val directorNameRs = statement.executeQuery("select name from directors where id = '" + directorIdRs.getInt("director_id") + "';")
          if (directorNameRs.next()) {
            director = Director(movieStringFormat(directorNameRs.getString(1)))
          } else println(s"no director`s name found for movie with title $title")
        } else println(s"no director ID found for movie with title $title")
      } else println(s"no movie found with title $title")
    } catch  {
      case e: Exception => println(s"something wrong with director with title $title: $e")
    }
    finally {
      if (statement != null) statement.close()
    }
    director
  }

  def getActorByMovie(connection: Connection, title: String) = {
    val statement = connection.createStatement()
    var actor: MovieActor = null
    try {

      val movieIdQuery = statement.executeQuery(s"select id from movies where title = '${title.toLowerCase}';")

      if (movieIdQuery.next()) {
        val actorIdRs = statement.executeQuery("select * from movies_actors where movie_id = '" + movieIdQuery.getString(1) + "';")
        if (actorIdRs.next()) {
          val actorNameRs = statement.executeQuery("select name from actors where id = '" + actorIdRs.getInt("actor_id") + "';")
          if (actorNameRs.next()) {
            actor = MovieActor(movieStringFormat(actorNameRs.getString(1)))
          } else println(s"no actor`s name found for movie with title $title")
        } else println(s"no actor ID found for movie with title $title")
      } else println(s"no movie found with title $title")
    } catch  {
      case e: Exception => println(s"something wrong with actor with title $title: $e")
    }
    finally {
      if (statement != null) statement.close()
    }
    actor
  }

  def deleteDirector(connection: Connection, name: String) = {
    val statement = connection.createStatement()
    try {
      statement.executeUpdate(s"DELETE FROM movies_directors WHERE director_id = (SELECT id FROM directors WHERE name = '${name.toLowerCase()}');")
      println(s"$name was deleted from movies_directors")
    } catch {
      case e: Exception => println(s"something wrong with deleting $name from movies_directors: $e")
    }
    try {
      statement.executeUpdate(s"DELETE FROM directors WHERE name = '${name.toLowerCase}'")
      println(s"$name was deleted from directors")
    } catch {
      case e: Exception => println(s"something wrong with deleting $name from directors: $e")

    }
    if (statement != null) statement.close()
  }

  def deleteActor(connection: Connection, name: String) = {
    val statement = connection.createStatement()
    try {
      statement.executeUpdate(s"DELETE FROM movies_actors WHERE actor_id = (SELECT id FROM actors WHERE name = '${name.toLowerCase()}');")
      println(s"$name was deleted from movies_actors")
    } catch {
      case e: Exception => println(s"something wrong with deleting $name from movies_actors: $e")
    }
    try {
      statement.executeUpdate(s"DELETE FROM actors WHERE name = '${name.toLowerCase}'")
      println(s"$name was deleted from actors")
    } catch {
      case e: Exception => println(s"something wrong with deleting $name from actors: $e")

    }
    if (statement != null) statement.close()
  }

  def deleteMovie(connection: Connection, title: String) = {
    val statement = connection.createStatement()
    getDirectorByMovie(connection, title) match {
      case director: Director => deleteDirector(connection, director.name.toLowerCase)
      case _ => println(s"director of $title not found and can't be deleted")
    }
    getActorByMovie(connection, title) match {
      case actor: MovieActor => deleteActor(connection, actor.name.toLowerCase)
      case _ => println(s"actor of $title not found and can't be deleted")
    }
    try {
      statement.executeUpdate(s"DELETE FROM movies WHERE title = '${title.toLowerCase}';")
      println(s"$title was deleted from movies")
    } catch {
      case e: Exception => println(s"something wrong with deleting $title from movies: $e")
    }
    if (statement != null) statement.close()
  }

}

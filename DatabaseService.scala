package com.Royhang

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, SQLException}

class DatabaseService(url: String, user: String, password: String) {
  Class.forName("com.mysql.cj.jdbc.Driver")



  def getConnection(): Connection =
    DriverManager.getConnection(url, user, password)

  def readRatings(): Map[String, Map[Int, Double]] = {
    val conn = getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT USER_MD5, MOVIE_ID, RATING FROM ratings")
      val ratingsMap = collection.mutable.HashMap.empty[String, Map[Int, Double]]

      while (rs.next()) {
        val user = rs.getString("USER_MD5")
        val movie = rs.getInt("MOVIE_ID")
        val rating = rs.getDouble("RATING")
        val userMap = ratingsMap.getOrElse(user, Map.empty).updated(movie, rating)
        ratingsMap.update(user, userMap)
      }
      ratingsMap.toMap
    } finally {
      conn.close()
    }
  }

  def deleteOldRecommendations(userMd5: String): Unit = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement("DELETE FROM user_recommendations WHERE USER_MD5 = ?")
      stmt.setString(1, userMd5)
      stmt.executeUpdate()
    } finally {
      conn.close()
    }
  }

  def insertRecommendations(userMd5: String, recs: List[(Int, Double)]): Unit = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement(
        "INSERT INTO user_recommendations (USER_MD5, MOVIE_ID, PREDICTED_RATING) VALUES (?, ?, ?)"
      )
      recs.foreach { case (movieId, rating) =>
        stmt.setString(1, userMd5)
        stmt.setInt(2, movieId)
        stmt.setDouble(3, rating)
        stmt.addBatch()
      }
      stmt.executeBatch()
    } finally {
      conn.close()
    }
  }

  def registerUser(username: String, password: String): Boolean = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement("INSERT INTO users (USER_MD5, USER_NICKNAME) VALUES (?, ?)")
      stmt.setString(1, username)
      stmt.setString(2, password)
      stmt.executeUpdate() > 0
    } catch {
      case _: Exception => false
    } finally {
      conn.close()
    }
  }

  def loginUser(username: String, password: String): Boolean = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement("SELECT * FROM users WHERE USER_MD5 = ? AND USER_NICKNAME = ?")
      stmt.setString(1, username)
      stmt.setString(2, password)
      val rs: ResultSet = stmt.executeQuery()
      rs.next()
    } finally {
      conn.close()
    }
  }

  def getToken(): String = "111"

  def getAllMovies(): List[Recommendation] = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement(
        "SELECT MOVIE_ID, NAME, COVER, YEAR, GENRES, DOUBAN_SCORE FROM movies ORDER BY RAND() LIMIT 20"
      )
      val rs = stmt.executeQuery()
      val buffer = collection.mutable.ListBuffer[Recommendation]()

      while (rs.next()) {
        buffer += Recommendation(
          rs.getInt("MOVIE_ID"),
          rs.getString("NAME"),
          rs.getString("COVER"),
          rs.getString("GENRES"),
          rs.getInt("YEAR"),
          rs.getFloat("DOUBAN_SCORE")
        )
      }
      buffer.toList
    } finally {
      conn.close()
    }
  }

  def insertRating(userMd5: String, movieId: Int, rating: Double, ratingTime: String): Boolean = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement(
        """INSERT INTO ratings(USER_MD5, MOVIE_ID, RATING, RATING_TIME)
          |VALUES (?, ?, ?, ?)""".stripMargin)

      stmt.setString(1, userMd5)
      stmt.setInt(2, movieId)
      stmt.setDouble(3, rating)
      stmt.setString(4, ratingTime)

      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    } finally {
      conn.close()
    }
  }

  def getUserRatings(userMd5: String): List[UserRating] = {
    val conn = getConnection()
    try {
      // 修改SQL使用JOIN查询
      val sql =
        """SELECT r.MOVIE_ID, r.RATING,
          |       m.NAME, m.COVER, m.GENRES, m.YEAR
          |FROM ratings r
          |JOIN movies m ON r.MOVIE_ID = m.MOVIE_ID
          |WHERE r.USER_MD5 = ?""".stripMargin

      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, userMd5)

      val rs = stmt.executeQuery()
      val buffer = collection.mutable.ListBuffer[UserRating]()

      while (rs.next()) {
        buffer += UserRating(
          rs.getInt("MOVIE_ID"),
          rs.getDouble("RATING"),
          rs.getString("NAME"),
          rs.getString("COVER"),
          rs.getString("GENRES"),
          rs.getInt("YEAR")
        )
      }
      buffer.toList
    } finally {
      conn.close()
    }
  }
  def getMovieDetail(movieId: Int): Option[MovieDetail] = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement(
        """SELECT
          |  MOVIE_ID,
          |  NAME,
          |  ALIAS,
          |  COVER,
          |  DIRECTORS,
          |  ACTORS,
          |  GENRES,
          |  LANGUAGES,
          |  MINS,
          |  OFFICIAL_SITE,
          |  REGIONS,
          |  RELEASE_DATE,
          |  DOUBAN_SCORE,
          |  DOUBAN_VOTES,
          |  STORYLINE,
          |  IMDB_ID,
          |  DIRECTOR_IDS,
          |  ACTOR_IDS
          |FROM movies WHERE MOVIE_ID = ?""".stripMargin)

      stmt.setInt(1, movieId)
      val rs = stmt.executeQuery()

      if (rs.next()) {
        Some(MovieDetail(
          movieId = rs.getInt("MOVIE_ID"),
          name = rs.getString("NAME"),
          alias = Option(rs.getString("ALIAS")),
          cover = Option(rs.getString("COVER")),
          directors = Option(rs.getString("DIRECTORS")),
          actors = Option(rs.getString("ACTORS")),
          genres = Option(rs.getString("GENRES")),
          languages = Option(rs.getString("LANGUAGES")),
          mins = tryOpt(rs.getInt("MINS")),  // 处理可能的0值
          officialSite = Option(rs.getString("OFFICIAL_SITE")),
          regions = Option(rs.getString("REGIONS")),
          releaseDate = Option(rs.getString("RELEASE_DATE")),
          doubanScore = tryOpt(rs.getDouble("DOUBAN_SCORE")),
          doubanVotes = tryOpt(rs.getInt("DOUBAN_VOTES")),
          storyline = Option(rs.getString("STORYLINE")),
          imdbId = Option(rs.getString("IMDB_ID")),
          directorIds = Option(rs.getString("DIRECTOR_IDS")),
          actorIds = Option(rs.getString("ACTOR_IDS"))
        ))
      } else {
        None
      }
    } finally {
      conn.close()
    }
  }
  private def tryOpt[T](getter: => T): Option[T] = {
    try {
      val value = getter
      if (value.asInstanceOf[AnyRef] == null) None else Some(value)
    } catch {
      case _: SQLException => None
    }
  }
  def getPersonDetail(personId: Int): Option[PersonDetail] = {
    val conn = getConnection()
    try {
      val stmt = conn.prepareStatement(
        """SELECT
          |  PERSON_ID,
          |  NAME,
          |  SEX,
          |  NAME_EN,
          |  NAME_ZH,
          |  BIRTH,
          |  BIRTHPLACE,
          |  CONSTELLATORY,
          |  PROFESSION,
          |  BIOGRAPHY
          |FROM person WHERE PERSON_ID = ?""".stripMargin)

      stmt.setInt(1, personId)
      val rs = stmt.executeQuery()

      if (rs.next()) {
        Some(PersonDetail(
          id = rs.getInt("PERSON_ID"),
          name = Option(rs.getString("NAME")),
          sex = Option(rs.getString("SEX")),
          nameEn = Option(rs.getString("NAME_EN")),
          nameZh = Option(rs.getString("NAME_ZH")),
          birth = Option(rs.getString("BIRTH")),
          birthplace = Option(rs.getString("BIRTHPLACE")),
          constellatory = Option(rs.getString("CONSTELLATORY")),
          profession = Option(rs.getString("PROFESSION")),
          biography = Option(rs.getString("BIOGRAPHY"))
        ))
      } else {
        None
      }
    } finally {
      conn.close()
    }
  }
}
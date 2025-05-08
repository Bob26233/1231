package com.Royhang

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import spray.json.RootJsonFormat

import scala.concurrent.Future
import scala.io.StdIn
case class UserRatingResponse(
                               movieId: Int,
                               rating: Double,
                               title: String,
                               cover: String,
                               genres: String,
                               year: Int
                             )
case class UserRating(
                       movieId: Int,
                       rating: Double,
                       title: String,
                       cover: String,
                       genres: String,
                       year: Int
                     )
case class MovieDetail(
                        movieId: Int,
                        name: String,
                        alias: Option[String],
                        cover: Option[String],
                        directors: Option[String],
                        actors: Option[String],
                        genres: Option[String],
                        languages: Option[String],
                        mins: Option[Int],
                        officialSite: Option[String],
                        regions: Option[String],
                        releaseDate: Option[String],
                        doubanScore: Option[Double],
                        doubanVotes: Option[Int],
                        storyline: Option[String],
                        imdbId: Option[String],
                        directorIds: Option[String],
                        actorIds: Option[String]
                      )
case class PersonDetail(
                         id: Int,
                         name: Option[String],
                         sex: Option[String],
                         nameEn: Option[String],
                         nameZh: Option[String],
                         birth: Option[String],
                         birthplace: Option[String],
                         constellatory: Option[String],
                         profession: Option[String],
                         biography: Option[String]
                       )
case class Recommendation(movieId: Int, title: String, cover: String, genres: String, year:Int, score: Float)
case class ApiResponse(code: Int, data: List[Recommendation], message: String)
case class RateResponse(code: Int, data: List[UserRatingResponse], message: String)
case class DetailResponse(code: Int, data: Option[MovieDetail], message: String)
case class AuthRequest(username: String, password: String)
case class AuthResponse(code: Int, message: String)
case class PersonResponse(code: Int, data: Option[PersonDetail], message: String)

object RecommendationServer extends App {
  implicit val system: ActorSystem = ActorSystem("recommendation-system")

  import system.dispatcher
  // 注册JSON格式
  implicit val personDetailFormat: RootJsonFormat[PersonDetail] = jsonFormat10(PersonDetail)
  implicit val personResponseFormat: RootJsonFormat[PersonResponse] = jsonFormat3(PersonResponse)
  implicit val recommendationFormat: RootJsonFormat[Recommendation] = jsonFormat6(Recommendation)
  implicit val responseFormat: RootJsonFormat[ApiResponse] = jsonFormat3(ApiResponse)
  implicit val authRequestFormat: RootJsonFormat[AuthRequest] = jsonFormat2(AuthRequest)
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat2(AuthResponse)
  implicit val movieDetailFormat: RootJsonFormat[MovieDetail] = jsonFormat18(MovieDetail)
  implicit val detailResponseFormat: RootJsonFormat[DetailResponse] = jsonFormat3(DetailResponse)
  case class RatingRequest(
                            userId: String,
                            movieId: Int,
                            rating: Double,
                            timestamp: String
                          )

  implicit val userRatingFormat: RootJsonFormat[UserRatingResponse] = jsonFormat6(UserRatingResponse)
  implicit val ratingRequestFormat: RootJsonFormat[RatingRequest] = jsonFormat4(RatingRequest)
  val dbService = new DatabaseService(
    "jdbc:mysql://localhost:3306/movie_db",
    "root",
    "123"
  )

  val route =
    path("api" / "recommend") {
      get {
        parameters("userId".as[String]) { userId =>
          complete {
            Future {
              val ratings = dbService.readRatings()
              val recommender = new UserCFRecommender(ratings)
              val recommendations = recommender.recommend(userId)

              // 获取电影详细信息
              val conn = dbService.getConnection()
              try {
                val movieDetails = recommendations.flatMap { case (movieId, score) =>
                  val stmt = conn.prepareStatement(
                    "SELECT MOVIE_ID, NAME, COVER, YEAR, GENRES, DOUBAN_SCORE FROM movies WHERE MOVIE_ID = ?")
                  stmt.setInt(1, movieId)
                  val rs = stmt.executeQuery()
                  if (rs.next()) {
                    Some(Recommendation(
                      rs.getInt("MOVIE_ID"),
                      rs.getString("NAME"),
                      rs.getString("COVER"),
                      rs.getString("GENRES"),
                      rs.getInt("YEAR"),
                      rs.getFloat("DOUBAN_SCORE")
                    ))
                  } else None
                }
                ApiResponse(200, movieDetails, "Success")
              } finally {
                conn.close()
              }
            }
          }
        }
      }
    } ~
      path("api" / "users" / Segment / "ratings") { userId =>
        get {
          complete {
            Future {
              val ratings = dbService.getUserRatings(userId)
              ApiResponse(
                200,
                ratings.map(r => Recommendation(
                  r.movieId,
                  r.title,
                  r.cover,
                  r.genres,
                  r.year,
                  r.rating.toFloat
                )),
                "Success"
              )
            }
          }
        }
      } ~
    path("api" / "ratings") {
      post {
        entity(as[RatingRequest]) { request =>
          complete {
            Future {
              if (dbService.insertRating(
                request.userId,
                request.movieId,
                request.rating,
                request.timestamp
              )) {
                ApiResponse(200, Nil, "评分成功")
              } else {
                ApiResponse(500, Nil, "评分失败")
              }
            }
          }
        }
      }
    } ~
    path("api" / "register") {
      post {
        entity(as[AuthRequest]) { request =>
          complete {
            Future {
              if (dbService.registerUser(request.username, request.password)) {
                AuthResponse(200, "Registration successful")
              } else {
                AuthResponse(400, "Registration failed")
              }
            }
          }
        }
      }
    } ~
    path("api" / "login") {
      post {
        entity(as[AuthRequest]) { request =>
          complete {
            Future {
              if (dbService.loginUser(request.username, request.password)) {
                // 返回假定的 token
                AuthResponse(200, dbService.getToken())
              } else {
                AuthResponse(401, "Invalid credentials")
              }
            }
          }
        }
      }
    } ~
    path("api" / "movies" / "recommendations") {
    get {
      complete {
        Future {
          val movies = dbService.getAllMovies()
          // 转换为前端需要的结构
          val recommendations = movies.map(m =>
            Recommendation(
              m.movieId,
              m.title,
              m.cover,
              m.genres,
              m.year,
              m.score
            )
          )
          ApiResponse(200, recommendations, "Success")
        }
      }
    }
  } ~
      path("api" / "movies" / IntNumber) { movieId =>
        get {
          complete {
            Future {
              dbService.getMovieDetail(movieId) match {
                case Some(detail) =>
                  DetailResponse(200, Some(detail), "Success")
                case None =>
                  DetailResponse(404, None, "Movie not found")
              }
            }
          }
        }
      } ~
      path("api" / "person" / IntNumber) { personId =>
        get {
          complete {
            Future {
              dbService.getPersonDetail(personId) match {
                case Some(detail) =>
                  PersonResponse(200, Some(detail), "Success")
                case None =>
                  PersonResponse(404, None, "Person not found")
              }
            }
          }
        }
      }

  def getToken(): String = "111"

  // CORS处理
  def corsHandler(innerRoute: Route): Route =
    respondWithHeaders(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST),
      `Access-Control-Allow-Headers`("Content-Type")
    ) {
      innerRoute
    }

  val finalRoute = corsHandler(route)

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(finalRoute)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
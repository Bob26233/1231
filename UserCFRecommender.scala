package com.Royhang

class UserCFRecommender(ratings: Map[String, Map[Int, Double]]) {
  private def cosineSimilarity(
                                u1: Map[Int, Double],
                                u2: Map[Int, Double]
                              ): Double = {
    val common = u1.keySet.intersect(u2.keySet)
    if (common.isEmpty) 0.0 else {
      val (dot, norm1, norm2) = common.foldLeft((0.0, 0.0, 0.0)) {
        case ((d, n1, n2), m) =>
          (d + u1(m)*u2(m), n1 + math.pow(u1(m), 2), n2 + math.pow(u2(m), 2))
      }
      if (norm1 == 0 || norm2 == 0) 0.0 else dot / (math.sqrt(norm1) * math.sqrt(norm2))
    }
  }

  def recommend(
                 targetUser: String,
                 k: Int = 20,
                 n: Int = 10
               ): List[(Int, Double)] = {
    val targetRatings = ratings.getOrElse(targetUser, Map.empty)

    // 计算相似用户
    val similarities = ratings
      .filterKeys(_ != targetUser)
      .map { case (user, ratings) =>
        user -> cosineSimilarity(targetRatings, ratings)
      }
      .toList
      .sortBy(-_._2)
      .take(k)

    // 收集候选电影
    val candidates = similarities.flatMap { case (user, _) =>
      ratings(user).keys.filterNot(targetRatings.contains)
    }.toSet

    // 计算预测评分
    candidates.toList.map { movie =>
        val (totalSim, weightedSum) = similarities.foldLeft((0.0, 0.0)) {
          case ((ts, ws), (user, sim)) =>
            ratings(user).get(movie) match {
              case Some(r) => (ts + sim, ws + sim * r)
              case None => (ts, ws)
            }
        }
        val score = if (totalSim > 0) weightedSum / totalSim else 0.0
        (movie, score)
      }
      .sortBy(-_._2)
      .take(n)
  }
}
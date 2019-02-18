package algo

import scala.collection.mutable
import scala.collection.parallel.ParSeq

object TextSearch {
  def levenshtein(s1: String, s2: String): mutable.Map[(Int, Int), Int] = {
    val memoizedCosts = mutable.Map[(Int, Int), Int]()

    def lev: ((Int, Int)) => Int = {
      case (k1, k2) =>
        memoizedCosts.getOrElseUpdate((k1, k2), (k1, k2) match {
          case (i, 0) => i
          case (0, j) => j
          case (i, j) =>
            ParSeq(1 + lev((i - 1, j)),
              1 + lev((i, j - 1)),
              lev((i - 1, j - 1))
                + (if (s1(i - 1) != s2(j - 1)) 1 else 0)).min
        })
    }

    lev((s1.length, s2.length))
    memoizedCosts
  }

}

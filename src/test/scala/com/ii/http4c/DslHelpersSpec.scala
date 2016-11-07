package com.ii.http4c

import org.http4s.ParseFailure
import org.typelevel.scalatest.DisjunctionMatchers
import org.scalacheck._
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scalaz._
import Scalaz._

class DslHelpersSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks with DisjunctionMatchers {

  val genPaths = Gen.chooseNum(1, 10).flatMap(n => Gen.listOfN(n, Gen.alphaLowerChar)).map(_.mkString(""))

  describe("#pathMatcher") {

    it("should match a path") {
      forAll(genPaths) { path =>
        val matcher = DslHelpers.pathMatcher(s => Option(s).filter(_.length <= 10))
        matcher.unapply(path) should be (Some(path))
      }
    }

    it("should fail to match a path") {
      forAll(genPaths) { path =>
        val matcher = DslHelpers.pathMatcher(s => Option(s).filter(_.length > 10))
        matcher.unapply(path) should be (None)
      }
    }
  }

  describe("#validatingPathMatcher") {

    it("should match a path and provide a successful validation") {
      forAll(genPaths) { path =>
        val matcher = DslHelpers.validatingPathMatcher { path =>
          if (path.length <= 10) path.right
          else s"$path should be longer than 10".left
        }
        matcher.unapply(path) should be(Some(right))
      }
    }

    it("should match a path and provide a failed validation") {
      forAll(genPaths) { path =>
        val matcher = DslHelpers.validatingPathMatcher { path =>
          if (path.length > 11) path.right
          else s"$path should be longer than 10".left
        }
        matcher.unapply(path) should be(Some(left))
      }
    }
  }

}

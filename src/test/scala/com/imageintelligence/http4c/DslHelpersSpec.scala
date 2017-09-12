package com.imageintelligence.http4c

import org.scalacheck._
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class DslHelpersSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks {

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
          if (path.length <= 10) Right(path)
          else Left(s"$path should be longer than 10")
        }
        matcher.unapply(path) should be(Some(Right(path)))
      }
    }

    it("should match a path and provide a failed validation") {
      forAll(genPaths) { path =>
        val error = s"$path should be longer than 10"
        val matcher = DslHelpers.validatingPathMatcher { path =>
          if (path.length > 11) Right(path)
          else Left(error)
        }
        matcher.unapply(path) should be(Some(Left(error)))
      }
    }
  }

}

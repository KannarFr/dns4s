/*
 * Copyright 2014 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mkroli.dns4s.section.resource

import com.github.mkroli.dns4s._
import com.github.mkroli.dns4s.section.ResourceRecord
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.language.implicitConversions

class MXResourceSpec extends AnyFunSpec with ScalaCheckPropertyChecks {
  describe("MXResource") {
    describe("validation") {
      describe("preference") {
        it("should fail if it is out of bounds") {
          intercept[IllegalArgumentException](MXResource(-1, ""))
          intercept[IllegalArgumentException](MXResource(maxInt(16) + 1, ""))
        }

        it("should not fail if it is within bounds") {
          MXResource(0, "")
          MXResource(maxInt(16), "")
        }
      }
    }

    describe("encoding/decoding") {
      it("decode(encode(resource)) should be the same as resource") {
        forAll(uintGen(16), dnGen) { (preference, exchange) =>
          val mr = MXResource(preference, exchange)
          assert(mr === MXResource(mr(MessageBuffer()).flipped()))
        }
      }

      it("should be decoded wrapped in ResourceRecord") {
        val rr = ResourceRecord("test", ResourceRecord.typeMX, 0, 0, MXResource(123, "test.test"))
        val a  = rr(MessageBuffer()).flipped()
        val b  = bytes("04 74 65 73 74 00  000F 0000 00000000 0009 007B 04 74 65 73 74 C000")
        assert(b === a.getBytes(a.remaining()))
        assert(rr === ResourceRecord(MessageBuffer().put(b.toArray).flipped()))
      }
    }
  }
}

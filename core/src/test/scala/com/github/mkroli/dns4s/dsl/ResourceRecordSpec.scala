/*
 * Copyright 2015-2017 Michael Krolikowski
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
package com.github.mkroli.dns4s.dsl

import java.net.InetAddress

import com.github.mkroli.dns4s.section.ResourceRecord
import com.github.mkroli.dns4s.section.resource.CAAResource.{CustomCAAResource, IODEFResource, IssueResource, IssueWildResource}
import com.github.mkroli.dns4s.section.resource.OPTResource.{ClientSubnetOPTOptionData, UnknownOPTOptionData}
import com.github.mkroli.dns4s.section.resource._
import org.scalatest.funspec.AnyFunSpec

import scala.language.implicitConversions

class ResourceRecordSpec extends AnyFunSpec {
  describe("ResourceRecord") {
    it("should be possible to create an answer") {
      Response ~ Answers(RRName("test") ~ TXTRecord("test")) match {
        case Response(Answers(RRName("test") ~ TXTRecord(TXTResource(Seq("test"))) :: Nil)) =>
      }
    }

    it("should be possible to create an authority") {
      Response ~ Authority(RRName("test1") ~ TXTRecord("test1"), RRName("test2") ~ TXTRecord("test2")) match {
        case Response(Authority(RRName("test1") :: RRName("test2") :: Nil)) =>
      }
    }

    it("should be possible to create an additional") {
      Response ~ Additional(RRName("test") ~ TXTRecord("test")) match {
        case Response(Additional(RRName("test") :: Nil)) =>
      }
    }

    it("should replace all resource records if append is false") {
      Response ~ Answers(RRName("test") ~ TXTRecord("first")) ~ Answers(append = false, RRName("test") ~ TXTRecord("second")) match {
        case Response(Answers(RRName("test") ~ TXTRecord(TXTResource(Seq("second"))) :: Nil)) =>
      }
    }

    it("should preserve all resource records if append is true") {
      Response ~ Answers(RRName("test") ~ TXTRecord("first")) ~ Answers(append = true, RRName("test") ~ TXTRecord("second")) match {
        case Response(Answers(RRName("test") ~ TXTRecord(TXTResource(Seq("first"))) :: RRName("test") ~ TXTRecord(TXTResource(Seq("second"))) :: Nil)) =>
      }
    }

    it("should be possible to set the class") {
      Response ~ Answers(RRName("test") ~ RRClass(ResourceRecord.classCH)) match {
        case Response(Answers(RRName("test") ~ ClassCH() :: Nil)) =>
      }

      Response ~ Answers(RRName("test") ~ ClassCH) match {
        case Response(Answers(RRName("test") ~ RRClass(ResourceRecord.classCH) :: Nil)) =>
      }
    }

    it("should be possible to set the type") {
      Response ~ Answers(RRName("test") ~ TypeA) match {
        case Response(Answers(RRName("test") ~ RRType(ResourceRecord.typeA) :: Nil)) =>
      }

      Response ~ Answers(RRName("test") ~ RRType(ResourceRecord.typeA)) match {
        case Response(Answers(RRName("test") ~ TypeA() :: Nil)) =>
      }
    }

    it("should be possible to set the ttl") {
      Response ~ Authority(RRName("test") ~ RRTtl(123)) match {
        case Response(_) ~ Authority(RRTtl(123) :: Nil) =>
      }
    }

    it("should be possible to set edns") {
      Response ~ EDNS() match {
        case Response(EDNS(4096)) =>
      }

      Response ~ EDNS(123) match {
        case Response(_) ~ EDNS(123) =>
      }

      Response ~ EDNS() match {
        case Response(Additional(OPTRecord(OPTResource(Nil)) ~ RRClass(4096) :: Nil)) =>
      }
    }

    it("should be possible to use ARecord") {
      Response ~ Answers(ARecord("1.2.3.4")) match {
        case Response(Answers(ARecord(AResource(ip)) :: Nil)) =>
          assert(ip === InetAddress.getByName("1.2.3.4"))
      }

      intercept[RuntimeException](ARecord("1:2:3:4:5:6:7:8"))

      Response ~ Answers(ARecord(Array[Byte](1, 2, 3, 4))) match {
        case Response(Answers(ARecord(AResource(ip)) :: Nil)) =>
          assert(ip === InetAddress.getByName("1.2.3.4"))
      }

      intercept[RuntimeException](ARecord(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)))
    }

    it("should be possible to use AAAARecord") {
      Response ~ Answers(AAAARecord("1:2:3:4:5:6:7:8")) match {
        case Response(Answers(ARecord(AResource(ip)) :: Nil)) =>
          fail()
        case Response(Answers(AAAARecord(AAAAResource(ip)) :: Nil)) =>
          assert(ip === InetAddress.getByName("1:2:3:4:5:6:7:8"))
      }

      intercept[RuntimeException](AAAARecord("1.2.3.4"))

      Response ~ Answers(AAAARecord(Array[Byte](0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8))) match {
        case Response(Answers(AAAARecord(AAAAResource(ip)) :: Nil)) =>
          assert(ip === InetAddress.getByName("1:2:3:4:5:6:7:8"))
      }

      intercept[RuntimeException](AAAARecord(Array[Byte](1, 2, 3, 4)))
    }

    it("should be possible to use CNameRecord") {
      Response ~ Answers(RRName("test1") ~ CNameRecord("test2")) match {
        case Response(_) ~ Answers(RRName("test1") ~ CNameRecord(CNameResource("test2")) :: Nil) =>
      }
    }

    it("should be possible to use MXRecord") {
      Response ~ Answers(MXRecord(123, "test123")) match {
        case Response(Answers(MXRecord(MXResource(123, "test123")) :: Nil)) =>
      }
    }

    it("should be possible to use NAPTRRecord") {
      Response ~ Answers(NAPTRRecord(1, 2, "test1", "test2", "test3", "test4")) match {
        case Response(Answers(NAPTRRecord(NAPTRResource(1, 2, "test1", "test2", "test3", "test4")) :: Nil)) =>
      }
    }

    it("should be possible to use OPTRecord") {
      Response ~ Additional(OPTRecord()) match {
        case Response(Additional(OPTRecord(OPTResource(Nil)) :: Nil)) =>
      }
    }

    describe("OPTRecord OptionData") {
      it("should be possible to use UnknownOption") {
        val bytes = Array[Byte](1, 2, 3, 4)
        Response ~ Additional(OPTRecord(UnknownOption(1, bytes) :: Nil)) match {
          case Response(Additional(OPTRecord(OPTResource(UnknownOption(UnknownOPTOptionData(`bytes`)) :: Nil)) :: Nil)) =>
        }
      }

      it("should be possible to use ClientSubnetOption") {
        val address = InetAddress.getByAddress(Array(-1, -1, -1, -1))
        Response ~ Additional(OPTRecord(ClientSubnetOption(ClientSubnetOPTOptionData.familyIPv4, 32, 32, address) :: Nil)) match {
          case Response(Additional(OPTRecord(OPTResource(UnknownOption(UnknownOPTOptionData(_)) :: Nil)) :: Nil)) => fail()
          case Response(
              Additional(
                OPTRecord(OPTResource(ClientSubnetOption(ClientSubnetOPTOptionData(ClientSubnetOPTOptionData.familyIPv4, 32, 32, `address`)) :: Nil)) :: Nil
              )
              ) =>
        }
      }
    }

    it("should be possible to use NSRecord") {
      Response ~ Answers(NSRecord("test")) match {
        case Response(Answers(NSRecord(NSResource("test")) :: Nil)) =>
      }
    }

    it("should be possible to use PTRRecord") {
      Response ~ Answers(PTRRecord("test")) match {
        case Response(Answers(PTRRecord(PTRResource("test")) :: Nil)) =>
      }
    }

    it("should be possible to use HInfoRecord") {
      Response ~ Answers(HInfoRecord("cpu", "os")) match {
        case Response(Answers(HInfoRecord(HInfoResource("cpu", "os")) :: Nil)) =>
      }
    }

    it("should be possible to use TXTRecord") {
      Response ~ Answers(TXTRecord("test")) match {
        case Response(Answers(TXTRecord(TXTResource(Seq("test"))) :: Nil)) =>
      }
    }

    it("should be possible to use SOARecord") {
      Response ~ Answers(SOARecord("test1", "test2", 3, 4, 5, 6, 7)) match {
        case Response(
            Answers(
              SOARecord(SOAResource("test1", "test2", 3, 4, 5, 6, 7)) :: Nil
            )
            ) =>
      }
    }
    describe("should be possible to use CAARecord") {
      it("Issue") {
        Response ~ Answers(CAARecord.Issue("someValue", issuerCritical = true)) match {
          case Response(
              Answers(CAARecord.Issue(IssueResource("someValue", true)) :: Nil)
              ) =>
        }
      }

      it("IssueWild") {
        Response ~ Answers(
          CAARecord.IssueWild("someValue", issuerCritical = true)
        ) match {
          case Response(
              Answers(
                CAARecord.IssueWild(IssueWildResource("someValue", true)) :: Nil
              )
              ) =>
        }
      }

      it("IODEF") {
        Response ~ Answers(CAARecord.IODEF("mailto:someone@something")) match {
          case Response(
              Answers(
                CAARecord.IODEF(IODEFResource("mailto:someone@something")) :: Nil
              )
              ) =>
        }
      }

      it("Custom") {
        Response ~ Answers(
          CAARecord.Custom("something", "somevalue".getBytes, 2.toByte)
        ) match {
          case Response(
              Answers(
                CAARecord.Custom(CustomCAAResource("something", bytes, flags)) :: Nil
              )
              ) =>
        }
      }
    }
  }
}

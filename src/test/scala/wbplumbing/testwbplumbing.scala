package wbplumbing

import org.scalatest.{Matchers, FlatSpec}
import chisel3._
import chisel3.tester._
import chisel3.util._
import chisel3.experimental._

object general {
  val optn = Array("-td", "output",
                    // <firrtl|treadle|verilator|ivl|vcs>
                   "--backend-name", "verilator"
                  )
}


class WbInterconPTSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "A WbInterconPT"

  it should "read and write wishbone value on one slave" in {
    val args = general.optn
    val dataWidth = 16
    val wbm = new WbMaster(dataWidth, 2)
    val wbs = new WbSlave(dataWidth, 2)

    test(new WbInterconPT(wbm, wbs)){ dut =>
      // just a stupid testbench that verify point to point connexion
      dut.io.wbm.adr_o.poke(1)
      dut.io.wbm.dat_o.poke(1)
      dut.io.wbm.we_o.poke (1)
      dut.io.wbm.stb_o.poke(1)
      dut.io.wbm.cyc_o.poke(1)
      dut.io.wbs.ack_o.poke(1)
      dut.io.wbs.dat_o.poke(1)
      dut.clock.step(1)
      dut.io.wbm.adr_o.expect(1, "Wrong value read on wbm.adr_o")
      dut.io.wbm.dat_o.expect(1, "Wrong value read on wbm.dat_o")
      dut.io.wbm.we_o.expect( 1, "Wrong value read on wbm.we_o ")
      dut.io.wbm.stb_o.expect(1, "Wrong value read on wbm.stb_o")
      dut.io.wbm.cyc_o.expect(1, "Wrong value read on wbm.cyc_o")
      dut.io.wbs.ack_o.expect(1, "Wrong value read on wbs.ack_o")
      dut.io.wbs.dat_o.expect(1, "Wrong value read on wbs.dat_o")
      dut.io.wbm.adr_o.poke(0)
      dut.io.wbm.dat_o.poke(0)
      dut.io.wbm.we_o.poke( 0)
      dut.io.wbm.stb_o.poke(0)
      dut.io.wbm.cyc_o.poke(0)
      dut.io.wbs.ack_o.poke(0)
      dut.io.wbs.dat_o.poke(0)
      dut.clock.step(1)
      dut.io.wbm.adr_o.expect(0, "Wrong value read on wbm.adr_o")
      dut.io.wbm.dat_o.expect(0, "Wrong value read on wbm.dat_o")
      dut.io.wbm.we_o.expect( 0, "Wrong value read on wbm.we_o ")
      dut.io.wbm.stb_o.expect(0, "Wrong value read on wbm.stb_o")
      dut.io.wbm.cyc_o.expect(0, "Wrong value read on wbm.cyc_o")
      dut.io.wbs.ack_o.expect(0, "Wrong value read on wbs.ack_o")
      dut.io.wbs.dat_o.expect(0, "Wrong value read on wbs.dat_o")
    }
  }
}

class WbInterconOneMasterSpec extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "A WbInterconOneMaster"

  it should "read and write wishbone value on two slaves" in {
    val args = general.optn
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 7, "Spi2WbMaster")
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1")
    val wbs2 = new WbSlave(dataWidth, 2, "Ksz2")

    test(new WbInterconOneMaster(wbm, Seq(wbs1, wbs2))) { dut =>
      val firstSlave = 0
      val secondSlave = 4
      step(1)
      // write on second slave
      val value = 0xcafe
      dut.io.wbm.adr_o.poke(secondSlave)
      dut.io.wbm.dat_o.poke(value)
      dut.io.wbm.we_o.poke(1)
      dut.io.wbm.cyc_o.poke(1)
      dut.io.wbm.stb_o.poke(1)
      for(wbs <- dut.io.wbs) {
        wbs.ack_o.poke(0.U)
      }
      dut.clock.step(1)
      dut.io.wbm.adr_o.poke(0)
      dut.io.wbm.dat_o.poke(0)
      dut.io.wbm.we_o.poke(0)
      dut.io.wbm.cyc_o.poke(0)
      dut.io.wbm.stb_o.poke(0)
      dut.io.wbs(1).ack_o.poke(1)

      dut.io.wbs(1).we_i.expect(1)
      dut.io.wbs(1).cyc_i.expect(1)
      dut.clock.step(1)
      dut.io.wbs(0).ack_o.poke(0)
      dut.io.wbs(1).ack_o.poke(0)

    }
  }
}

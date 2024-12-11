package wbplumbing

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

object general {
  val optn = Array("-td", "output",
                    // <firrtl|treadle|verilator|ivl|vcs>
                   "--backend-name", "verilator"
                  )
}

class WbInterconPTSpec extends AnyFlatSpec with Matchers {

  behavior of "WbInterconPT"

  it should "read and write wishbone value on one slave" in {
    val args = general.optn
    val dataWidth = 16
    val wbm = new WbMaster(dataWidth, 2, feature_err=true)
    val wbs = new WbSlave(dataWidth, 2, feature_err=true)

    simulate(new WbInterconPT(wbm, wbs)){ dut =>
      // just a stupid testbench that verify point to point connexion
      dut.io.wbm.adr_o.poke(1.U)
      dut.io.wbm.dat_o.poke(1.U)
      dut.io.wbm.we_o.poke (true.B)
      dut.io.wbm.stb_o.poke(true.B)
      dut.io.wbm.cyc_o.poke(true.B)
      dut.io.wbs.ack_o.poke(true.B)
      dut.io.wbs.err_o.get.poke(true.B)
      dut.io.wbs.dat_o.poke(1.U)
      dut.clock.step(1)
      dut.io.wbm.adr_o.expect(1.U, "Wrong value read on wbm.adr_o")
      dut.io.wbm.dat_o.expect(1.U, "Wrong value read on wbm.dat_o")
      dut.io.wbm.we_o.expect(true.B, "Wrong value read on wbm.we_o ")
      dut.io.wbm.stb_o.expect(true.B, "Wrong value read on wbm.stb_o")
      dut.io.wbm.cyc_o.expect(true.B, "Wrong value read on wbm.cyc_o")
      dut.io.wbs.ack_o.expect(true.B, "Wrong value read on wbs.ack_o")
      dut.io.wbs.err_o.get.expect(true.B, "Wrong value read on wbs.err_o")
      dut.io.wbs.dat_o.expect(1.U, "Wrong value read on wbs.dat_o")
      dut.io.wbm.adr_o.poke(0.U)
      dut.io.wbm.dat_o.poke(0.U)
      dut.io.wbm.we_o.poke(false.B)
      dut.io.wbm.stb_o.poke(false.B)
      dut.io.wbm.cyc_o.poke(false.B)
      dut.io.wbs.ack_o.poke(false.B)
      dut.io.wbs.err_o.get.poke(false.B)
      dut.io.wbs.dat_o.poke(0.U)
      dut.clock.step(1)
      dut.io.wbm.adr_o.expect(0.U, "Wrong value read on wbm.adr_o")
      dut.io.wbm.dat_o.expect(0.U, "Wrong value read on wbm.dat_o")
      dut.io.wbm.we_o.expect( false.B, "Wrong value read on wbm.we_o ")
      dut.io.wbm.stb_o.expect(false.B, "Wrong value read on wbm.stb_o")
      dut.io.wbm.cyc_o.expect(false.B, "Wrong value read on wbm.cyc_o")
      dut.io.wbs.ack_o.expect(false.B, "Wrong value read on wbs.ack_o")
      dut.io.wbs.err_o.get.expect(false.B, "Wrong value read on wbs.err_o")
      dut.io.wbs.dat_o.expect(0.U, "Wrong value read on wbs.dat_o")
    }
  }
}

class WbInterconOneMasterSpec extends AnyFlatSpec with Matchers {

  behavior of "WbInterconOneMaster"

   it should "read and write wishbone value on two slaves" in {
    val args = general.optn
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 7, "Spi2WbMaster")
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1")
    val wbs2 = new WbSlave(dataWidth, 2, "Ksz2")

    simulate(new WbInterconOneMaster(wbm, Seq(wbs1, wbs2))) { dut =>
      val firstSlave = 0
      val secondSlave = 4.U
      dut.clock.step(1)
      // write on second slave
      val value = "hcafe".U
      dut.io.wbm.adr_o.poke(secondSlave)
      dut.io.wbm.dat_o.poke(value)
      dut.io.wbm.we_o.poke( true.B)
      dut.io.wbm.cyc_o.poke(true.B)
      dut.io.wbm.stb_o.poke(true.B)
      for(wbs <- dut.io.wbs) {
        wbs.ack_o.poke(false.B)
      }
      dut.clock.step(1)
      dut.io.wbm.adr_o.poke(0.U)
      dut.io.wbm.dat_o.poke(0.U)
      dut.io.wbm.we_o.poke( false.B)
      dut.io.wbm.cyc_o.poke(false.B)
      dut.io.wbm.stb_o.poke(false.B)
      dut.io.wbs(1).ack_o.poke(true.B)
      dut.clock.step(1)
      dut.io.wbs(0).ack_o.poke(false.B)
      dut.io.wbs(1).ack_o.poke(false.B)

    }
  }

  it should "raise the err_i line of the master if read address is unmapped" in {
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 3, "Spi2WbMaster", feature_err=true)
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1")

    simulate(new WbInterconOneMaster(wbm, Seq(wbs1))) { dut =>
      dut.io.wbm.err_i.get.expect(false.B, "bad init of err_i")

      // Read on unmapped address
      dut.io.wbm.adr_o.poke(0x4)
      dut.io.wbm.we_o.poke(false.B)
      dut.io.wbm.cyc_o.poke(true.B)
      dut.io.wbm.stb_o.poke(true.B)
      dut.clock.step(1)
      dut.io.wbm.err_i.get.expect(true.B, "err_i not raised")

      // Stop transaction
      dut.io.wbm.cyc_o.poke(false.B)
      dut.io.wbm.stb_o.poke(false.B)
      dut.clock.step(1)
      dut.io.wbm.err_i.get.expect(false.B, "err_i not resetted correctly")
    }
  }

  it should "raise the err_i line of the master if write address is unmapped" in {
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 3, "Spi2WbMaster", feature_err=true)
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1")

    simulate(new WbInterconOneMaster(wbm, Seq(wbs1))) { dut =>
      dut.io.wbm.err_i.get.expect(false.B, "bad init of err_i")

      // Write on unmapped address
      dut.io.wbm.adr_o.poke(0x4)
      dut.io.wbm.we_o.poke(true.B)
      dut.io.wbm.cyc_o.poke(true.B)
      dut.io.wbm.stb_o.poke(true.B)
      dut.clock.step(1)
      dut.io.wbm.err_i.get.expect(true.B, "err_i not raised")

      // Stop transaction
      dut.io.wbm.we_o.poke(false.B)
      dut.io.wbm.cyc_o.poke(false.B)
      dut.io.wbm.stb_o.poke(false.B)
      dut.clock.step(1)
      dut.io.wbm.err_i.get.expect(false.B, "err_i not resetted correctly")
    }
  }

  it should "pass through the err_o line of the slave to the err_i line of the master" in {
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 3, "Spi2WbMaster", feature_err=true)
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1", feature_err=true)

    simulate(new WbInterconOneMaster(wbm, Seq(wbs1))) { dut =>
      dut.io.wbm.err_i.get.expect(false.B, "bad init of err_i")

      // Write on unmapped address
      dut.io.wbm.adr_o.poke(0x0)
      dut.io.wbm.we_o.poke(false.B)
      dut.io.wbm.cyc_o.poke(true.B)
      dut.io.wbm.stb_o.poke(true.B)
      dut.clock.step(1)
      dut.io.wbs(0).err_o.get.poke(true.B)
      dut.io.wbm.err_i.get.expect(true.B, "err_i not raised")

      // Reset err_o
      dut.io.wbm.we_o.poke(false.B)
      dut.io.wbm.cyc_o.poke(false.B)
      dut.io.wbm.stb_o.poke(false.B)
      dut.io.wbs(0).err_o.get.poke(false.B)
      dut.clock.step(1)
      dut.io.wbm.err_i.get.expect(false.B, "err_i not resetted correctly")
    }
  }
}

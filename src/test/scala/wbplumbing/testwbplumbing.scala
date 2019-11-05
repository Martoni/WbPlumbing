package wbplumbing

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}

object general {
  val optn = Array("-td", "output",
                    // <firrtl|treadle|verilator|ivl|vcs>
                   "--backend-name", "verilator"
                  )
}

class TestWbIntercon (dut: WbInterconPT) extends PeekPokeTester(dut){
  // just a stupid testbench that verify point to point connexion
  poke(dut.io.wbm.adr_o, 1)
  poke(dut.io.wbm.dat_o, 1)
  poke(dut.io.wbm.we_o , 1)
  poke(dut.io.wbm.stb_o, 1)
  poke(dut.io.wbm.cyc_o, 1)
  poke(dut.io.wbs.ack_o, 1)
  poke(dut.io.wbs.dat_o, 1)
  step(1)
  expect(dut.io.wbm.adr_o, 1, "Wrong value read on wbm.adr_o")
  expect(dut.io.wbm.dat_o, 1, "Wrong value read on wbm.dat_o")
  expect(dut.io.wbm.we_o , 1, "Wrong value read on wbm.we_o ")
  expect(dut.io.wbm.stb_o, 1, "Wrong value read on wbm.stb_o")
  expect(dut.io.wbm.cyc_o, 1, "Wrong value read on wbm.cyc_o")
  expect(dut.io.wbs.ack_o, 1, "Wrong value read on wbs.ack_o")
  expect(dut.io.wbs.dat_o, 1, "Wrong value read on wbs.dat_o")
  poke(dut.io.wbm.adr_o, 0)
  poke(dut.io.wbm.dat_o, 0)
  poke(dut.io.wbm.we_o , 0)
  poke(dut.io.wbm.stb_o, 0)
  poke(dut.io.wbm.cyc_o, 0)
  poke(dut.io.wbs.ack_o, 0)
  poke(dut.io.wbs.dat_o, 0)
  step(1)
  expect(dut.io.wbm.adr_o, 0, "Wrong value read on wbm.adr_o")
  expect(dut.io.wbm.dat_o, 0, "Wrong value read on wbm.dat_o")
  expect(dut.io.wbm.we_o , 0, "Wrong value read on wbm.we_o ")
  expect(dut.io.wbm.stb_o, 0, "Wrong value read on wbm.stb_o")
  expect(dut.io.wbm.cyc_o, 0, "Wrong value read on wbm.cyc_o")
  expect(dut.io.wbs.ack_o, 0, "Wrong value read on wbs.ack_o")
  expect(dut.io.wbs.dat_o, 0, "Wrong value read on wbs.dat_o")
}

class TestWbInterconDualSlave (dut: WbInterconOneMaster) extends PeekPokeTester(dut){
  val firstSlave = 0
  val secondSlave = 4
  step(1)
  // write on second slave
  val value = 0xcafe
  poke(dut.io.wbm.adr_o, secondSlave)
  poke(dut.io.wbm.dat_o, value)
  poke(dut.io.wbm.we_o, 1)
  poke(dut.io.wbm.cyc_o, 1)
  poke(dut.io.wbm.stb_o, 1)
// Doesn't work 
  for(wbs <- dut.io.wbs) {
    wbs.ack_o := 0.U
  }
  step(1)
  poke(dut.io.wbm.adr_o, 0)
  poke(dut.io.wbm.dat_o, 0)
  poke(dut.io.wbm.we_o, 0)
  poke(dut.io.wbm.cyc_o, 0)
  poke(dut.io.wbm.stb_o, 0)
  poke(dut.io.wbs(1).ack_o, 1)
  expect(dut.io.wbs(1).we_i, 1)
  expect(dut.io.wbs(1).cyc_i, 1)
  step(1)
  poke(dut.io.wbs(0).ack_o, 0)
  poke(dut.io.wbs(1).ack_o, 0)
}

class WbInterconPTSpec extends FlatSpec with Matchers {
  behavior of "A WbInterconPT"

  it should "read and write wishbone value on one slave" in {
    val args = general.optn
    val dataWidth = 16
    val wbm = new WbMaster(dataWidth, 2)
    val wbs = new WbSlave(dataWidth, 2)
    chisel3.iotesters.Driver.execute(args, () => new WbInterconPT(wbm, wbs))
          {c => new TestWbIntercon(c)} should be(true)

  }
}

class WbInterconOneMasterSpec extends FlatSpec with Matchers {
  behavior of "A WbInterconOneMaster"

  it should "read and write wishbone value on two slaves" in {
    val args = general.optn
    val dataWidth = 16
    val wbm =  new WbMaster(dataWidth, 7, "Spi2WbMaster")
    val wbs1 = new WbSlave(dataWidth, 2, "Ksz1")
    val wbs2 = new WbSlave(dataWidth, 2, "Ksz2")

    chisel3.iotesters.Driver.execute(args, () => new WbInterconOneMaster(wbm, Seq(wbs1, wbs2)))
          {c => new TestWbInterconDualSlave(c)} should be(true)

  }
}

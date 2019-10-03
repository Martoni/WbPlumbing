package wbplumbing

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}

object general {
  val optn = Array("-td", "output",
                    // <firrtl|treadle|verilator|ivl|vcs>
                   "--backend-name", "verilator"
                  )
}

class TestWbIntercon (dut: WbIntercon) extends PeekPokeTester(dut){
  step(1)
}

class WbInterconSpec extends FlatSpec with Matchers {
  behavior of "A WbIntercon"

  it should "read and write wishbone value on one slave" in {
    val args = general.optn
    val dataWidth = 16
    chisel3.iotesters.Driver.execute(args, () => new WbIntercon(dataWidth))
          {c => new TestWbIntercon(c)} should be(true)

  }
}

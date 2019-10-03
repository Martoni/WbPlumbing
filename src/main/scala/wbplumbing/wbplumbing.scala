package wbplumbing

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.Driver

// minimal signals definition for a wishbone bus
// (no SEL, no TAG, no pipeline, ...)
class WbMaster (private val dwidth: Int,
                private val awidth: Int) extends Bundle {
    val adr_o = Output(UInt(awidth.W))
    val dat_i = Input(UInt(dwidth.W))
    val dat_o = Output(UInt(dwidth.W))
    val we_o = Output(Bool())
    val stb_o = Output(Bool())
    val ack_i = Input(Bool())
    val cyc_o = Output(Bool())
}

// Wishbone slave interface
class WbSlave (private val dwidth: Int,
               private val awidth: Int) extends Bundle {
  val adr_i = Input(UInt(awidth.W))
  val dat_i = Input(UInt(dwidth.W))
  val dat_o = Output(UInt(dwidth.W))
  val we_i = Input(Bool())
  val stb_i = Input(Bool())
  val ack_o = Output(Bool())
  val cyc_i = Input(Bool())
}

class WbIntercon (val dwidth: Int) extends Module {
  val io = IO(new Bundle{
    val wbm = Flipped(new WbMaster(dwidth, 2))
    val wbs = Flipped(new WbSlave(dwidth, 2))
  })

  // XXX delete me
  io.wbm.dat_i := 1.U
  io.wbm.ack_i := 1.U
  io.wbs.adr_i := 1.U
  io.wbs.dat_i := 1.U
  io.wbs.we_i  := 1.U
  io.wbs.stb_i := 1.U
  io.wbs.cyc_i := 1.U

}

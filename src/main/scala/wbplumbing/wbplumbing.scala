package wbplumbing

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.Driver

// minimal signals definition for a wishbone bus
// (no SEL, no TAG, no pipeline, ...)
class WbMaster (val dwidth: Int,
                val awidth: Int) extends Bundle {
    val adr_o = Output(UInt(awidth.W))
    val dat_i = Input(UInt(dwidth.W))
    val dat_o = Output(UInt(dwidth.W))
    val we_o = Output(Bool())
    val stb_o = Output(Bool())
    val ack_i = Input(Bool())
    val cyc_o = Output(Bool())
    override def cloneType = (new WbMaster(dwidth, awidth)).asInstanceOf[this.type]
}

// Wishbone slave interface
class WbSlave (val dwidth: Int,
               val awidth: Int) extends Bundle {
  val adr_i = Input(UInt(awidth.W))
  val dat_i = Input(UInt(dwidth.W))
  val dat_o = Output(UInt(dwidth.W))
  val we_i = Input(Bool())
  val stb_i = Input(Bool())
  val ack_o = Output(Bool())
  val cyc_i = Input(Bool())
  override def cloneType = (new WbSlave(dwidth, awidth)).asInstanceOf[this.type]
}

// Wishbone Intercon Pass Trought : one master, one slave
class WbInterconPT (val awbm: WbMaster,
                    val awbs: WbSlave) extends Module {
  val io = IO(new Bundle{
    val wbm = Flipped(new WbMaster(awbm.dwidth, awbm.awidth))
    val wbs = Flipped(new WbSlave(awbs.dwidth, awbs.awidth))
  })

  assert(awbm.dwidth == awbs.dwidth,
    "only same datasize supported")
  assert(awbm.awidth == awbs.awidth,
    "it's passthrought intercon, same same address size is required")

  // wbm <-> wbs simple connexions
  io.wbs.adr_i := io.wbm.adr_o
  io.wbs.dat_i := io.wbm.dat_o
  io.wbs.we_i  := io.wbm.we_o 
  io.wbs.stb_i := io.wbm.stb_o
  io.wbs.cyc_i := io.wbm.cyc_o

  io.wbm.ack_i := io.wbs.ack_o
  io.wbm.dat_i := io.wbs.dat_o

}

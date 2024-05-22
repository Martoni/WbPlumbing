package wbplumbing

import chisel3._
import chisel3.util._
import chisel3.experimental._

/** WbOneReg:
 *  create a simple wishbone register
 *  port :
 *  wbs : WbSlave()
 *  reg_out : output value
 *  reg_in : decoupled input value (set valid true.B to write the register)
 */
class WbOneReg(dwidth: Int, addr: UInt, init_value: UInt) extends Module {
  val io = IO(new Bundle {
    val wbs = new WbSlave(dwidth=dwidth, awidth=1)
    val reg_out = Output(UInt(dwidth.W))
    val reg_in = Flipped(Decoupled(UInt(dwidth.W)))
  })

  val wbReg = RegInit(init_value)

  io.wbs.dat_o := 0.U(16.W)
  io.wbs.ack_o := false.B
  io.reg_in.ready := true.B
  when(io.wbs.stb_i === true.B && io.wbs.cyc_i === true.B){
    when(io.wbs.adr_i === addr){
      when(io.wbs.we_i === false.B){
        io.wbs.dat_o := wbReg
      }.otherwise{
        wbReg := io.wbs.dat_i
        io.reg_in.ready := false.B
      }
      io.wbs.ack_o := true.B
    }
  }
  io.reg_out := wbReg
  when(io.reg_in.valid === true.B && io.reg_in.ready === true.B){
    wbReg := io.reg_in.bits
  }
}

object WbOneReg {
  def apply(wbs: WbSlave, addr: UInt, dwidth: Int, init_value: UInt) = {
    val wbregister = Module(new WbOneReg(dwidth, addr, init_value))
    wbregister.io.wbs <> wbs
    wbregister.io.reg_in.valid := false.B
    wbregister.io.reg_in.bits := DontCare
    wbregister
  }
}

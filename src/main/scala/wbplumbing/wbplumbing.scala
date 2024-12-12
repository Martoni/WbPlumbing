package wbplumbing

import chisel3._
import chisel3.util._
import chisel3.experimental._

// minimal signals definition for a wishbone bus
// (no SEL, no TAG, no pipeline, ...)
class WbMaster (val dwidth: Int,
                val awidth: Int,
                val iname: String = "Noname",
                val feature_err: Boolean = false) extends Bundle {
    val adr_o = Output(UInt(awidth.W))
    val dat_i = Input(UInt(dwidth.W))
    val dat_o = Output(UInt(dwidth.W))
    val we_o = Output(Bool())
    val stb_o = Output(Bool())
    val ack_i = Input(Bool())
    val cyc_o = Output(Bool())
    val err_i = if (feature_err == true) Some(Input(Bool())) else None
}

// Wishbone slave interface
class WbSlave (val dwidth: Int,
               val awidth: Int,
               val iname: String = "Noname",
               val feature_err: Boolean = false) extends Bundle {
  val adr_i = Input(UInt(awidth.W))
  val dat_i = Input(UInt(dwidth.W))
  val dat_o = Output(UInt(dwidth.W))
  val we_i  = Input(Bool())
  val stb_i = Input(Bool())
  val ack_o = Output(Bool())
  val err_o = if (feature_err == true) Some(Output(Bool())) else None
  val cyc_i = Input(Bool())
}

// Wishbone Intercon Pass Trought : one master, one slave
class WbInterconPT (val awbm: WbMaster,
                    val awbs: WbSlave) extends Module {
  val io = IO(new Bundle{
    val wbm = Flipped(new WbMaster(awbm.dwidth, awbm.awidth, awbm.iname, awbm.feature_err))
    val wbs = Flipped(new WbSlave(awbs.dwidth, awbs.awidth, awbs.iname, awbs.feature_err))
  })

  assert(awbm.dwidth == awbs.dwidth,
    "only same datasize supported")
  assert(awbm.awidth >= awbs.awidth,
    "Address size of master should be >= of slave address size")

  // wbm <-> wbs simple connexions
  io.wbs.adr_i := io.wbm.adr_o(awbs.awidth-1, 0)
  io.wbs.dat_i := io.wbm.dat_o
  io.wbs.we_i  := io.wbm.we_o
  io.wbs.stb_i := io.wbm.stb_o
  io.wbs.cyc_i := io.wbm.cyc_o

  io.wbm.ack_i := io.wbs.ack_o
  io.wbm.dat_i := io.wbs.dat_o

  if (io.wbm.feature_err && io.wbs.feature_err) {
    io.wbm.err_i.get := io.wbs.err_o.get
  }
}

// Wishbone Intercon with one master and several slaves
// data bus is same size as master
class WbInterconOneMaster(val awbm: WbMaster,
                          val awbs: Seq[WbSlave]
                          ) extends Module {
    val io = IO(new Bundle{
      val wbm = Flipped(new WbMaster(awbm.dwidth, awbm.awidth, awbm.iname, awbm.feature_err))
      val wbs = MixedVec(awbs.map{i => Flipped(new WbSlave(i.dwidth, i.awidth, i.iname, i.feature_err))})
    })

    var addrSlave = Seq(0)

    io.wbm.dat_i := 0.U
    io.wbm.ack_i := 0.U
    if (io.wbm.feature_err) {
      io.wbm.err_i.get := false.B
    }
    val dataByteSize = awbm.dwidth/8
    for(wbs <- io.wbs) {
      val slaveInterfaceName = wbs.iname
      // Doing some checks
      assert(awbm.dwidth == wbs.dwidth,
        "Error all databusses should be same size")
      assert(awbm.awidth > wbs.awidth,
        f"Error address width is too large for slave $slaveInterfaceName")
      // Address decoding
      addrSlave = addrSlave ++ Seq(addrSlave.last + (1 << wbs.awidth))
      println(f"@${(addrSlave(addrSlave.length-2))*dataByteSize}%08X: ${wbs.iname}")

      wbs.adr_i := io.wbm.adr_o
      wbs.dat_i := io.wbm.dat_o
      wbs.we_i := false.B
      wbs.stb_i := false.B
      wbs.cyc_i := false.B
      when(io.wbm.stb_o === true.B && io.wbm.cyc_o === true.B){
        when(io.wbm.adr_o < addrSlave.last.U &&
                    io.wbm.adr_o >= addrSlave(addrSlave.length - 2).U){
          wbs.we_i  := io.wbm.we_o
          wbs.stb_i := io.wbm.stb_o
          wbs.cyc_i := io.wbm.cyc_o
          io.wbm.dat_i := wbs.dat_o
          io.wbm.ack_i := wbs.ack_o
          if (wbs.feature_err) {
            io.wbm.err_i.get := wbs.err_o.get
          }
        }.otherwise {
          if (io.wbm.feature_err) {
            io.wbm.err_i.get := true.B
          }
        }
      }

    }
    val masterAddrMax = 1 << io.wbm.awidth
    assert(addrSlave.last <= masterAddrMax,
      f"Not enouth address space available for all slaves (0x$masterAddrMax%X)")
}

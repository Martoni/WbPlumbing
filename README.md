# WbPlumbing
Wishbone plumbing written in Chisel3. The aim of this project is mainly to
generate Wishbone intercon as described in [specification](https://github.com/fossi-foundation/wishbone).

# Installation and usages

WbPlumbing is a scala package that can be published localy. Then to install it we can simply clone the project and publish it localy 
```shell
$ git clone https://github.com/Martoni/WbPlumbing.git
$ cd WbPlumbing
$ make publishlocal
```

To use it under your project, add this line in your `build.sbt` file :
```scala
libraryDependencies ++= Seq("org.armadeus" %% "wbplumbing" % "0.1")
```

And import package in your chisel code :
```scala
import wbplumbing
```

# Modules description

## WbInterconPT

This is just a "passthrought" module, give one WbMaster bundle and one WbSlave
bundle in parameters and get the intercon module for your plumbing.

To use it simply give a WbMaster bundle and WbSlave bundle as parameter like following example :
```scala
// Imports
import wbplumbing.WbInterconPT
import wbplumbing.{WbMaster, WbSlave}

//...

// module instantiation
val spi2Wb = Module(new Spi2Wb(dwidth, awidth))
val wbMdio = Module(new MdioWb(mainFreq, targetFreq))
val wbIntercon = Module(new WbInterconPT(spi2Wb.io.wbm, wbMdio.io.wbs))
// wishbone Interconnexion
spi2Wb.io.wbm <> wbIntercon.io.wbm
wbIntercon.io.wbs <> wbMdio.io.wbs

// ...
```

## WbInterconOneMaster

Make the address decoding for several slaves with the same data size. And with
only one master.

To use it the master bundle must be given 'as it' in parameter, but slave should be given as a Seq() parameters like following example :
```scala
  // module instantiation
  val mspi2Wb = Module(new Spi2Wb(dwidth, 7))
  val wbMdio1 = Module(new MdioWb(mainFreq, targetFreq))
  val wbMdio2 = Module(new MdioWb(mainFreq, targetFreq))

  val wbIntercon = Module(new WbInterconOneMaster(mspi2Wb.io.wbm,
                            Seq(wbMdio1.io.wbs, wbMdio2.io.wbs)))
  // wishbone Interconnexion
  mspi2Wb.io.wbm <> wbIntercon.io.wbm
  wbIntercon.io.wbs(0) <> wbMdio1.io.wbs
  wbIntercon.io.wbs(1) <> wbMdio2.io.wbs
```

# Projects using WbPlumbing

* [MDIO](https://github.com/Martoni/MDIO): Generate ethernet phy MDIO protocol frame
* [Spi2Wb](https://github.com/Martoni/spi2wb): Drive a Wishbone master bus with SPI protocol.

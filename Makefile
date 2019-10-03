SBT=sbt

hdl:
	$(SBT) "runMain wbplumbing.WbPlumbing"

test:
	$(SBT) "test:testOnly wbplumbing.WbInterconSpec"

publishlocal:
	$(SBT) publishLocal

mrproper:
	make -C cocotb/ mrproper
	-rm *.anno.json
	-rm *.fir
	-rm *.v
	-rm -rf target
	-rm -rf test_run_dir
	-rm -rf project

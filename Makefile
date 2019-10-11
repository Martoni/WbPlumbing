SBT=sbt

hdl:
	$(SBT) "runMain wbplumbing.WbPlumbing"

test:
	$(SBT) "test:testOnly wbplumbing.WbInterconOneMasterSpec"

testall:
	$(SBT) "test:testOnly wbplumbing.WbInterconPTSpec"
	$(SBT) "test:testOnly wbplumbing.WbInterconOneMasterSpec"

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

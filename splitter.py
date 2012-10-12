import sys,math
filename= sys.argv[2]
parts	= int(sys.argv[1])
if __name__ == "__main__":
	lines = [l for l in open(filename,'r')]
	lpp   = int(math.ceil(len(lines)/float(parts)))

	for p in range(parts):
		train_file = open(filename + '%d_trn'%(p+1),'w')
		test_file  = open(filename + '%d_tst'%(p+1),'w')
		train_file.writelines(lines[:p*lpp])
		train_file.writelines(lines[(p+1)*lpp:])
		test_file.writelines(lines[p*lpp:(p+1)*lpp])

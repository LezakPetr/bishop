
OBJ=`ls ../../bishop/base/*.o`

for cpp in `ls *.cpp`; do
	bin=`echo $cpp | cut -d "." -f 1`
	g++ -o $bin $OBJ $cpp
done


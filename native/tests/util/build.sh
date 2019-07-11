
for cpp in `ls *.cpp`; do
	bin=`echo $cpp | cut -d "." -f 1`
	g++ $cpp -o $bin
done


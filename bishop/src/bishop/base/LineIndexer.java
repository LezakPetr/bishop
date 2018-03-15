package bishop.base;

import java.util.Arrays;

/**
 * 
 * @author Ing. Petr Ležák
 */
public class LineIndexer {
	
	private static final long[] coeffTable = new long[] {
		6377099826812796930L, 1513227208775221254L, 2449972206894077440L, 3602886299243520064L, 
		3459046127318339592L, 7349901050412924963L, 8935175917664306192L, 7061652645847892108L, 
		35888079931870244L, 4785219559325957L, 15199719093600769L, 16325643845254656L, 
		18577425782814214L, 9570443146428552L, 281556586987524L, 7318372202119732L, 
		54292234988109837L, 1765816245125892L, 17207357276848453L, 3683364189814272L, 
		4131965267609614L, 501382672031748L, 1024745069703320L, 204509738271753L, 
		18024850312710753L, 6336768980945664L, 5348312322408849L, 730077869375617L, 
		1126451810664764L, 1689133329715340L, 1011258642354362L, 331236469605381L, 
		5490707699606020L, 26494390562210056L, 9292110289179074L, 7214723812561408L, 
		563341097437975L, 1407658502401024L, 220233507800592L, 48383981008513L, 
		9113350445236233L, 34486198311518250L, 16118224169074723L, 2853232942579736L, 
		3940993953038340L, 563242149740560L, 803983790440514L, 353785055150103L, 
		17467973380220416L, 12957749688564224L, 11123077345859072L, 5280004791218688L, 
		3203092123649536L, 1543989741225985L, 651401446650880L, 470632896072192L, 
		23365326833893762L, 12791730206154882L, 5772436978066242L, 2465105606766918L, 
		1689194015762442L, 562985119782210L, 378321393812668L, 62139670645166L, 
		31859590778060978L, 1987384768185342L, 22559170402608434L, 3426102052361073L, 
		7885847761885997L, 2547195862031758L, 1556973167406908L, 703876689040492L, 
		22885978082035L, 62053773721863L, 88287491171692L, 44103257744256L, 
		22077756440357L, 6769342445993L, 1942300414060L, 1172464345445L, 
		24897822877486163L, 13614945651727362L, 11263805539606913L, 14644979559323908L, 
		562967180250060L, 281809988420676L, 2005766975665686L, 881649478942262L, 
		4574297230950708L, 4583159335427344L, 1132598177923120L, 18018804600801408L, 
		5774638298701843L, 4522299336215820L, 3951683513816852L, 1478855361824040L, 
		5678345775800336L, 1149954700756064L, 1337899527702504L, 807043688039040L, 
		27029861292605728L, 13548122148680936L, 1792029460007956L, 2674622303372557L, 
		2254334411788423L, 712655578933331L, 294944799770668L, 687480211473L, 
		51035519114244L, 10211174201231875L, 13687855511607564L, 3303142679173576L, 
		884031514610378L, 495983409530139L, 1950128023050L, 6511920298L, 
		301457474899L, 44874094380201L, 18032069784000719L, 3903180442795693L, 
		1066535579816830L, 3234246165565L, 7617393672L, 50907138L, 
		1893859875L, 417218962976L, 101275533851137L, 18071727737818880L
	};

	private static final int[] DEPTHS = {
		12, 11, 11, 11, 11, 11, 11, 12, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		11, 10, 10, 10, 10, 10, 10, 11, 
		12, 11, 11, 11, 11, 11, 11, 12, 
		6, 5, 5, 5, 5, 5, 5, 6, 
		5, 5, 5, 5, 5, 5, 5, 5, 
		5, 5, 7, 7, 7, 7, 5, 5, 
		5, 5, 7, 9, 9, 7, 5, 5, 
		5, 5, 7, 9, 9, 7, 5, 5, 
		5, 5, 7, 7, 7, 7, 5, 5, 
		5, 5, 5, 5, 5, 5, 5, 5, 
		6, 5, 5, 5, 5, 5, 5, 6
	};
		
	private static final FileRankOffset[][] directionOffsets = {
		{ new FileRankOffset(+1, 0), new FileRankOffset(-1, 0), new FileRankOffset(0, +1), new FileRankOffset(0, -1) },   // ORTHOGONAL
		{ new FileRankOffset(+1, +1), new FileRankOffset(-1, -1), new FileRankOffset(+1, -1), new FileRankOffset(-1, +1) }   // DIAGONAL
	};
	
	private static final long[] maskTable;
	private static final byte[] shiftTable;
	private static final int[] baseTable;
	private static final int lastIndex;
	
	static {
		maskTable = new long[CrossDirection.LAST * Square.LAST];
		shiftTable = new byte[CrossDirection.LAST * Square.LAST];
		baseTable = new int[CrossDirection.LAST * Square.LAST];
		int base = 0;
		
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final int tableIndex = getCellIndex(direction, square);
				final long mask = calculateDirectionMask (direction, square);
				final int bits = DEPTHS[tableIndex];

				maskTable[tableIndex] = mask;
				shiftTable[tableIndex] = (byte) (Square.LAST - bits);
				baseTable[tableIndex] = base;
				
				base += 1 << bits;
			}
		}
		
		lastIndex = base;
	}
	
	public static int getCellIndex (final int direction, final int square) {
		return (direction << Square.BIT_COUNT) + square;
	}
	
	public static long calculateDirectionMask (final int direction, final int square) {
		long mask = 0;
		
		for (FileRankOffset offset: directionOffsets[direction]) {
			int file = Square.getFile(square) + offset.getFileOffset();
			int rank = Square.getRank(square) + offset.getRankOffset();
			
			while (true) {
				final int testSquare = Square.onFileRank(file, rank);
				
				file += offset.getFileOffset();
				rank += offset.getRankOffset();
				
				if (!File.isValid(file) || !Rank.isValid(rank))
					break;
				
				mask |= BitBoard.getSquareMask(testSquare);
			}
		}
		
		return mask;
	}
	
	/**
	 * Returns line index of given cross from given square with given occupancy.
	 * @param direction cross direction
	 * @param square begin square
	 * @param occupancy occupancy mask
	 * @return line index
	 */
	public static int getLineIndex (final int direction, final int square, final long occupancy) {
		final int cellIndex = getCellIndex (direction, square);

		final long mask = maskTable[cellIndex];
		final long coeff = coeffTable[cellIndex];
		final int shift = shiftTable[cellIndex];
		final int base = baseTable[cellIndex];
		
		return base + (int) (((occupancy & mask) * coeff) >>> shift);
	}
	
	public static int getLastIndex() {
		return lastIndex;
	}

	public static long getDirectionMask(final int direction, final int square) {
		final int cellIndex = getCellIndex (direction, square);
		
		return maskTable[cellIndex];
	}
	
	public static FileRankOffset[] getDirectionOffsets (final int direction) {
		return directionOffsets[direction];
	}

	public static long[] getCoeffs() {
		return Arrays.copyOf(coeffTable, coeffTable.length);
	}
	
	public static int[] getDepths() {
		return Arrays.copyOf(DEPTHS, DEPTHS.length);
	}

}

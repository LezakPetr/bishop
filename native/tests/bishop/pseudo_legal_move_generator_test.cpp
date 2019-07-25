

#include "../../bishop/base/color.h"
#include "../../bishop/base/position.h"
#include "../../bishop/base/move.h"
#include "../../bishop/base/pseudo_legal_move_generator.h"
#include "../test_base/test_base.h"
#include "../../bishop/base/null_position_caching.h"

#include <random>
#include <algorithm>
#include <string>

using namespace bishop::base;
using namespace std;

long long counter;

class Walker {
	public:
		bool processMove (const Move move) {
			counter++;
			return true;
		}
};

static constexpr long long SPEED_ITERATION_COUNT = 10000000;

TEST(speedTest) {
	Position<NullPositionCaching> position;
	position.setInitialPosition();
	
	Walker walker;
	PseudoLegalMoveGenerator<Position<NullPositionCaching>, Walker> generator (position, walker);
	
	counter = 0;

	clock_t t1 = clock();

	for (int i = 0; i < SPEED_ITERATION_COUNT; i++)
		generator.generateMoves();
	
	clock_t t2 = clock();

	assertEquals (20 * SPEED_ITERATION_COUNT, counter);

	double speed = (double) SPEED_ITERATION_COUNT * CLOCKS_PER_SEC / (double) (t2 - t1);

	::std::cout << speed << ::std::endl;
}

TEST_RUNNER_BEGIN
	RUN_TEST(speedTest);
TEST_RUNNER_END


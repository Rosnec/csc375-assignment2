# rwl

A clojure library of readers-writer locks and testing utilities to ensure they
are thread-safe. I implement a countdown-semaphore-lock (CSL) in clojure, and
also import java.util.concurrent's ReentrantReadWriteLock, and give them both a
common interface function general-rwl. test/rwl/core_test.clj includes some unit
tests to prove that the locks work correctly.

The lock's performance is tested on a multiplayer game called star-flight-3d,
and plots of this performance on two machines are compiled into the html file
examples/csc375assignment2plots.html. More plots can be generated based on the
command-line arguments provided to main.

## Usage

java -jar <jar-name.jar>
	<output-directory> <start> <iterations> <times-to-avg> <step>

or using Leiningen

lein run
	<output-directory> <start> <iterations> <times-to-avg> <step>

## License

Copyright © 2013 Dan Wysocki

Distributed under the GNU General Public License

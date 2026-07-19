/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2026 Parsington developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.parsington.bench;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Entry point for running the Parsington JMH benchmarks.
 * <p>
 * With no arguments, runs a curated default suite: every {@code *Benchmark}
 * class in this package, one fork, with the GC (allocation) profiler enabled so
 * both time-per-op and bytes-per-op are reported. The warmup/measurement budget
 * is tuned so the whole suite completes in roughly 10&ndash;15 minutes; for more
 * rigorous numbers, run individual benchmarks with more forks and longer
 * iterations via the argument form below.
 * </p>
 * <p>
 * With arguments, delegates to the standard JMH command line
 * ({@link org.openjdk.jmh.Main}), so any JMH option is available, e.g.:
 * </p>
 *
 * <pre>
 * mvn -Pbenchmark -Dbenchmark.args="-prof gc -f 3 -wi 5 -i 10 LiteralsBenchmark"
 * </pre>
 *
 * @author Curtis Rueden
 */
public final class Benchmarks {

	private Benchmarks() {
		// prevent instantiation
	}

	public static void main(final String[] args) throws Exception {
		if (args.length > 0) {
			// Defer entirely to the JMH command line.
			org.openjdk.jmh.Main.main(args);
			return;
		}

		// Curated default suite. The timing here is a deliberate compromise
		// biased toward a short total runtime (~10-15 min across all parameter
		// combinations) rather than maximal statistical rigor. The benchmark
		// classes declare a single mode (Mode.AverageTime) to avoid doubling the
		// runtime with a redundant reciprocal (throughput) measurement.
		new Runner(new OptionsBuilder() //
			.include(".*Benchmark") //
			.warmupIterations(3) //
			.warmupTime(TimeValue.seconds(1)) //
			.measurementIterations(4) //
			.measurementTime(TimeValue.seconds(2)) //
			.forks(1) //
			.addProfiler(GCProfiler.class) //
			.shouldFailOnError(true) //
			.build()).run();
	}
}

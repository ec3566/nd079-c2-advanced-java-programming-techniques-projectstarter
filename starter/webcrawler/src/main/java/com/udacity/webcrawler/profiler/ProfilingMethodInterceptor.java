package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

	private final Clock clock;
	private final Object delegate;
	private final ZonedDateTime startTime;
	private final ProfilingState state;


	// TODO: You will need to add more instance fields and constructor arguments to this class.
	ProfilingMethodInterceptor ( Clock clock, Object delegate, ProfilingState state, ZonedDateTime startTime ) {
		this.clock = Objects.requireNonNull ( clock );
		this.delegate = delegate;
		this.state = state;
		this.startTime = startTime;
	}

	public < T > ProfilingMethodInterceptor ( Clock clock, T delegate, ProfilingState state, ZonedDateTime startTime, Clock clock1, Object delegate1, ProfilingState state1, ZonedDateTime startTime1 ) {
		this.clock = clock1;
		this.delegate = delegate1;
		this.state = state1;
		this.startTime = startTime1;
	}

	@Override
	public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
		// TODO: This method interceptor should inspect the called method to see if it is a profiled
		//       method. For profiled methods, the interceptor should record the start time, then
		//       invoke the method using the object that is being profiled. Finally, for profiled
		//       methods, the interceptor should record how long the method call took, using the
		//       ProfilingState methods.




		//
		//left off here, review to make sure you remember and dont fuck it up
		//
		Object invoked = null;
		Instant start = null;
		boolean profiled = method.getAnnotation ( Profiled.class ) != null;
		if ( profiled ) {
			start = clock.instant ( );
		} try {
			invoked = method.invoke ( delegate, args );
		} catch ( InvocationTargetException e ) {
			throw e.getTargetException ( );
		} finally {
			if ( profiled ) {
				Duration dur = Duration.between ( start, clock.instant ( ) );
				state.record ( delegate.getClass ( ), method, dur );
			}
		}
		return invoked;
	}
}

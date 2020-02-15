/*
 * Copyright 2011-2020 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.internal.eventbus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;

/**
 * @since 4.5.1
 */
public class WorkerExecutorServiceFactory implements ExecutorServiceFactory {

	@Override
	public ExecutorService createExecutorService(String description, int maxThreads) {
		final ThreadGroup group = new ThreadGroup(description);

		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				final Thread thread = new Thread(group, r);
				thread.setName(description + "-" + thread.getId());
				thread.setDaemon(true);
				return thread;
			}
		};

		return new ThreadPoolExecutor(
			Ints.constrainToRange(Runtime.getRuntime().availableProcessors(), 1, maxThreads), maxThreads, 
			1L, TimeUnit.MINUTES,
			new LinkedBlockingQueue<Runnable>(), 
			threadFactory
		);
	}
}

/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerManager {
	private static Logger log = LoggerFactory.getLogger(TimerManager.class);
	private static TimerManager instance = new TimerManager();
	private ScheduledThreadPoolExecutor ses;
	
	public static TimerManager getInstance() {
		return instance;
	}

	public void start() {
		if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
			return;
		}
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Timermanager-Worker-" + threadNumber.getAndIncrement());
				return t;
			}
		});
		stpe.setMaximumPoolSize(4);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		ses = stpe;
	}

	public void stop() {
		ses.shutdown();
	}
	
	public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay) {
		return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
		return schedule(r, timestamp - System.currentTimeMillis());
	}
	
	
	public long getActiveCount() {
		return ses.getActiveCount();
	}

	public long getCompletedTaskCount() {
		return ses.getCompletedTaskCount();
	}

	public int getQueuedTasks() {
		return ses.getQueue().toArray().length;
	}

	public long getTaskCount() {
		return ses.getTaskCount();
	}

	public boolean isShutdown() {
		return ses.isShutdown();
	}

	public boolean isTerminated() {
		return ses.isTerminated();
	}

	private static class LoggingSaveRunnable implements Runnable {
		Runnable r;

		public LoggingSaveRunnable(Runnable r) {
			this.r = r;
		}

		@Override
		public void run() {
			try {
				r.run();
			} catch (Throwable t) {
				log.error("ERROR", t);
			}
		}
	}
}
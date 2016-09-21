/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

/**
 *
 * @author Itzik
 */
public class JobConstants {

	public static final boolean enableJobs = true;
	public static final int jobOrder = 8;

	public enum LoginJob {

		RESISTANCE(0, JobFlag.DISABLED),
		EXPLORER(1, JobFlag.ENABLED),
		CYGNUS(2, JobFlag.DISABLED),
		ARAN(3, JobFlag.DISABLED),
		EVAN(4, JobFlag.DISABLED),
		MERCEDES(5, JobFlag.ENABLED),
		DEMON(6, JobFlag.DISABLED),
		PHANTOM(7, JobFlag.DISABLED),
		DUAL_BLADE(8, JobFlag.DISABLED),
		MIHILE(9, JobFlag.DISABLED),
		LUMINOUS(10, JobFlag.DISABLED),
		KAISER(11, JobFlag.ENABLED),
		ANGELIC(12, JobFlag.DISABLED),
		CANNONER(13, JobFlag.DISABLED),
		XENON(14, JobFlag.DISABLED),
		ZERO(15, JobFlag.DISABLED),
		SHADE(16, JobFlag.DISABLED),
		JETT(17, JobFlag.DISABLED),
		HAYATO(18, JobFlag.DISABLED),
		KANNA(19, JobFlag.DISABLED),
		CHASE(20, JobFlag.DISABLED),
		PINK_BEAN(21, JobFlag.ENABLED),
		KINESIS(22, JobFlag.DISABLED);

		private final int jobType, flag;

		private LoginJob(int jobType, JobFlag flag) {
			this.jobType = jobType;
			this.flag = flag.getFlag();
		}

		public int getJobType() {
			return jobType;
		}

		public int getFlag() {
			return flag;
		}

		public enum JobFlag {

			DISABLED(0), 
			ENABLED(1);
			private final int flag;

			private JobFlag(int flag) {
				this.flag = flag;
			}

			public int getFlag() {
				return flag;
			}
		}
	}
}

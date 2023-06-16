package co.za.imac.judge;

import co.za.imac.judge.utils.SettingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;

@SpringBootApplication
public class JudgeApplication {
	private static final Logger logger =
			LoggerFactory.getLogger(JudgeApplication.class);

	public static void main(String[] args) {
		checkAppConfigDirOverrides();
		SpringApplication.run(JudgeApplication.class, args);
	}

	/**********************
	 * Checks if settings.json exists in the CWD or if a directory is provided specifically as a property.
	 * If specifically provided then that takes preference.
	 * Otherwise if it's in the CWD then that's next.
	 * Finally it defaults to the standard location.
	 */
	private static void checkAppConfigDirOverrides() {

		String AppConfigPath = System.getProperty("judge.config.path");
		if (AppConfigPath == null) {
			String userDirectory = System.getProperty("user.dir");
			logger.debug("Working directory is : " + userDirectory);
			File f = new File(userDirectory + File.separator + "settings.json");
			if(f.exists() && !f.isDirectory()) {
				logger.info("Settings file found - setting Application Config path to Current Working Dir.");
				SettingUtils.setApplicationConfigPath(userDirectory);
			}
		} else {
			File appConfigDir = new File (AppConfigPath);
			if (appConfigDir.exists() && appConfigDir.isDirectory()) {
				logger.info("Setting Applicatication Config path to " + AppConfigPath + ".");
				SettingUtils.setApplicationConfigPath(AppConfigPath);
			} else {
				logger.error("Application Config path:" + AppConfigPath + " does not exist.  Refusing to use it.");
			}
		}
	}
}

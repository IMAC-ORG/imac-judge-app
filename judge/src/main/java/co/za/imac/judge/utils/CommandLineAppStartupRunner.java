package co.za.imac.judge.utils;

import co.za.imac.judge.JudgeApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Properties;

import co.za.imac.judge.service.SettingService;
import co.za.imac.judge.dto.SettingDTO;

@Profile("!test")
@Component
public class CommandLineAppStartupRunner implements ApplicationRunner {
    private static final Logger logger =
            LoggerFactory.getLogger(CommandLineAppStartupRunner.class);


    public static String GIT_COMMIT_ID_FULL = null;
    public static String GIT_COMMIT_ID_ABBREV = null;
    public static String GIT_COMMIT_TIME = null;
    public static String GIT_BUILD_TIME = null;
    public static String GIT_BUILD_VERSION = null;
    public static String GIT_BRANCH = null;
    public static Boolean GIT_DIRTY = null;
    public static String GIT_COMMIT_USER_NAME = null;
    public static String GIT_COMMIT_USER_EMAIL = null;
    public static String GIT_COMMIT_MESSAGE_FULL = null;
    public static String GIT_COMMIT_MESSAGE_SHORT = null;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.debug("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.debug("NonOptionArgs: {}", args.getNonOptionArgs());
        logger.debug("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()){
            logger.debug("arg: " + name + "=" + args.getOptionValues(name));
        }

        // Note: Moved this stuff to main() because it was too late for initialisation...
        // Maybe there is a springboot way to do this, I am not sure.
        //if (args.containsOption("judge.config.path")) {
        //    List<String> configPaths = args.getOptionValues("judge.config.path");
        //    SettingUtils.setApplicationConfigPath(configPaths.get(configPaths.size() - 1));
        //} else {
        //    // Check if settings file exists in CWD.
        //    String userDirectory = System.getProperty("user.dir");
        //    File f = new File(userDirectory + File.separator + "settings.json");
        //    if(f.exists() && !f.isDirectory()) {
        //        logger.info("Settings file found - setting application path to Current Working Dir.");
        //        SettingUtils.setApplicationConfigPath(userDirectory);
        //    }
        //}

        logger.info("Config Path: " + SettingUtils.getApplicationConfigPath());

        Properties prop = new Properties();
        try {
            prop.load(JudgeApplication.class.getResourceAsStream("/props/git.properties"));
            GIT_COMMIT_ID_FULL = prop.getProperty("git.commit.id.full");
            GIT_COMMIT_ID_ABBREV = prop.getProperty("git.commit.id.abbrev");
            GIT_COMMIT_TIME = prop.getProperty("git.commit.time");
            GIT_BUILD_TIME = prop.getProperty("git.build.time");
            GIT_BUILD_VERSION = prop.getProperty("git.build.version");
            GIT_BRANCH = prop.getProperty("git.branch");
            GIT_DIRTY = Boolean.parseBoolean(prop.getProperty("git.dirty"));
            GIT_COMMIT_USER_NAME = prop.getProperty("git.build.user.name");
            GIT_COMMIT_USER_EMAIL = prop.getProperty("git.build.user.email");
            GIT_COMMIT_MESSAGE_FULL = prop.getProperty("git.commit.message.full");
            GIT_COMMIT_MESSAGE_SHORT = prop.getProperty("git.commit.message.short");

            logger.info("Application information:");
            logger.info("\tGit Build Version: " + GIT_BUILD_VERSION);
            logger.info("\tGit Build Time: " + GIT_BUILD_TIME);
            logger.info("\tGit Commit ID: " + GIT_COMMIT_ID_ABBREV);
            logger.debug("\tGit Commit Long ID: " + GIT_COMMIT_ID_FULL);
            logger.info("\tGit Commit Time: " + GIT_COMMIT_TIME);
            logger.info("\tGit Branch: " + GIT_BRANCH);
            logger.info("\tGit Dirty: " + GIT_DIRTY);
            logger.info("\tGit Commit User: " + GIT_COMMIT_USER_NAME);
            logger.debug("\tGit Commit User (email): " + GIT_COMMIT_USER_EMAIL);
            logger.info("\tGit Message: " + GIT_COMMIT_MESSAGE_SHORT);
            logger.debug("\tGit Message (Full): " + GIT_COMMIT_MESSAGE_FULL);
        } catch (Exception e) {
            logger.error ("Could not get the git properties.");
            e.printStackTrace();
        }

        SettingService settingSvc = new SettingService();
        SettingDTO settingDTO = settingSvc.getSettings();

        logger.info ("Judge ID: " + settingDTO.getJudge_id());
        logger.info ("Line No.: " + settingDTO.getLine_number());
        logger.info ("Score Host: " + settingDTO.getScore_host());
        logger.info ("Score Port: " + settingDTO.getScore_http_port());
    }
}
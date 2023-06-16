package co.za.imac.judge.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import co.za.imac.judge.service.SettingService;
import co.za.imac.judge.dto.SettingDTO;

@Profile("!test")
@Component
public class CommandLineAppStartupRunner implements ApplicationRunner {
    private static final Logger logger =
            LoggerFactory.getLogger(CommandLineAppStartupRunner.class);


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

        SettingService settingSvc = new SettingService();
        SettingDTO settingDTO = settingSvc.getSettings();

        logger.info ("Judge ID: " + settingDTO.getJudge_id());
        logger.info ("Line No.: " + settingDTO.getLine_number());
        logger.info ("Score Host: " + settingDTO.getScore_host());
        logger.info ("Score Port: " + settingDTO.getScore_http_port());
    }


}
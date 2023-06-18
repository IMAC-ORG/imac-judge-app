package co.za.imac.judge.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import co.za.imac.judge.dto.SettingDTO;
import co.za.imac.judge.utils.SettingUtils;

@Service
public class SettingService {

    private static final Logger logger =
            LoggerFactory.getLogger(SettingService.class);
    private final String SETTINGS_FILE_NAME = SettingUtils.getApplicationConfigPath() + "/settings.json";

    public boolean isSettings() {
        File targetFile = new File(SETTINGS_FILE_NAME);
        logger.debug("Exists: " + SETTINGS_FILE_NAME + " - " + targetFile.exists());
        return targetFile.exists();
    }

    public SettingDTO updateSettings(SettingDTO settingDTO) throws IOException {
        File newFile = new File(SETTINGS_FILE_NAME);
        newFile.createNewFile();
        String compdtoJson = new Gson().toJson(settingDTO);
        byte[] strToBytes = compdtoJson.getBytes();
        FileOutputStream outputStream = new FileOutputStream(SETTINGS_FILE_NAME);
        outputStream.write(strToBytes);
        outputStream.close();

        // get settings file
        return settingDTO;
    }

    public SettingDTO createSettings() throws IOException {
        SettingDTO settingDTO = new SettingDTO();
        File newFile = new File(SETTINGS_FILE_NAME);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            logger.error ("Could not create " + SETTINGS_FILE_NAME);
            throw(e);
        }
        String compdtoJson = new Gson().toJson(settingDTO);
        byte[] strToBytes = compdtoJson.getBytes();
        FileOutputStream outputStream = new FileOutputStream(SETTINGS_FILE_NAME);
        outputStream.write(strToBytes);
        outputStream.close();
        // get settings file
        return settingDTO;
    }

    public SettingDTO getSettings() throws IOException {
        if (isSettings()) {
            FileInputStream inputStream = new FileInputStream(SETTINGS_FILE_NAME);
            StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            }
            return new Gson().fromJson(resultStringBuilder.toString(), SettingDTO.class);
        } else {
            return createSettings();
        }
    }
}

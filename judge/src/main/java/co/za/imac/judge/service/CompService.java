package co.za.imac.judge.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.utils.SettingUtils;

@Service
public class CompService {
    

private final String COMP_FILE_NAME = SettingUtils.APPLICATION_CONFIG_PATH + "/comp.json";

public boolean isCurrentComp(){
    File targetFile = new File(COMP_FILE_NAME);
    return targetFile.exists();
}

public CompDTO createComp(CompDTO compDTO) throws IOException{
    File newFile = new File(COMP_FILE_NAME);
    newFile.createNewFile();
    String compdtoJson = new Gson().toJson(compDTO);
    byte[] strToBytes = compdtoJson.getBytes();
    FileOutputStream outputStream = new FileOutputStream(COMP_FILE_NAME);
    outputStream.write(strToBytes);
    outputStream.close();

    //get pilots dat file
    return compDTO;
}

public CompDTO getComp() throws IOException{
    FileInputStream inputStream = new FileInputStream(COMP_FILE_NAME);
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br
      = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = br.readLine()) != null) {
            resultStringBuilder.append(line).append("\n");
        }
    }
    return new Gson().fromJson(resultStringBuilder.toString(),CompDTO.class);
}


}

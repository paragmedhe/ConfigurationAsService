package com.cas.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cas.dao.FileDao;
import com.cas.service.FileService;

public class FileServiceImpl implements FileService {
    FileDao fileDao;
    private  static final Logger LOGGER = Logger.getLogger(FileServiceImpl.class.getName()); 
    public static final String SCRIPT_HOME = "SCRIPT_HOME";
    public static final String PASSWD = "password" ;
    public static final String HOSTNAME = "hostname";
    public static final String USERNAME = "username";
    public static final String SCRIPT_FILE = "launchExpect.sh";
    
    public FileDao getFileDao() {
        return fileDao;
    }

    public void setFileDao(FileDao fileDao) {
        this.fileDao = fileDao;
    }
    
    @Override
    public List<String> getFile(int fileId) {

        List<String> fileContent = new ArrayList<String>();
        Map<String, String> fileData = new HashMap<String, String>();
        String propertyHome = System.getenv("PROPERTY_HOME");
        String scriptHome = System.getenv(SCRIPT_HOME);



        String baseFileName = null;
        try {
            fileData = fileDao.getFileData(fileId);
            baseFileName = fileId + "_" + fileData.get("filename");
        } catch (SQLException e1) {
            LOGGER.log(Level.SEVERE,e1.getMessage(),e1);

        }

        /*
         * sh launchExpect.sh parag ubuntu.local root pull /home/parag/abc.txt
         * /home/prasad/CAS/
         */
        Timestamp filetimestamp;

        try {

            String cmd = "sh " + scriptHome + SCRIPT_FILE + " " +scriptHome+" "+fileData.get(USERNAME) + " "
                    + fileData.get(HOSTNAME) + " " + fileData.get(PASSWD) + " pull" + " "
                    + fileData.get("configfilepath") + " " + propertyHome;


            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader r2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while (r.ready()) {
                String readLine = r.readLine();
                if (readLine.startsWith("Modify: ")) {


                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date parsedDate = dateFormat.parse(readLine.substring(8, 27));
                        filetimestamp = new java.sql.Timestamp(parsedDate.getTime());
                        fileDao.insertFileTimeStamp(filetimestamp, fileId);

                    } catch (java.text.ParseException e) {

                        LOGGER.log(Level.SEVERE, e.toString());
                    }
                }
            }
            while (r2.ready()) {
            }
            r.close();
            r2.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);


        }

        File oldFile = new File(propertyHome + fileData.get("filename"));
        File newFile = new File(propertyHome + baseFileName);

        oldFile.renameTo(newFile);

        fileContent.add(baseFileName);
        Scanner s = null;

        try {

            s = new Scanner(newFile);
            while (s.hasNextLine()) {
                fileContent.add(s.nextLine());
            }

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            if (s != null) {
                s.close();
            }

        }

        return fileContent;

    }
    @Override
    public boolean saveFile(String name, String content, String serverId, String isRestart) {

        String newContent = content;
        String propertyHome = System.getenv("PROPERTY_HOME");
        String scriptHome = System.getenv(SCRIPT_HOME);
        Map<String,String> replaceMap = new HashMap<String, String>();
        replaceMap.put("<br><br>", "\n");
        replaceMap.put("<div>", "");
        replaceMap.put("</div>", "");
        replaceMap.put("<br>", "\n");
        
        for(Map.Entry<String, String> entry: replaceMap.entrySet()){
            newContent = newContent.replaceAll(entry.getKey(), entry.getValue());
        }
        
        
        try{
            String filename = propertyHome + name;
            int splitIndex = name.indexOf("_");
            int fileId = Integer.parseInt(name.substring(0, splitIndex));
            String oldFileName = name.substring(splitIndex + 1, name.length());
            File thisFile = new File(filename);
            File oldFile = new File(propertyHome + oldFileName);
            thisFile.renameTo(oldFile);

            try {

                FileWriter fileWriter = new FileWriter(oldFile, false);
                fileWriter.write(newContent);
                fileWriter.close();

                // Scp file to required server

            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);


            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);

            }
            Map<String, String> serverData = fileDao.getServerData(fileId, Integer.parseInt(serverId));
            String remotePath = serverData.get("remotefilepath");
            remotePath = remotePath.substring(0, remotePath.lastIndexOf("/") + 1);
            String cmd = "sh " + scriptHome + SCRIPT_FILE + " " + scriptHome+" "+serverData.get(USERNAME) + " "
                    + serverData.get(HOSTNAME) + " " + serverData.get(PASSWD) + " push" + " " + propertyHome
                    + oldFileName + " " + remotePath;

            Process p = Runtime.getRuntime().exec(cmd);

            p.waitFor();


            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader r2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while (r.ready()) {
            }
            while (r2.ready()) {
            }

            if("true".equals(isRestart)){
                String restartCommand = "sh " + scriptHome + SCRIPT_FILE + " " + scriptHome+" "+serverData.get(USERNAME) + " "
                        + serverData.get(HOSTNAME) + " " + serverData.get(PASSWD) + " restart ";

                Process processRestart = new ProcessBuilder(restartCommand, serverData.get("restartcommand")).start();
                processRestart.waitFor();


                BufferedReader iStream = new BufferedReader(new InputStreamReader(processRestart.getInputStream()));
                BufferedReader eStream = new BufferedReader(new InputStreamReader(processRestart.getErrorStream()));

                while (iStream.ready()) {

                }
                while (eStream.ready()) {

                }
                iStream.close();
                eStream.close();


            }
            r.close();
            r2.close();
            return false;

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
           
        } catch(IOException e){

           LOGGER.log(Level.SEVERE,e.getMessage(),e);

        }catch (InterruptedException e) {

            LOGGER.log(Level.SEVERE, e.toString());

        }

        return false;

    }

    private boolean checkifModified(String substring, int fileId) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(substring);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            Timestamp firstTimeStamp = fileDao.getRetrievedTimestamp(fileId);

            if (timestamp.after(firstTimeStamp)) {
                return true;
            }

        } catch (java.text.ParseException e) {
            LOGGER.log(Level.SEVERE, e.toString());
        }
        return false;
    }
    @Override
    public boolean checkModified(String name, String content, String serverId) {

        String scriptHome = System.getenv(SCRIPT_HOME);
        int splitIndex = name.indexOf("_");
        int fileId = Integer.parseInt(name.substring(0, splitIndex));



        boolean isModified = false;
        Map<String, String> serverData = fileDao.getServerData(fileId, Integer.parseInt(serverId));

        try {

            String remotePathCheck = serverData.get("remotefilepath");

            String checkCommand = "sh " + scriptHome + SCRIPT_FILE + " " + scriptHome+" "+serverData.get(USERNAME) + " "
                    + serverData.get(HOSTNAME) + " " + serverData.get(PASSWD) + " getModTime" + " "
                    + remotePathCheck;

            Process checkProc = Runtime.getRuntime().exec(checkCommand);
            checkProc.waitFor();

            BufferedReader iStream = new BufferedReader(new InputStreamReader(checkProc.getInputStream()));
            BufferedReader eStream = new BufferedReader(new InputStreamReader(checkProc.getErrorStream()));

            while (iStream.ready()) {
                String readLine = iStream.readLine();
                if (readLine.startsWith("Modify: ")) {

                    isModified = checkifModified(readLine.substring(8, 27), fileId);

                }

            }
            while (eStream.ready()) {

            }
            iStream.close();
            eStream.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);

        }


        return isModified;
    }
    @Override
    public com.cas.model.File addFile(com.cas.model.File file) {

        return fileDao.addFile(file);
    }

}

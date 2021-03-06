package com.cas.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.cas.dao.FileDao;
import com.cas.model.File;

public class FileDaoImpl implements FileDao {
    DataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(FileDaoImpl.class.getName());

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public File addFile(File file) {

        boolean isFileExists = false;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        if (file != null) {
            try {

                for (int i = 0; i < file.getServerId().length; i++) {
                    int serverId = Integer.parseInt(file.getServerId()[i]);

                    String query = "Select count(1) from config where config_file_path = ? and server_id = ?";
                    pstmt = dataSource.getConnection().prepareStatement(query);
                    pstmt.setString(1, file.getFilePath());
                    pstmt.setInt(2, serverId);
                    resultSet = pstmt.executeQuery();
                    if (resultSet.next()) {
                        isFileExists = resultSet.getInt(1) > 0;
                    }
                    if (isFileExists) {
                        return null;
                    }
                    if (!(resultSet.getInt(1) > 0)) {
                        String insertTableSQL = "INSERT INTO config"
                                + "(config_name, config_desc,config_file_path,server_id) VALUES" + "(?,?,?,?)";
                        PreparedStatement preparedStatement = dataSource.getConnection()
                                .prepareStatement(insertTableSQL);
                        preparedStatement.setString(1, file.getFileName());
                        preparedStatement.setString(2, file.getFileDesc());
                        preparedStatement.setString(3, file.getFilePath());
                        preparedStatement.setInt(4, serverId);
                        preparedStatement.executeUpdate();

                    }

                }
                return file;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }

        }

        return null;
    }

    @Override
    public Map<String, String> getFileData(int fileId) throws SQLException {
        /*
         * sh launchExpect.sh parag ubuntu.local root pull /home/parag/abc.txt
         * /home/prasad/CAS/
         */
        Map<String, String> fileData = new HashMap<String, String>();
        String query = "Select * from config where config_id=?";
        PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
        pstmt.setInt(1, fileId);
        ResultSet resultSet = pstmt.executeQuery();
        if (resultSet.next()) {

            fileData.put("configfilepath", resultSet.getString("config_file_path"));
            fileData.put("serverid", resultSet.getString("server_id"));
        }

        String filePath = fileData.get("configfilepath");
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        fileData.put("filename", fileName);

        int serverId = Integer.parseInt(fileData.get("serverid"));

        String query1 = "Select * from server where server_id=?";
        PreparedStatement pstmt1 = dataSource.getConnection().prepareStatement(query1);
        pstmt1.setInt(1, serverId);
        ResultSet resultSet1 = pstmt1.executeQuery();
        if (resultSet1.next()) {
            fileData.put("username", resultSet1.getString("server_username"));
            fileData.put("hostname", resultSet1.getString("server_ipaddress"));
            fileData.put("password", resultSet1.getString("server_password"));
        }

        return fileData;
    }

    @Override
    public Map<String, String> getServerData(int fileId, int serverId) {

        Map<String, String> serverData = new HashMap<String, String>();

        try {
            String query = "Select * from config where config_id=?";
            PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
            pstmt.setInt(1, fileId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                serverData.put("remotefilepath", resultSet.getString("config_file_path"));
            }

            String query1 = "Select * from server where server_id=?";
            PreparedStatement pstmt1 = dataSource.getConnection().prepareStatement(query1);
            pstmt1.setInt(1, serverId);
            ResultSet resultSet1 = pstmt1.executeQuery();
            if (resultSet1.next()) {
                serverData.put("username", resultSet1.getString("server_username"));
                serverData.put("hostname", resultSet1.getString("server_ipaddress"));
                serverData.put("password", resultSet1.getString("server_password"));
                String serverRestartCommand = "\"" + resultSet1.getString("server_restart_cmd") + "\"";
                serverData.put("restartcommand", serverRestartCommand);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return serverData;
    }

    @Override
    public Timestamp getRetrievedTimestamp(int fileId) {
        Timestamp tStamp = null;
        try {
            String query = "Select * from fileoperations where config_id=?";
            PreparedStatement pstmt = dataSource.getConnection().prepareStatement(query);
            pstmt.setInt(1, fileId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                tStamp = resultSet.getTimestamp("retrieved_at");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return tStamp;
    }

    @Override
    public void insertFileTimeStamp(Timestamp timestamp, int fileId) {

        try {
            String insertTableSQL = "INSERT INTO fileoperations" + "(config_id, retrieved_at) VALUES"
                    + "(?,?) ON DUPLICATE KEY UPDATE retrieved_at=values(retrieved_at)";
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(insertTableSQL);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setTimestamp(2, timestamp);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

}

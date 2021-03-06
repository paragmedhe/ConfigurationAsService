package com.cas.service.impl;

import java.sql.SQLException;

import com.cas.dao.LoginDao;
import com.cas.model.User;
import com.cas.service.LoginService;


public class LoginServiceImpl implements LoginService {

    private LoginDao loginDao;

    public LoginDao getLoginDao()
    {
        return this.loginDao;
    }

    public void setLoginDao(LoginDao loginDao)
    {
        this.loginDao = loginDao;
    }

    @Override
    public User isValidUser(String emailId, String password) throws SQLException
    {
        return loginDao.isValidUser(emailId, password);
    }

}

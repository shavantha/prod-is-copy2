/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.scenarios.test.user.mgt.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.testng.annotations.Test;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.carbon.CarbonConstants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import java.util.Properties;
import java.io.FileInputStream;
import org.wso2.identity.scenarios.commons.SOAPTestBase;

import static org.testng.Assert.*;

public class ProvisionNewUserRoleSOAPServiceTestCase extends SOAPTestBase {

    private static Log log = LogFactory.getLog(ProvisionUserSOAPServiceTestCase.class);
    private SOAPTestBase soapTestBase;

    private String ROLE_NAME;

    private String SERVICE_NAME;
    private String userName = "admin";
    private String passWord = "admin";
    private String endPoint;
    private CloseableHttpClient client;


    private static String CARBON_HOME;
    private static String SERVICES;

    private String WSDL;
    private String fileName ="data.properties";
    private String trustStore;
    private String SEPARATOR = "/";
    ConfigurationContext configContext;
    boolean  authenticate;
    private String backendURL;
    private String JKS_FILE;
    private String KEYSTORE_PW;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String configPath = rootPath + fileName;

        Properties prop = new Properties();
        prop.load(new FileInputStream(configPath));

        backendURL = super.serverURL();

        CARBON_HOME = prop.getProperty("CARBON_HOME");
        SERVICE_NAME = prop.getProperty("SERVICE_NAME");
        ROLE_NAME = prop.getProperty("ROLE_NAME");
        SERVICES = prop.getProperty("SERVICES");
        JKS_FILE = prop.getProperty("KEYSTORE_FILE");
        KEYSTORE_PW = prop.getProperty("KEYSTORE_PASSWORD");
        WSDL = prop.getProperty("WSDL_FILE");

        endPoint =  backendURL + SEPARATOR + SERVICES + SEPARATOR +WSDL;
        client = HttpClients.createDefault();

        soapTestBase =new SOAPTestBase(endPoint,userName,passWord);
        authenticateUser();
    }

    @Test(description = "1.1.3.1s")
    public void addUserRole()throws Exception {

        String[] userList = new String[2];

        String[] permissions = {
                "/permission/admin/login",
                "/permission/admin/manage",
                "/permission/admin/configure/security/usermgt/profiles"};

        PermissionDTO permissionDTO;
        PermissionDTO[] permissionDTOs = new PermissionDTO[permissions.length];

        for (int i = 0; i < permissions.length; i++)
        {
            permissionDTO = new PermissionDTO();
            permissionDTO.setAction(CarbonConstants.UI_PERMISSION_ACTION);
            permissionDTO.setResourceId(permissions[i]);
            permissionDTOs[i] = permissionDTO;

        }

        soapTestBase.provisionNewRole(ROLE_NAME,null,permissionDTOs);
        assertNotNull(soapTestBase.getRoleNames());
        assertTrue(soapTestBase.isExistingRole(ROLE_NAME));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws java.rmi.RemoteException,
            org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException{

        soapTestBase.deleteRole(ROLE_NAME);
    }


    private void authenticateUser() throws Exception{

        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
        trustStore = CARBON_HOME + SEPARATOR + JKS_FILE;
        System.setProperty("javax.net.ssl.trustStore",  trustStore );
        System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PW);

        RemoteUserStoreManagerServiceStub adminStub = new RemoteUserStoreManagerServiceStub(configContext, endPoint);

        ServiceClient client = adminStub._getServiceClient();
        Options option = client.getOptions();

        option.setProperty(HTTPConstants.COOKIE_STRING, null);
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName);
        auth.setPassword(passWord);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);

        authenticate = false;

        try{
            authenticate = adminStub.authenticate(userName, passWord);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(authenticate){
            log.debug("User is authenticated successfully");
        } else {
            log.error("User is authentication failed");
        }

    }

}

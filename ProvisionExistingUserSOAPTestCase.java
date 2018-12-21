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

import org.apache.axis2.AxisFault;
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
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import java.util.Properties;
import java.io.FileInputStream;
import org.wso2.identity.scenarios.commons.SOAPTestBase;

import static org.testng.Assert.*;

public class ProvisionExistingUserSOAPTestCase extends SOAPTestBase {

    private static Log log = LogFactory.getLog(ProvisionUserSOAPServiceTestCase.class);
    private SOAPTestBase soapTestBase;

    private String ROLE_NAME = "Internal/everyone";
    private String USER_NAME = "shavantha";
    private String USER_PASSWORD = "welcome";


    private String SERVICE_NAME;
    private String userName = "admin";
    private String passWord = "admin";
    private String endPoint;
    private CloseableHttpClient client;

    private String claimURI;
    private String claimVal;
    private String claimURL2;
    private String claimVal2;
    private String claimURL3;
    private String claimVal3;
    private static String CARBON_HOME;
    private static String ADMIN_USERNAME ="admin";
    private static String ADMIN_PASSWORD = "admin";
    private static String SERVICES;

    private String WSDL;
    private String fileName ="data.properties";
    private String trustStore;
    private String SEPARATOR = "/";
    ConfigurationContext configContext;
    RemoteUserStoreManagerServiceStub adminStub;
    boolean  authenticate;
    private String backendURL;
    private String JKS_FILE;
    private String KEYSTORE_PW;
    private String EXPECTED_MESSAGE ="Username already exists in the system.";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String configPath = rootPath + fileName;

        Properties prop = new Properties();
        prop.load(new FileInputStream(configPath));

        backendURL = super.serverURL();

        CARBON_HOME = prop.getProperty("CARBON_HOME");
        SERVICE_NAME = prop.getProperty("SERVICE_NAME");
        claimURI = prop.getProperty("CLAIM_URL");
        claimVal = prop.getProperty("CLAIM_VAL");
        claimURL2 = prop.getProperty("CLAIM_URL2");
        claimVal2 = prop.getProperty("CLAIM_VAL2");
        claimURL3 = prop.getProperty("CLAIM_URL3");
        claimVal3 = prop.getProperty("CLAIM_VAL3");
        SERVICES = prop.getProperty("SERVICES");
        JKS_FILE = prop.getProperty("KEYSTORE_FILE");
        KEYSTORE_PW = prop.getProperty("KEYSTORE_PASSWORD");
        WSDL = prop.getProperty("WSDL_FILE");

        endPoint =  backendURL + SEPARATOR + SERVICES + SEPARATOR +WSDL;
        client = HttpClients.createDefault();

        soapTestBase =new SOAPTestBase(endPoint,ADMIN_USERNAME,ADMIN_PASSWORD);
        authenticateUser();
        addUser();
    }

    public void addUser()throws Exception {

        //Add the user roles
        String[] userRoleList = new String[1];
        userRoleList[0] = new String(ROLE_NAME);

        //Configure the claims
        ClaimValue[] claims = new ClaimValue[3];

        ClaimValue claim1 = new ClaimValue();
        claim1.setClaimURI(claimURI);
        claim1.setValue(claimVal);
        claims[0] = claim1;

        ClaimValue claim2 = new ClaimValue();
        claim2.setClaimURI(claimURL2);
        claim2.setValue(claimVal2);
        claims[1] = claim2;

        ClaimValue claim3 = new ClaimValue();
        claim3.setClaimURI(claimURL3);
        claim3.setValue(claimVal3);
        claims[2] = claim3;

        soapTestBase.provisionNewUser(USER_NAME, USER_PASSWORD, userRoleList, claims, "default",
                false);

        assertTrue(soapTestBase.isExistingUser(USER_NAME));
        assertNotNull(soapTestBase.getUserId(USER_NAME));
    }

    @Test(description = "1.2.3.1 adding a user")
    public void testAddSecondUser()throws Exception {

        //Add the user roles
        String[] userRoleList = new String[1];
        userRoleList[0] = new String(ROLE_NAME);

        //Configure the claims
        ClaimValue[] claims = new ClaimValue[3];

        ClaimValue claim1 = new ClaimValue();
        claim1.setClaimURI(claimURI);
        claim1.setValue(claimVal);
        claims[0] = claim1;

        ClaimValue claim2 = new ClaimValue();
        claim2.setClaimURI(claimURL2);
        claim2.setValue(claimVal2);
        claims[1] = claim2;

        ClaimValue claim3 = new ClaimValue();
        claim3.setClaimURI(claimURL3);
        claim3.setValue(claimVal3);
        claims[2] = claim3;

        try {
            soapTestBase.provisionNewUser(USER_NAME, USER_PASSWORD, userRoleList, claims, "default",
                    false);
        } catch (AxisFault axisFault) {
            String actualError = axisFault.toString();
            assertEquals(actualError.substring(48,86),EXPECTED_MESSAGE,"The user name is not existing");
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws java.rmi.RemoteException,
            org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException{

        soapTestBase.deleteUser(USER_NAME);
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

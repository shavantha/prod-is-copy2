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

package org.wso2.identity.scenarios.test.scim;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;

import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendDeleteRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UserProvisionSCIMTestCase extends SCIMProvisioningTestBase {

    private String userId;
    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private String fileName = "provision.json";

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        setKeyStoreProperties();
        client = HttpClients.createDefault();
        scimUsersEndpoint = getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM_ENDPOINT + "/Users";
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.1.2.1")
    public void testSCIMCreateUser() throws Exception {

        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");

        JSONObject responseObj = getJSONFromResponse(response);
        String usernameFromResponse = responseObj.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, ProcessJsonFile.getJsonObject().get("userName"), "Username not found.");

        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId, "User id not found.");
        EntityUtils.consume(response.getEntity());
    }

    @AfterClass(alwaysRun = true)
    public void testDeleteUser() throws Exception {

            response = deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                    Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has not been deleted" +
                    " successfully");
    }

}

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

package org.wso2.identity.scenarios.test.scim2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;

import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;

public class ProvisionUserWithAllAttributesTestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;
    private String fileName = "allattributes.json";

    HttpResponse response;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.2.1.2.7")
    public void testSCIM2CreateUserWithAllAttributes() throws Exception {

        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);

        userNameResponse = ProcessJsonFile.getJsonObject().get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");
   }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been" +
                " deleted successfully");
    }
}

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
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.apache.http.client.methods.HttpPut;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;

public class UpdateProvisionedUserSCIM1TestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;
    private String updateURL;
    private String firstName;
    private String SEPARATOR = "/";
    private static final String NEW_FIRST_NAME ="newfirstname";
    private String fileName = "provision.json";
    private String userUpdates = "updates.json";

    JSONObject responseObj;

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        createUser();
    }

    public void createUser() throws Exception {

        ProcessJsonFile.readFile(fileName);
        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created " +
                "successfully");

        userNameResponse = ProcessJsonFile.getJsonObject().get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");

        firstName = ProcessJsonFile.getJsonObject().get(SCIMConstants.NAME_ATTRIBUTE).toString();
        assertEquals(firstName.substring(14,19),SCIMConstants.GIVEN_NAME_CLAIM_VALUE,"The given first name " +
                "does not exist");
    }

    @Test(description = "1.1.2.1.1.15")
    public void testUpdateUser() throws Exception {

        ProcessJsonFile.readFile(userUpdates);
        responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        updateURL = backendURL + SEPARATOR  + SCIMConstants.SCIM_ENDPOINT + SEPARATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER + SEPARATOR + userId;

        updateUserRequest(client, updateURL, ProcessJsonFile.getJsonObject(), getCommonHeaders());
        userNameResponse = ProcessJsonFile.getJsonObject().get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, ProcessJsonFile.getJsonObject().get("userName"), "username not found");

        firstName = ProcessJsonFile.getJsonObject().get(SCIMConstants.NAME_ATTRIBUTE).toString();

        assertEquals(firstName.substring(14,26),NEW_FIRST_NAME,"The given first name does not exist");
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has not been deleted" +
                " successfully");
    }

    public static HttpResponse updateUserRequest(HttpClient client, String url, JSONObject jsonObject, Header[] headers) throws IOException {

        HttpPut request = new HttpPut(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        request.setEntity(new StringEntity(jsonObject.toString()));
        return client.execute(request);
    }

    private static Header[] getCommonHeaders() {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(ADMIN_USERNAME, ADMIN_PASSWORD))
        };
        return headers;
    }

}

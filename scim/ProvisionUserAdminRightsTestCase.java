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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.*;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;


public class ProvisionUserAdminRightsTestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private final String SEPARATOR = "/";
    private String userId;
    private String fileName = "provisionAdmin.json";
    private String groupURL;
    private String groupId;
    private String groupResponse;
    private static final String RESOURCES = "Resources";

    HttpResponse response,roleResponse;
    JSONObject responseObj,groupObject;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        createUser();
    }

    private void createUser() throws Exception {

        ProcessJsonFile.readFile(fileName);

        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created " +
                "successfully");
        testCreateAdminUser();
    }

    @Test(description = "1.1.2.1.1.19")
    public void testCreateAdminUser() throws Exception {

        groupObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);

        groupURL = backendURL + SEPARATOR + Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPARATOR +
                SCIMConstants.GROUP_ENDPOINT;

        roleResponse = getGroups(client,groupURL,getCommonHeaders());

        responseObj = getJSONFromResponse(this.roleResponse);

        groupResponse = responseObj.get(RESOURCES).toString();
        groupId = groupResponse.substring(213,249);

        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONArray members = new JSONArray();
        for (int i = 0; i < 1; i++) {
            JSONObject member = new JSONObject();
            member.put(SCIMConstants.DISPLAY, ProcessJsonFile.getJsonObject().get("userName"));
            member.put(SCIMConstants.VALUE_PARAM, userId);
            members.add(member);
        }

        groupObject.put(SCIMConstants.MEMBERS, members);

        groupURL = backendURL + SEPARATOR  + Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPARATOR +
                SCIMConstants.GROUP_ENDPOINT + SEPARATOR + groupId;

        updateRoleRequest(client,groupURL,groupObject,getCommonHeaders());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has been " +
                "deleted successfully");
    }

    public static HttpResponse getGroups(HttpClient client, String url, Header[] headers) throws Exception {

        HttpGet request = new HttpGet(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        return client.execute(request);
    }

    public static HttpResponse updateRoleRequest(HttpClient client, String url, JSONObject jsonObject,
                                                 Header[] headers) throws Exception {

        HttpPatch request = new HttpPatch(url);
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

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
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.*;

public class UserProvisionWithInsufficientPrivilegesSCIM1TestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private final String SEPARATOR = "/";
    private String fileName = "userwrites.json";

    private String userId;
    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        scimUsersEndpoint = backendURL + SEPARATOR +  Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPARATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;
        scimCreateFirstUser();
    }

    private void scimCreateFirstUser() throws Exception {

        ProcessJsonFile.readFile(fileName);
        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created" +
                " successfully");

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
    }

    @Test(description = "1.1.2.1.1.11")
    public void testSCIMCreateSecondUser() throws Exception {

        response = sendPostRequestWithJSON(client, scimUsersEndpoint, ProcessJsonFile.getJsonObject(),
                new Header[]{getFaultyAuthzHeader(), getContentTypeApplicationJSONHeader()});

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR, "User is not " +
                "authorized to perform provisioning");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = new JSONArray();
        schemasArray.add(responseObj);
    }

   @AfterClass(alwaysRun = true)
   public void cleanUp() throws Exception {

       response = deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT,
               Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
       assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has not been deleted" +
               " successfully");
   }

    private Header getFaultyAuthzHeader()throws IOException,org.json.simple.parser.ParseException {

        return new BasicHeader(HttpHeaders.AUTHORIZATION,
                constructBasicAuthzHeader(ProcessJsonFile.getJsonObject().get("userName").toString(),
                ProcessJsonFile.getJsonObject().get("password").toString()));
    }

    private Header getContentTypeApplicationJSONHeader() {

        return new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON);
    }
}

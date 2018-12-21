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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.apache.http.util.EntityUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;

public class FilterOutEmailUserProvisioningSCIM2TestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private final String EMAIL_ID="scim2user@wso2.com";
    private String fileName = "email.json";
    private String userId;

    HttpResponse response;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.2.1.2.13")
    public void testSCIM2CreateUser() throws Exception {

        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR, "Username " +
                "format is not valid ");

        userNameResponse = ProcessJsonFile.getJsonObject().get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, EMAIL_ID, "username not found");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = new JSONArray();
        schemasArray.add(responseObj);

        assertEquals(((JSONObject) responseObj).get("schemas"), SCIMConstants.ERROR_SCHEMA,"The expected" +
                " ERROR_SCHEMA is not returned");
    }
}

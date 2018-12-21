
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
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;

public class ProvisionUserWithMalformedRequestTestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String fileName = "malformed.json";

    JSONArray schemasArray;

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.2.1.2.2")
    public void testMalformedSCIMUserCreate() throws Exception {

        response = provisionUser(backendURL, ProcessJsonFile.getJsonObject(),
                Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "User creation request is malformed hence server should have returned a bad request");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        schemasArray = (JSONArray) (ProcessJsonFile.getJsonObject()).get("schemas");

        assertNotNull(schemasArray);
        assertTrue(responseObj.toString().contains("Required attribute userName is missing"));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = new JSONArray();
        schemasArray.add(responseObj);

        assertEquals(((JSONObject) responseObj).get("schemas"), SCIMConstants.ERROR_SCHEMA,"Expected ERROR_SCHEMA " +
                "not returned");
    }

}

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;

import static org.testng.Assert.assertEquals;

import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;

public class UserProvisionSCIMWithDifferentContentTypeTestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private final String SEPARATOR = "/";
    private final String CONTENT_TYPE ="application/xml";
    private String fileName = "provision.json";


    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        scimUsersEndpoint = backendURL + SEPARATOR + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPARATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.2.1.2.10")
    public void testWrongContentType() throws Exception {

        response = sendPostRequestWithJSON(client, scimUsersEndpoint, ProcessJsonFile.getJsonObject(),
                new Header[]{getBasicAuthzHeader(), getContentTypeApplicationXMLHeader()});

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_ACCEPTABLE, "The expected " +
                "response code 406 has not been received");

        assertEquals(response.getStatusLine().getReasonPhrase(), "Not Acceptable","The expected" +
                " Content-Type has not been received");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = new JSONArray();
        schemasArray.add(responseObj);

        assertEquals(((JSONObject) responseObj).get("schemas"),SCIMConstants.ERROR_SCHEMA,"Expected ERROR_SCHEMA," +
                "not returned");
    }

    private Header getBasicAuthzHeader() {

        return new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    private Header getContentTypeApplicationXMLHeader() {

        return new BasicHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
    }
}

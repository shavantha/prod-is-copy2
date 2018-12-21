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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import org.wso2.identity.scenarios.commons.ProcessJsonFile;
import org.wso2.identity.scenarios.commons.SCIMProvisioningTestBase;


public class ProvisionUserWithoutAuthHeaderTestCase extends SCIMProvisioningTestBase {

    private CloseableHttpClient client;
    private static String SEPARATOR ="/";
    private String fileName = "provision.json";
    private String scimEndpoint;

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        ProcessJsonFile.readFile(fileName);
    }

    @Test(description = "1.1.2.1.2.5")
    public void testSCIMUserWithoutAuthHeader() throws Exception {

        scimEndpoint =
                backendURL + SEPARATOR + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPARATOR +
                        Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;

        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        StringEntity entity = new StringEntity(ProcessJsonFile.getJsonObject().toString());
        request.setEntity(entity);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED,
                "User creation should fail without auth header hence server should have returned an unauthorized" +
                        " message");
    }
}

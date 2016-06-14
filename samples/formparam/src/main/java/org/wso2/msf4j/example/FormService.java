/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.example.bean.Company;
import org.wso2.msf4j.example.bean.Person;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Sample FormParam Service class.
 *
 */
@Path("/formService")
public class FormService {

    private static final Logger log = LoggerFactory.getLogger(FormService.class);

    @POST
    @Path("/simpleForm")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response simpleForm(@FormDataParam("age") int age, @FormDataParam("name") String name) {
        return Response.ok().entity("Name and age " + name + ", " + age).build();
    }

    @POST
    @Path("/simpleFormWithList")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response simpleFormWithList(@FormDataParam("name") List<String> name) {
        return Response.ok().entity("Name " + name).build();
    }

    @POST
    @Path("/simpleFormStreaming")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response simpleFormStreaming(@Context FormParamIterator formParamIterator) {
        try {
            while (formParamIterator.hasNext()) {
                FormItem item = formParamIterator.next();
                if (item.isFormField()) {
                    System.out.println(item.getFieldName() + " - " + StreamUtil.asString(item.openStream()));
                } else {
                    Files.copy(item.openStream(), Paths.get(System.getProperty("java.io.tmpdir"), item.getName()));
                }
            }
        } catch (FormUploadException e) {
            log.error("Error while uploading the file " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Unable to upload the file " + e.getMessage(), e);
        }
        return Response.ok().entity("Request completed").build();
    }

    @POST
    @Path("/complexForm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response complexForm(@FormDataParam("file") File file,
                                @FormDataParam("id") int id,
                                @FormDataParam("people") List<Person> personList,
                                @FormDataParam("company") Company company) {
        System.out.println("First Person in List " + personList.get(0).getName());
        System.out.println("Id " + id);
        System.out.println("Company " + company.getType());
        try {
            Files.copy(file.toPath(), Paths.get(System.getProperty("java.io.tmpdir"), file.getName()));
        } catch (IOException e) {
            log.error("Error while Copying the file " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().entity("Request completed").build();
    }

    @POST
    @Path("/multipleFiles")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("files") List<File> files) {
        files.forEach(file -> {
            try {
                Files.copy(file.toPath(),Paths.get(System.getProperty("java.io.tmpdir"), file.getName()));
            } catch (IOException e) {
                log.error("Error while Copying the file " + e.getMessage(), e);
            }
        });
        return Response.ok().entity("Request completed").build();
    }

    @POST
    @Path("/streamFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("file") FileInfo fileInfo,
                                  @FormDataParam("file") InputStream inputStream) {
        try {
            Files.copy(inputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()));
        } catch (IOException e) {
            log.error("Error while Copying the file " + e.getMessage(), e);
        }
        return Response.ok().entity("Request completed").build();
    }
}

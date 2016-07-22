/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package embl.ebi.variation.eva.pipeline.annotation;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by jmmut on 2016-07-19.
 *
 * There's no way to read a compressed file with Spring classes.
 * reference: https://jira.spring.io/browse/BATCH-1750
 *
 * It's lazy because otherwise it will try to open the file on creation. The creation may be at the start of the
 * runtime if this class is used to create beans for autowiring, and at the start of the application it's
 * possible that the file doesn't exist yet.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
public class GzipLazyResource extends FileSystemResource implements Resource {

    public GzipLazyResource(File file) {
        super(file);
    }

    public GzipLazyResource(String path) {
        super(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new GZIPInputStream(super.getInputStream());
    }
}


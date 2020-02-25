<p align="center">
  <img src="https://github.com/kazooo/indexcast/blob/master/logo.png?raw=true" alt="Indexcast logo">
</p>

<br>

[![Build Status](https://travis-ci.com/kazooo/indexcast.svg?token=9hx2FG2heDSbUifJsALk&branch=master)](https://travis-ci.com/kazooo/indexcast)
[![codecov](https://codecov.io/gh/kazooo/indexcast/branch/master/graph/badge.svg?token=3IPajdP7Sf)](https://codecov.io/gh/kazooo/indexcast)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Indexcast is a simple migration tool for the Solr search engine.
This allows quickly transfer documents from one Solr instance to another and, moreover,
process and automatically change document fields content during migration using custom processors.

### Application description

Indexcast is a Spring Batch based application that can quickly transfer Solr documents 
in parallel across multiple threads. It uses Solr's cursor pagination to logically separate 
source Solr index into parts that are migrated by application threads later.

At the start according with given parameter **THREADS=n** Indexcast starts **n+1** threads.
All threads request for cursor marks and documents using query given by **QUERY** parameter.

One thread continuously initializes cursor marks that logically separate source Solr index into parts.
Then it stored received cursor marks into global storage with that another threads can manipulate.
This thread finishes its job and closes storage when it reaches the end of source index.

Another threads retrieve cursor marks from the storage with the limit of documents 
that can be migrated from given index part, in this project called *"docs-to-migrate number"*.
Then each thread requests source index for Solr documents in several cycles, starting from
received cursor mark. Each thread request asks source index for documents quantity given by **PER_CYCLE** parameter.

Received Solr documents are converted to documents with fields specified by *migration schema*.
In that new documents each field contains the content from old document fields.
Then that documents are processed by *processors* that can modify document fields content.
After processing all documents are sent to destination Solr instance.

When thread transfers *docs-to-migrate number* documents it requesting the next cursor mark from global storage.
If global storage is closed and has no cursor marks, application finishes successfully.

### Tool parameters

Before launching Indexcast you must configure application providing parameters below:

| parameter         | description                           | example                                 | required | default value |
|   :---            |    :---                               |  :---                                   |   :---:  |  :---:        |
| THREADS           | threads number                        | 5                                       | false    | 4             |
| QUERY             | query specifying documents            | \*:*                                    | false    | \*:*          |
| PER_CYCLE         | how many docs thread can load at once | 100                                     | false    | 5000          |
| SCHEMA_PATH       | path to migration schema              | src/test/resources/migration-schema.yml | true     |
| SRC_SOLR_HOST     | source Solr host                      | http://solr-host.com                    | true     |
| DST_SOLR_HOST     | destination Solr host                 | http://solr-host.com                    | true     |
| SRC_CORE_NAME     | source Solr core name                 | solr/test_src_core                      | true     |
| DST_CORE_NAME     | source Solr core name                 | solr/test_dst_core                      | true     |
| LOGGING_LEVEL_COM | application logging level             | DEBUG                                   | false    | INFO          |

With parameters above you can start Indexcast executable jar file

```bash
java -DSRC_SOLR_HOST=http://solr-host <another parameters with '-D' prefix> -jar build/libs/indexcast-1.0.0.jar 
```

or using Gradle 'bootRun'

```bash
./gradlew bootRun --args='--SRC_SOLR_HOST=http://solr-host <another parameters with "--" prefix>'
```

Indexcast can read mentioned above parameters from environment variables too.

### Migration schema

Indexcast migrate Solr documents according with *migration schema* in YAML format. 
In this schema you must specify source Solr unique key and fields you want to be migrated.
Unique key must be in 'uniqueKey' section, the fields should be listed in 'fields' section.
Note that the fields of destination Solr instance should not have the same names as in source Solr instance.
Processors section is not required, you can skip it if you don't want to use any processors.
Processors are applied to a Solr documents in the order they are written in the 'process' section.

In example below the migration schema involves the migration of 'id' and 'text' fields from
source Solr to 'id' and 'transformed_text' fields of destination Solr using 'id' field as an unique key.
Processor 'TextTransformationProcessor' can be used to transform content of 'text' field to content of 'transformed_text' field. 

```yaml
uniqueKey: id

fields:
  id : id
  text : transformed_text

processors:
  - TextTransformationProcessor
```

### Processors

Processors are the parts of application that can modify document fields content.
You can write your own processor, it must implement the *ProcessorInterface* interface 
and be placed in *src/main/java/cz/mzk/processor* package. Add your processor name to the 
*migration schema* 'processors' section and Indexcast will automatically load it at startup.

```java
package cz.mzk.processor;

public class TestProcessor implements ProcessorInterface {

    private final Logger logger = LoggerFactory.getLogger(TestProcessor.class);

    @Override
    public List<SolrInputDocument> process(List<SolrInputDocument> item) {
        for (SolrInputDocument doc : item) {
            logger.info("document has id:" + doc.getFieldValue("id"));
        }
        return item;  // return documents to index them later
    }
}
```

### Docker

Indexcast can run in Docker container. Official Indexcast Docker image (without any processors) is available on DockerHub.
You can normally dockerize Indexcast with your own processors using Gradle Docker plugin:

```bash
./gradlew docker
```

Also you can use Docker compose to quickly configure and launch Indexcast:

```yaml
version: '3'
services:
  indexcast:
    image: ermak/indexcast:1.0.0
    container_name: indexcast_container
    volumes:
    - ../migration-schema.yml:/indexcast/configs/migration-schema.yml
    environment:
      - THREADS=4
      - PER_CYCLE=5000
      - QUERY=*:*
      - SCHEMA_PATH=/indexcast/configs/migration-schema.yml
      - SRC_SOLR_HOST=http://localhost:8983
      - SRC_CORE_NAME=solr/test_src_core
      - DST_SOLR_HOST=http://localhost:8984
      - DST_CORE_NAME=solr/test_dst_core
      - LOGGING_LEVEL_CZ_MZK=DEBUG
```
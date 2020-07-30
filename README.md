<p align="center">
  <img src="https://github.com/kazooo/indexcast/blob/master/logo.png?raw=true" alt="Indexcast logo">
</p>

<br>

[![Build Status](https://travis-ci.com/kazooo/indexcast.svg?branch=master)](https://travis-ci.com/kazooo/indexcast)
[![codecov](https://codecov.io/gh/kazooo/indexcast/branch/master/graph/badge.svg)](https://codecov.io/gh/kazooo/indexcast)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Indexcast is a simple migration tool for the [Solr](https://lucene.apache.org/solr) search engine.
It allows to quickly copy documents from one Solr instance to another and, moreover, to process and 
to automatically change document field content during migration using custom processors.

## Table of contents

 - [How it works](#how-it-works)
 - [Prerequisites](#prerequisites)
 - [Tool parameters](#tool-parameters)
 - [Migration schema](#migration-schema)
 - [Processors](#processors)
 - [Docker](#docker)
 - [Contributing](#contributing)
 - [License](#license)
 - [Author](#author)

## How it works

Indexcast is a [Spring Batch](https://spring.io/projects/spring-batch) based application that copies Solr documents 
in parallel via multiple threads. It uses Solr's cursor pagination to logically divide 
source Solr index into parts that are migrated by application threads later.

According to given parameter **THREADS=n** Indexcast initializes **n+1** threads.
All threads request cursor marks and documents using query given by **QUERY** parameter.

One thread continuously creates cursor marks that logically separate source Solr index into parts.
Then it stores received cursor marks into global storage, they could be processed by other threads.
This thread finishes its job and closes storage once it reaches the end of the source index.

Each thread (except the first one mentioned above) retrieves cursor marks and number of documents 
from the storage that are being migrated from Solr’s source index to another Solr instance (*docs-to-migrate number*).
The thread copies Solr’s documents via cycles. Number of documents that being copied during the cycle 
is set via parameter **PER_CYCLE**.

Indexcast creates a dump for each document which copies fields specified in *migration
schema*. If the dump does not contain specified field in *migration schema* it leaves the field of
the dump empty. Documents in the dump could be handled by *processors* that could modify
the content of the document fields. Documents are sent to the destination Solr instance after
being processed.

When thread copied *docs-to-migrate number* documents, it requests the next cursor mark
from global storage. If global storage is closed and has no cursor marks, migration is finished
successfully.

## Prerequisites

The source Solr instance should support deep paging by cursor marks.
This feature [appeared](https://issues.apache.org/jira/browse/SOLR-5463) in Solr version 4.7.0 
and is still supported in modern versions of Solr.

This tool is build with the help of the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
which uses [Gradle build tool](https://gradle.org/) version 6.1.1. 
To add Gradle Wrapper you should have installed Gradle version >= 5.6.2 and run command in project folder:

```bash
gradle wrapper
```

## Tool parameters

You must configure Indexcast via specified parameters:

| parameter         | description                           | example                                 | required | default value |
|   :---            |    :---                               |  :---                                   |   :---:  |  :---:        |
| THREADS           | threads number                        | 5                                       | false    | 4             |
| QUERY             | query specifying documents            | \*:*                                    | false    | \*:*          |
| PER_CYCLE         | how many docs thread can load at once | 100                                     | false    | 5000          |
| STORAGE_SIZE      | how many cursor can be stored in global storage | 14                            | false    | 20            |
| SCHEMA_PATH       | path to migration schema              | src/test/resources/migration-schema.yml | true     |               |
| SRC_SOLR_HOST     | source Solr host                      | http://solr-host.com                    | true     |               |
| DST_SOLR_HOST     | destination Solr host                 | http://solr-host.com                    | true     |               |
| SRC_CORE_NAME     | source Solr core name                 | solr/test_src_core                      | true     |               |
| DST_CORE_NAME     | source Solr core name                 | solr/test_dst_core                      | true     |               |
| LOGGING_LEVEL_COM | application logging level             | DEBUG                                   | false    | INFO          |
| WAIT_IF_SOLR_FAIL | time to wait in milliseconds if any Solr instance has a problem | 3000          | false    | 60000         |

With parameters above you can start Indexcast executable jar file

```bash
./gradlew bootJar
java -DSRC_SOLR_HOST=http://solr-host <another parameters with '-D' prefix> -jar indexcast-1.0.0.jar 
```

or using Gradle 'bootRun'

```bash
./gradlew bootRun --args='--SRC_SOLR_HOST=http://solr-host <another parameters with "--" prefix>'
```

Indexcast can accept mentioned parameters from environment variables.

## Migration schema

Indexcast migrates Solr documents according to *migration schema* specified in YAML format. 
In this schema you must specify source Solr unique key and fields you want to be migrated.
Unique key must be in 'uniqueKey' section, the fields should be listed in 'fields' section.
Note that the fields of destination Solr instance must not have the same names as in a source Solr instance.
The 'processors' section is optional, it could be skipped if you don't need to modify the document fields by any processors.
Processors are applied to Solr documents in the order they are written in the 'processors' section.

In example below the migration schema involves the migration of 'id' and 'text' fields from
source Solr to 'id' and 'transformed_text' fields of destination Solr using 'id' field as an unique key.
Processor 'TextTransformationProcessor' can be used to transform content of 'text' field to content of 'transformed_text' field. 

```yaml
unique_key: id

fields:
  id : id
  text : transformed_text

processors:
  - TextTransformationProcessor
```

If no 'fields' section is specified, Indexcast copies all fields using the same field names as in the source Solr.
You can write 'ignored_fields' section to make Indexcast copy all fields except specific ones.

```yaml
unique_key: id

ignored_fields:
 - version
```

## Processors

Processors are the part of application that can modify document fields content.
You can write your own processor, it must implement the *ProcessorInterface* interface 
and be placed in *src/main/java/cz/mzk/processor* package. Add your processor name to the 
*migration schema* 'processors' section and Indexcast will automatically load it on startup.

```java
package com.indexcast.processor;

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

## Docker

Indexcast could be run in [Docker](https://www.docker.com) container. Official Indexcast Docker 
image (without any processors) is available on [DockerHub](https://hub.docker.com/repository/docker/ermak/indexcast).
You can normally dockerize Indexcast with your own processors using Gradle Docker plugin:

```bash
./gradlew docker
```

Also you can use [Docker compose](https://docs.docker.com/compose) to quickly configure and launch Indexcast:

```yaml
version: '3'
services:
  indexcast:
    image: ermak/indexcast:1.0.0
    container_name: indexcast_container
    volumes:
    - ./migration-schema.yml:/indexcast/configs/migration-schema.yml
    environment:
      - THREADS=4
      - PER_CYCLE=5000
      - QUERY=*:*
      - SCHEMA_PATH=/indexcast/configs/migration-schema.yml
      - SRC_SOLR_HOST=http://localhost:8983
      - SRC_CORE_NAME=solr/test_src_core
      - DST_SOLR_HOST=http://localhost:8984
      - DST_CORE_NAME=solr/test_dst_core
      - LOGGING_LEVEL_COM=DEBUG
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[GPL v3](https://www.gnu.org/licenses/gpl-3.0)

## Author

[Aleksei Ermak](https://github.com/kazooo)

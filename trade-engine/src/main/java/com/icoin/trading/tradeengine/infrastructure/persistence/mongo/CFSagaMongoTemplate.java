/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.icoin.trading.tradeengine.infrastructure.persistence.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.axonframework.saga.repository.mongo.MongoTemplate;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * @author Jettro Coenradie
 */
public class CFSagaMongoTemplate implements MongoTemplate {
    static final String SAGA_COLLECTION = "sagacollection";
    private MongoDbFactory mongoDbFactory;

    public CFSagaMongoTemplate(MongoDbFactory mongoDbFactory) {
        this.mongoDbFactory = mongoDbFactory;
    }

    @Override
    public DBCollection sagaCollection() {
        return database().getCollection(SAGA_COLLECTION);
    }

    private DB database() {
        return mongoDbFactory.getDb();
    }
}

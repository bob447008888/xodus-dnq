/**
 * Copyright 2006 - 2017 JetBrains s.r.o.
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
package com.jetbrains.teamsys.dnq.database;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.query.metadata.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: 8/12/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintsUtil.class);


    public static void validateEntity(@NotNull Entity entity, @NotNull ModelMetaData modelMetaData) {

        // 1. validate associations
        validateAssociations(entity, modelMetaData);

        // 2. validate required properties
        validateRequiredProperties(entity, modelMetaData);
    }


    // Validate associations
    static void validateAssociations(@NotNull Entity entity, @NotNull ModelMetaData modelMetaData) {
        EntityMetaData md = modelMetaData.getEntityMetaData(entity.getType());
        for (AssociationEndMetaData aemd : md.getAssociationEndsMetaData()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Validate cardinality [" + entity.getType() + "." + aemd.getName() + "]. Required is [" + aemd.getCardinality().getName() + "]");
            }

            if (!checkCardinality(entity, aemd)) {
                cardinalityViolation(entity, aemd);
            }
        }
    }

    static boolean checkCardinality(Entity e, AssociationEndMetaData md) {
        return checkCardinality(e, md.getCardinality(), md.getName());
    }

    static boolean checkCardinality(Entity entity, AssociationEndCardinality cardinality, String associationName) {
        int size = 0;
        for (Iterator<Entity> it = entity.getLinks(associationName).iterator(); it.hasNext(); ++size) {
            Entity e = it.next();
            if (e == null) {
                fakeEntityLink(e, associationName);
                --size;
            }
        }

        switch (cardinality) {
            case _0_1:
                return size <= 1;

            case _0_n:
                return true;

            case _1:
                return size == 1;

            case _1_n:
                return size >= 1;
        }
        unknownCardinality(cardinality);
        return false;
    }


    // Validate entity properties.

    static void validateRequiredProperties(@NotNull Entity entity, @NotNull ModelMetaData mmd) {
        EntityMetaData emd = mmd.getEntityMetaData(entity.getType());

        Set<String> required = emd.getRequiredProperties();
        Set<String> requiredIf = EntityMetaDataUtils.getRequiredIfProperties(emd, entity);

        for (String property : required) {
            checkProperty(entity, emd, property);
        }
        for (String property : requiredIf) {
            checkProperty(entity, emd, property);
        }
    }

    private static void checkProperty(Entity e, EntityMetaData emd, String name) {
        final PropertyMetaData pmd = emd.getPropertyMetaData(name);
        final PropertyType type;
        if (pmd == null) {
            logger.warn("Can't determine property type. Try to get property value as if it of primitive type.");
            type = PropertyType.PRIMITIVE;
        } else {
            type = pmd.getType();
        }
        checkProperty(e, name, type);
    }

    private static void checkProperty(Entity e, String name, PropertyType type) {

        switch (type) {
            case PRIMITIVE:
                if (isEmptyPrimitiveProperty(e.getProperty(name))) {
                    noProperty(e, name);
                }
                break;

            case BLOB:
                if (e.getBlob(name) == null) {
                    noProperty(e, name);
                }
                break;

            case TEXT:
                if (isEmptyPrimitiveProperty(e.getBlobString(name))) {
                    noProperty(e, name);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown property type: " + name);
        }
    }

    private static boolean isEmptyPrimitiveProperty(Comparable propertyValue) {
        return propertyValue == null || "".equals(propertyValue);
    }


    // Error log

    private static void noProperty(Entity entity, String propertyName) {
        logger.error("Validation: Property [" + entity + "." + propertyName + "]" + " is empty.");
    }

    private static void unknownCardinality(AssociationEndCardinality cardinality) {
        logger.error("Validation: Unknown cardinality [" + cardinality + "]");
    }

    private static void cardinalityViolation(Entity entity, AssociationEndMetaData md) {
        logger.error("Validation: Cardinality violation for [" + entity + "." + md.getName() + "]. Required cardinality is [" + md.getCardinality().getName() + "]");
    }

    private static void fakeEntityLink(Entity entity, String associationName) {
        logger.error("Validation: Null entity in the [" + entity + "." + associationName + "]");
    }

}
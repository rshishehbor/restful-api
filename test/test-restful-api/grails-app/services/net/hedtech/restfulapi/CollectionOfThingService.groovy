/* ****************************************************************************
 * Copyright 2013 Ellucian Company L.P. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/
package net.hedtech.restfulapi


/**
 * Bulk wrapper service for Things.
 * Expects an object with property elements.
 * Elements is a list of objects each object having a unique index property
 * and a object property containing the thing resource to perform the operation on.
 * Supports partial success.
 * {
 *    "elements": [
 *        { "index": "1", "resource": {} },
 *        { "index": "2", "resource": {} }
 *    ]
 * }
 */
class CollectionOfThingService {

    static transactional = false
    static ELEMENT_SUCCESS = "Success"
    static ELEMENT_FAILED = "Failed"

    def grailsApplication
    def thingService


    def create( Map content, Map params ) {
        log.trace "CollectionOfThingService.create invoked"
        def result = []

        validateElements(content)
        content.elements.each { item ->
            try {
                def resource = thingService.create(item.resource, params)
                result << getThingObject(item.index, resource )
            }
            catch ( Exception e ) {
                result << getErrorObject( item.index, e )
            }
        }
        [elements:result]
    }


    def update( Map content, Map params ) {
        log.trace "CollectionOfThingService.update invoked"
        def result = []

        validateElements(content)
        content.elements.each { item ->
            try {
                def resource = thingService.update(item.resource, [id:item.resource.id])
                result << getThingObject(item.index, resource )
            }
            catch ( Exception e ) {
                result << getErrorObject( item.index, e )
            }
        }
        [elements:result]
    }


    def delete( Map content, Map params ) {
        log.trace "CollectionOfThingService.delete invoked"
        def result = []

        validateElements(content)
        content.elements.each { item ->
            try {
                thingService.delete(item.resource, [id:item.resource.id])
                result << getThingObject(item.index, null )
            }
            catch ( Exception e ) {
                result << getErrorObject( item.index, e )
            }
        }
        [elements:result]
    }

    private static void validateElements( content ) {
        if( content && content.elements instanceof List) {
            content.elements.each { item ->
                if (!item?.index || !item?.resource) {
                    throw new DummyApplicationException(400,"Bulk objects in element list did not have required parameters","validation")
                }
            }
        }
        else {
            throw new DummyApplicationException(400,"Elements must contain a list of bulk objects","validation")
        }
    }

    private static def getErrorObject( index, Exception e ) {
        ["status": ELEMENT_FAILED, "index": index, "errors":[e.message]]
    }

    private static def getThingObject( index, thing ) {
        def thingObject = ["status": ELEMENT_SUCCESS, "index": index]
        if( thing ) {
            thingObject.put('resource',thing)
        }
        thingObject
    }
}

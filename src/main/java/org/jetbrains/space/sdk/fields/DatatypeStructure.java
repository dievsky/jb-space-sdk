package org.jetbrains.space.sdk.fields;

import java.util.NoSuchElementException;

/**
 * Describes a structure of a Space HTTP API object.
 *
 * Space HTTP API allows for a very fine control over which fields and embedded objects will be serialized.
 * This structure describes all fields that the object can contain.
 *
 * Use DatatypeStructureDiscovery.structure to generate an instance automatically.
 */
public interface DatatypeStructure {

    boolean hasField(String fieldName, String... fieldNames);

    /**
     * Will this field be serialized if the parent spec is "*".
     */
    boolean wildcardSerializable(String fieldName, String... fieldNames);

    DatatypeStructure getField(String fieldName, String... fieldNames);

    /**
     * A primitive is an entity whose fields, if any, cannot be specified in a request.
     *
     * Two examples of a primitive are a string (which has no fields) and a date (which is returned as a JSON object).
     */
    DatatypeStructure PRIMITIVE = new DatatypeStructure() {

        @Override
        public boolean hasField(String fieldName, String... fieldNames) {
            return false;
        }

        @Override
        public boolean wildcardSerializable(String fieldName, String... fieldNames) {
            throw new NoSuchElementException();
        }

        @Override
        public DatatypeStructure getField(String fieldName, String... fieldNames) {
            throw new NoSuchElementException();
        }
    };

}
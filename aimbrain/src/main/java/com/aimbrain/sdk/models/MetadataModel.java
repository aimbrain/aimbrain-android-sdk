package com.aimbrain.sdk.models;

import java.io.UnsupportedEncodingException;

/**
 * Base model class containing metadata received from server
 */
public class MetadataModel {
    private byte[] metadata;

    public MetadataModel(byte[] metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets response metadata
     * @return metadata received from server
     */
    public byte[] getMetadata() {
        return metadata;
    }

    /**
     * Gets response metadata string
     * @return metadata received from server encoded as utf-8
     */
    public String getMetadataString() throws UnsupportedEncodingException {
        if (metadata == null) {
            return null;
        }
        return new String(metadata, "UTF-8");
    }
}

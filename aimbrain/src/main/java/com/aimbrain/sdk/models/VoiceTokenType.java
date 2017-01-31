package com.aimbrain.sdk.models;

/**
 *
 */
public enum VoiceTokenType {

    ENROLL1 {
        @Override
        public String toString() {
            return "enroll-1";
        }
    },
    ENROLL2 {
        @Override
        public String toString() {
            return "enroll-2";
        }
    },
    ENROLL3 {
        @Override
        public String toString() {
            return "enroll-3";
        }
    },
    ENROLL4 {
        @Override
        public String toString() {
            return "enroll-4";
        }
    },
    ENROLL5 {
        @Override
        public String toString() {
            return "enroll-5";
        }
    },
    AUTH {
        @Override
        public String toString() {
            return "auth";
        }
    }

}

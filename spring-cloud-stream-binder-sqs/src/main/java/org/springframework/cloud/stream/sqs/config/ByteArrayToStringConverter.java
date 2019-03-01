package org.springframework.cloud.stream.sqs.config;

import org.springframework.core.convert.converter.Converter;

public class ByteArrayToStringConverter implements Converter<byte[], String> {

    @Override
    public String convert(byte[] source) {
        return new String(source);
    }
}
